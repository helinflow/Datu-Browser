package io.github.helinflow.datubrowser;

import android.app.Activity;
import android.os.Build;
import android.os.Bundle;
import android.view.Gravity;
import android.view.inputmethod.EditorInfo;
import android.webkit.CookieManager;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Private browsing uses a separate Android process and WebView data directory.
 * Session cookies, web storage and cache are cleared at session start and exit.
 */
public final class PrivateBrowserActivity extends Activity {
    private WebView webView;
    private EditText address;
    private TabManager tabManager;
    private TextView tabCount;

    private final java.util.Set<String> privateBlockedHosts =
        java.util.Collections.synchronizedSet(new java.util.HashSet<String>());
    private volatile boolean privateShieldsEnabled;
    private volatile boolean privateShieldsReady;

    @Override
    protected void onCreate(Bundle state) {
        if (Build.VERSION.SDK_INT < 28) {
            super.onCreate(state);
            Toast.makeText(this,
                "A navegação anônima isolada requer Android 9 ou superior.",
                Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        // Must run before the first WebView is created in this process.
        WebView.setDataDirectorySuffix("datu_private");
        super.onCreate(state);

        tabManager = new TabManager();
        privateShieldsEnabled = getSharedPreferences("datu_shields", MODE_PRIVATE)
            .getBoolean("bloqueio_anuncios_ativo", false);
        carregarFiltrosPrivados();

        buildInterface();
        configurePrivateWebView();

        String initial = getIntent().getStringExtra("url");
        String start = initial == null ? "https://search.brave.com/" : initial;
        tabManager.create(webView, start);
        atualizarContadorGuias();
        iniciarSessaoPrivada(start);
    }

    private void buildInterface() {
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(0xFF0E0D12);

        LinearLayout header = new LinearLayout(this);
        header.setGravity(Gravity.CENTER_VERTICAL);
        header.setPadding(dp(8), dp(4), dp(8), dp(4));

        TextView label = new TextView(this);
        label.setText(privateShieldsEnabled
            ? "◉  Navegação anônima · Shields"
            : "◉  Navegação anônima");
        label.setTextColor(0xFFD0BCFF);
        label.setTextSize(17);
        label.setPadding(dp(8), dp(8), dp(8), dp(8));
        header.addView(label, new LinearLayout.LayoutParams(0, -2, 1));

        tabCount = new TextView(this);
        tabCount.setText("1");
        tabCount.setTextColor(0xFFD0BCFF);
        tabCount.setTextSize(14);
        tabCount.setGravity(Gravity.CENTER);
        tabCount.setBackgroundColor(0xFF302A3D);
        tabCount.setPadding(dp(12), dp(6), dp(12), dp(6));
        tabCount.setOnClickListener(v -> abrirGuiasPrivadas());
        header.addView(tabCount, new LinearLayout.LayoutParams(dp(42), dp(40)));

        Button close = new Button(this);
        close.setText("Fechar");
        close.setOnClickListener(v -> finish());
        header.addView(close, new LinearLayout.LayoutParams(-2, dp(44)));
        root.addView(header, new LinearLayout.LayoutParams(-1, -2));

        LinearLayout bar = new LinearLayout(this);
        bar.setGravity(Gravity.CENTER_VERTICAL);
        bar.setPadding(dp(10), dp(6), dp(10), dp(6));

        address = new EditText(this);
        address.setSingleLine(true);
        address.setHint("Pesquisar ou digitar endereço");
        address.setTextColor(0xFFE6E0E9);
        address.setHintTextColor(0xFFCAC4D0);
        address.setBackgroundColor(0xFF24212B);
        address.setPadding(dp(12), 0, dp(12), 0);
        address.setImeOptions(EditorInfo.IME_ACTION_GO);
        bar.addView(address, new LinearLayout.LayoutParams(0, dp(48), 1));

        Button go = new Button(this);
        go.setText("Ir");
        bar.addView(go, new LinearLayout.LayoutParams(dp(64), dp(48)));
        go.setOnClickListener(v -> navigate());
        address.setOnEditorActionListener((v, action, event) -> {
            if (action == EditorInfo.IME_ACTION_GO
                    || action == EditorInfo.IME_ACTION_SEARCH) {
                navigate();
                return true;
            }
            return false;
        });
        root.addView(bar);

        webView = new WebView(this);
        root.addView(webView, new LinearLayout.LayoutParams(-1, 0, 1));
        setContentView(root);
    }

    private void configurePrivateWebView() {
        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setDatabaseEnabled(false);
        settings.setSaveFormData(false);
        settings.setSavePassword(false);
        settings.setCacheMode(WebSettings.LOAD_NO_CACHE);
        settings.setAllowFileAccess(false);
        settings.setAllowContentAccess(true);
        settings.setMixedContentMode(WebSettings.MIXED_CONTENT_NEVER_ALLOW);
        if (Build.VERSION.SDK_INT >= 26) settings.setSafeBrowsingEnabled(true);

        CookieManager cookies = CookieManager.getInstance();
        cookies.setAcceptCookie(true);
        if (Build.VERSION.SDK_INT >= 21) {
            cookies.setAcceptThirdPartyCookies(webView, false);
        }

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(
                    WebView view, WebResourceRequest request) {
                return false;
            }

            @Override
            public android.webkit.WebResourceResponse shouldInterceptRequest(
                    WebView view, WebResourceRequest request) {
                if (request == null || request.isForMainFrame()) return null;
                return bloquearRecursoPrivado(request.getUrl().toString());
            }

            @Override
            public android.webkit.WebResourceResponse shouldInterceptRequest(
                    WebView view, String url) {
                return bloquearRecursoPrivado(url);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                if (url != null) address.setText(url);
                if (tabManager != null) tabManager.captureActive(view);
            }
        });
        webView.setWebChromeClient(new WebChromeClient());
    }

