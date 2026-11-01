// Copyright 2026 The Datu Browser Authors.
// This screen ports the AndroidX PreferenceScreen structure used by Chromium's
// chrome/android MainSettings. Chromium-specific preference classes are not
// imported because they depend on Chromium's browser services and TabModel.
// Chromium source: https://source.chromium.org/chromium/chromium/src/+/main:chrome/android/
// Chromium source license: BSD-style; see the Chromium LICENSE file.

package io.github.helinflow.datubrowser;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.preference.Preference;
import androidx.preference.PreferenceCategory;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceScreen;
import androidx.preference.SwitchPreferenceCompat;

public class DatuSettingsActivity extends AppCompatActivity {
    private static final int CONTENT_ID = 0x00D47001;

    @Override
    protected void onCreate(Bundle state) {
        super.onCreate(state);

        android.widget.LinearLayout root = new android.widget.LinearLayout(this);
        root.setOrientation(android.widget.LinearLayout.VERTICAL);
        root.setBackgroundColor(0xFF0E0D12);

        Toolbar toolbar = new Toolbar(this);
        toolbar.setTitle("Configurações");
        toolbar.setElevation(0f);
        toolbar.setTitleTextColor(0xFFE6E0E9);
        toolbar.setNavigationIcon(R.drawable.ic_library_back);
        if (toolbar.getNavigationIcon() != null) {
            toolbar.getNavigationIcon().mutate().setTint(0xFFE6E0E9);
        }
        toolbar.setNavigationContentDescription("Voltar");
        toolbar.setBackgroundColor(0xFF141218);
        toolbar.setNavigationOnClickListener(v -> finish());
        root.addView(toolbar, new android.widget.LinearLayout.LayoutParams(
                -1, (int) (56 * getResources().getDisplayMetrics().density + 0.5f)));

        android.widget.FrameLayout content = new android.widget.FrameLayout(this);
        content.setId(CONTENT_ID);
        root.addView(content, new android.widget.LinearLayout.LayoutParams(-1, 0, 1));
        setContentView(root);

        if (state == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(CONTENT_ID, new MainSettingsFragment())
                    .commit();
        }
    }

    public static class MainSettingsFragment extends PreferenceFragmentCompat {
        @Override
        public void onCreatePreferences(Bundle state, String rootKey) {
            android.content.Context context = requireContext();
            PreferenceScreen screen = getPreferenceManager().createPreferenceScreen(context);
            setPreferenceScreen(screen);

            android.content.SharedPreferences data =
                    context.getSharedPreferences("dados", android.content.Context.MODE_PRIVATE);

            PreferenceCategory basics = new PreferenceCategory(context);
            basics.setTitle("Básico");
            screen.addPreference(basics);

            Preference search = new Preference(context);
            search.setTitle("Mecanismo de pesquisa");
            String selectedEngine = data.getString("search_engine", "brave");
            search.setSummary(searchEngineLabel(selectedEngine));
            search.setOnPreferenceClickListener(preference -> {
                String[] labels = {"Brave Search", "DuckDuckGo", "Google"};
                String[] values = {"brave", "duckduckgo", "google"};
                int selected = 0;
                for (int i = 0; i < values.length; i++) {
                    if (values[i].equals(data.getString("search_engine", "brave"))) selected = i;
                }
                new AlertDialog.Builder(context)
                        .setTitle("Mecanismo de pesquisa")
                        .setSingleChoiceItems(labels, selected, (dialog, which) -> {
                            data.edit().putString("search_engine", values[which]).apply();
                            search.setSummary(labels[which]);
                            dialog.dismiss();
                        })
                        .setNegativeButton("Cancelar", null)
                        .show();
                return true;
            });
            basics.addPreference(search);

            PreferenceCategory privacy = new PreferenceCategory(context);
            privacy.setTitle("Privacidade e segurança");
            screen.addPreference(privacy);

            SwitchPreferenceCompat javascript = new SwitchPreferenceCompat(context);
            javascript.setTitle("JavaScript");
            javascript.setSummary("Permitir que sites executem JavaScript");
            javascript.setPersistent(false);
            javascript.setChecked(!"desativado".equals(data.getString("javascript", "ativado")));
            javascript.setOnPreferenceChangeListener((preference, value) -> {
                data.edit().putString("javascript",
                        Boolean.TRUE.equals(value) ? "ativado" : "desativado").apply();
                return true;
            });
            privacy.addPreference(javascript);

            Preference clearData = new Preference(context);
            clearData.setTitle("Limpar dados de navegação");
            clearData.setSummary("Apagar histórico, cookies e dados armazenados por sites");
            clearData.setOnPreferenceClickListener(preference -> {
                new AlertDialog.Builder(context)
                        .setTitle("Limpar dados de navegação?")
                        .setMessage("Isso apagará o histórico salvo pelo Datu, cookies e dados locais de sites. Você poderá sair das suas contas.")
                        .setNegativeButton("Cancelar", null)
                        .setPositiveButton("Limpar", (dialog, which) -> {
                            context.getSharedPreferences("datu_browser_library", android.content.Context.MODE_PRIVATE)
                                    .edit().putString("history", "[]").apply();
                            android.webkit.CookieManager cookies = android.webkit.CookieManager.getInstance();
                            cookies.removeAllCookies(value -> cookies.flush());
                            android.webkit.WebStorage.getInstance().deleteAllData();
                            android.widget.Toast.makeText(context, "Dados de navegação apagados.", android.widget.Toast.LENGTH_SHORT).show();
                        })
                        .show();
                return true;
            });
            privacy.addPreference(clearData);

            PreferenceCategory advanced = new PreferenceCategory(context);
            advanced.setTitle("Avançado");
            screen.addPreference(advanced);

            SwitchPreferenceCompat wallpaper = new SwitchPreferenceCompat(context);
            wallpaper.setTitle("Imagens na página inicial");
            wallpaper.setSummary("Mostrar paisagens como plano de fundo");
            wallpaper.setPersistent(false);
            wallpaper.setChecked(data.getBoolean("home_wallpaper", true));
            wallpaper.setOnPreferenceChangeListener((preference, value) -> {
                data.edit().putBoolean("home_wallpaper", Boolean.TRUE.equals(value)).apply();
                return true;
            });
            advanced.addPreference(wallpaper);

            addLibraryPreference(advanced, "Favoritos", "favorites");
            addLibraryPreference(advanced, "Histórico", "history");
            addLibraryPreference(advanced, "Downloads", "downloads");

            PreferenceCategory about = new PreferenceCategory(context);
            about.setTitle("Sobre o Datu");
            screen.addPreference(about);

            Preference notices = new Preference(context);
            notices.setTitle("Licenças e avisos de terceiros");
            notices.setSummary("Bibliotecas e componentes de código aberto");
            notices.setOnPreferenceClickListener(preference -> {
                startActivity(new Intent(requireContext(), ThirdPartyNoticesActivity.class));
                return true;
            });
            about.addPreference(notices);
        }

        private String searchEngineLabel(String value) {
            if ("duckduckgo".equals(value)) return "DuckDuckGo";
            if ("google".equals(value)) return "Google";
            return "Brave Search";
        }

        private void addLibraryPreference(PreferenceCategory category, String title, String screen) {
            Preference item = new Preference(requireContext());
            item.setTitle(title);
            item.setOnPreferenceClickListener(preference -> {
                Intent intent = new Intent(requireContext(), LibraryActivity.class);
                intent.putExtra("screen", screen);
                startActivity(intent);
                return true;
            });
            category.addPreference(item);
        }
    }
}
