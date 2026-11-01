package io.github.helinflow.datubrowser;

import android.os.Bundle;
import android.graphics.Bitmap;
import android.graphics.Canvas;

/** A browser tab snapshot. The activity owns the single visible WebView. */
public final class BrowserTab {
    private final int id;
    private String title;
    private String url;
    private Bundle webState;
    private Bitmap thumbnail;

    BrowserTab(int id, String url) {
        this.id = id;
        this.url = url == null ? "https://search.brave.com/" : url;
        this.title = "Nova guia";
    }

    public int getId() { return id; }
    public String getTitle() { return title; }
    public String getUrl() { return url; }
    Bundle getWebState() { return webState; }
    public Bitmap getThumbnail() { return thumbnail; }

    void capture(android.webkit.WebView webView) {
        if (webView == null) return;
        Bundle snapshot = new Bundle();
        webView.saveState(snapshot);
        webState = snapshot;
        int sourceWidth = webView.getWidth();
        int sourceHeight = webView.getHeight();
        if (sourceWidth > 0 && sourceHeight > 0) {
            Bitmap next = Bitmap.createBitmap(240, 135, Bitmap.Config.RGB_565);
            Canvas canvas = new Canvas(next);
            canvas.drawColor(android.graphics.Color.WHITE);
            canvas.save();
            canvas.scale(240f / sourceWidth, 135f / sourceHeight);
            webView.draw(canvas);
            canvas.restore();
            if (thumbnail != null && !thumbnail.isRecycled()) thumbnail.recycle();
            thumbnail = next;
        }
        String currentUrl = webView.getUrl();
        String currentTitle = webView.getTitle();
        if (currentUrl != null) url = currentUrl;
        if (currentTitle != null && !currentTitle.trim().isEmpty()) title = currentTitle;
    }

    boolean restore(android.webkit.WebView webView) {
        if (webView == null || webState == null) return false;
        try {
            android.webkit.WebBackForwardList restored = webView.restoreState(webState);
            return restored != null;
        } catch (RuntimeException error) {
            return false;
        }
    }

    void updateFrom(android.webkit.WebView webView) {
        if (webView == null) return;
        if (webView.getUrl() != null) url = webView.getUrl();
        if (webView.getTitle() != null && !webView.getTitle().trim().isEmpty()) {
            title = webView.getTitle();
        }
    }
}
