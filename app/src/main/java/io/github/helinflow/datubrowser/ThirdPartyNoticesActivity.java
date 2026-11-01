package io.github.helinflow.datubrowser;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

public final class ThirdPartyNoticesActivity extends Activity {
    private int dp(float value) {
        return (int) (value * getResources().getDisplayMetrics().density + 0.5f);
    }

    @Override
    protected void onCreate(Bundle state) {
        super.onCreate(state);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(Color.rgb(20, 18, 20));
        root.setPadding(dp(18), dp(12), dp(18), dp(18));

        TextView back = new TextView(this);
        back.setText("‹     Sobre o Datu");
        back.setTextColor(Color.rgb(230, 224, 233));
        back.setTextSize(20);
        back.setGravity(Gravity.CENTER_VERTICAL);
        back.setPadding(dp(4), dp(10), dp(4), dp(14));
        back.setOnClickListener(v -> finish());
        root.addView(back, new LinearLayout.LayoutParams(-1, dp(56)));

        TextView heading = new TextView(this);
        heading.setText("Avisos de terceiros");
        heading.setTextColor(Color.rgb(230, 224, 233));
        heading.setTextSize(24);
        heading.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        heading.setPadding(dp(2), dp(8), dp(2), dp(12));
        root.addView(heading);

        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);

        TextView content = new TextView(this);
        content.setText(readNotices());
        content.setTextColor(Color.rgb(208, 196, 200));
        content.setTextSize(13);
        content.setLineSpacing(dp(3), 1f);
        content.setTextIsSelectable(true);
        content.setPadding(dp(4), dp(4), dp(4), dp(24));
        scroll.addView(content, new ScrollView.LayoutParams(-1, -2));
        root.addView(scroll, new LinearLayout.LayoutParams(-1, 0, 1));

        setContentView(root);
    }

    private String readNotices() {
        StringBuilder text = new StringBuilder();
        try (InputStream input = getAssets().open("THIRD_PARTY_NOTICES");
             BufferedReader reader = new BufferedReader(
                 new InputStreamReader(input, "UTF-8"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                text.append(line).append('\n');
            }
        } catch (Exception error) {
            return "Não foi possível carregar os avisos de terceiros.";
        }
        return text.toString();
    }
}