    private void carregarFiltrosPrivados() {
        new Thread(() -> {
            String[] arquivos = {
                "filters.min.txt", "privacy.min.txt", "quick-fixes.txt",
                "unbreak.txt", "easylist.txt", "easyprivacy.txt"
            };
            java.io.File pasta = new java.io.File(getFilesDir(), "ublock_lists");
            for (String nome : arquivos) {
                java.io.File arquivo = new java.io.File(pasta, nome);
                if (!arquivo.isFile()) continue;
                try (java.io.BufferedReader leitor = new java.io.BufferedReader(
                        new java.io.InputStreamReader(
                            new java.io.FileInputStream(arquivo), "UTF-8"))) {
                    String linha;
                    while ((linha = leitor.readLine()) != null) {
                        adicionarHostDeFiltro(linha);
                    }
                } catch (Exception ignorado) {
                    // Uma lista ausente ou inválida não impede a navegação privada.
                }
            }
            privateShieldsReady = true;
        }, "DatuPrivateShields").start();
    }

    private void adicionarHostDeFiltro(String linha) {
        if (linha == null) return;
        String regra = linha.trim();
        if (regra.isEmpty() || regra.startsWith("!") || regra.startsWith("[")
                || regra.startsWith("@@") || regra.startsWith("#")) return;

        if (regra.startsWith("||")) {
            String host = regra.substring(2);
            int fim = host.length();
            int posicao = host.indexOf('^');
            if (posicao >= 0 && posicao < fim) fim = posicao;
            posicao = host.indexOf('/');
            if (posicao >= 0 && posicao < fim) fim = posicao;
            posicao = host.indexOf('$');
            if (posicao >= 0 && posicao < fim) fim = posicao;
            posicao = host.indexOf('?');
            if (posicao >= 0 && posicao < fim) fim = posicao;
            posicao = host.indexOf('*');
            if (posicao >= 0 && posicao < fim) fim = posicao;
            posicao = host.indexOf('|');
            if (posicao >= 0 && posicao < fim) fim = posicao;
            adicionarHostValido(host.substring(0, fim));
            return;
        }

        String[] partes = regra.split("\\s+");
        if (partes.length > 1
                && (partes[0].equals("0.0.0.0")
                    || partes[0].equals("127.0.0.1"))) {
            for (int i = 1; i < partes.length; i++) {
                adicionarHostValido(partes[i]);
            }
        }
    }

    private void adicionarHostValido(String host) {
        if (host == null) return;
        String valor = host.toLowerCase(java.util.Locale.US).trim();
        while (valor.startsWith(".")) valor = valor.substring(1);
        if (valor.length() < 4 || valor.length() > 253 || !valor.contains(".")) return;
        if (!valor.matches("^[a-z0-9.-]+$")
                || valor.startsWith("-") || valor.endsWith("-")) return;
        privateBlockedHosts.add(valor);
    }

