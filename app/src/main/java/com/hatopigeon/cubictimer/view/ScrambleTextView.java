package com.hatopigeon.cubictimer.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.text.Layout;
import android.util.AttributeSet;

import androidx.appcompat.widget.AppCompatTextView;

import com.hatopigeon.cubictimer.utils.Prefs;

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
                // Match the offset TextView uses to draw the text, so the highlight aligns with it
                // (and the vertical padding gives room for the margin above/below the letters).
                canvas.save();
                canvas.translate(getTotalPaddingLeft(), getTotalPaddingTop());
                drawCompletedBackgrounds(canvas, layout);
                canvas.restore();
            }
        }
        super.onDraw(canvas);
    }

    private void drawCompletedBackgrounds(Canvas canvas, Layout layout) {
        String bgHex = Prefs.getString(com.hatopigeon.cubicify.R.string.pk_scramble_highlight_bg, "000000");
        bgPaint.setColor(Color.parseColor("#" + bgHex));
        bgPaint.setStyle(Paint.Style.FILL);

        float density = getResources().getDisplayMetrics().density;
        float padH = 4f * density;
        float padV = 8f * density;
        float radius = 4f * density;

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

    private final android.graphics.Rect refBounds = new android.graphics.Rect();

    private void drawGroupRect(Canvas canvas, Layout layout, int start, int end,
                               int line, Paint paint, float padH, float padV, float radius) {
        int baseline = layout.getLineBaseline(line);
        float left = layout.getPrimaryHorizontal(start) - padH;
        float right = layout.getPrimaryHorizontal(end) + padH;

        // Use a fixed uppercase reference (not the font's asymmetric ascent/descent, nor each
        // group's own ink which shifts with primes) so every highlight is the same height and
        // centred on the letter band with equal margin above and below.
        getPaint().getTextBounds("M", 0, 1, refBounds);
        float top = baseline + refBounds.top - padV;
        float bottom = baseline + refBounds.bottom + padV;

        left = Math.max(0, left);
        right = Math.min(getWidth(), right);

        canvas.drawRoundRect(new RectF(left, top, right, bottom), radius, radius, paint);
    }
}
