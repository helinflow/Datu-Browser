// Copyright 2026 The Datu Browser Authors.
// RecyclerView grid adapted to the TabList/RecyclerView architecture used by
// Chromium Android's tab_ui module. Chromium-specific TabModel dependencies
// are replaced by Datu's existing TabManager.
// Chromium source: chrome/android/features/tab_ui
// https://source.chromium.org/chromium/chromium/src/+/main:chrome/android/features/tab_ui/

package io.github.helinflow.datubrowser;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public final class TabGridAdapter extends RecyclerView.Adapter<TabGridAdapter.TabHolder> {
    public interface Listener {
        void onSelect(int position);
        void onClose(int position);
    }

    private final Context context;
    private final List<BrowserTab> tabs;
    private final int activeIndex;
    private final Listener listener;

    public TabGridAdapter(Context context, List<BrowserTab> tabs, int activeIndex, Listener listener) {
        this.context = context;
        this.tabs = tabs;
        this.activeIndex = activeIndex;
        this.listener = listener;
        setHasStableIds(true);
    }

    private int dp(float value) {
        return (int) (value * context.getResources().getDisplayMetrics().density + 0.5f);
    }

    private GradientDrawable shape(int color, int stroke) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setColor(color);
        drawable.setCornerRadius(dp(16));
        if (stroke > 0) drawable.setStroke(dp(2), stroke);
        return drawable;
    }

    @Override
    public long getItemId(int position) {
        return tabs.get(position).getId();
    }

    @Override
    public TabHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LinearLayout card = new LinearLayout(context);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(dp(8), dp(8), dp(8), dp(10));

        FrameLayout previewFrame = new FrameLayout(context);
        previewFrame.setBackground(shape(0xFF191919, 0));
        previewFrame.setClipToOutline(true);

        ImageView preview = new ImageView(context);
        preview.setScaleType(ImageView.ScaleType.CENTER_CROP);
        previewFrame.addView(preview, new FrameLayout.LayoutParams(-1, -1));

        TextView emptyPreview = new TextView(context);
        emptyPreview.setText("Prévia da página");
        emptyPreview.setTextColor(0xFFA6A6A6);
        emptyPreview.setTextSize(12);
        emptyPreview.setGravity(Gravity.CENTER);
        previewFrame.addView(emptyPreview, new FrameLayout.LayoutParams(-1, -1));

        ImageButton close = new ImageButton(context);
        close.setImageResource(android.R.drawable.ic_menu_close_clear_cancel);
        close.setColorFilter(0xFFF5F5F5);
        close.setBackground(shape(0xCC202124, 0));
        close.setContentDescription("Fechar guia");
        FrameLayout.LayoutParams closeParams = new FrameLayout.LayoutParams(dp(34), dp(34), Gravity.END | Gravity.TOP);
        closeParams.setMargins(0, dp(5), dp(5), 0);
        previewFrame.addView(close, closeParams);

        TextView title = new TextView(context);
        title.setTextColor(0xFFF5F5F5);
        title.setTextSize(14);
        title.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        title.setMaxLines(2);
        title.setMinHeight(dp(42));
        title.setPadding(dp(4), dp(8), dp(4), 0);

        TextView url = new TextView(context);
        url.setTextColor(0xFFA6A6A6);
        url.setTextSize(11);
        url.setMaxLines(1);
        url.setEllipsize(android.text.TextUtils.TruncateAt.END);
        url.setPadding(dp(4), dp(3), dp(4), 0);

        card.addView(previewFrame, new LinearLayout.LayoutParams(-1, dp(132)));
        card.addView(title, new LinearLayout.LayoutParams(-1, -2));
        card.addView(url, new LinearLayout.LayoutParams(-1, -2));

        return new TabHolder(card, preview, emptyPreview, close, title, url);
    }

    @Override
    public void onBindViewHolder(TabHolder holder, int position) {
        BrowserTab tab = tabs.get(position);
        boolean active = position == activeIndex;
        holder.itemView.setBackground(shape(active ? 0xFF191919 : 0xFF000000,
                active ? 0xFFFFFFFF : 0x00000000));
        holder.title.setText(tab.getTitle() == null || tab.getTitle().trim().isEmpty()
                ? "Nova guia" : tab.getTitle());
        holder.url.setText(tab.getUrl() == null ? "" : tab.getUrl());

        if (tab.getThumbnail() != null && !tab.getThumbnail().isRecycled()) {
            holder.preview.setImageBitmap(tab.getThumbnail());
            holder.preview.setVisibility(View.VISIBLE);
            holder.emptyPreview.setVisibility(View.GONE);
        } else {
            holder.preview.setImageDrawable(null);
            holder.preview.setVisibility(View.GONE);
            holder.emptyPreview.setVisibility(View.VISIBLE);
        }

        holder.itemView.setOnClickListener(v -> {
            int index = holder.getBindingAdapterPosition();
            if (index != RecyclerView.NO_POSITION) listener.onSelect(index);
        });
        holder.close.setOnClickListener(v -> {
            int index = holder.getBindingAdapterPosition();
            if (index != RecyclerView.NO_POSITION) listener.onClose(index);
        });
    }

    @Override
    public int getItemCount() {
        return tabs.size();
    }

    static final class TabHolder extends RecyclerView.ViewHolder {
        final ImageView preview;
        final TextView emptyPreview;
        final ImageButton close;
        final TextView title;
        final TextView url;

        TabHolder(View item, ImageView preview, TextView emptyPreview, ImageButton close,
                  TextView title, TextView url) {
            super(item);
            this.preview = preview;
            this.emptyPreview = emptyPreview;
            this.close = close;
            this.title = title;
            this.url = url;
        }
    }
}
