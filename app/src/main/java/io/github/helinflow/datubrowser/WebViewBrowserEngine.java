package io.github.helinflow.datubrowser;

import android.view.View;
import android.webkit.WebView;

/** Android System WebView adapter retained as the stable fallback. */
public final class WebViewBrowserEngine implements BrowserEngine {
    private final WebView webView;
    public WebViewBrowserEngine(WebView webView) {
        if (webView == null) throw new IllegalArgumentException("WebView cannot be null");
        this.webView = webView;
    }
    @Override public View getView() { return webView; }
    @Override public void loadUrl(String url) { webView.loadUrl(url); }
    @Override public String getUrl() { return webView.getUrl(); }
    @Override public boolean canGoBack() { return webView.canGoBack(); }
    @Override public void goBack() { webView.goBack(); }
    @Override public void reload() { webView.reload(); }
}
