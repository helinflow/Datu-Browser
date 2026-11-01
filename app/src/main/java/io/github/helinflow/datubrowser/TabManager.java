package io.github.helinflow.datubrowser;

import android.webkit.WebView;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * In-memory tab snapshot manager. It switches navigation snapshots through
 * the existing WebView so all current WebView clients and Shields remain attached.
 */
public final class TabManager {
    private final ArrayList<BrowserTab> tabs = new ArrayList<>();
    private int activeIndex = -1;
    private int nextId = 1;

    public BrowserTab create(WebView webView, String startUrl) {
        captureActive(webView);
        BrowserTab tab = new BrowserTab(nextId++, startUrl);
        tabs.add(tab);
        activeIndex = tabs.size() - 1;
        return tab;
    }

    public boolean select(int index, WebView webView) {
        if (index < 0 || index >= tabs.size()) return false;
        if (index == activeIndex) return true;
        captureActive(webView);
        activeIndex = index;
        BrowserTab selected = tabs.get(activeIndex);
        if (!selected.restore(webView)) webView.loadUrl(selected.getUrl());
        return true;
    }

    public boolean close(int index, WebView webView) {
        if (index < 0 || index >= tabs.size()) return false;
        boolean closingActive = index == activeIndex;
        tabs.remove(index);
        if (tabs.isEmpty()) {
            activeIndex = -1;
            create(webView, "https://search.brave.com/");
            webView.loadUrl("https://search.brave.com/");
            return true;
        }
        if (index < activeIndex) activeIndex--;
        else if (closingActive) {
            activeIndex = Math.min(index, tabs.size() - 1);
            BrowserTab selected = tabs.get(activeIndex);
            if (!selected.restore(webView)) webView.loadUrl(selected.getUrl());
        }
        return true;
    }

    public void captureActive(WebView webView) {
        BrowserTab tab = getActive();
        if (tab != null) tab.capture(webView);
    }

    public BrowserTab getActive() {
        return activeIndex >= 0 && activeIndex < tabs.size() ? tabs.get(activeIndex) : null;
    }

    public int getActiveIndex() { return activeIndex; }
    public int getCount() { return tabs.size(); }

    public List<BrowserTab> getTabs() {
        return Collections.unmodifiableList(tabs);
    }
}
