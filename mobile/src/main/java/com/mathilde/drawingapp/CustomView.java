package com.mathilde.drawingapp;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

/**
 * Created by mathilde on 08/11/2015.
 */
public class CustomView extends View {

    //drawing path
    private Path mDrawPath;

    //defines what to draw
    private Paint mCanvasPaint;

    //defines how to draw
    private Paint mDrawPaint;

    //initial color
    private int mPaintColor = 0xFF660000;

    //canvas - holding pen, holds your drawings
    //transfert them in views
    private Canvas mCanvas;

    //canvas bitmap
    private Bitmap mBitmap;

    //brush size
    private float mCurrentBrushSize, mLastBrushSize;

    public CustomView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        mCurrentBrushSize = getResources().getInteger(R.integer.medium_size);
        mLastBrushSize    = mCurrentBrushSize;

        mDrawPath  = new Path();
        mDrawPaint = new Paint();

        mDrawPaint.setColor(mPaintColor);
        mDrawPaint.setAntiAlias(true);
        mDrawPaint.setStrokeWidth(mCurrentBrushSize);
        mDrawPaint.setStyle(Paint.Style.STROKE);
        mDrawPaint.setStrokeJoin(Paint.Join.ROUND);
        mDrawPaint.setStrokeCap(Paint.Cap.ROUND);

        mCanvasPaint = new Paint(Paint.DITHER_FLAG);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return super.onTouchEvent(event);
    }
}
