package io.github.helinflow.datubrowser;

import android.app.AlertDialog;
import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.webkit.WebView;
import android.widget.Toast;
import org.json.JSONArray;
import org.json.JSONObject;

/** Persistent favorites/history and a readable view over Android DownloadManager. */
public final class BrowserLibrary {
    private static final String PREFS = "datu_browser_library";
    private BrowserLibrary() {}
    private static JSONArray read(Context c, String key) {
        try { return new JSONArray(c.getSharedPreferences(PREFS, 0).getString(key, "[]")); }
        catch (Exception e) { return new JSONArray(); }
    }
    private static void write(Context c, String key, JSONArray value) {
        c.getSharedPreferences(PREFS, 0).edit().putString(key, value.toString()).apply();
    }
    private static String title(String title, String url) {
        return title == null || title.trim().isEmpty() ? url : title.trim();
    }
    public static synchronized void recordVisit(Context c, String pageTitle, String url) {
        if (url == null || !(url.startsWith("https://") || url.startsWith("http://"))) return;
        try {
            JSONArray old = read(c, "history");
            if (old.length() > 0 && url.equals(old.optJSONObject(0).optString("url"))) return;
            JSONArray next = new JSONArray();
            JSONObject item = new JSONObject();
            item.put("title", title(pageTitle, url)); item.put("url", url); item.put("time", System.currentTimeMillis());
            next.put(item);
            for (int i=0; i<old.length() && i<499; i++) next.put(old.getJSONObject(i));
            write(c, "history", next);
        } catch (Exception ignored) {}
    }
    public static synchronized void addFavorite(Context c, String pageTitle, String url) {
        if (url == null || !(url.startsWith("https://") || url.startsWith("http://"))) return;
        try {
            JSONArray items = read(c, "favorites");
            for (int i=0; i<items.length(); i++) if (url.equals(items.getJSONObject(i).optString("url"))) {
                Toast.makeText(c, "Essa página já está nos favoritos.", Toast.LENGTH_SHORT).show(); return;
            }
            JSONObject item = new JSONObject(); item.put("title", title(pageTitle,url)); item.put("url",url); items.put(item);
            write(c, "favorites", items);
            Toast.makeText(c, "Página adicionada aos favoritos.", Toast.LENGTH_SHORT).show();
        } catch (Exception e) { Toast.makeText(c, "Não foi possível salvar o favorito.", Toast.LENGTH_SHORT).show(); }
    }
    public static void showFavorites(Context c, WebView w) { showSaved(c,w,"favorites","Favoritos",false); }
    public static void showHistory(Context c, WebView w) { showSaved(c,w,"history","Histórico",true); }
    private static void showSaved(Context c, WebView w, String key, String heading, boolean clear) {
        JSONArray items = read(c,key); String[] labels = new String[items.length()];
        for (int i=0;i<items.length();i++) { JSONObject o=items.optJSONObject(i); labels[i]=o==null?"":o.optString("title",o.optString("url")); }
        AlertDialog.Builder b = new AlertDialog.Builder(c).setTitle(heading);
        if (labels.length==0) b.setMessage("Nada por aqui ainda.");
        else b.setItems(labels,(d,n)->{ JSONObject o=items.optJSONObject(n); if(o!=null && w!=null) w.loadUrl(o.optString("url")); });
        if (clear && items.length()>0) b.setNeutralButton("Limpar histórico",(d,n)->{write(c,"history",new JSONArray());Toast.makeText(c,"Histórico limpo.",Toast.LENGTH_SHORT).show();});
        b.setPositiveButton("Fechar",null).show();
    }
    public static void showDownloads(Context c) {
        DownloadManager dm=(DownloadManager)c.getSystemService(Context.DOWNLOAD_SERVICE);
        if(dm==null){Toast.makeText(c,"Gerenciador de downloads indisponível.",Toast.LENGTH_SHORT).show();return;}
        JSONArray rows=new JSONArray();
        try { android.database.Cursor cur=dm.query(new DownloadManager.Query()); if(cur!=null){try{while(cur.moveToNext()){JSONObject o=new JSONObject();o.put("id",cur.getLong(cur.getColumnIndexOrThrow(DownloadManager.COLUMN_ID)));o.put("title",cur.getString(cur.getColumnIndexOrThrow(DownloadManager.COLUMN_TITLE)));o.put("status",cur.getInt(cur.getColumnIndexOrThrow(DownloadManager.COLUMN_STATUS)));rows.put(o);}}finally{cur.close();}}}catch(Exception ignored){}
        String[] labels=new String[rows.length()];
        for(int i=0;i<rows.length();i++){JSONObject o=rows.optJSONObject(i);labels[i]=o.optString("title")+" — "+status(o.optInt("status"));}
        AlertDialog.Builder b=new AlertDialog.Builder(c).setTitle("Downloads");
        if(labels.length==0)b.setMessage("Nenhum download encontrado.");
        else b.setItems(labels,(d,n)->{try{long id=rows.getJSONObject(n).getLong("id");Uri uri=dm.getUriForDownloadedFile(id);if(uri==null){Toast.makeText(c,"Esse download ainda não foi concluído.",Toast.LENGTH_SHORT).show();return;}String type=c.getContentResolver().getType(uri);Intent intent=new Intent(Intent.ACTION_VIEW);intent.setDataAndType(uri,type==null?"*/*":type);intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);c.startActivity(intent);}catch(Exception e){Toast.makeText(c,"Não há aplicativo compatível para abrir esse arquivo.",Toast.LENGTH_SHORT).show();}});
        b.setPositiveButton("Fechar",null).show();
    }
    private static String status(int s){switch(s){case DownloadManager.STATUS_SUCCESSFUL:return "Concluído";case DownloadManager.STATUS_RUNNING:return "Em andamento";case DownloadManager.STATUS_PAUSED:return "Pausado";case DownloadManager.STATUS_FAILED:return "Falhou";default:return "Na fila";}}
}