    private android.webkit.WebResourceResponse bloquearRecursoPrivado(String url) {
        if (!privateShieldsEnabled || !privateShieldsReady || url == null) return null;
        try {
            String host = android.net.Uri.parse(url).getHost();
            if (host == null) return null;
            host = host.toLowerCase(java.util.Locale.US);

            boolean bloqueado = false;
            synchronized (privateBlockedHosts) {
                String candidato = host;
                while (candidato != null && !candidato.isEmpty()) {
                    if (privateBlockedHosts.contains(candidato)) {
                        bloqueado = true;
                        break;
                    }
                    int ponto = candidato.indexOf('.');
                    if (ponto < 0) break;
                    candidato = candidato.substring(ponto + 1);
                }
            }

            if (bloqueado) {
                return new android.webkit.WebResourceResponse(
                    "text/plain", "UTF-8",
                    new java.io.ByteArrayInputStream(new byte[0]));
            }
        } catch (Exception ignorado) {
        }
        return null;
    }

    private void iniciarSessaoPrivada(String urlInicial) {
        // Limpa a sessão anterior mesmo se o Android encerrou o processo sem
        // executar onDestroy na sessão privada anterior.
        webView.clearCache(true);
        webView.clearHistory();
        webView.clearFormData();
        android.webkit.WebStorage.getInstance().deleteAllData();
        CookieManager cookies = CookieManager.getInstance();
        cookies.removeAllCookies(removidos -> {
            cookies.flush();
            if (!isFinishing() && webView != null) loadAddress(urlInicial);
        });
    }

    private void atualizarContadorGuias() {
        if (tabCount != null && tabManager != null) {
            tabCount.setText(String.valueOf(tabManager.getCount()));
            tabCount.setContentDescription(
                tabManager.getCount() + " guias anônimas");
        }
    }

    private void abrirGuiasPrivadas() {
        if (tabManager == null) return;
        tabManager.captureActive(webView);
        java.util.List<BrowserTab> tabs = tabManager.getTabs();
        String[] labels = new String[tabs.size()];
        for (int i = 0; i < tabs.size(); i++) {
            BrowserTab tab = tabs.get(i);
            String title = tab.getTitle();
            if (title == null || title.trim().isEmpty()) {
                title = "Nova guia anônima";
            }
            labels[i] = title + "\\n" + tab.getUrl();
        }

        new android.app.AlertDialog.Builder(this)
            .setTitle("Guias anônimas")
            .setItems(labels, (dialog, which) -> {
                tabManager.select(which, webView);
                BrowserTab active = tabManager.getActive();
                if (active != null) address.setText(active.getUrl());
                atualizarContadorGuias();
            })
            .setPositiveButton("Nova guia", (dialog, which) -> {
                tabManager.create(webView, "https://search.brave.com/");
                loadAddress("https://search.brave.com/");
                atualizarContadorGuias();
            })
            .setNegativeButton("Fechar guia atual", (dialog, which) -> {
                tabManager.close(tabManager.getActiveIndex(), webView);
                BrowserTab active = tabManager.getActive();
                if (active != null) address.setText(active.getUrl());
                atualizarContadorGuias();
            })
            .show();
    }

    private void navigate() {
        String value = address.getText().toString().trim();
        if (value.isEmpty()) return;
        String target;
        if (value.matches("(?i)^(https?|about):.*")) {
            target = value;
        } else if (value.matches("^[a-z0-9.-]+\\.[a-z]{2,}(:[0-9]+)?(/.*)?$")) {
            target = "https://" + value;
        } else {
            try {
                target = "https://search.brave.com/search?q="
                    + java.net.URLEncoder.encode(value, "UTF-8");
            } catch (Exception error) {
                target = "https://search.brave.com/";
            }
        }
        loadAddress(target);
    }

    private void loadAddress(String url) {
        address.setText(url);
        webView.loadUrl(url);
    }

    private int dp(int value) {
        return (int) (value * getResources().getDisplayMetrics().density + 0.5f);
    }

    @Override
    public void onBackPressed() {
        if (webView != null && webView.canGoBack()) {
            webView.goBack();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onDestroy() {
        if (webView != null) {
            webView.stopLoading();
            webView.clearHistory();
            webView.clearFormData();
            webView.clearCache(true);
            webView.loadUrl("about:blank");
            webView.removeAllViews();
            webView.destroy();
            webView = null;
        }
        CookieManager.getInstance().removeAllCookies(null);
        CookieManager.getInstance().flush();
        android.webkit.WebStorage.getInstance().deleteAllData();
        super.onDestroy();
    }
}
