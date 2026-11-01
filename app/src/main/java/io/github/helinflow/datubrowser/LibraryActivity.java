package io.github.helinflow.datubrowser;

import android.app.Activity;
import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import java.util.ArrayList;
import android.widget.LinearLayout;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import org.json.JSONArray;
import org.json.JSONObject;

public class LibraryActivity extends Activity {
    private LinearLayout content;
    private String screen;
    private final ArrayList<View> searchableCards = new ArrayList<>();
    private final ArrayList<String> searchableText = new ArrayList<>();

    @Override protected void onCreate(Bundle state) {
        super.onCreate(state);
        screen = getIntent().getStringExtra("screen");
        if (screen == null) screen = "favorites";
        buildPage();
        if ("favorites".equals(screen) || "history".equals(screen)) loadSaved();
        else loadDownloads();
    }

    private int dp(float value) { return (int)(value * getResources().getDisplayMetrics().density + 0.5f); }

    private GradientDrawable shape(int color, int radius) {
        GradientDrawable d = new GradientDrawable();
        d.setColor(color);
        d.setCornerRadius(dp(radius));
        return d;
    }

    private TextView text(String value, int size, int color, boolean bold) {
        TextView t = new TextView(this);
        t.setText(value);
        t.setTextSize(size);
        t.setTextColor(color);
        if (bold) t.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        t.setGravity(Gravity.CENTER_VERTICAL);
        return t;
    }

    private void buildPage() {
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(Color.rgb(32, 33, 36));
        root.setPadding(dp(16), dp(12), dp(16), dp(12));

        LinearLayout header = new LinearLayout(this);
        header.setGravity(Gravity.CENTER_VERTICAL);
        header.setOrientation(LinearLayout.HORIZONTAL);

        ImageView back = new ImageView(this);
        back.setImageResource(R.drawable.ic_library_back);
        back.setColorFilter(Color.rgb(232, 234, 237));
        back.setContentDescription("Voltar");
        back.setPadding(dp(12), dp(12), dp(12), dp(12));
        back.setBackground(shape(Color.rgb(48, 49, 52), 24));
        header.addView(back, new LinearLayout.LayoutParams(dp(48), dp(48)));
        back.setOnClickListener(v -> finish());

        String title = "favorites".equals(screen) ? "Favoritos" :
                "history".equals(screen) ? "Histórico" : "Downloads";
        TextView heading = text(title, 23, Color.rgb(232, 234, 237), true);
        LinearLayout.LayoutParams hp = new LinearLayout.LayoutParams(0, dp(52), 1);
        hp.leftMargin = dp(14);
        header.addView(heading, hp);
        root.addView(header);

        EditText search = new EditText(this);
        search.setSingleLine(true);
        search.setTextSize(15);
        search.setTextColor(Color.rgb(232, 234, 237));
        search.setHintTextColor(Color.rgb(154, 160, 166));
        search.setHint("Pesquisar em " + title.toLowerCase());
        search.setPadding(dp(18), 0, dp(16), 0);
        search.setBackground(shape(Color.rgb(48, 49, 52), 26));
        LinearLayout.LayoutParams searchParams = new LinearLayout.LayoutParams(-1, dp(50));
        searchParams.topMargin = dp(16);
        searchParams.bottomMargin = dp(16);
        root.addView(search, searchParams);

        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);
        scroll.setClipToPadding(false);
        content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setPadding(0, 0, 0, dp(24));
        scroll.addView(content, new ScrollView.LayoutParams(-1, -2));
        root.addView(scroll, new LinearLayout.LayoutParams(-1, 0, 1));

