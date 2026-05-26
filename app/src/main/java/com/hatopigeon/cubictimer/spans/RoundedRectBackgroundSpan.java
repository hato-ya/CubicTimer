package com.hatopigeon.cubictimer.spans;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.text.TextPaint;
import android.text.style.ReplacementSpan;

public class RoundedRectBackgroundSpan extends ReplacementSpan {
    private final int bgColor;
    private final int textColor;
    private final float cornerRadius;
    private final float paddingH;
    private final float paddingV;

    public RoundedRectBackgroundSpan(int bgColor, int textColor) {
        this(bgColor, textColor, 12f, 8f, 4f);
    }

    public RoundedRectBackgroundSpan(int bgColor, int textColor,
                                     float cornerRadius, float paddingH, float paddingV) {
        this.bgColor = bgColor;
        this.textColor = textColor;
        this.cornerRadius = cornerRadius;
        this.paddingH = paddingH;
        this.paddingV = paddingV;
    }

    @Override
    public int getSize(Paint paint, CharSequence text,
                       int start, int end, Paint.FontMetricsInt fm) {
        float textWidth = paint.measureText(text, start, end);
        return (int) (textWidth + paddingH * 2 + 0.5f);
    }

    @Override
    public void draw(Canvas canvas, CharSequence text, int start, int end,
                     float x, int top, int y, int bottom, Paint paint) {
        TextPaint tp = new TextPaint(paint);
        tp.setColor(textColor);

        Paint bg = new Paint(Paint.ANTI_ALIAS_FLAG);
        bg.setColor(bgColor);
        bg.setStyle(Paint.Style.FILL);

        float textWidth = tp.measureText(text, start, end);
        float textHeight = tp.descent() - tp.ascent();
        float rectTop = y + tp.ascent() - paddingV;
        float rectBottom = y + tp.descent() + paddingV;

        RectF rect = new RectF(x, rectTop, x + textWidth + paddingH * 2, rectBottom);
        canvas.drawRoundRect(rect, cornerRadius, cornerRadius, bg);

        canvas.drawText(text, start, end, x + paddingH, y, tp);
    }
}
