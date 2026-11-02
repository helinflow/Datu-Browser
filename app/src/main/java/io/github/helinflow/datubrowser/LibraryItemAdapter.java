// Copyright 2026 The Datu Browser Authors.
// RecyclerView-based library list following Chromium's list-row pattern.
// Datu keeps its own JSON library storage and Android DownloadManager integration.

package io.github.helinflow.datubrowser;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class LibraryItemAdapter extends RecyclerView.Adapter<LibraryItemAdapter.Holder> {
    public interface Listener {
        void onOpen(LibraryActivity.LibraryItem item);
        void onRemove(LibraryActivity.LibraryItem item);
    }

    private final Context context;
    private final Listener listener;
    private final ArrayList<LibraryActivity.LibraryItem> allItems = new ArrayList<>();
    private final ArrayList<LibraryActivity.LibraryItem> visibleItems = new ArrayList<>();
    private final String screen;

    public LibraryItemAdapter(Context context, String screen, Listener listener) {
        this.context = context;
        this.screen = screen;
        this.listener = listener;
        setHasStableIds(true);
    }

    public void setItems(List<LibraryActivity.LibraryItem> items) {
        allItems.clear();
        if (items != null) allItems.addAll(items);
        filter("");
    }

    public void filter(String query) {
        String needle = query == null ? "" : query.trim().toLowerCase(Locale.ROOT);
        visibleItems.clear();
        for (LibraryActivity.LibraryItem item : allItems) {
            String haystack = (item.title + " " + item.detail).toLowerCase(Locale.ROOT);
            if (haystack.contains(needle)) visibleItems.add(item);
        }
        notifyDataSetChanged();
    }

    public int getVisibleCount() { return visibleItems.size(); }

    private int dp(float value) {
        return (int) (value * context.getResources().getDisplayMetrics().density + 0.5f);
    }

    private GradientDrawable rounded(int color, int radius) {
        GradientDrawable d = new GradientDrawable();
        d.setColor(color);
        d.setCornerRadius(dp(radius));
        return d;
    }

    @Override public long getItemId(int position) {
        LibraryActivity.LibraryItem item = visibleItems.get(position);
        return item.id != 0 ? item.id : ((long) item.title.hashCode() << 32) ^ item.detail.hashCode();
    }

    @Override public Holder onCreateViewHolder(ViewGroup parent, int viewType) {
        LinearLayout row = new LinearLayout(context);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(dp(14), dp(9), dp(8), dp(9));
        row.setBackground(rounded(0xFF151515, 18));

        TextView icon = new TextView(context);
        icon.setGravity(Gravity.CENTER);
        icon.setTextSize(20);
        icon.setTextColor(0xFFA6A6A6);
        icon.setBackground(rounded(0xFF292929, 14));
        row.addView(icon, new LinearLayout.LayoutParams(dp(44), dp(44)));

        LinearLayout labels = new LinearLayout(context);
        labels.setOrientation(LinearLayout.VERTICAL);
        labels.setPadding(dp(13), dp(2), dp(6), dp(2));
        TextView title = new TextView(context);
        title.setTextColor(0xFFF5F5F5);
        title.setTextSize(15);
        title.setTypeface(Typeface.create("sans-serif-medium", Typeface.NORMAL));
        title.setMaxLines(2);
        TextView detail = new TextView(context);
        detail.setTextColor(0xFFA6A6A6);
        detail.setTextSize(12);
        detail.setMaxLines(1);
        detail.setEllipsize(android.text.TextUtils.TruncateAt.END);
        LinearLayout.LayoutParams detailParams = new LinearLayout.LayoutParams(-1, -2);
        detailParams.topMargin = dp(4);
        labels.addView(title, new LinearLayout.LayoutParams(-1, -2));
        labels.addView(detail, detailParams);
        row.addView(labels, new LinearLayout.LayoutParams(0, -2, 1));

        ImageButton more = new ImageButton(context);
        more.setImageResource(android.R.drawable.ic_menu_more);
        more.setColorFilter(0xFFA6A6A6);
        more.setBackground(rounded(0x001D1B20, 22));
        more.setContentDescription("Mais opções");
        row.addView(more, new LinearLayout.LayoutParams(dp(42), dp(44)));
        return new Holder(row, icon, title, detail, more);
    }

    @Override public void onBindViewHolder(Holder holder, int position) {
        LibraryActivity.LibraryItem item = visibleItems.get(position);
        int iconResource = "favorites".equals(screen) ? R.drawable.ic_library_favorite :
                "history".equals(screen) ? R.drawable.ic_library_history : R.drawable.ic_library_download;
        holder.icon.setText("");
        holder.icon.setCompoundDrawablesWithIntrinsicBounds(iconResource, 0, 0, 0);
        holder.title.setText(item.title);
        holder.detail.setText(item.detail);
        holder.itemView.setOnClickListener(v -> listener.onOpen(item));
        holder.more.setOnClickListener(v -> {
            PopupMenu menu = new PopupMenu(context, holder.more);
            menu.getMenu().add("Abrir").setOnMenuItemClickListener(m -> {
                listener.onOpen(item);
                return true;
            });
            menu.getMenu().add("Remover").setOnMenuItemClickListener(m -> {
                listener.onRemove(item);
                return true;
            });
            menu.show();
        });
        holder.itemView.setContentDescription(item.title + ". " + item.detail);
    }

    @Override public int getItemCount() { return visibleItems.size(); }

    static final class Holder extends RecyclerView.ViewHolder {
        final TextView icon;
        final TextView title;
        final TextView detail;
        final ImageButton more;
        Holder(View item, TextView icon, TextView title, TextView detail, ImageButton more) {
            super(item);
            this.icon = icon;
            this.title = title;
            this.detail = detail;
            this.more = more;
        }
    }
}
