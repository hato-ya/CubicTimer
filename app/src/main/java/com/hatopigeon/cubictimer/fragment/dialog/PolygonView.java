package com.hatopigeon.cubictimer.fragment.dialog;

import static java.lang.Math.cos;
import static java.lang.Math.sin;
import static java.lang.Math.toRadians;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Region;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

public class PolygonView extends View {
    private static final String TAG = PolygonView.class.getSimpleName();
    private Region mRegion;
    private int mVertex;

    public PolygonView(Context context) {
        super(context);
    }
    public PolygonView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
    public PolygonView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public void setRegion(int vertex) {
        mVertex = vertex;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        Path path = new Path();
        for (int i = 0; i < mVertex; i++) {
            double rad = toRadians(-90.0+360.0/mVertex*i);
            if (i != 0) path.lineTo((float)cos(rad),(float)sin(rad));
            else        path.moveTo((float)cos(rad),(float)sin(rad));
            //Log.d(TAG, i + ":" + cos(rad) + "," +sin(rad));
        }
        path.close();

        RectF rectF = new RectF();
        path.computeBounds(rectF, true);

        Matrix matrix = new Matrix();
        matrix.setRectToRect(rectF, new RectF(0.0f,0.0f,(float)this.getWidth(),(float)this.getHeight()), Matrix.ScaleToFit.FILL);
        path.transform(matrix);

        path.computeBounds(rectF, true);
        //Log.d(TAG, "rectF : " + rectF.left + "," + rectF.top + " " + rectF.right + "," + rectF.bottom);

        mRegion = new Region();
        mRegion.setPath(path, new Region((int)rectF.left, (int)rectF.top, (int)rectF.right, (int)rectF.bottom));
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        //Log.d(TAG, "x = " + event.getX() + " y = " + event.getY() + " w = " + this.getWidth() + " h = " + this.getHeight());
        if (mRegion.contains((int)event.getX(), (int)event.getY())) {
            //Log.d(TAG, "Contain");
            return super.onTouchEvent(event);
        } else {
            //Log.d(TAG, "Not Contain");
            return false;
        }
    }
}
