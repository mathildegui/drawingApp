package com.mathilde.drawingapp;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.util.Log;
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
        //super.onDraw(canvas);
        canvas.drawBitmap(mBitmap, 0, 0, mCanvasPaint);
        canvas.drawPath(mDrawPath, mDrawPaint);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        //create canvas of certain device size
        super.onSizeChanged(w, h, oldw, oldh);

        //create Bitmap of certain w,.h
        mBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);

        //apply bitmap to graphic to start drawing
        mCanvas = new Canvas(mBitmap);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        //return super.onTouchEvent(event);

        float touchX = event.getX();
        float touchY = event.getY();

        //respond to down, up and move events
        switch(event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mDrawPath.moveTo(touchX, touchY);
                break;
            case MotionEvent.ACTION_MOVE:
                mDrawPath.lineTo(touchX, touchY);
                break;
            case MotionEvent.ACTION_UP:
                mDrawPath.lineTo(touchX, touchY);
                mCanvas.drawPath(mDrawPath, mDrawPaint);
                mDrawPath.reset();
                break;
            default:
                return false;
        }

        //redrw
        invalidate();
        return true;
    }
}
