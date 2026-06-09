package com.example.nasax.ui.stats;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

/**
 * Lightweight bar chart — no external library needed.
 * Each bar shows score/total as a percentage.
 */
public class ScoreBarChartView extends View {

    private final Paint barPaint    = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint dimPaint    = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint textPaint   = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint labelPaint  = new Paint(Paint.ANTI_ALIAS_FLAG);

    private List<float[]> data = new ArrayList<>(); // [score, total] pairs, newest last

    public ScoreBarChartView(Context ctx, AttributeSet attrs) {
        super(ctx, attrs);
        barPaint.setColor(0xFFFF9100);
        dimPaint.setColor(0xFF281A08);
        textPaint.setColor(0xFFFFFFFF);
        textPaint.setTextSize(28f);
        textPaint.setTextAlign(Paint.Align.CENTER);
        labelPaint.setColor(0xFFAAAAAA);
        labelPaint.setTextSize(22f);
        labelPaint.setTextAlign(Paint.Align.CENTER);
    }

    public void setData(List<float[]> data) {
        this.data = data;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (data == null || data.isEmpty()) return;

        int count   = data.size();
        float w     = getWidth();
        float h     = getHeight();
        float pad   = 16f;
        float gap   = 10f;
        float barW  = (w - 2 * pad - (count - 1) * gap) / count;
        float chartH = h - 56f; // leave room for labels at bottom

        for (int i = 0; i < count; i++) {
            float score = data.get(i)[0];
            float total = data.get(i)[1];
            float pct   = total > 0 ? score / total : 0f;

            float left   = pad + i * (barW + gap);
            float right  = left + barW;
            float bottom = chartH;
            float top    = chartH - (chartH - pad) * pct;

            // Background bar
            canvas.drawRoundRect(new RectF(left, pad, right, bottom), 8, 8, dimPaint);
            // Filled bar
            if (top < bottom)
                canvas.drawRoundRect(new RectF(left, top, right, bottom), 8, 8, barPaint);

            // Percentage label above bar
            String pctStr = Math.round(pct * 100) + "%";
            canvas.drawText(pctStr, left + barW / 2, top - 4, textPaint);

            // Game number below
            canvas.drawText("#" + (count - i), left + barW / 2, h - 6, labelPaint);
        }
    }
}
