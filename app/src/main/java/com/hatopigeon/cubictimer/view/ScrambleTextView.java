package com.hatopigeon.cubictimer.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.text.Layout;
import android.util.AttributeSet;

import androidx.appcompat.widget.AppCompatTextView;

public class ScrambleTextView extends AppCompatTextView {

    private String[] scrambleTokens;
    private int completedMoves;
    private final Paint bgPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    public ScrambleTextView(Context context) {
        super(context);
    }

    public ScrambleTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ScrambleTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void setScrambleProgress(String[] tokens, int completed) {
        this.scrambleTokens = tokens;
        this.completedMoves = completed;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (scrambleTokens != null && completedMoves > 0 && completedMoves <= scrambleTokens.length) {
            Layout layout = getLayout();
            if (layout != null) {
                drawCompletedBackgrounds(canvas, layout);
            }
        }
        super.onDraw(canvas);
    }

    private void drawCompletedBackgrounds(Canvas canvas, Layout layout) {
        bgPaint.setColor(Color.BLACK);
        bgPaint.setStyle(Paint.Style.FILL);

        float padH = 8f;
        float padV = 4f;
        float radius = 12f;

        String fullText = getText().toString();
        int searchPos = 0;
        int idx = 0;

        while (idx < scrambleTokens.length) {
            String tok = scrambleTokens[idx];
            int tokStart = fullText.indexOf(tok, searchPos);
            if (tokStart < 0) break;
            int tokEnd = tokStart + tok.length();
            searchPos = tokEnd;

            if (idx < completedMoves) {
                int gStart = tokStart;
                int gEnd = tokEnd;
                int gLine = layout.getLineForOffset(tokStart);
                idx++;

                while (idx < completedMoves) {
                    tok = scrambleTokens[idx];
                    tokStart = fullText.indexOf(tok, searchPos);
                    if (tokStart < 0) break;
                    tokEnd = tokStart + tok.length();
                    searchPos = tokEnd;

                    int line = layout.getLineForOffset(tokStart);
                    if (line != gLine) {
                        drawGroupRect(canvas, layout, gStart, gEnd, gLine, bgPaint, padH, padV, radius);
                        gStart = tokStart;
                        gEnd = tokEnd;
                        gLine = line;
                    } else {
                        gEnd = tokEnd;
                    }
                    idx++;
                }
                drawGroupRect(canvas, layout, gStart, gEnd, gLine, bgPaint, padH, padV, radius);
            } else {
                idx++;
            }
        }
    }

    private void drawGroupRect(Canvas canvas, Layout layout, int start, int end,
                               int line, Paint paint, float padH, float padV, float radius) {
        int baseline = layout.getLineBaseline(line);
        float left = layout.getPrimaryHorizontal(start) - padH;
        float right = layout.getPrimaryHorizontal(end) + padH;
        float top = baseline + layout.getLineAscent(line) - padV;
        float bottom = baseline + layout.getLineDescent(line) + padV;

        left = Math.max(0, left);
        right = Math.min(getWidth(), right);

        canvas.drawRoundRect(new RectF(left, top, right, bottom), radius, radius, paint);
    }
}