        search.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                String query = s == null ? "" : s.toString().trim().toLowerCase(java.util.Locale.ROOT);
                for (int i = 0; i < searchableCards.size(); i++) {
                    searchableCards.get(i).setVisibility(searchableText.get(i).contains(query) ? View.VISIBLE : View.GONE);
                }
            }
            @Override public void afterTextChanged(Editable s) { }
        });

        setContentView(root);
    }

    private void loadSaved() {
        String key = "history".equals(screen) ? "history" : "favorites";
        JSONArray items;
        try {
            items = new JSONArray(getSharedPreferences("datu_browser_library", 0).getString(key, "[]"));
        } catch (Exception e) { items = new JSONArray(); }

        if (items.length() == 0) {
            addEmpty("history".equals(screen) ? "Seu histórico está vazio" : "Nenhum favorito salvo ainda");
            return;
        }

        for (int i = 0; i < items.length(); i++) {
            JSONObject item = items.optJSONObject(i);
            if (item == null) continue;
            final String url = item.optString("url", "");
            String title = item.optString("title", url);
            addCard(title, url, () -> {
                Intent result = new Intent();
                result.putExtra("url", url);
                setResult(Activity.RESULT_OK, result);
                finish();
            });
        }

        if ("history".equals(screen)) {
            TextView clear = text("Limpar histórico", 15, Color.rgb(255,176,200), true);
            clear.setGravity(Gravity.CENTER);
            clear.setPadding(dp(16), dp(14), dp(16), dp(14));
            clear.setBackground(shape(Color.rgb(73,43,54), 18));
            LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(-1, dp(52));
            p.topMargin = dp(12);
            content.addView(clear, p);
            clear.setOnClickListener(v -> {
                getSharedPreferences("datu_browser_library", 0).edit().putString("history", "[]").apply();
                content.removeAllViews();
                addEmpty("Seu histórico está vazio");
                Toast.makeText(this, "Histórico limpo.", Toast.LENGTH_SHORT).show();
            });
        }
    }

    private void loadDownloads() {
        DownloadManager manager = (DownloadManager)getSystemService(Context.DOWNLOAD_SERVICE);
        if (manager == null) { addEmpty("Gerenciador de downloads indisponível"); return; }
        Cursor cursor = null;
        int count = 0;
        try {
            cursor = manager.query(new DownloadManager.Query());
            if (cursor == null) { addEmpty("Nenhum download encontrado"); return; }
            while (cursor.moveToNext()) {
                final long id = cursor.getLong(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_ID));
                String title = cursor.getString(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_TITLE));
                int status = cursor.getInt(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_STATUS));
                addCard(title + " · " + downloadStatus(status), "Toque para abrir o arquivo", () -> openDownload(manager, id));
                count++;
            }
        } catch (Exception ignored) {
        } finally {
            if (cursor != null) cursor.close();
        }
        if (count == 0) addEmpty("Nenhum download encontrado");
    }

    private String downloadStatus(int status) {
        if (status == DownloadManager.STATUS_SUCCESSFUL) return "Concluído";
        if (status == DownloadManager.STATUS_RUNNING) return "Em andamento";
        if (status == DownloadManager.STATUS_PAUSED) return "Pausado";
        if (status == DownloadManager.STATUS_FAILED) return "Falhou";
        return "Na fila";
    }

    private void openDownload(DownloadManager manager, long id) {
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

    private void addCard(String title, String detail, Runnable action) {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.HORIZONTAL);
        card.setGravity(Gravity.CENTER_VERTICAL);
        card.setPadding(dp(14), dp(10), dp(12), dp(10));
        card.setBackground(shape(Color.rgb(41, 42, 45), 16));

        TextView symbol = text("favorites".equals(screen) ? "★" :
                "history".equals(screen) ? "◷" : "↓", 21,
                Color.rgb(138, 180, 248), true);
        symbol.setGravity(Gravity.CENTER);
        card.addView(symbol, new LinearLayout.LayoutParams(dp(36), dp(42)));

        LinearLayout labels = new LinearLayout(this);
        labels.setOrientation(LinearLayout.VERTICAL);
        labels.setPadding(dp(12), dp(2), dp(8), dp(2));
        TextView name = text(title, 15, Color.rgb(232, 234, 237), false);
        name.setMaxLines(2);
        TextView url = text(detail, 12, Color.rgb(154, 160, 166), false);
        url.setMaxLines(1);
        url.setSingleLine(true);
        url.setEllipsize(android.text.TextUtils.TruncateAt.END);
        labels.addView(name, new LinearLayout.LayoutParams(-1, -2));
        LinearLayout.LayoutParams detailParams = new LinearLayout.LayoutParams(-1, -2);
        detailParams.topMargin = dp(4);
        labels.addView(url, detailParams);
        card.addView(labels, new LinearLayout.LayoutParams(0, -2, 1));

        TextView arrow = text("›", 25, Color.rgb(154, 160, 166), false);
        arrow.setGravity(Gravity.CENTER);
        card.addView(arrow, new LinearLayout.LayoutParams(dp(24), dp(42)));

        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(-1, -2);
        p.bottomMargin = dp(8);
        content.addView(card, p);
        searchableCards.add(card);
        searchableText.add((title + " " + detail).toLowerCase(java.util.Locale.ROOT));
        card.setOnClickListener(v -> action.run());
    }

    private void addEmpty(String message) {
        searchableCards.clear();
        searchableText.clear();
        TextView empty = text(message, 16, Color.rgb(208,196,200), false);
        empty.setGravity(Gravity.CENTER);
        empty.setPadding(dp(20), dp(28), dp(20), dp(28));
        content.addView(empty, new LinearLayout.LayoutParams(-1, -2));
    }
}
