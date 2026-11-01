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
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

public class LibraryActivity extends Activity {
    static final class LibraryItem {
        final long id;
        final int storageIndex;
        final String title;
        final String detail;
        final String url;

        LibraryItem(long id, int storageIndex, String title, String detail, String url) {
            this.id = id;
            this.storageIndex = storageIndex;
            this.title = title == null || title.trim().isEmpty() ? "Sem título" : title;
            this.detail = detail == null ? "" : detail;
            this.url = url == null ? "" : url;
        }
    }

    private String screen;
    private LibraryItemAdapter adapter;
    private TextView emptyState;
    private TextView clearHistory;
    private DownloadManager downloadManager;
    private final ArrayList<LibraryItem> items = new ArrayList<>();

    @Override protected void onCreate(Bundle state) {
        super.onCreate(state);
        screen = getIntent().getStringExtra("screen");
        if (screen == null) screen = "favorites";
        downloadManager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
        buildPage();
        loadItems();
    }

    private int dp(float value) {
        return (int) (value * getResources().getDisplayMetrics().density + 0.5f);
    }

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
        root.setBackgroundColor(0xFF0E0D12);
        root.setPadding(dp(16), dp(12), dp(16), dp(12));

        LinearLayout header = new LinearLayout(this);
        header.setGravity(Gravity.CENTER_VERTICAL);
        TextView back = text("‹", 34, 0xFFE6E0E9, false);
        back.setGravity(Gravity.CENTER);
        back.setBackground(shape(0xFF29252F, 24));
        back.setContentDescription("Voltar");
        header.addView(back, new LinearLayout.LayoutParams(dp(48), dp(48)));
        back.setOnClickListener(v -> finish());

        String title = "favorites".equals(screen) ? "Favoritos" :
                "history".equals(screen) ? "Histórico" : "Downloads";
        TextView heading = text(title, 23, 0xFFE8EAED, true);
        LinearLayout.LayoutParams hp = new LinearLayout.LayoutParams(0, dp(52), 1);
        hp.leftMargin = dp(14);
        header.addView(heading, hp);
        root.addView(header);

        TextView subtitle = text("Sua biblioteca no Datu", 13, 0xFFCAC4D0, false);
        LinearLayout.LayoutParams subtitleParams = new LinearLayout.LayoutParams(-1, dp(28));
        subtitleParams.leftMargin = dp(62);
        root.addView(subtitle, subtitleParams);

        EditText search = new EditText(this);
        search.setSingleLine(true);
        search.setTextSize(15);
        search.setTextColor(0xFFE8EAED);
        search.setHintTextColor(0xFF9AA0A6);
        search.setHint("Pesquisar em " + title.toLowerCase(java.util.Locale.ROOT));
        search.setPadding(dp(18), 0, dp(16), 0);
        search.setBackground(shape(0xFF1D1B20, 24));
        LinearLayout.LayoutParams searchParams = new LinearLayout.LayoutParams(-1, dp(50));
        searchParams.topMargin = dp(12);
        searchParams.bottomMargin = dp(10);
        root.addView(search, searchParams);

        if ("history".equals(screen)) {
            clearHistory = text("Limpar histórico", 14, 0xFFD0BCFF, true);
            clearHistory.setGravity(Gravity.CENTER);
            clearHistory.setBackground(shape(0xFF4F378B, 18));
            LinearLayout.LayoutParams clearParams = new LinearLayout.LayoutParams(-1, dp(44));
            clearParams.bottomMargin = dp(10);
            root.addView(clearHistory, clearParams);
            clearHistory.setOnClickListener(v -> clearHistory());
        }

        RecyclerView list = new RecyclerView(this);
        list.setLayoutManager(new LinearLayoutManager(this));
        list.setClipToPadding(false);
        list.setPadding(0, dp(2), 0, dp(20));
        list.setItemAnimator(new androidx.recyclerview.widget.DefaultItemAnimator());
        adapter = new LibraryItemAdapter(this, screen, new LibraryItemAdapter.Listener() {
            @Override public void onOpen(LibraryItem item) {
                if ("downloads".equals(screen)) {
                    openDownload(item.id);
                } else {
                    Intent result = new Intent();
                    result.putExtra("url", item.url);
                    setResult(Activity.RESULT_OK, result);
                    finish();
                }
            }

            @Override public void onRemove(LibraryItem item) {
                removeItem(item);
            }
        });
        list.setAdapter(adapter);
        root.addView(list, new LinearLayout.LayoutParams(-1, 0, 1));

