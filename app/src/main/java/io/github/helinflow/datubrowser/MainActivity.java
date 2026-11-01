package io.github.helinflow.datubrowser;

import android.animation.*;
import android.app.*;
import android.app.Activity;
import android.content.*;
import android.database.Cursor;
import android.content.SharedPreferences;
import android.content.res.*;
import android.graphics.*;
import android.graphics.drawable.*;
import android.media.*;
import android.net.*;
import android.os.*;
import android.text.*;
import android.text.style.*;
import android.util.*;
import android.view.*;
import android.view.View;
import android.view.View.*;
import android.view.animation.*;
import android.webkit.*;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.*;
import android.widget.CompoundButton;
import androidx.annotation.*;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import io.github.helinflow.datubrowser.databinding.*;
import java.io.*;
import java.text.*;
import java.util.*;
import java.util.Timer;
import java.util.TimerTask;
import java.util.regex.*;
import org.json.*;

public class MainActivity extends AppCompatActivity {
	
	private Timer _timer = new Timer();
	
	private MainBinding binding;
	private double click = 0;
	
	private TimerTask timer;
	private SharedPreferences dados;
	private android.webkit.ValueCallback<android.net.Uri[]> uploadCallback;
	private static final int FILE_CHOOSER_REQUEST = 7201;
	private static final int LIBRARY_REQUEST = 7202;
	private BrowserEngine browserEngine;
	private TabManager tabManager;
	private PopupWindow menuNavegacao;
	private boolean carregandoPreferencias = true;
	
	@Override
	protected void onCreate(Bundle _savedInstanceState) {
		super.onCreate(_savedInstanceState);
		binding = MainBinding.inflate(getLayoutInflater());
		setContentView(binding.getRoot());
		browserEngine = new WebViewBrowserEngine(binding.webview);
		initialize(_savedInstanceState);
		initializeLogic();
        configurarBuscaConfiguracoes();
		tabManager = new TabManager();
		tabManager.create(binding.webview, "https://search.brave.com/");
		tabManager.captureActive(binding.webview);
		atualizarContadorGuias();
		View contadorGuias = findViewById(R.id.tab_counter);
		if (contadorGuias != null) contadorGuias.setOnClickListener(v -> abrirSeletorGuias());
	}
	

