package com.mathilde.drawingapp.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;

import com.mathilde.drawingapp.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by mathilde on 08/11/2015.
 */
public class CustomView extends View {

    private float mX, mY;
    private static final float TOUCH_TOLERANCE = 4;

    //drawing path
    private Path mDrawPath;

    //defines how to draw
    private Paint mDrawPaint;

    //initial color
    private int mPaintColor = 0xFF660000;

    //canvas - holding pen, holds your drawings
    //transfert them in views
    private Canvas mCanvas;

    //brush size
    private float mCurrentBrushSize, mLastBrushSize;

    //list for redo and undo actions
    private List<MyCustomList> paths = new ArrayList<>();
    private List<MyCustomList> undonePaths = new ArrayList<>();

    public CustomView(Context context) {
        super(context);
    }

    private class MyCustomList {
        public float brush;
        public Integer color;
        public Path path;
    }

    public int getColor(){
        return mPaintColor;
    }

    public void updateColor (int color) {
        MyCustomList list = new MyCustomList();
        list.path = mDrawPath;
        list.color = color;
        list.brush = mCurrentBrushSize;
        paths.add(list);
        mDrawPath = new Path();
        mPaintColor = color;
    }

    public CustomView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        setBackgroundColor(Color.WHITE);

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
    }

    @Override
    protected void onDraw(Canvas canvas) {
        for(MyCustomList p : paths) {
            mDrawPaint.setColor(p.color.intValue());
            mDrawPaint.setStrokeWidth(p.brush);
            canvas.drawPath(p.path, mDrawPaint);
        }
        canvas.drawPath(mDrawPath, mDrawPaint);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        //create canvas of certain device size
        super.onSizeChanged(w, h, oldw, oldh);

        Log.d("W", w + "");
        Log.d("H", h + "");
        Log.d("oldw", oldw + "");
        Log.d("oldh", oldh + "");

        //create Bitmap of certain w,.h
        Bitmap bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);

        //apply bitmap to graphic to start drawing
        mCanvas = new Canvas(bitmap);
    }

    @Override
    public boolean onTouchEvent(@NonNull MotionEvent event) {
        float touchX = event.getX();
        float touchY = event.getY();

        //respond to down, up and move events
        switch(event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                touch_start(touchX, touchY);
                invalidate();
                break;
            case MotionEvent.ACTION_MOVE:
                touch_move(touchX, touchY);
                invalidate();
                break;
            case MotionEvent.ACTION_UP:
                touch_up();
                invalidate();
                break;
            default:
                return false;
        }
        return true;
    }

    private void touch_start(float x, float y) {
        undonePaths.clear();
        mDrawPath.reset();
        mDrawPath.moveTo(x, y);

        mX = x;
        mY = y;
    }

    private void touch_move(float x, float y) {
        float dx = Math.abs(x - mX);
        float dy = Math.abs(y - mY);
        if(dx >= TOUCH_TOLERANCE || dy >= TOUCH_TOLERANCE) {
            mDrawPath.quadTo(mX, mY, (x + mX)/2, (y + mY)/2);
            mX = x;
            mY = y;
        }
    }

    private void touch_up() {
        mDrawPath.lineTo(mX, mY);
        mCanvas.drawPath(mDrawPath, mDrawPaint);
        MyCustomList list = new MyCustomList();
        list.path = mDrawPath;
        list.color = mPaintColor;
        list.brush = mCurrentBrushSize;

        paths.add(list);
        mDrawPath = new Path();
    }

    /* Start new drawing */
    public void eraseAll() {
        mDrawPath = new Path();
        paths.clear();
        undonePaths.clear();
        mCanvas.drawColor(Color.WHITE);
        invalidate();
    }

    public void undo() {
        if(paths.size() > 0) {
            undonePaths.add(paths.remove(paths.size() - 1));
            invalidate();
        }
    }

    public void redo() {
        if(undonePaths.size() > 0) {
            paths.add(undonePaths.remove(undonePaths.size() - 1));
            invalidate();
        }
    }

    public float getLastBrushSize() {
        return mLastBrushSize;
    }

    public void setLastBrushSize(float size) {
        mLastBrushSize = size;
    }

    public void setBrushSize(float newSize) {
        float pixelAmount = TypedValue.applyDimension(1, newSize, getResources().getDisplayMetrics());
        MyCustomList list = new MyCustomList();
        list.brush = pixelAmount;
        list.color = mPaintColor;
        list.path  = mDrawPath;

        paths.add(list);
        mDrawPath = new Path();
        mCurrentBrushSize = pixelAmount;
    }

    @Override
    protected Parcelable onSaveInstanceState() {
        Parcelable superState = super.onSaveInstanceState();
        SavedState ss = new SavedState(superState);

        ss.savedX = mX;
        ss.savedY = mY;

        return ss;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        if(!(state instanceof SavedState)) {
            super.onRestoreInstanceState(state);
            return;
        }

        SavedState ss = (SavedState)state;
        super.onRestoreInstanceState(ss.getSuperState());
        //end

        mX = ss.savedX;
        mY = ss.savedY;
    }

    static class SavedState extends BaseSavedState {
        float savedY, savedX;

        SavedState(Parcelable superState) {
            super(superState);
        }

        @SuppressWarnings("unchecked")
        private SavedState(Parcel in) {
            super(in);

            this.savedX = in.readFloat();
            this.savedY = in.readFloat();
        }

        @Override
        public void writeToParcel(Parcel out, int flags) {
            super.writeToParcel(out, flags);
            out.writeFloat(this.savedX);
            out.writeFloat(this.savedY);
        }

        //required field that makes Parcelables from a Parcel
        public static final Parcelable.Creator<SavedState> CREATOR =
                new Parcelable.Creator<SavedState>() {
                    public SavedState createFromParcel(Parcel in) {
                        return new SavedState(in);
                    }
                    public SavedState[] newArray(int size) {
                        return new SavedState[size];
                    }
                };
    }
}