        emptyState = text("", 15, 0xFFCAC4D0, false);
        emptyState.setGravity(Gravity.CENTER);
        emptyState.setPadding(dp(24), dp(24), dp(24), dp(24));
        root.addView(emptyState, new LinearLayout.LayoutParams(-1, dp(100)));

        search.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                adapter.filter(s == null ? "" : s.toString());
                updateEmptyState();
            }
            @Override public void afterTextChanged(Editable s) { }
        });
        setContentView(root);
    }

    private void loadItems() {
        items.clear();
        if ("downloads".equals(screen)) loadDownloads();
        else loadSaved();
        adapter.setItems(items);
        updateEmptyState();
    }

    private void loadSaved() {
        String key = "history".equals(screen) ? "history" : "favorites";
        JSONArray saved;
        try {
            saved = new JSONArray(getSharedPreferences("datu_browser_library", MODE_PRIVATE)
                    .getString(key, "[]"));
        } catch (Exception e) {
            saved = new JSONArray();
        }

        for (int i = 0; i < saved.length(); i++) {
            JSONObject object = saved.optJSONObject(i);
            if (object == null) continue;
            String url = object.optString("url", "");
            String title = object.optString("title", url);
            items.add(new LibraryItem(i + 1L, i, title, url, url));
        }
    }

    private void loadDownloads() {
        if (downloadManager == null) return;
        Cursor cursor = null;
        try {
            cursor = downloadManager.query(new DownloadManager.Query());
            if (cursor == null) return;
            while (cursor.moveToNext()) {
                long id = cursor.getLong(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_ID));
                String title = cursor.getString(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_TITLE));
                int status = cursor.getInt(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_STATUS));
                String description = downloadStatus(status);
                items.add(new LibraryItem(id, -1, title, description, ""));
            }
        } catch (Exception ignored) {
        } finally {
            if (cursor != null) cursor.close();
        }
    }

    private String downloadStatus(int status) {
        if (status == DownloadManager.STATUS_SUCCESSFUL) return "Concluído";
        if (status == DownloadManager.STATUS_RUNNING) return "Em andamento";
        if (status == DownloadManager.STATUS_PAUSED) return "Pausado";
        if (status == DownloadManager.STATUS_FAILED) return "Falhou";
        return "Na fila";
    }

    private void openDownload(long id) {
        try {
            Uri uri = downloadManager == null ? null : downloadManager.getUriForDownloadedFile(id);
            if (uri == null) {
                Toast.makeText(this, "Esse download ainda não foi concluído.", Toast.LENGTH_SHORT).show();
                return;
            }
            String type = getContentResolver().getType(uri);
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(uri, type == null ? "*/*" : type);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(this, "Não há aplicativo compatível para abrir esse arquivo.", Toast.LENGTH_SHORT).show();
        }
    }

    private void removeItem(LibraryItem item) {
        if ("downloads".equals(screen)) {
            if (downloadManager != null) downloadManager.remove(item.id);
        } else {
            String key = "history".equals(screen) ? "history" : "favorites";
            try {
                JSONArray source = new JSONArray(getSharedPreferences("datu_browser_library", MODE_PRIVATE)
                        .getString(key, "[]"));
                JSONArray updated = new JSONArray();
                for (int i = 0; i < source.length(); i++) {
                    if (i != item.storageIndex) updated.put(source.get(i));
                }
                getSharedPreferences("datu_browser_library", MODE_PRIVATE).edit()
                        .putString(key, updated.toString()).apply();
            } catch (Exception ignored) {
            }
        }
        loadItems();
        Toast.makeText(this, "Item removido.", Toast.LENGTH_SHORT).show();
    }

    private void clearHistory() {
        getSharedPreferences("datu_browser_library", MODE_PRIVATE).edit()
                .putString("history", "[]").apply();
        loadItems();
        Toast.makeText(this, "Histórico limpo.", Toast.LENGTH_SHORT).show();
    }

    private void updateEmptyState() {
        if (emptyState == null || adapter == null) return;
        boolean empty = adapter.getItemCount() == 0;
        emptyState.setVisibility(empty ? View.VISIBLE : View.GONE);
        if (!empty) return;
        emptyState.setText(items.isEmpty()
                ? ("history".equals(screen) ? "Seu histórico está vazio" :
                   "favorites".equals(screen) ? "Nenhum favorito salvo ainda" :
                   "Nenhum download encontrado")
                : "Nenhum resultado encontrado");
    }
}
