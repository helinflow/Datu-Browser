package io.github.helinflow.datubrowser;

import android.view.View;

/** Narrow boundary between Datu UI and its browser engine. */
public interface BrowserEngine {
    View getView();
    void loadUrl(String url);
    String getUrl();
    boolean canGoBack();
    void goBack();
    void reload();
}