    private void configurarBuscaConfiguracoes() {
        EditText busca = findViewById(R.id.config_search);
        if (busca == null) return;

        busca.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                String termo = s == null ? "" : s.toString().trim().toLowerCase(Locale.ROOT);
                boolean tudo = termo.isEmpty();

                boolean navegacao = tudo || "navegação navegacao abas aba guias guia favoritos favorito histórico historico downloads download".contains(termo);
                boolean aparencia = tudo || "aparência aparencia paisagem papel parede imagem imagens inicial fundo".contains(termo);
                boolean sites = tudo || "sites site javascript script permissões permissao".contains(termo);
                boolean privacidade = tudo || "privacidade segurança seguranca anuncios anúncios bloqueio bloqueador filtros filtro shields estatísticas estatisticas rastreadores".contains(termo);
                boolean sobre = tudo || "sobre datu avisos terceiros licenças licencas licença licenca código aberto".contains(termo);

                findViewById(R.id.settings_group_navigation).setVisibility(navegacao ? View.VISIBLE : View.GONE);
                findViewById(R.id.settings_heading_navigation).setVisibility(navegacao ? View.VISIBLE : View.GONE);
                findViewById(R.id.settings_group_appearance).setVisibility(aparencia ? View.VISIBLE : View.GONE);
                findViewById(R.id.settings_heading_basic).setVisibility(aparencia ? View.VISIBLE : View.GONE);
                findViewById(R.id.settings_group_site).setVisibility(sites ? View.VISIBLE : View.GONE);
                findViewById(R.id.settings_heading_site).setVisibility(sites ? View.VISIBLE : View.GONE);
                findViewById(R.id.settings_group_privacy).setVisibility(privacidade ? View.VISIBLE : View.GONE);
                findViewById(R.id.settings_heading_privacy).setVisibility(privacidade ? View.VISIBLE : View.GONE);
                findViewById(R.id.settings_group_about).setVisibility(sobre ? View.VISIBLE : View.GONE);
                findViewById(R.id.settings_heading_about).setVisibility(sobre ? View.VISIBLE : View.GONE);

                boolean nenhum = !navegacao && !aparencia && !sites && !privacidade && !sobre;
                findViewById(R.id.text_config_sem_resultados).setVisibility(nenhum ? View.VISIBLE : View.GONE);
            }
            @Override public void afterTextChanged(Editable s) { }
        });

        findViewById(R.id.settings_open_tabs).setOnClickListener(v -> abrirSeletorGuias());
        findViewById(R.id.settings_open_favorites).setOnClickListener(v -> abrirBiblioteca("favorites"));
        findViewById(R.id.settings_open_history).setOnClickListener(v -> abrirBiblioteca("history"));
        findViewById(R.id.settings_open_downloads).setOnClickListener(v -> abrirBiblioteca("downloads"));
        findViewById(R.id.settings_third_party_notices).setOnClickListener(v ->
            startActivity(new Intent(MainActivity.this, ThirdPartyNoticesActivity.class)));
    }

	private void initialize(Bundle _savedInstanceState) {
		dados = getSharedPreferences("dados", Activity.MODE_PRIVATE);
		
		binding.imageview9.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View _view) {
				binding.tela1.setVisibility(View.GONE);
				binding.tela2.setVisibility(View.VISIBLE);
				animarEntradaExpressiva(binding.tela2);
				binding.tela3.setVisibility(View.GONE);
			}
		});
		
		binding.webview.setWebViewClient(new WebViewClient() {
			@Override
			public void onPageStarted(WebView _param1, String _param2, Bitmap _param3) {
				final String _url = _param2;
				
				super.onPageStarted(_param1, _param2, _param3);
			}
			
			@Override
			public void onPageFinished(WebView _param1, String _param2) {
				final String _url = _param2;
				
				super.onPageFinished(_param1, _param2);
			}
		});
		
		binding.add.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View _view) {
				browserEngine.loadUrl("https://search.brave.com/");
			}
		});
		
		binding.textBarra.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View _view) {
				binding.tela1.setVisibility(View.VISIBLE);
				animarEntradaExpressiva(binding.tela1);
				binding.tela2.setVisibility(View.GONE);
				binding.tela3.setVisibility(View.GONE);
			}
		});
		
        binding.mais.setOnClickListener(v -> abrirMenuNavegacao());
        findViewById(R.id.library_back).setOnClickListener(v -> fecharBiblioteca());
		
		binding.linearAnuncios.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View _view) {
				click++;
				if ((click % 2) == 1) {
					binding.abrir.setImageResource(R.drawable.icon_keyboard_arrow_down_round);
					binding.linearUblock.setVisibility(View.VISIBLE);
					animarEntradaExpressiva(binding.linearUblock);
				} else {
					binding.abrir.setImageResource(R.drawable.icon_keyboard_arrow_up_round);
					binding.linearUblock.setVisibility(View.GONE);
				}
			}
		});
		
		binding.voltar.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View _view) {
				binding.tela1.setVisibility(View.GONE);
				binding.tela2.setVisibility(View.VISIBLE);
				animarEntradaExpressiva(binding.tela2);
				binding.tela3.setVisibility(View.GONE);
			}
		});
		
		binding.switchJavascript.setOnCheckedChangeListener((button, enabled) -> {
            binding.webview.getSettings().setJavaScriptEnabled(enabled);
            if (!carregandoPreferencias) {
                dados.edit().putString("javascript", enabled ? "ativado" : "desativado").apply();
                String paginaAtual = binding.webview.getUrl();
                if (paginaAtual != null && !paginaAtual.trim().isEmpty()) binding.webview.reload();
                Toast.makeText(this, enabled ? "JavaScript ativado" : "JavaScript desativado", Toast.LENGTH_SHORT).show();
            }
        });
        binding.switchPapelParede.setOnCheckedChangeListener((button, enabled) -> {
            dados.edit().putBoolean("home_wallpaper", enabled).apply();
            if (!enabled) binding.tela1.setBackgroundColor(Color.rgb(14, 13, 18));
            if (enabled) carregarPapelParede();
        });
		
		binding.switchEstatisticas.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton _param1, boolean _param2) {
				final boolean _isChecked = _param2;
				if (_isChecked) {
					dados.edit().putString("estatisticas", "ligado").commit();
					binding.linearEstatisticas.setVisibility(View.VISIBLE);
				} else {
					dados.edit().putString("estatisticas", "desligado").commit();
					binding.linearEstatisticas.setVisibility(View.GONE);
				}
			}
		});
		
		binding.textAtualize.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View _view) {
				final android.widget.TextView tvPorcentagem =
				(android.widget.TextView) findViewById(R.id.text_porcentagem);
				
				final android.widget.TextView tvInformacao =
				(android.widget.TextView) findViewById(R.id.text_informacao);
				
				final android.os.Handler handler =
				new android.os.Handler(android.os.Looper.getMainLooper());
				
				final String[] nomes = {
					"uBlock Filters",
					"uBlock Privacy",
					"Quick Fixes",
					"Unbreak",
					"EasyList",
					"EasyPrivacy"
				};
				
				final String[] urls = {
					"https://ublockorigin.github.io/uAssets/filters/filters.min.txt",
					"https://ublockorigin.github.io/uAssets/filters/privacy.min.txt",
					"https://ublockorigin.github.io/uAssets/filters/quick-fixes.txt",
					"https://ublockorigin.github.io/uAssets/filters/unbreak.txt",
					"https://ublockorigin.github.io/uAssets/thirdparties/easylist.txt",
					"https://ublockorigin.github.io/uAssets/thirdparties/easyprivacy.txt"
				};
				
				final String[] arquivos = {
					"filters.min.txt",
					"privacy.min.txt",
					"quick-fixes.txt",
					"unbreak.txt",
					"easylist.txt",
					"easyprivacy.txt"
				};
				
				tvPorcentagem.setText("0%");
				tvInformacao.setText("Preparando atualização...");
				
				new android.os.AsyncTask<Void, String, String>() {
					
					private int arquivoAtual = 0;
					
					@Override
					protected String doInBackground(Void... params) {
						
						java.io.File pasta =
						new java.io.File(getFilesDir(), "ublock_lists");
						
						if (!pasta.exists()) {
							if (!pasta.mkdirs()) {
								return "ERRO_PASTA";
							}
						}
						
						for (arquivoAtual = 0;
						arquivoAtual < urls.length;
						arquivoAtual++) {
							
							java.net.HttpURLConnection conexao = null;
							java.io.InputStream entrada = null;
							java.io.FileOutputStream saida = null;
							
							try {
								
								publishProgress(
								"INFO",
								nomes[arquivoAtual],
								"0"
								);
								
								java.net.URL url =
								new java.net.URL(urls[arquivoAtual]);
								
								conexao =
								(java.net.HttpURLConnection) url.openConnection();
								
								conexao.setRequestMethod("GET");
								conexao.setConnectTimeout(15000);
								conexao.setReadTimeout(30000);
								conexao.setUseCaches(false);
								conexao.setDoInput(true);
								
								/*
                 * Evita compressão para que o progresso
                 * corresponda aos bytes realmente recebidos.
                 */
								conexao.setRequestProperty(
								"Accept-Encoding",
								"identity"
								);
								
								conexao.setRequestProperty(
								"User-Agent",
								"Mozilla/5.0 Android uBlockUpdater"
								);
								
								conexao.connect();
								
								int codigo =
								conexao.getResponseCode();
								
								if (codigo < 200 || codigo >= 300) {
									
									publishProgress(
									"ERRO",
									nomes[arquivoAtual],
									String.valueOf(codigo)
									);
									
									continue;
								}
								
								int tamanho =
								conexao.getContentLength();
								
								entrada =
								new java.io.BufferedInputStream(
								conexao.getInputStream(),
								8192
								);
								
								java.io.File temporario =
								new java.io.File(
								pasta,
								arquivos[arquivoAtual] + ".tmp"
								);
								
								java.io.File destino =
								new java.io.File(
								pasta,
								arquivos[arquivoAtual]
								);
								
								if (temporario.exists()) {
									temporario.delete();
								}
								
								saida =
								new java.io.FileOutputStream(
								temporario
								);
								
								byte[] buffer =
								new byte[8192];
								
								int lidos;
								long baixados = 0;
								
								while ((lidos = entrada.read(buffer)) != -1) {
									
									saida.write(buffer, 0, lidos);
									
									baixados += lidos;
									
									int porcentagem = 0;
									
									if (tamanho > 0) {
										
										porcentagem =
										(int) (
										(baixados * 100L)
										/ tamanho
										);
										
										if (porcentagem > 100) {
											porcentagem = 100;
										}
									}
									
									publishProgress(
									"PROGRESSO",
									nomes[arquivoAtual],
									String.valueOf(porcentagem),
									String.valueOf(baixados),
									String.valueOf(tamanho)
									);
								}
								
								saida.flush();
								saida.close();
								saida = null;
								
								if (destino.exists()) {
									destino.delete();
								}
								
								if (!temporario.renameTo(destino)) {
									
									temporario.delete();
									
									publishProgress(
									"ERRO",
									nomes[arquivoAtual],
									"Não foi possível salvar"
									);
									
									continue;
								}
								
								publishProgress(
								"PROGRESSO",
								nomes[arquivoAtual],
								"100",
								String.valueOf(baixados),
								String.valueOf(tamanho)
								);
								
							} catch (Exception e) {
								
								publishProgress(
								"ERRO",
								nomes[arquivoAtual],
								e.getClass().getSimpleName()
								);
								
							} finally {
								
								try {
									if (entrada != null) {
										entrada.close();
									}
								} catch (Exception e) {
								}
								
								try {
									if (saida != null) {
										saida.close();
									}
								} catch (Exception e) {
								}
								
								if (conexao != null) {
									conexao.disconnect();
								}
							}
						}
						
						return "FINALIZADO";
					}
					
					@Override
					protected void onProgressUpdate(String... valores) {
						
						if (valores == null || valores.length == 0) {
							return;
						}
						
						String tipo = valores[0];
						
						if (tipo.equals("INFO")) {
							
							String nome = valores[1];
							
							tvPorcentagem.setText("0%");
							
							tvInformacao.setText(
							"Baixando: " + nome
							);
							
						} else if (tipo.equals("PROGRESSO")) {
							
							String nome = valores[1];
							String porcentagem = valores[2];
							
							tvPorcentagem.setText(
							porcentagem + "%"
							);
							
							if (valores.length >= 5) {
								
								try {
									
									long baixados =
									Long.parseLong(valores[3]);
									
									long total =
									Long.parseLong(valores[4]);
									
									if (total > 0) {
										
										long baixadosKB =
										baixados / 1024L;
										
										long totalKB =
										total / 1024L;
										
										tvInformacao.setText(
										"Baixando: "
										+ nome
										+ " • "
										+ baixadosKB
										+ " KB / "
										+ totalKB
										+ " KB"
										);
										
									} else {
										
										long baixadosKB =
										baixados / 1024L;
										
										tvInformacao.setText(
										"Baixando: "
										+ nome
										+ " • "
										+ baixadosKB
										+ " KB"
										);
									}
									
								} catch (Exception e) {
									
									tvInformacao.setText(
									"Baixando: " + nome
									);
								}
								
							} else {
								
								tvInformacao.setText(
								"Baixando: " + nome
								);
							}
							
						} else if (tipo.equals("ERRO")) {
							
							String nome = valores[1];
							
							tvInformacao.setText(
							"Falha em: " + nome
							);
						}
					}
					
					@Override
					protected void onPostExecute(String resultado) {
						
						if ("FINALIZADO".equals(resultado)) {
							
							tvPorcentagem.setText("100%");
							
							tvInformacao.setText(
							"Atualização concluída!"
							);
							
						} else if ("ERRO_PASTA".equals(resultado)) {
							
							tvPorcentagem.setText("0%");
							
							tvInformacao.setText(
							"Erro ao criar pasta das listas"
							);
							
						} else {
							
							tvInformacao.setText(
							"Atualização finalizada"
							);
						}
					}
					
				}.execute();
			}
		});
	}
	
	private void initializeLogic() {
		// base Datu
		// ================================
		// REFERÊNCIAS DOS COMPONENTES
		// ================================
		
		final android.webkit.WebView webview =
		(android.webkit.WebView) findViewById(R.id.webview);
		
		final android.widget.EditText edittext1 =
		(android.widget.EditText) findViewById(R.id.edittext1);
		
		final android.widget.TextView text_barra =
		(android.widget.TextView) findViewById(R.id.text_barra);
		
		final android.view.View tela1 =
		findViewById(R.id.tela1);
		
		final android.view.View tela2 =
		findViewById(R.id.tela2);
		
		final android.view.View tela3 =
		findViewById(R.id.tela3);
		
		final android.view.View linear_pesquisa =
		findViewById(R.id.linear_pesquisa);
		
		// ================================
		// MD3 — ARREDONDAMENTO
		// ================================
		
		final float raio =
		20 * getResources()
		.getDisplayMetrics()
		.density;
		
		// ================================
		// FUNDO DO CAMPO DE PESQUISA
		// ================================
		
		android.graphics.drawable.GradientDrawable fundoPesquisa =
		new android.graphics.drawable.GradientDrawable();
		
		fundoPesquisa.setColor(
		android.graphics.Color.parseColor("#24212B")
		);
		
		fundoPesquisa.setCornerRadius(raio);
		
		linear_pesquisa.setBackground(fundoPesquisa);
		
		// ================================
		// FUNDO DA BARRA
		// ================================
		
		android.graphics.drawable.GradientDrawable fundoBarra =
		new android.graphics.drawable.GradientDrawable();
		
		fundoBarra.setColor(
		android.graphics.Color.parseColor("#24212B")
		);
		
		fundoBarra.setCornerRadius(raio);
		
		text_barra.setBackground(fundoBarra);
		
		// ================================
		// COOKIES
		// ================================
		
		android.webkit.CookieManager cookies =
		android.webkit.CookieManager.getInstance();
		
		cookies.setAcceptCookie(true);
		
		if (android.os.Build.VERSION.SDK_INT >= 21) {
			
			cookies.setAcceptThirdPartyCookies(  
			webview,  
			true  
			);
			
		}
		
		// ================================
		// WEBVIEW
		// ================================
		
		webview.setScrollBarStyle(
		android.view.View.SCROLLBARS_INSIDE_OVERLAY
		);
		
		webview.setVerticalScrollBarEnabled(true);
		
		webview.setHorizontalScrollBarEnabled(false);
		
		// ================================
		// SETTINGS
		// ================================
		
		final android.webkit.WebSettings s =
		webview.getSettings();
		
		// ================================
		// JAVASCRIPT
		// ================================
		
		s.setJavaScriptEnabled(true);
		
		s.setDomStorageEnabled(true);
		
		s.setCacheMode(
		android.webkit.WebSettings.LOAD_DEFAULT
		);
		
		s.setSaveFormData(true);
		
		s.setSupportMultipleWindows(true);
		
		s.setJavaScriptCanOpenWindowsAutomatically(
		true
		);
		
		s.setAllowFileAccess(true);
		
		s.setAllowContentAccess(true);
		
		s.setLoadsImagesAutomatically(true);
		
		s.setBlockNetworkImage(false);
		
		s.setBlockNetworkLoads(false);
		
		s.setMediaPlaybackRequiresUserGesture(false);
		
		s.setUseWideViewPort(true);
		
		s.setLoadWithOverviewMode(true);
		
		// ================================
		// USER AGENT
		// ================================
		
		s.setUserAgentString(
		"Mozilla/5.0 (Linux; Android 16; SM-A065M) " +
		"AppleWebKit/537.36 (KHTML, like Gecko) " +
		"Chrome/120.0.0.0 Mobile Safari/537.36"
		);
		
		// ================================
		// SAFE BROWSING
		// ================================
		
		if (android.os.Build.VERSION.SDK_INT >= 26) {
			
			try {  
				
				s.setSafeBrowsingEnabled(true);  
				
			} catch (Exception ignored) {  
			}
			
		}
		
		// ================================
		// MIXED CONTENT
		// ================================
		
		if (android.os.Build.VERSION.SDK_INT >= 21) {
			
			s.setMixedContentMode(  
			android.webkit.WebSettings  
			.MIXED_CONTENT_COMPATIBILITY_MODE  
			);
			
		}
		
		// ================================
		// DADOS — JAVASCRIPT
		// ================================
		
		String javascriptSalvo =
		dados.getString(
		"javascript",
		""
		);
		
		if (javascriptSalvo.equals("ativado")) {
			
			binding.switchJavascript.setChecked(true);  
			
			s.setJavaScriptEnabled(true);
			
		} else if (javascriptSalvo.equals("desativado")) {
			
			binding.switchJavascript.setChecked(false);  
			
			s.setJavaScriptEnabled(false);
			
		} else {
			
			// Padrão: ativado  
			
			binding.switchJavascript.setChecked(true);  
			
			s.setJavaScriptEnabled(true);
			
		}
		
		// ================================
		// WEBVIEW CLIENT
		// ================================
		
		webview.setWebViewClient(
		new android.webkit.WebViewClient()
		);
		
		// ================================
		// CHROME CLIENT
		// ================================
		
		webview.setWebChromeClient(
		new android.webkit.WebChromeClient() {
			
			private android.view.View customView;  
			
			private android.webkit.WebChromeClient.CustomViewCallback  
			customViewCallback;  
			
			private android.widget.FrameLayout  
			fullscreenContainer;  
			
			
			// ================================  
			// PROGRESSO  
			// ================================  
			
			@Override  
			public void onProgressChanged(  
			android.webkit.WebView view,  
			int progress  
			) {  
				
				if (progress < 100) {  
					
					text_barra.setText(formatarEndereco(view.getUrl()));  
					
				} else {  
					
					String title =  
					view.getTitle();  
					
					if (title != null  
					&& !title.isEmpty()) {  
						
						text_barra.setText(formatarEndereco(view.getUrl()));  
					}  
				}  
			}  
			
			
			// ================================  
			// TÍTULO  
			// ================================  
			
			@Override  
			public void onReceivedTitle(  
			android.webkit.WebView view,  
			String title  
			) {  
				
				if (title != null  
				&& !title.isEmpty()) {  
					
					text_barra.setText(formatarEndereco(view.getUrl()));  
				}  
			}  
			
			
			// ================================  
			// FULLSCREEN  
			// ================================  
			
			@Override
			public boolean onShowFileChooser(android.webkit.WebView webView, android.webkit.ValueCallback<android.net.Uri[]> callback, android.webkit.WebChromeClient.FileChooserParams params) {
				if (uploadCallback != null) uploadCallback.onReceiveValue(null);
				uploadCallback = callback;
				try {
					Intent intent = params != null ? params.createIntent() : new Intent(Intent.ACTION_OPEN_DOCUMENT);
					intent.addCategory(Intent.CATEGORY_OPENABLE);
					intent.setType("*/*");
					startActivityForResult(intent, FILE_CHOOSER_REQUEST);
					return true;
				} catch (Exception e) {
					uploadCallback = null;
					Toast.makeText(MainActivity.this, "Não foi possível abrir os arquivos.", Toast.LENGTH_SHORT).show();
					return false;
				}
			}

			@Override  
			public void onShowCustomView(  
			android.view.View view,  
			android.webkit.WebChromeClient.CustomViewCallback callback  
			) {  
				
				if (customView != null) {  
					
					callback.onCustomViewHidden();  
					
					return;  
				}  
				
				
				customView = view;  
				
				customViewCallback =  
				callback;  
				
				
				android.widget.FrameLayout decor =  
				(android.widget.FrameLayout)  
				getWindow()  
				.getDecorView();  
				
				
				fullscreenContainer =  
				new android.widget.FrameLayout(  
				MainActivity.this  
				);  
				
				
				fullscreenContainer  
				.setBackgroundColor(  
				android.graphics.Color.BLACK  
				);  
				
				
				fullscreenContainer.addView(  
				view,  
				new android.widget.FrameLayout.LayoutParams(  
				-1,  
				-1  
				)  
				);  
				
				
				decor.addView(  
				fullscreenContainer  
				);  
				
				
				decor.setSystemUiVisibility(  
				android.view.View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY  
				| android.view.View.SYSTEM_UI_FLAG_FULLSCREEN  
				| android.view.View.SYSTEM_UI_FLAG_HIDE_NAVIGATION  
				| android.view.View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN  
				| android.view.View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION  
				);  
			}  
			
			
			// ================================  
			// SAIR DO FULLSCREEN  
			// ================================  
			
			@Override  
			public void onHideCustomView() {  
				
				android.widget.FrameLayout decor =  
				(android.widget.FrameLayout)  
				getWindow()  
				.getDecorView();  
				
				
				if (fullscreenContainer != null) {  
					
					decor.removeView(  
					fullscreenContainer  
					);  
				}  
				
				
				fullscreenContainer = null;  
				
				customView = null;  
				
				
				decor.setSystemUiVisibility(  
				android.view.View.SYSTEM_UI_FLAG_VISIBLE  
				);  
				
				
				if (customViewCallback != null) {  
					
					customViewCallback  
					.onCustomViewHidden();  
					
					customViewCallback = null;  
				}  
			}  
		}
		
		);
		
		// ================================
		// PESQUISA
		// ================================
		
		edittext1.setOnEditorActionListener(
		new android.widget.TextView.OnEditorActionListener() {
			
			@Override  
			public boolean onEditorAction(  
			android.widget.TextView v,  
			int actionId,  
			android.view.KeyEvent event  
			) {  
				
				boolean enter =  
				actionId ==  
				android.view.inputmethod  
				.EditorInfo  
				.IME_ACTION_SEARCH  
				
				|| actionId ==  
				android.view.inputmethod  
				.EditorInfo  
				.IME_ACTION_GO  
				
				|| actionId ==  
				android.view.inputmethod  
				.EditorInfo  
				.IME_ACTION_DONE  
				
				|| (  
				event != null  
				
				&& event.getKeyCode() ==  
				android.view.KeyEvent.KEYCODE_ENTER  
				
				&& event.getAction() ==  
				android.view.KeyEvent.ACTION_DOWN  
				);  
				
				
				if (!enter) {  
					
					return false;  
				}  
				
				
				String input =  
				edittext1  
				.getText()  
				.toString()  
				.trim();  
				
				
				if (input.isEmpty()) {  
					
					return true;  
				}  
				
				
				String url;  
				
				
				// ================================  
				// URL DIRETA  
				// ================================  
				
				if (input.matches(  
				"^(?i)(https?|file)://.*"  
				)) {  
					
					url = input;  
					
				} else {  
					
					
					// ================================  
					// BRAVE SEARCH  
					// ================================  
					
					try {  
						
						url =  
						"https://search.brave.com/search?q="  
						+ java.net.URLEncoder.encode(  
						input,  
						"UTF-8"  
						);  
						
					} catch (Exception e) {  
						
						url =  
						"https://search.brave.com/search?q="  
						+ input.replace(  
						" ",  
						"+"  
						);  
					}  
				}  
				
				
				// ================================  
				// CARREGAR PÁGINA  
				// ================================  
				
				webview.loadUrl(url);  
				
				
				// ================================  
				// TROCA DE TELA  
				// ================================  
				
				tela1.setVisibility(  
				android.view.View.GONE  
				);  
				
				tela2.setVisibility(  
				android.view.View.VISIBLE  
				);  
				
				tela3.setVisibility(  
				android.view.View.GONE  
				);  
				
				
				// ================================  
				// LIMPAR PESQUISA  
				// ================================  
				
				edittext1.setText("");  
				
				
				return true;  
			}  
		}
		
		);
		binding.webview.loadUrl("https://search.brave.com");
		// anúncios
		// ============================================================
		// DATU BROWSER - SHIELDS V3.5
		// BLOQUEADOR HÍBRIDO SEGURO
		//
		// - Switch persistente
		// - Contador de bloqueios
		// - Economia estimada
		// - Cache
		// - uBlock/EasyList/EasyPrivacy
		// - Bloqueio cosmético
		// - Proteção contra quebra de CSS/JS
		// - Não transforma recursos bloqueados em downloadfile.bin
		//
		// Java 7 / Sketchware Pro
		// ============================================================
		
		
		// ============================================================
		// REGRA DE REDE
		// ============================================================
		
		final class RegraRede {
			
			String padrao;
			
			boolean apenasTerceiro = false;
			
			java.util.ArrayList<String> dominiosPermitidos =
			new java.util.ArrayList<String>();
			
			java.util.ArrayList<String> dominiosNegados =
			new java.util.ArrayList<String>();
		}
		
		
		// ============================================================
		// CATEGORIA
		// ============================================================
		
		final class ShieldsCategoria {
			
			String nome;
			
			boolean ativa = true;
			
			java.util.HashSet<String> exatos =
			new java.util.HashSet<String>();
			
			java.util.ArrayList<RegraRede> parciais =
			new java.util.ArrayList<RegraRede>();
			
			java.util.ArrayList<String> excecoes =
			new java.util.ArrayList<String>();
			
			StringBuilder cosmeticos =
			new StringBuilder();
			
			int contagemCss = 0;
			
			int cotaCssMax = 3000;
		}
		
		
		// ============================================================
		// ESTRUTURAS
		// ============================================================
		
		final java.util.LinkedHashMap<String, ShieldsCategoria>
		shieldsCategorias =
		new java.util.LinkedHashMap<String, ShieldsCategoria>();
		
		
		final java.util.concurrent.atomic.AtomicInteger
		shieldsBloqueadosContador =
		new java.util.concurrent.atomic.AtomicInteger(0);
		
		
		final java.util.concurrent.atomic.AtomicLong
		shieldsBytesEconomizados =
		new java.util.concurrent.atomic.AtomicLong(0);

		final java.util.concurrent.atomic.AtomicLong
		shieldsUltimaAtualizacaoUI =
		new java.util.concurrent.atomic.AtomicLong(0);
		
		
		final java.util.concurrent.atomic.AtomicBoolean shieldsPronto =
		new java.util.concurrent.atomic.AtomicBoolean(false);
		
		
		final String[] urlPaginaAtual =
		new String[] {""};
		
		
		final java.io.File shieldsPasta =
		new java.io.File(
		getFilesDir(),
		"ublock_lists"
		);
		
		
		final java.util.HashMap<String, Boolean>
		shieldsCache =
		new java.util.HashMap<String, Boolean>();
		
		
		final int SHIELDS_CACHE_MAX = 6000;
		
		
		// ============================================================
		// ECONOMIA ESTIMADA
		// ============================================================
		
		final long SHIELDS_BYTES_POR_BLOQUEIO =
		15L * 1024L;
		
		
		// ============================================================
		// PREFERÊNCIAS
		// ============================================================
		
		final android.content.SharedPreferences
		shieldsPrefs =
		getSharedPreferences(
		"datu_shields",
		MODE_PRIVATE
		);
		
		
		final String SHIELDS_SWITCH_KEY =
		"bloqueio_anuncios_ativo";
		
		
		// ============================================================
		// CSS GENÉRICO
		// ============================================================
		
		final String SHIELDS_CSS_COLAPSO_GENERICO =
		"ins.adsbygoogle,"
		+ "iframe[id^='google_ads'],"
		+ "iframe[src*='doubleclick'],"
		+ "iframe[src*='googlesyndication'],"
		+ "div[id^='div-gpt-ad'],"
		+ "div[id*='google_ads'],"
		+ "div[class*='ad-slot'],"
		+ "div[class*='ad-container'],"
		+ "div[class*='ad-banner'],"
		+ "div[class*='ads-'],"
		+ "div[class^='ad-'],"
		+ "aside[class*='ad'],"
		+ "ins[class*='ad'],"
		+ "div[data-ad-slot],"
		+ "div[data-ad-client]"
		+ "{display:none!important;"
		+ "height:0!important;"
		+ "min-height:0!important;"
		+ "max-height:0!important;"
		+ "margin:0!important;"
		+ "padding:0!important;"
		+ "border:none!important;"
		+ "overflow:hidden!important;}";
		
		
		// ============================================================
		// MOTOR
		// ============================================================
		
		final class ShieldsMotor {
			
			
			// --------------------------------------------------------
			// LIMPAR DOMÍNIO
			// --------------------------------------------------------
			
			String limparDominio(String s) {
				
				if (s == null) {
					return "";
				}
				
				s = s.trim().toLowerCase();
				
				if (s.startsWith("||")) {
					s = s.substring(2);
				}
				
				while (s.startsWith(".")) {
					s = s.substring(1);
				}
				
				while (s.endsWith(".")) {
					s = s.substring(
					0,
					s.length() - 1
					);
				}
				
				return s;
			}
			
			
			// --------------------------------------------------------
			// DOMÍNIO VÁLIDO
			// --------------------------------------------------------
			
			boolean dominioValido(String s) {
				
				if (
				s == null
				|| s.length() == 0
				) {
					return false;
				}
				
				if (s.indexOf("/") >= 0) {
					return false;
				}
				
				if (s.indexOf(" ") >= 0) {
					return false;
				}
				
				if (s.indexOf("*") >= 0) {
					return false;
				}
				
				return true;
			}
			
			
			// --------------------------------------------------------
			// PERTENCE AO DOMÍNIO
			// --------------------------------------------------------
			
			boolean pertenceDominio(
			String host,
			String dominio) {
				
				if (
				host == null
				|| dominio == null
				) {
					return false;
				}
				
				host = host.toLowerCase();
				dominio = dominio.toLowerCase();
				
				if (host.equals(dominio)) {
					return true;
				}
				
				return host.endsWith(
				"." + dominio
				);
			}
			
			
			// --------------------------------------------------------
			// HOST
			// --------------------------------------------------------
			
			String obterHost(String url) {
				
				if (url == null) {
					return "";
				}
				
				try {
					
					android.net.Uri uri =
					android.net.Uri.parse(url);
					
					String host =
					uri.getHost();
					
					if (host == null) {
						return "";
					}
					
					return host.toLowerCase();
					
				} catch (Exception e) {
					
					return "";
				}
			}
			
			
			// --------------------------------------------------------
			// HIERARQUIA DO DOMÍNIO
			// --------------------------------------------------------
			
			java.util.ArrayList<String>
			hierarquiaDominio(String host) {
				
				java.util.ArrayList<String> lista =
				new java.util.ArrayList<String>();
				
				if (
				host == null
				|| host.length() == 0
				) {
					return lista;
				}
				
				host = host.toLowerCase();
				
				String[] partes =
				host.split("\\.");
				
				if (partes.length < 2) {
					
					lista.add(host);
					
					return lista;
				}
				
				for (
				int i = 0;
				i < partes.length - 1;
				i++
				) {
					
					StringBuilder sb =
					new StringBuilder();
					
					for (
					int j = i;
					j < partes.length;
					j++
					) {
						
						if (sb.length() > 0) {
							sb.append(".");
						}
						
						sb.append(partes[j]);
					}
					
					lista.add(
					sb.toString()
					);
				}
				
				return lista;
			}
			
			
			// --------------------------------------------------------
			// MATCH
			// --------------------------------------------------------
			
			boolean verificarMatch(
			String url,
			String regra) {
				
				if (
				url == null
				|| regra == null
				) {
					return false;
				}
				
				String r =
				regra.trim().toLowerCase();
				
				if (r.length() == 0) {
					return false;
				}
				
				
				// --------------------------------------------
				// REGRA ||dominio/caminho
				// --------------------------------------------
				
				if (r.startsWith("||")) {
					
					String semPrefixo =
					r.substring(2);
					
					int pos =
					semPrefixo.indexOf("/");
					
					if (pos >= 0) {
						
						String dominio =
						semPrefixo.substring(
						0,
						pos
						);
						
						String caminho =
						semPrefixo.substring(
						pos
						);
						
						
						if (
						dominioValido(dominio)
						&&
						pertenceDominio(
						obterHost(url),
						limparDominio(
						dominio
						)
						)
						) {
							
							if (
							caminho.length() <= 1
							) {
								
								return true;
							}
							
							String urlLower =
							url.toLowerCase();
							
							String caminhoLower =
							caminho.toLowerCase();
							
							if (
							urlLower.contains(
							caminhoLower
							)
							) {
								
								return true;
							}
						}
					}
				}
				
				
				// --------------------------------------------
				// REGRA NORMAL
				// --------------------------------------------
				
				String regraLimpa =
				r.replace(
				"||",
				""
				);
				
				regraLimpa =
				regraLimpa.replace(
				"|",
				""
				);
				
				
				// âncora final
				if (
				regraLimpa.endsWith("^")
				) {
					
					regraLimpa =
					regraLimpa.substring(
					0,
					regraLimpa.length() - 1
					);
				}
				
				
				if (
				regraLimpa.length() == 0
				) {
					return false;
				}
				
				
				return url.toLowerCase()
				.contains(
				regraLimpa
				);
			}
			
			
			// --------------------------------------------------------
			// DOMÍNIO PURO
			// --------------------------------------------------------
			
			boolean ehRegraDominioPuro(
			String regraSemOpcoes) {
				
				if (
				regraSemOpcoes == null
				||
				regraSemOpcoes.length() == 0
				) {
					return false;
				}
				
				String r =
				regraSemOpcoes.trim();
				
				if (r.startsWith("||")) {
					r = r.substring(2);
				}
				
				if (r.endsWith("^")) {
					
					r =
					r.substring(
					0,
					r.length() - 1
					);
				}
				
				if (
				r.indexOf("/") >= 0
				||
				r.indexOf("*") >= 0
				||
				r.indexOf("?") >= 0
				||
				r.indexOf("=") >= 0
				||
				r.indexOf("&") >= 0
				) {
					
					return false;
				}
				
				return dominioValido(
				limparDominio(r)
				);
			}
			
			
			// --------------------------------------------------------
			// RECURSO SEGURO PARA BLOQUEIO
			// --------------------------------------------------------
			
			boolean recursoSeguroParaBloquear(
			String url) {
				
				if (url == null) {
					return false;
				}
				
				String u =
				url.toLowerCase();
				
				
				// --------------------------------------------
				// Nunca bloquear protocolos especiais
				// --------------------------------------------
				
				if (
				u.startsWith("data:")
				||
				u.startsWith("blob:")
				||
				u.startsWith("file:")
				||
				u.startsWith("about:")
				||
				u.startsWith("javascript:")
				) {
					
					return false;
				}
				
				
				// --------------------------------------------
				// Recursos que não devemos bloquear
				// genericamente porque podem quebrar o site
				// --------------------------------------------
				
				if (
				u.contains(".css")
				||
				u.contains(".woff")
				||
				u.contains(".woff2")
				||
				u.contains(".ttf")
				||
				u.contains(".otf")
				||
				u.contains(".eot")
				) {
					
					return false;
				}
				
				
				return true;
			}
			
			
			// --------------------------------------------------------
			// RECURSO PROVAVELMENTE PUBLICIDADE
			// --------------------------------------------------------
			
			boolean parecePublicidade(
			String url) {
				
				if (url == null) {
					return false;
				}
				
				String u =
				url.toLowerCase();
				
				
				String[] sinais = {
					
					"doubleclick",
					"googlesyndication",
					"googleadservices",
					"adservice",
					"adsystem",
					"adserver",
					"advertising",
					"advertisement",
					"ads.",
					"/ads/",
					"/ad/",
					"banner",
					"prebid",
					"adnxs",
					"outbrain",
					"taboola",
					"criteo",
					"pubmatic",
					"rubiconproject",
					"openx",
					"amazon-adsystem",
					"googletagmanager"
				};
				
				
				for (
				int i = 0;
				i < sinais.length;
				i++
				) {
					
					if (
					u.contains(
					sinais[i]
					)
					) {
						
						return true;
					}
				}
				
				
				return false;
			}
			
			
			// --------------------------------------------------------
			// RESPOSTA VAZIA SEGURA
			// --------------------------------------------------------
			
			android.webkit.WebResourceResponse
			respostaVazia() {
				
				return new android.webkit
				.WebResourceResponse(
				"text/plain",
				"UTF-8",
				new java.io.ByteArrayInputStream(
				new byte[0]
				)
				);
			}
		}
		
		
		final ShieldsMotor shields =
		new ShieldsMotor();
		
		
		// ============================================================
		// TEXTVIEWS
		// ============================================================
		
		final android.widget.TextView
		shieldsTextBloqueados =
		(android.widget.TextView) findViewById(
		getResources().getIdentifier(
		"text_bloqueiados",
		"id",
		getPackageName()
		)
		);
		
		
		final android.widget.TextView
		shieldsTextEconomia =
		(android.widget.TextView) findViewById(
		getResources().getIdentifier(
		"text_economia",
		"id",
		getPackageName()
		)
		);
		
		
		// ============================================================
		// ATUALIZAR TEXTOS
		// ============================================================
		
		final Runnable
		atualizarShieldsTextos =
		new Runnable() {
			
			@Override
			public void run() {
				
				if (
				shieldsTextBloqueados
				!= null
				) {
					
					shieldsTextBloqueados.setText(
					String.valueOf(
					shieldsBloqueadosContador
					.get()
					)
					);
				}
				
				
				if (
				shieldsTextEconomia
				!= null
				) {
					
					long bytes =
					shieldsBytesEconomizados
					.get();
					
					
					String texto;
					
					
					if (
					bytes < 1024L
					) {
						
						texto =
						bytes + " B";
						
					} else if (
					bytes
					<
					1024L * 1024L
					) {
						
						texto =
						String.format(
						java.util.Locale.US,
						"%.1f KB",
						bytes / 1024.0
						);
						
					} else {
						
						texto =
						String.format(
						java.util.Locale.US,
						"%.2f MB",
						bytes
						/
						(
						1024.0
						*
						1024.0
						)
						);
					}
					
					
					shieldsTextEconomia.setText(
					texto
					);
				}
			}
		};
		
		
		final Runnable
		atualizarShieldsTextosUI =
		new Runnable() {
			
			@Override
			public void run() {
				
				if (
				android.os.Looper.myLooper()
				==
				android.os.Looper
				.getMainLooper()
				) {
					
					atualizarShieldsTextos.run();
					
				} else {
					
					runOnUiThread(
					atualizarShieldsTextos
					);
				}
			}
		};
		
		
		// ============================================================
		// CATEGORIAS
		// ============================================================
		
		final ShieldsCategoria categoriaPrincipal =
		new ShieldsCategoria();
		
		categoriaPrincipal.nome =
		"principal";
		
		categoriaPrincipal.cotaCssMax =
		350;
		
		shieldsCategorias.put(
		"principal",
		categoriaPrincipal
		);
		
		
		final ShieldsCategoria categoriaPrivacidade =
		new ShieldsCategoria();
		
		categoriaPrivacidade.nome =
		"privacidade";
		
		categoriaPrivacidade.cotaCssMax =
		200;
		
		shieldsCategorias.put(
		"privacidade",
		categoriaPrivacidade
		);
		
		
		final ShieldsCategoria categoriaCorrecoes =
		new ShieldsCategoria();
		
		categoriaCorrecoes.nome =
		"correcoes";
		
		categoriaCorrecoes.cotaCssMax =
		100;
		
		shieldsCategorias.put(
		"correcoes",
		categoriaCorrecoes
		);
		
		
		final ShieldsCategoria categoriaCompatibilidade =
		new ShieldsCategoria();
		
		categoriaCompatibilidade.nome =
		"compatibilidade";
		
		categoriaCompatibilidade.cotaCssMax =
		100;
		
		shieldsCategorias.put(
		"compatibilidade",
		categoriaCompatibilidade
		);
		
		
		final ShieldsCategoria categoriaEasyList =
		new ShieldsCategoria();
		
		categoriaEasyList.nome =
		"easylist";
		
		categoriaEasyList.cotaCssMax =
		350;
		
		shieldsCategorias.put(
		"easylist",
		categoriaEasyList
		);
		
		
		final ShieldsCategoria categoriaEasyPrivacy =
		new ShieldsCategoria();
		
		categoriaEasyPrivacy.nome =
		"easyprivacy";
		
		categoriaEasyPrivacy.cotaCssMax =
		200;
		
		shieldsCategorias.put(
		"easyprivacy",
		categoriaEasyPrivacy
		);
		
		
		// ============================================================
		// LOADER
		// ============================================================
		
		final class ShieldsLoader {
			
			
			void carregar(
			java.io.File arquivo,
			ShieldsCategoria categoria) {
				
				if (
				arquivo == null
				||
				categoria == null
				||
				!arquivo.exists()
				) {
					
					return;
				}
				
				
				java.io.BufferedReader br =
				null;
				
				
				try {
					
					br =
					new java.io.BufferedReader(
					new java.io.InputStreamReader(
					new java.io.FileInputStream(
					arquivo
					),
					"UTF-8"
					)
					);
					
					
					String linha;
					
					
					while (
					(linha = br.readLine())
					!= null
					) {
						
						linha =
						linha.trim();
						
						
						// --------------------------------------------
						// COMENTÁRIOS
						// --------------------------------------------
						
						if (
						linha.length() == 0
						||
						linha.startsWith("!")
						||
						linha.startsWith("[")
						) {
							
							continue;
						}
						
						
						// --------------------------------------------
						// EXCEÇÃO
						// --------------------------------------------
						
						if (
						linha.startsWith("@@")
						) {
							
							String excecao =
							linha.substring(2);
							
							
							int pos =
							excecao.indexOf("$");
							
							
							if (pos >= 0) {
								
								excecao =
								excecao.substring(
								0,
								pos
								);
							}
							
							
							excecao =
							excecao.trim();
							
							
							if (
							excecao.length() > 0
							) {
								
								categoria.excecoes.add(
								excecao
								);
							}
							
							
							continue;
						}
						
						
						// --------------------------------------------
						// CSS COSMÉTICO
						// --------------------------------------------
						
						if (
						linha.indexOf("##")
						>= 0
						) {
							
							int pos = linha.indexOf("##");
							// Seletores de domínio não podem ser aplicados globalmente.
							if (pos != 0) continue;
							
							
							String seletor =
							linha.substring(
							pos + 2
							).trim();
							
							
							if (
							seletor.length() > 0
							&&
							categoria.contagemCss
							<
							categoria.cotaCssMax
							) {
								
								categoria.cosmeticos
								.append(
								seletor
								)
								.append(",");
								
								
								categoria.contagemCss++;
							}
							
							
							continue;
						}
						
						
						// --------------------------------------------
						// OPÇÕES
						// --------------------------------------------
						
						String regra =
						linha;
						
						
						int posOpcao =
						regra.indexOf("$");
						
						
						String opcoes =
						"";
						
						
						if (
						posOpcao >= 0
						) {
							
							opcoes =
							regra.substring(
							posOpcao + 1
							);
							
							
							regra =
							regra.substring(
							0,
							posOpcao
							);
						}
						
						
						regra =
						regra.trim();
						
						
						if (
						regra.length() == 0
						) {
							
							continue;
						}
						
						
						// --------------------------------------------
						// THIRD PARTY
						// --------------------------------------------
						
						boolean apenasTerceiro =
						false;
						
						
						String[] partesOpcoes =
						opcoes.split(",");
						
						
						for (
						int i = 0;
						i < partesOpcoes.length;
						i++
						) {
							
							String opcao =
							partesOpcoes[i]
							.trim()
							.toLowerCase();
							
							
							if (
							opcao.equals(
							"third-party"
							)
							) {
								
								apenasTerceiro =
								true;
							}
						}
						
						
						// --------------------------------------------
						// DOMÍNIO PURO
						// --------------------------------------------
						
						if (
						shields.ehRegraDominioPuro(
						regra
						)
						) {
							
							String dominio =
							shields.limparDominio(
							regra
							);
							
							
							if (
							shields.dominioValido(
							dominio
							)
							) {
								
								categoria.exatos.add(
								dominio
								);
							}
							
							
							continue;
						}
						
						
						// --------------------------------------------
						// REGRA NORMAL
						// --------------------------------------------
						
						boolean opcoesCompativeis = true;
						for (int oi = 0; oi < partesOpcoes.length; oi++) {
							String opcao = partesOpcoes[oi].trim().toLowerCase();
							if (opcao.length() == 0) continue;
							if (!opcao.equals("third-party") && !opcao.startsWith("domain=")) {
								opcoesCompativeis = false;
								break;
							}
						}
						if (!opcoesCompativeis) continue;

						RegraRede rr =
						new RegraRede();
						
						
						rr.padrao =
						regra;
						
						
						rr.apenasTerceiro =
						apenasTerceiro;
						
						
						// --------------------------------------------
						// DOMAIN=
						// --------------------------------------------
						
						for (
						int i = 0;
						i < partesOpcoes.length;
						i++
						) {
							
							String opcao =
							partesOpcoes[i]
							.trim();
							
							
							if (
							opcao.startsWith(
							"domain="
							)
							) {
								
								String dominios =
								opcao.substring(
								7
								);
								
								
								String[] ds =
								dominios.split(
								"\\|"
								);
								
								
								for (
								int j = 0;
								j < ds.length;
								j++
								) {
									
									String d =
									ds[j].trim();
									
									
									if (
									d.length() == 0
									) {
										
										continue;
									}
									
									
									if (
									d.startsWith("~")
									) {
										
										d =
										d.substring(
										1
										);
										
										
										d =
										shields
										.limparDominio(
										d
										);
										
										
										if (
										shields
										.dominioValido(
										d
										)
										) {
											
											rr.dominiosNegados
											.add(d);
										}
										
									} else {
										
										d =
										shields
										.limparDominio(
										d
										);
										
										
										if (
										shields
										.dominioValido(
										d
										)
										) {
											
											rr.dominiosPermitidos
											.add(d);
										}
									}
								}
							}
						}
						
						
						if (categoria.parciais.size() < 12000) {
							categoria.parciais.add(rr);
						}
					}
					
					
				} catch (Exception e) {
					
					
				} finally {
					
					if (br != null) {
						
						try {
							br.close();
						} catch (Exception e) {
						}
					}
				}
			}
		}
		
		
		final ShieldsLoader shieldsLoader =
		new ShieldsLoader();
		
		
		// ============================================================
		// CARREGAR LISTAS
		// ============================================================
		
		// Analisar as listas em segundo plano para não travar a interface.
		new Thread(new Runnable() {
			@Override
			public void run() {
				shieldsLoader.carregar(new java.io.File(shieldsPasta, "filters.min.txt"), categoriaPrincipal);
				shieldsLoader.carregar(new java.io.File(shieldsPasta, "privacy.min.txt"), categoriaPrivacidade);
				shieldsLoader.carregar(new java.io.File(shieldsPasta, "quick-fixes.txt"), categoriaCorrecoes);
				shieldsLoader.carregar(new java.io.File(shieldsPasta, "unbreak.txt"), categoriaCompatibilidade);
				shieldsLoader.carregar(new java.io.File(shieldsPasta, "easylist.txt"), categoriaEasyList);
				shieldsLoader.carregar(new java.io.File(shieldsPasta, "easyprivacy.txt"), categoriaEasyPrivacy);
				shieldsPronto.set(true);
			}
		}, "DatuShieldsFilterLoader").start();
		
		
		// ============================================================
		// SWITCH
		// ============================================================
		
		final java.util.concurrent.atomic.AtomicBoolean shieldsAtivo =
		new java.util.concurrent.atomic.AtomicBoolean(false);

		final android.widget.Switch
		switchBloqueioAnuncios =
		(android.widget.Switch) findViewById(
		getResources().getIdentifier(
		"switch_bloqueio_de_anuncios",
		"id",
		getPackageName()
		)
		);
		
		
		if (
		switchBloqueioAnuncios
		!= null
		) {
			
			
			boolean estadoSalvo =
			shieldsPrefs.getBoolean(
			SHIELDS_SWITCH_KEY,
			false
			);
			shieldsAtivo.set(estadoSalvo);
			
			
			switchBloqueioAnuncios
			.setOnCheckedChangeListener(
			null
			);
			
			
			switchBloqueioAnuncios
			.setChecked(
			estadoSalvo
			);
			
			
			switchBloqueioAnuncios
			.setOnCheckedChangeListener(
			
			new android.widget.CompoundButton
			.OnCheckedChangeListener() {
				
				@Override
				public void onCheckedChanged(
				android.widget.CompoundButton buttonView,
				boolean isChecked) {
					
					
					// ----------------------------------------
					// SALVAR ESTADO
					// ----------------------------------------
					
					shieldsPrefs
					.edit()
					.putBoolean(
					SHIELDS_SWITCH_KEY,
					isChecked
					)
					.apply();
					shieldsAtivo.set(isChecked);
					
					
					// ----------------------------------------
					// LIMPAR CACHE
					// ----------------------------------------
					
					synchronized (
					shieldsCache
					) {
						
						shieldsCache.clear();
					}
					
					
					// ----------------------------------------
					// RESETAR ESTATÍSTICAS
					// ----------------------------------------
					
					shieldsBloqueadosContador
					.set(0);
					
					
					shieldsBytesEconomizados
					.set(0);
					
					
					atualizarShieldsTextosUI
					.run();
					
					
					// ----------------------------------------
					// LIMPAR CSS
					// ----------------------------------------
					
					if (
					webview != null
					) {
						
						webview.evaluateJavascript(
						"(function(){"
						+ "var s=document.getElementById('shields-css');"
						+ "if(s)s.remove();"
						+ "})();",
						null
						);
						
						
						// ------------------------------------
						// RECARREGAR
						// ------------------------------------
						
						webview.reload();
					}
				}
			}
			);
		}
		
		
		// ============================================================
		// WEBVIEW CLIENT
		// ============================================================
		
		webview.setWebViewClient(
		new android.webkit.WebViewClient() {
			
			
			// ====================================================
			// PÁGINA COMEÇOU
			// ====================================================
			
			@Override
			public void onPageStarted(
			android.webkit.WebView view,
			String url,
			android.graphics.Bitmap favicon) {
				
				super.onPageStarted(
				view,
				url,
				favicon
				);
				
				
				urlPaginaAtual[0] =
				url == null
				? ""
				: url;
				
				
				synchronized (
				shieldsCache
				) {
					
					shieldsCache.clear();
				}
				
				
				shieldsBloqueadosContador
				.set(0);
				
				
				shieldsBytesEconomizados
				.set(0);
				
				
				atualizarShieldsTextosUI
				.run();
			}
			
			
			// ====================================================
			// CONTEXTO
			// ====================================================
			
			boolean regraAplicaAoContexto(
			RegraRede regra,
			String url,
			String pagina) {
				
				
				if (
				regra == null
				|| url == null
				) {
					
					return false;
				}
				
				
				String hostRecurso =
				shields.obterHost(
				url
				);
				
				
				String hostPagina =
				shields.obterHost(
				pagina
				);
				
				
				// --------------------------------------------
				// THIRD PARTY
				// --------------------------------------------
				
				if (
				regra.apenasTerceiro
				) {
					
					if (
					hostRecurso.length() == 0
					||
					hostPagina.length() == 0
					) {
						
						return false;
					}
					
					
					if (
					shields.pertenceDominio(
					hostRecurso,
					hostPagina
					)
					) {
						
						return false;
					}
				}
				
				
				// --------------------------------------------
				// DOMAIN PERMITIDO
				// --------------------------------------------
				
				if (
				regra.dominiosPermitidos
				.size() > 0
				) {
					
					boolean permitido =
					false;
					
					
					for (
					int i = 0;
					i <
					regra.dominiosPermitidos
					.size();
					i++
					) {
						
						if (
						shields.pertenceDominio(
						hostPagina,
						regra.dominiosPermitidos
						.get(i)
						)
						) {
							
							permitido =
							true;
							
							break;
						}
					}
					
					
					if (!permitido) {
						return false;
					}
				}
				
				
				// --------------------------------------------
				// DOMAIN NEGADO
				// --------------------------------------------
				
				for (
				int i = 0;
				i <
				regra.dominiosNegados
				.size();
				i++
				) {
					
					if (
					shields.pertenceDominio(
					hostPagina,
					regra.dominiosNegados
					.get(i)
					)
					) {
						
						return false;
					}
				}
				
				
				return true;
			}
			
			
			// ====================================================
			// DEVE BLOQUEAR
			// ====================================================
			
			boolean deveBloquear(
			String url) {
				
				
				// --------------------------------------------
				// SWITCH
				// --------------------------------------------
				
				if (!shieldsAtivo.get()) {
					
					return false;
				}
				
				
				// --------------------------------------------
				// MOTOR
				// --------------------------------------------
				
				if (
				!shieldsPronto.get()
				) {
					
					return false;
				}
				
				
				// --------------------------------------------
				// URL
				// --------------------------------------------
				
				if (
				url == null
				||
				url.length() == 0
				) {
					
					return false;
				}
				
				
				String u =
				url.toLowerCase();
				
				
				// --------------------------------------------
				// PROTOCOLOS ESPECIAIS
				// --------------------------------------------
				
				if (
				u.startsWith("data:")
				||
				u.startsWith("blob:")
				||
				u.startsWith("file:")
				||
				u.startsWith("about:")
				||
				u.startsWith("javascript:")
				) {
					
					return false;
				}
				
				
				// --------------------------------------------
				// NÃO INTERCEPTAR CSS/FONTES
				// --------------------------------------------
				
				if (
				u.contains(".css")
				||
				u.contains(".woff")
				||
				u.contains(".woff2")
				||
				u.contains(".ttf")
				||
				u.contains(".otf")
				||
				u.contains(".eot")
				) {
					
					return false;
				}
				
				
				// --------------------------------------------
				// CACHE
				// --------------------------------------------
				
				synchronized (
				shieldsCache
				) {
					
					Boolean cached =
					shieldsCache.get(
					url
					);
					
					
					if (
					cached != null
					) {
						
						return cached
						.booleanValue();
					}
				}
				
				
				boolean bloquear =
				false;
				
				
				String pagina =
				urlPaginaAtual[0];
				
				
				String host =
				shields.obterHost(
				url
				);
				
				
				// =================================================
				// CATEGORIAS
				// =================================================
				
				for (
				ShieldsCategoria categoria :
				shieldsCategorias.values()
				) {
					
					
					if (
					!categoria.ativa
					) {
						
						continue;
					}
					
					
					// --------------------------------------------
					// EXCEÇÕES
					// --------------------------------------------
					
					boolean excecao =
					false;
					
					
					for (
					int i = 0;
					i <
					categoria.excecoes
					.size();
					i++
					) {
						
						String ex =
						categoria.excecoes
						.get(i);
						
						
						if (
						shields.verificarMatch(
						url,
						ex
						)
						) {
							
							excecao =
							true;
							
							break;
						}
					}
					
					
					if (excecao) {
						continue;
					}
					
					
					// --------------------------------------------
					// DOMÍNIO EXATO
					//
					// Só tratamos como bloqueio forte
					// quando o domínio inteiro está na lista.
					// --------------------------------------------
					
					if (
					host.length() > 0
					) {
						
						java.util.ArrayList<String>
						hierarquia =
						shields.hierarquiaDominio(
						host
						);
						
						
						for (
						int i = 0;
						i <
						hierarquia.size();
						i++
						) {
							
							if (
							categoria.exatos
							.contains(
							hierarquia.get(i)
							)
							) {
								
								bloquear =
								true;
								
								break;
							}
						}
					}
					
					
					if (bloquear) {
						break;
					}
					
					
					// --------------------------------------------
					// REGRAS PARCIAIS
					// --------------------------------------------
					
					for (
					int i = 0;
					i <
					categoria.parciais
					.size();
					i++
					) {
						
						RegraRede rr =
						categoria.parciais
						.get(i);
						
						
						if (
						!regraAplicaAoContexto(
						rr,
						url,
						pagina
						)
						) {
							
							continue;
						}
						
						
						if (
						!shields.verificarMatch(
						url,
						rr.padrao
						)
						) {
							
							continue;
						}
						
						
						// ----------------------------------------
						// REGRA THIRD-PARTY
						//
						// É uma indicação forte de tracker/ad.
						// ----------------------------------------
						
						if (
						rr.apenasTerceiro
						) {
							
							bloquear =
							true;
							
							break;
						}
						
						
						// ----------------------------------------
						// REGRA COM SINAL DE PUBLICIDADE
						// ----------------------------------------
						
						if (
						shields
						.parecePublicidade(
						url
						)
						) {
							
							bloquear =
							true;
							
							break;
						}
						
						
						// ----------------------------------------
						// PRIVACIDADE / TRACKERS
						//
						// Só bloqueia regras de privacidade
						// quando o recurso é claramente
						// de terceiro.
						// ----------------------------------------
						
						if (
						categoria.nome.equals(
						"privacidade"
						)
						||
						categoria.nome.equals(
						"easyprivacy"
						)
						) {
							
							String hostPaginaAtual =
							shields.obterHost(
							pagina
							);
							
							
							if (
							hostPaginaAtual.length() > 0
							&&
							host.length() > 0
							&&
							!shields.pertenceDominio(
							host,
							hostPaginaAtual
							)
							) {
								
								bloquear =
								true;
								
								break;
							}
						}
					}
					
					
					if (bloquear) {
						break;
					}
				}
				
				
				// =================================================
				// SEGURANÇA FINAL
				// =================================================
				
				if (
				bloquear
				&&
				!shields
				.recursoSeguroParaBloquear(
				url
				)
				) {
					
					bloquear =
					false;
				}
				
				
				// =================================================
				// CONTADOR
				// =================================================
				
				if (bloquear) {
					
					shieldsBloqueadosContador
					.incrementAndGet();
					
					
					shieldsBytesEconomizados.addAndGet(SHIELDS_BYTES_POR_BLOQUEIO);
					long agoraUI = android.os.SystemClock.uptimeMillis();
					long ultimaUI = shieldsUltimaAtualizacaoUI.get();
					if (agoraUI - ultimaUI >= 500L && shieldsUltimaAtualizacaoUI.compareAndSet(ultimaUI, agoraUI)) {
						atualizarShieldsTextosUI.run();
					}
				}
				
				
				// =================================================
				// CACHE
				// =================================================
				
				synchronized (
				shieldsCache
				) {
					
					if (
					shieldsCache.size()
					>=
					SHIELDS_CACHE_MAX
					) {
						
						shieldsCache.clear();
					}
					
					
					shieldsCache.put(
					url,
					Boolean.valueOf(
					bloquear
					)
					);
				}
				
				
				return bloquear;
			}
			
			
			// ====================================================
			// CSS COSMÉTICO
			// ====================================================
			
			void injetarCss(
			final android.webkit.WebView view) {
				
				
				if (view == null || !shieldsAtivo.get()) {
					
					return;
				}
				
				
				StringBuilder css =
				new StringBuilder();
				
				
				css.append(
				SHIELDS_CSS_COLAPSO_GENERICO
				);
				
				
				// --------------------------------------------
				// CSS DAS LISTAS
				// --------------------------------------------
				
				for (
				ShieldsCategoria categoria :
				shieldsCategorias.values()
				) {
					
					if (
					!categoria.ativa
					||
					categoria.cosmeticos
					.length() == 0
					) {
						
						continue;
					}
					
					
					css.append(
					categoria.cosmeticos
					);
				}
				
				
				String cssFinal =
				css.toString();
				
				
				String cssSeguro =
				cssFinal
				.replace(
				"\\",
				"\\\\"
				)
				.replace(
				"'",
				"\\'"
				)
				.replace(
				"\n",
				" "
				)
				.replace(
				"\r",
				" "
				);
				
				
				String javascript =
				"(function(){"
				+ "var old=document.getElementById('shields-css');"
				+ "if(old)old.remove();"
				+ "var s=document.createElement('style');"
				+ "s.id='shields-css';"
				+ "s.type='text/css';"
				+ "s.innerHTML='"
				+ cssSeguro
				+ "';"
				+ "if(document.documentElement){"
				+ "document.documentElement.appendChild(s);"
				+ "}"
				+ "})();";
				
				
				view.evaluateJavascript(
				javascript,
				null
				);
			}
			
			
			// ====================================================
			// PAGE FINISHED
			// ====================================================
			
			@Override
			public void onPageFinished(
			android.webkit.WebView view,
			String url) {
				
				super.onPageFinished(
				view,
				url
				);
				
				
				if (shieldsAtivo.get()) {
					
					injetarCss(
					view
					);
					
				} else {
					
					view.evaluateJavascript(
					"(function(){"
					+ "var s=document.getElementById('shields-css');"
					+ "if(s)s.remove();"
					+ "})();",
					null
					);
				}
				
				
				atualizarShieldsTextosUI
				.run();
				BrowserLibrary.recordVisit(MainActivity.this, view.getTitle(), url);
			}
			
			
			// ====================================================
			// ANDROID 5+
			// ====================================================
			
			@Override
			public android.webkit.WebResourceResponse
			shouldInterceptRequest(
			android.webkit.WebView view,
			android.webkit.WebResourceRequest request) {
				
				
				try {
					
					if (
					request == null
					) {
						
						return null;
					}
					
					
					// --------------------------------------------
					// NUNCA BLOQUEAR A PÁGINA PRINCIPAL
					// --------------------------------------------
					
					if (
					request.isForMainFrame()
					) {
						
						return null;
					}
					
					
					// --------------------------------------------
					// SOMENTE GET
					// --------------------------------------------
					
					String metodo =
					request.getMethod();
					
					
					if (
					metodo != null
					&&
					!metodo.equalsIgnoreCase(
					"GET"
					)
					) {
						
						return null;
					}
					
					
					String url =
					request.getUrl()
					.toString();
					
					
					if (
					deveBloquear(
					url
					)
					) {
						
						// ----------------------------------------
						// NÃO retorna 204.
						// Retorna conteúdo vazio neutro.
						// ----------------------------------------
						
						return shields
						.respostaVazia();
					}
					
					
				} catch (Exception e) {
				}
				
				
				return null;
			}
			
			
			// ====================================================
			// ANDROID ANTIGO
			// ====================================================
			
			@Override
			public android.webkit.WebResourceResponse
			shouldInterceptRequest(
			android.webkit.WebView view,
			String url) {
				
				
				try {
					
					if (
					url == null
					) {
						
						return null;
					}
					
					
					if (
					deveBloquear(
					url
					)
					) {
						
						return shields
						.respostaVazia();
					}
					
					
				} catch (Exception e) {
				}
				
				
				return null;
			}
		}
		);
		
		
		// ============================================================
		// ATUALIZAÇÃO INICIAL
		// ============================================================
		
		atualizarShieldsTextosUI.run();
		
		
		// ============================================================
		// FIM SHIELDS V3.5
		// ============================================================
		// download
		webview.setDownloadListener(new DownloadListener() {
			@Override
			public void onDownloadStart(final String url,
			final String userAgent,
			final String contentDisposition,
			final String mimetype,
			long contentLength) {
				
				// Aceita somente HTTP/HTTPS
				if (url == null ||
				(!url.startsWith("http://") && !url.startsWith("https://"))) {
					
					Toast.makeText(
					MainActivity.this,
					"Download bloqueado: URL inválida.",
					Toast.LENGTH_SHORT
					).show();
					
					return;
				}
				
				// Gera nome do arquivo
				String fileName = URLUtil.guessFileName(
				url,
				contentDisposition,
				mimetype
				);
				
				if (fileName == null || fileName.trim().length() == 0) {
					fileName = "download";
				}
				
				// Remove caracteres inválidos
				fileName = fileName.replaceAll(
				"[\\\\/:*?\"<>|]",
				"_"
				);
				
				final String finalFileName = fileName;
				
				AlertDialog.Builder builder =
				new AlertDialog.Builder(MainActivity.this);
				
				builder.setTitle("Download");
				
				builder.setMessage(
				"Deseja baixar:\n\n" + finalFileName + "?"
				);
				
				builder.setPositiveButton(
				"Baixar",
				new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(
					DialogInterface dialog,
					int which) {
						
						try {
							
							Uri uri = Uri.parse(url);
							
							DownloadManager.Request request =
							new DownloadManager.Request(uri);
							
							request.setTitle(finalFileName);
							request.setDescription("Baixando...");
							
							if (mimetype != null &&
							mimetype.length() > 0) {
								
								request.setMimeType(mimetype);
							}
							
							request.setNotificationVisibility(
							DownloadManager.Request
							.VISIBILITY_VISIBLE_NOTIFY_COMPLETED
							);
							
							// Cookies do WebView
							String cookie =
							CookieManager
							.getInstance()
							.getCookie(url);
							
							if (cookie != null &&
							cookie.length() > 0) {
								
								request.addRequestHeader(
								"Cookie",
								cookie
								);
							}
							
							// User-Agent do WebView
							if (userAgent != null &&
							userAgent.length() > 0) {
								
								request.addRequestHeader(
								"User-Agent",
								userAgent
								);
							}
							
							request.setDestinationInExternalPublicDir(
							Environment.DIRECTORY_DOWNLOADS,
							finalFileName
							);
							
							DownloadManager manager =
							(DownloadManager)
							getSystemService(
							Context.DOWNLOAD_SERVICE
							);
							
							if (manager != null) {
								
								manager.enqueue(request);
								
								Toast.makeText(
								MainActivity.this,
								"Download iniciado",
								Toast.LENGTH_SHORT
								).show();
								
							} else {
								
								Toast.makeText(
								MainActivity.this,
								"Gerenciador de downloads indisponível.",
								Toast.LENGTH_SHORT
								).show();
							}
							
						} catch (Exception e) {
							
							Toast.makeText(
							MainActivity.this,
							"Não foi possível iniciar o download.",
							Toast.LENGTH_SHORT
							).show();
						}
					}
				}
				);
				
				builder.setNegativeButton(
				"Cancelar",
				null
				);
				
				final AlertDialog dialog = builder.create();
				
				dialog.show();
				
				/*
         * ==========================================
         * DATU BROWSER — DARK v1
         * ==========================================
         */
				
				// Surface: #242022
				GradientDrawable background =
				new GradientDrawable();
				
				background.setColor(0xFF242022);
				background.setCornerRadius(20);
				
				// Borda: #3A3235
				background.setStroke(
				1,
				0xFF3A3235
				);
				
				if (dialog.getWindow() != null) {
					
					dialog.getWindow()
					.setBackgroundDrawable(background);
					
					dialog.getWindow()
					.setDimAmount(0.7f);
				}
				
				// Texto principal: #F5EEF1
				TextView message =
				dialog.findViewById(
				android.R.id.message
				);
				
				if (message != null) {
					
					message.setTextColor(
					0xFFF5EEF1
					);
				}
				
				// Título: #F5EEF1
				int titleId =
				getResources().getIdentifier(
				"alertTitle",
				"id",
				"android"
				);
				
				if (titleId > 0) {
					
					TextView title =
					dialog.findViewById(titleId);
					
					if (title != null) {
						
						title.setTextColor(
						0xFFF5EEF1
						);
					}
				}
				
				// Botão "Baixar" — Primary Light: #FFB0C8
				dialog.getButton(
				DialogInterface.BUTTON_POSITIVE
				).setTextColor(
				0xFFFFB0C8
				);
				
				// Botão "Cancelar" — Text Secondary: #D0C4C8
				dialog.getButton(
				DialogInterface.BUTTON_NEGATIVE
				).setTextColor(
				0xFFD0C4C8
				);
			}
		});
		// gone
		binding.linearEstatisticas.setVisibility(View.GONE);
		binding.linearUblock.setVisibility(View.GONE);
		// dados
		// estatísticas: ligado/desligado
		if (dados.getString("estatisticas", "").equals("ligado")) {
			binding.linearEstatisticas.setVisibility(View.VISIBLE);
			binding.switchEstatisticas.setChecked(true);
		} else {
			
		}
        if (dados.getString("estatisticas", "").equals("desligado")) {
            binding.linearEstatisticas.setVisibility(View.GONE);
            binding.switchEstatisticas.setChecked(false);
        }
        boolean mostrarPapelParede = dados.getBoolean("home_wallpaper", true);
        binding.switchPapelParede.setChecked(mostrarPapelParede);
        if (!mostrarPapelParede) binding.tela1.setBackgroundColor(Color.rgb(14, 13, 18));
        if (mostrarPapelParede) carregarPapelParede();
        carregandoPreferencias = false;
    }
	
    private int dp(float value) { return (int)(value * getResources().getDisplayMetrics().density + 0.5f); }
    private GradientDrawable formaArredondada(int cor, int raioDp) {
        GradientDrawable shape = new GradientDrawable();
        shape.setColor(cor);
        shape.setCornerRadius(dp(raioDp));
        return shape;
    }
    private void abrirMenuNavegacao() {
        if (menuNavegacao != null && menuNavegacao.isShowing()) { menuNavegacao.dismiss(); return; }
        LinearLayout painel = new LinearLayout(this);
        painel.setOrientation(LinearLayout.VERTICAL);
        painel.setPadding(dp(14), dp(14), dp(14), dp(14));
        GradientDrawable fundo = formaArredondada(Color.rgb(29,27,34), 28);
        fundo.setStroke(dp(1), Color.rgb(73,69,79));
        painel.setBackground(fundo);
        TextView titulo = new TextView(this);
        titulo.setText("Ações do navegador"); titulo.setTextColor(Color.rgb(230,224,233));
        titulo.setTextSize(18); titulo.setTypeface(null, Typeface.BOLD);
        titulo.setPadding(dp(6), dp(2), dp(6), dp(12)); painel.addView(titulo);
        LinearLayout linha = null;
        final int[] icones = {R.drawable.icon_arrow_back_ios_round,R.drawable.icon_home_round,R.drawable.icon_refresh_round,R.drawable.icon_settings_round,R.drawable.ic_library_favorite,R.drawable.ic_library_history,R.drawable.ic_library_download};
        final String[] nomes = {"Voltar","Início","Atualizar","Configurações","Favoritos","Histórico","Downloads"};
        for (int i=0;i<icones.length;i++) {
            if (i%3==0) { linha=new LinearLayout(this); linha.setOrientation(LinearLayout.HORIZONTAL); LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,-2); lp.bottomMargin=dp(8); painel.addView(linha,lp); }
            final int acao=i;
            adicionarAcaoMenu(linha,icones[i],nomes[i],()->{
                if(menuNavegacao!=null) menuNavegacao.dismiss();
                switch(acao) {
                    case 0: if(browserEngine!=null && browserEngine.canGoBack()) browserEngine.goBack(); else Toast.makeText(this,"Não há página anterior",Toast.LENGTH_SHORT).show(); break;
                    case 1: binding.tela1.setVisibility(View.VISIBLE); animarEntradaExpressiva(binding.tela1); binding.tela2.setVisibility(View.GONE); binding.tela3.setVisibility(View.GONE); break;
                    case 2: if(browserEngine!=null) browserEngine.reload(); break;
                    case 3: binding.tela1.setVisibility(View.GONE); binding.tela2.setVisibility(View.GONE); binding.tela3.setVisibility(View.VISIBLE); animarEntradaExpressiva(binding.tela3); break;
                    case 4: abrirBiblioteca("favorites"); break;
                    case 5: abrirBiblioteca("history"); break;
                    case 6: abrirBiblioteca("downloads"); break;
                }
            });
        }
        int largura=getResources().getDisplayMetrics().widthPixels-dp(28);
        menuNavegacao=new PopupWindow(painel,largura,-2,true);
        menuNavegacao.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        menuNavegacao.setOutsideTouchable(true); menuNavegacao.setElevation(dp(12));
        menuNavegacao.setAnimationStyle(android.R.style.Animation_Dialog);
        menuNavegacao.setOnDismissListener(()->menuNavegacao=null);
        menuNavegacao.showAsDropDown(binding.mais,-largura+binding.mais.getWidth(),dp(8));
    }
    private void adicionarAcaoMenu(LinearLayout linha,int icone,String nome,Runnable acao) {
        LinearLayout item=new LinearLayout(this); item.setOrientation(LinearLayout.VERTICAL);
        item.setGravity(Gravity.CENTER); item.setPadding(dp(4),dp(10),dp(4),dp(8));
        item.setBackground(formaArredondada(Color.rgb(48,45,55),20));
        ImageView imagem=new ImageView(this); imagem.setImageResource(icone); imagem.setColorFilter(Color.rgb(208,188,255), android.graphics.PorterDuff.Mode.SRC_IN); imagem.setScaleType(ImageView.ScaleType.FIT_CENTER);
        LinearLayout.LayoutParams ip=new LinearLayout.LayoutParams(dp(26),dp(26)); ip.bottomMargin=dp(5); item.addView(imagem,ip);
        TextView texto=new TextView(this); texto.setText(nome); texto.setTextColor(Color.rgb(230,224,233));
        texto.setTextSize(11); texto.setGravity(Gravity.CENTER); texto.setMaxLines(1); item.addView(texto,new LinearLayout.LayoutParams(-1,-2));
        LinearLayout.LayoutParams params=new LinearLayout.LayoutParams(0,dp(78),1f); params.setMargins(dp(3),dp(2),dp(3),dp(2));
        linha.addView(item,params); item.setOnClickListener(v->acao.run());
    }
    private void aplicarPapelParede(Bitmap original) {
        if (original == null || isFinishing()) return;
        final View area = binding.tela1;
        area.post(() -> {
            int largura = area.getWidth();
            int altura = area.getHeight();
            if (largura <= 0 || altura <= 0) return;

            // Center-crop: mantém a proporção e corta as bordas, sem deformar a paisagem.
            float escala = Math.max((float) largura / original.getWidth(),
                                    (float) altura / original.getHeight());
            int larguraEscalada = Math.round(original.getWidth() * escala);
            int alturaEscalada = Math.round(original.getHeight() * escala);
            Bitmap escalada = Bitmap.createScaledBitmap(original, larguraEscalada, alturaEscalada, true);
            int x = Math.max(0, (larguraEscalada - largura) / 2);
            int y = Math.max(0, (alturaEscalada - altura) / 2);
            Bitmap recorte = Bitmap.createBitmap(escalada, x, y,
                Math.min(largura, escalada.getWidth() - x),
                Math.min(altura, escalada.getHeight() - y));
            area.setBackground(new BitmapDrawable(getResources(), recorte));
            if (escalada != original && !escalada.isRecycled()) escalada.recycle();
        });
    }

    private void carregarPapelParede() {
        if (!dados.getBoolean("home_wallpaper", true) || isFinishing()) return;

        final java.io.File arquivo = new java.io.File(getFilesDir(), "datu_home_wallpaper.jpg");
        if (arquivo.exists() && arquivo.length() > 0) {
            Bitmap salvo = BitmapFactory.decodeFile(arquivo.getAbsolutePath());
            if (salvo != null) aplicarPapelParede(salvo);
        }

        // Seleciona uma paisagem diferente da última imagem salva a cada abertura.
        final String[] imagens = {
            "https://images.unsplash.com/photo-1470770841072-f978cf4d019e?auto=format&fit=crop&w=1800&q=88",
            "https://images.unsplash.com/photo-1500530855697-b586d89ba3ee?auto=format&fit=crop&w=1800&q=88",
            "https://images.unsplash.com/photo-1470252649378-9c29740c9fa8?auto=format&fit=crop&w=1800&q=88",
            "https://images.unsplash.com/photo-1501785888041-af3ef285b470?auto=format&fit=crop&w=1800&q=88",
            "https://images.unsplash.com/photo-1441974231531-c6227db76b6e?auto=format&fit=crop&w=1800&q=88"
        };
        int anterior = dados.getInt("home_wallpaper_index", -1);
        java.util.Random sorteio = new java.util.Random();
        int indice = sorteio.nextInt(imagens.length);
        if (imagens.length > 1 && indice == anterior) indice = (indice + 1 + sorteio.nextInt(imagens.length - 1)) % imagens.length;
        final int indiceEscolhido = indice;
        final String endereco = imagens[indiceEscolhido] + "&datu_refresh=" + System.currentTimeMillis();

        new AsyncTask<Void, Void, Bitmap>() {
            @Override protected Bitmap doInBackground(Void... params) {
                java.net.HttpURLConnection conexao = null;
                java.io.File temporario = new java.io.File(getFilesDir(), "datu_home_wallpaper.tmp");
                try {
                    conexao = (java.net.HttpURLConnection) new java.net.URL(endereco).openConnection();
                    conexao.setConnectTimeout(12000);
                    conexao.setReadTimeout(25000);
                    conexao.setUseCaches(false);
                    conexao.setRequestProperty("Cache-Control", "no-cache");
                    conexao.setRequestProperty("User-Agent", "DatuBrowser/1.0 Android");
                    if (conexao.getResponseCode() != 200) return null;

                    try (java.io.InputStream entrada = new java.io.BufferedInputStream(conexao.getInputStream());
                         java.io.FileOutputStream saida = new java.io.FileOutputStream(temporario)) {
                        byte[] buffer = new byte[8192];
                        int lidos;
                        while ((lidos = entrada.read(buffer)) != -1) saida.write(buffer, 0, lidos);
                        saida.flush();
                    }

                    Bitmap bitmap = BitmapFactory.decodeFile(temporario.getAbsolutePath());
                    if (bitmap == null) { temporario.delete(); return null; }

                    // Substituição atômica: nunca perde o cache antigo se o download falhar.
                    java.io.File backup = new java.io.File(getFilesDir(), "datu_home_wallpaper.old");
                    if (backup.exists()) backup.delete();
                    if (arquivo.exists() && !arquivo.renameTo(backup)) {
                        temporario.delete();
                        return null;
                    }
                    if (!temporario.renameTo(arquivo)) {
                        if (backup.exists()) backup.renameTo(arquivo);
                        temporario.delete();
                        return null;
                    }
                    if (backup.exists()) backup.delete();
                    dados.edit().putInt("home_wallpaper_index", indiceEscolhido)
                        .putLong("home_wallpaper_last_update", System.currentTimeMillis()).apply();
                    return bitmap;
                } catch (Exception erro) {
                    temporario.delete();
                    return null;
                } finally {
                    if (conexao != null) conexao.disconnect();
                }
            }

            @Override protected void onPostExecute(Bitmap bitmap) {
                if (bitmap != null && !isFinishing()
                    && dados.getBoolean("home_wallpaper", true)) aplicarPapelParede(bitmap);
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

	private void fecharBiblioteca() {
		findViewById(R.id.tela4).setVisibility(View.GONE);
		findViewById(R.id.linear1).setVisibility(View.VISIBLE);
	}

	private void abrirBiblioteca(String screen) {
        Intent intent = new Intent(MainActivity.this, LibraryActivity.class);
        intent.putExtra("screen", screen);
        startActivityForResult(intent, LIBRARY_REQUEST);
    }

	private void adicionarItemBiblioteca(LinearLayout parent, String name, String detail, Runnable action) {
		LinearLayout card = new LinearLayout(this);
		card.setOrientation(LinearLayout.VERTICAL);
		card.setPadding(16, 14, 16, 14);
		GradientDrawable background = new GradientDrawable();
		background.setColor(Color.rgb(36, 32, 34));
		background.setCornerRadius(20 * getResources().getDisplayMetrics().density);
		card.setBackground(background);
		TextView title = new TextView(this);
		title.setText(name);
		title.setTextSize(16);
		title.setTextColor(Color.rgb(245, 238, 241));
		title.setTypeface(null, Typeface.BOLD);
		card.addView(title);
		if (detail != null && !detail.isEmpty()) {
			TextView subtitle = new TextView(this);
			subtitle.setText(detail);
			subtitle.setTextSize(12);
			subtitle.setTextColor(Color.rgb(208, 196, 200));
			subtitle.setMaxLines(2);
			card.addView(subtitle);
		}
		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(-1, -2);
		params.bottomMargin = (int)(10 * getResources().getDisplayMetrics().density);
		parent.addView(card, params);
		if (action != null) card.setOnClickListener(v -> action.run());
	}

	private void abrirDownload(DownloadManager manager, long id) {
		try {
			Uri uri = manager.getUriForDownloadedFile(id);
			if (uri == null) { Toast.makeText(this, "Esse download ainda não foi concluído.", Toast.LENGTH_SHORT).show(); return; }
			String type = getContentResolver().getType(uri);
			Intent intent = new Intent(Intent.ACTION_VIEW);
			intent.setDataAndType(uri, type == null ? "*/*" : type);
			intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
			startActivity(intent);
		} catch (Exception e) {
			Toast.makeText(this, "Não há aplicativo compatível para abrir esse arquivo.", Toast.LENGTH_SHORT).show();
		}
	}

	private String formatarEndereco(String url) {
		if (url == null || url.trim().isEmpty()) return "Pesquisar ou digitar endereço";
		try {
			String host = android.net.Uri.parse(url).getHost();
			if (host != null && !host.isEmpty()) return host.replaceFirst("^www\\.", "");
		} catch (Exception ignored) {}
		return url;
	}

	private void animarEntradaExpressiva(android.view.View view) {
		if (view == null) return;
		float deslocamento = 14f * getResources().getDisplayMetrics().density;
		view.animate().cancel();
		view.setAlpha(0f);
		view.setTranslationY(deslocamento);
		androidx.dynamicanimation.animation.SpringForce mola =
			new androidx.dynamicanimation.animation.SpringForce(0f)
			.setDampingRatio(0.78f).setStiffness(520f);
		new androidx.dynamicanimation.animation.SpringAnimation(view,
			androidx.dynamicanimation.animation.DynamicAnimation.TRANSLATION_Y, 0f)
			.setSpring(mola).start();
		androidx.dynamicanimation.animation.SpringForce molaAlpha =
			new androidx.dynamicanimation.animation.SpringForce(1f)
			.setDampingRatio(0.9f).setStiffness(650f);
		new androidx.dynamicanimation.animation.SpringAnimation(view,
			androidx.dynamicanimation.animation.DynamicAnimation.ALPHA, 1f)
			.setSpring(molaAlpha).start();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == LIBRARY_REQUEST && resultCode == Activity.RESULT_OK && data != null) {
            String url = data.getStringExtra("url");
            if (url != null && !url.trim().isEmpty()) {
                browserEngine.loadUrl(url);
                binding.tela2.setVisibility(View.VISIBLE);
                binding.tela1.setVisibility(View.GONE);
                binding.tela3.setVisibility(View.GONE);
            }
        }
		if (requestCode == FILE_CHOOSER_REQUEST && uploadCallback != null) {
			android.net.Uri[] result = null;
			if (resultCode == Activity.RESULT_OK && data != null) {
				if (data.getClipData() != null) {
					int count = data.getClipData().getItemCount();
					result = new android.net.Uri[count];
					for (int i = 0; i < count; i++) result[i] = data.getClipData().getItemAt(i).getUri();
				} else if (data.getData() != null) result = new android.net.Uri[]{data.getData()};
			}
			uploadCallback.onReceiveValue(result);
			uploadCallback = null;
		}
	}

	private void atualizarContadorGuias() {
		View contador = findViewById(R.id.tab_counter);
		if (contador instanceof TextView) {
			((TextView) contador).setText(String.valueOf(tabManager == null ? 1 : tabManager.getCount()));
		}
	}

	private int dpGuias(int valor) {
		return (int) (valor * getResources().getDisplayMetrics().density + 0.5f);
	}

	private android.graphics.drawable.GradientDrawable fundoCardGuia(boolean ativo) {
		android.graphics.drawable.GradientDrawable fundo = new android.graphics.drawable.GradientDrawable();
		fundo.setColor(ativo ? 0xFF302A3D : 0xFF1B1920);
		fundo.setCornerRadius(dpGuias(22));
		fundo.setStroke(dpGuias(1), ativo ? 0xFFD0BCFF : 0xFF494451);
		return fundo;
	}

	private void abrirSeletorGuias() {
		if (tabManager == null) return;
		tabManager.captureActive(binding.webview);

		final android.app.AlertDialog dialog = new android.app.AlertDialog.Builder(this)
			.create();
		android.widget.LinearLayout raiz = new android.widget.LinearLayout(this);
		raiz.setOrientation(android.widget.LinearLayout.VERTICAL);
		raiz.setPadding(dpGuias(20), dpGuias(18), dpGuias(20), dpGuias(16));
		raiz.setBackgroundColor(0xFF141218);

		android.widget.LinearLayout topo = new android.widget.LinearLayout(this);
		topo.setGravity(android.view.Gravity.CENTER_VERTICAL);
		android.widget.TextView titulo = new android.widget.TextView(this);
		titulo.setText("Guias");
		titulo.setTextColor(0xFFE6E0E9);
		titulo.setTextSize(25);
		titulo.setTypeface(null, android.graphics.Typeface.BOLD);
		topo.addView(titulo, new android.widget.LinearLayout.LayoutParams(0, -2, 1));
		android.widget.TextView quantidade = new android.widget.TextView(this);
		quantidade.setText(String.valueOf(tabManager.getCount()));
		quantidade.setTextColor(0xFFD0BCFF);
		quantidade.setTextSize(14);
		quantidade.setPadding(dpGuias(12), dpGuias(7), dpGuias(12), dpGuias(7));
		android.graphics.drawable.GradientDrawable pCount = new android.graphics.drawable.GradientDrawable();
		pCount.setColor(0xFF302A3D);
		pCount.setCornerRadius(dpGuias(18));
		quantidade.setBackground(pCount);
		topo.addView(quantidade);
		raiz.addView(topo);

		android.widget.TextView descricao = new android.widget.TextView(this);
		descricao.setText("Selecione uma página ou abra uma nova guia.");
		descricao.setTextColor(0xFFCAC4D0);
		descricao.setTextSize(13);
		descricao.setPadding(0, dpGuias(5), 0, dpGuias(16));
		raiz.addView(descricao);

		android.widget.ScrollView rolagem = new android.widget.ScrollView(this);
		rolagem.setFillViewport(false);
		android.widget.LinearLayout grade = new android.widget.LinearLayout(this);
		grade.setOrientation(android.widget.LinearLayout.VERTICAL);
		java.util.List<BrowserTab> guias = new java.util.ArrayList<>(tabManager.getTabs());
		for (int i = 0; i < guias.size(); i += 2) {
			android.widget.LinearLayout linha = new android.widget.LinearLayout(this);
			linha.setOrientation(android.widget.LinearLayout.HORIZONTAL);
			linha.setGravity(android.view.Gravity.TOP);
			for (int coluna = 0; coluna < 2 && i + coluna < guias.size(); coluna++) {
				final int indice = i + coluna;
				final BrowserTab guia = guias.get(indice);
				final boolean ativa = indice == tabManager.getActiveIndex();
				android.widget.LinearLayout cartao = new android.widget.LinearLayout(this);
				cartao.setOrientation(android.widget.LinearLayout.VERTICAL);
				cartao.setPadding(dpGuias(12), dpGuias(10), dpGuias(12), dpGuias(10));
				cartao.setBackground(fundoCardGuia(ativa));

				android.widget.LinearLayout cabecalho = new android.widget.LinearLayout(this);
				cabecalho.setGravity(android.view.Gravity.CENTER_VERTICAL);
				android.widget.TextView marcador = new android.widget.TextView(this);
				marcador.setText(ativa ? "●" : "○");
				marcador.setTextColor(ativa ? 0xFFD0BCFF : 0xFF938D99);
				marcador.setTextSize(13);
				cabecalho.addView(marcador);
				android.widget.TextView fechar = new android.widget.TextView(this);
				fechar.setText("×");
				fechar.setTextColor(0xFFE6E0E9);
				fechar.setTextSize(23);
				fechar.setGravity(android.view.Gravity.CENTER);
				cabecalho.addView(new android.view.View(this), new android.widget.LinearLayout.LayoutParams(0, 1, 1));
				cabecalho.addView(fechar, new android.widget.LinearLayout.LayoutParams(dpGuias(30), dpGuias(30)));
				cartao.addView(cabecalho);

				android.widget.TextView nome = new android.widget.TextView(this);
				String textoTitulo = guia.getTitle();
				nome.setText(textoTitulo == null || textoTitulo.trim().isEmpty() ? "Nova guia" : textoTitulo);
				nome.setTextColor(0xFFE6E0E9);
				nome.setTextSize(14);
				nome.setTypeface(null, android.graphics.Typeface.BOLD);
				nome.setMaxLines(2);
				nome.setMinHeight(dpGuias(42));
				cartao.addView(nome);

				android.widget.TextView endereco = new android.widget.TextView(this);
				endereco.setText(formatarEndereco(guia.getUrl()));
				endereco.setTextColor(0xFFBDB6C5);
				endereco.setTextSize(11);
				endereco.setMaxLines(2);
				endereco.setEllipsize(android.text.TextUtils.TruncateAt.END);
				cartao.addView(endereco);

				android.widget.LinearLayout.LayoutParams cartaoParams =
					new android.widget.LinearLayout.LayoutParams(0, dpGuias(142), 1);
				cartaoParams.setMargins(dpGuias(4), dpGuias(4), dpGuias(4), dpGuias(8));
				linha.addView(cartao, cartaoParams);
				cartao.setOnClickListener(v -> {
					tabManager.select(indice, binding.webview);
					atualizarContadorGuias();
					binding.tela1.setVisibility(View.GONE);
					binding.tela2.setVisibility(View.VISIBLE);
					binding.tela3.setVisibility(View.GONE);
					dialog.dismiss();
				});
				fechar.setOnClickListener(v -> {
					tabManager.close(indice, binding.webview);
					atualizarContadorGuias();
					dialog.dismiss();
					abrirSeletorGuias();
				});
			}
			if (guias.size() % 2 != 0 && i + 1 >= guias.size()) {
				android.view.View espaco = new android.view.View(this);
				linha.addView(espaco, new android.widget.LinearLayout.LayoutParams(0, 1, 1));
			}
			grade.addView(linha);
		}
		rolagem.addView(grade);
		android.widget.LinearLayout.LayoutParams scrollParams =
			new android.widget.LinearLayout.LayoutParams(-1, dpGuias(360));
		scrollParams.bottomMargin = dpGuias(12);
		raiz.addView(rolagem, scrollParams);

		android.widget.LinearLayout acoes = new android.widget.LinearLayout(this);
		acoes.setGravity(android.view.Gravity.CENTER_VERTICAL);
		android.widget.Button nova = new android.widget.Button(this);
		nova.setText("+ Nova guia");
		acoes.addView(nova, new android.widget.LinearLayout.LayoutParams(0, dpGuias(48), 1));
		android.widget.Button anonima = new android.widget.Button(this);
		anonima.setText("Anônima");
		acoes.addView(anonima, new android.widget.LinearLayout.LayoutParams(-2, dpGuias(48)));
		raiz.addView(acoes);
		nova.setOnClickListener(v -> {
			tabManager.create(binding.webview, "https://search.brave.com/");
			binding.webview.loadUrl("https://search.brave.com/");
			atualizarContadorGuias();
			binding.tela1.setVisibility(View.GONE);
			binding.tela2.setVisibility(View.VISIBLE);
			binding.tela3.setVisibility(View.GONE);
			dialog.dismiss();
		});
		anonima.setOnClickListener(v -> {
			dialog.dismiss();
			Intent privateIntent = new Intent(MainActivity.this, PrivateBrowserActivity.class);
			startActivity(privateIntent);
		});
		dialog.setView(raiz);
		dialog.show();
	}

	@Override
	public void onBackPressed() {
		if (findViewById(R.id.tela4).getVisibility() == View.VISIBLE) {
			fecharBiblioteca();
			return;
		}
		if (browserEngine != null && browserEngine.canGoBack()) {
			browserEngine.goBack();
		} else {
			binding.tela1.setVisibility(View.VISIBLE);
				animarEntradaExpressiva(binding.tela1);
			binding.tela2.setVisibility(View.GONE);
			binding.tela3.setVisibility(View.GONE);
			click++;
			if (click == 1) {
				timer = new TimerTask() {
					@Override
					public void run() {
						runOnUiThread(new Runnable() {
							@Override
							public void run() {
								click = 0;
							}
						});
					}
				};
				_timer.schedule(timer, (int)(2000));
			} else {
				finish();
			}
		}
	}
}