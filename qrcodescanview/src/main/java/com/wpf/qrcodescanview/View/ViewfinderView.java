package com.wpf.qrcodescanview.View;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.wpf.qrcodescanview.R;

/**
 * Created by wpf on 6-20-0020.
 *
 */

class ViewfinderView extends SurfaceView implements
        SurfaceHolder.Callback2 , Runnable {

    private SurfaceHolder surfaceHolder;
    private int mWidget = 0, mHeight = 0,mDown = 1;
    private Rect mRect;
    private Rect mRect_Line;
    private Paint mPaint_CenterFK = new Paint();
    private Paint mPaint_CenterBJ = new Paint();
    private Paint mPaint_CenterOther = new Paint();
    private TextPaint mPaint_Text = new TextPaint();
    private int textSize = getResources().getInteger(R.integer.size);;
    private boolean isDestroyed;

    public ViewfinderView(Context context) {
        this(context, null);
    }

    public ViewfinderView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ViewfinderView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initPaint();
        init();
    }

    private void init() {
        setZOrderOnTop(true);
        surfaceHolder = getHolder();
        surfaceHolder.setFormat(PixelFormat.TRANSLUCENT);
        surfaceHolder.addCallback(this);
    }

    private void initPaint() {
        mPaint_CenterFK.setStyle(Paint.Style.STROKE);
        mPaint_CenterFK.setStrokeWidth(3);
        mPaint_CenterFK.setColor(Color.WHITE);
        mPaint_CenterFK.setAntiAlias(true);

        mPaint_CenterBJ.setColor(Color.parseColor("#5677FC"));
        mPaint_CenterBJ.setStrokeWidth(1);
        mPaint_CenterBJ.setAntiAlias(true);
        mPaint_CenterBJ.setStyle(Paint.Style.FILL);

        mPaint_CenterOther.setStyle(Paint.Style.FILL);
        mPaint_CenterOther.setStrokeWidth(1);
        mPaint_CenterOther.setColor(Color.BLACK);
        mPaint_CenterOther.setAntiAlias(true);
        mPaint_CenterOther.setAlpha(64);

        mPaint_Text.setColor(Color.WHITE);
        mPaint_Text.setTextSize(textSize);
        mPaint_Text.setTextAlign(Paint.Align.CENTER);
    }

    public void draw(Canvas canvas) {
        canvas.drawColor(0,android.graphics.PorterDuff.Mode.CLEAR);
        drawCenter(canvas);
        drawOther(canvas);
        drawText(canvas);
    }

    private void drawLine(Canvas canvas) {
        for(int i=0;i<4;++i) {
            if (mRect_Line.top <= mRect.top + 20)
                mDown = 1;
            else if (mRect_Line.top >= mRect.bottom - 20)
                mDown = -1;
            mRect_Line.offset(0, mDown);
            canvas.drawRect(mRect_Line, mPaint_CenterBJ);
        }
    }

    private void drawCenter(Canvas canvas) {
        Path path = new Path();
        path.moveTo(mRect.left, mRect.top);
        path.lineTo(mRect.right, mRect.top);
        path.lineTo(mRect.right, mRect.bottom);
        path.lineTo(mRect.left, mRect.bottom);
        path.lineTo(mRect.left, mRect.top);
        path.close();
        canvas.drawPath(path, mPaint_CenterFK);

        path = new Path();
        int lineWidget = mRect.width() / 5, lineHeight = mHeight >> 7;
        path.moveTo(mRect.left, mRect.top);
        path.lineTo(mRect.left, mRect.top + lineWidget);
        path.lineTo(mRect.left - lineHeight, mRect.top + lineWidget);
        path.lineTo(mRect.left - lineHeight, mRect.top - lineHeight);
        path.lineTo(mRect.left + lineWidget, mRect.top - lineHeight);
        path.lineTo(mRect.left + lineWidget, mRect.top);
        path.lineTo(mRect.left, mRect.top);
        path.close();

        path.moveTo(mRect.right, mRect.top);
        path.lineTo(mRect.right, mRect.top + lineWidget);
        path.lineTo(mRect.right + lineHeight, mRect.top + lineWidget);
        path.lineTo(mRect.right + lineHeight, mRect.top - lineHeight);
        path.lineTo(mRect.right - lineWidget, mRect.top - lineHeight);
        path.lineTo(mRect.right - lineWidget, mRect.top);
        path.lineTo(mRect.right, mRect.top);
        path.close();

        path.moveTo(mRect.right, mRect.bottom);
        path.lineTo(mRect.right - lineWidget, mRect.bottom);
        path.lineTo(mRect.right - lineWidget, mRect.bottom + lineHeight);
        path.lineTo(mRect.right + lineHeight, mRect.bottom + lineHeight);
        path.lineTo(mRect.right + lineHeight, mRect.bottom - lineWidget);
        path.lineTo(mRect.right, mRect.bottom - lineWidget);
        path.lineTo(mRect.right, mRect.bottom);
        path.close();

        path.moveTo(mRect.left, mRect.bottom);
        path.lineTo(mRect.left + lineWidget, mRect.bottom);
        path.lineTo(mRect.left + lineWidget, mRect.bottom + lineHeight);
        path.lineTo(mRect.left - lineHeight, mRect.bottom + lineHeight);
        path.lineTo(mRect.left - lineHeight, mRect.bottom - lineWidget);
        path.lineTo(mRect.left, mRect.bottom - lineWidget);
        path.lineTo(mRect.left, mRect.bottom);
        path.close();

        canvas.drawPath(path, mPaint_CenterBJ);

        drawLine(canvas);
    }

    private void drawOther(Canvas canvas) {
        Path path = new Path();

        path.lineTo(0, 0);
        path.lineTo(mWidget, 0);
        path.lineTo(mWidget, mRect.top);
        path.lineTo(0, mRect.top);
        path.lineTo(0, 0);
        path.close();

        path.moveTo(mRect.right, mRect.top);
        path.lineTo(mWidget, mRect.top);
        path.lineTo(mWidget, mRect.bottom);
        path.lineTo(mRect.right, mRect.bottom);
        path.lineTo(mRect.right, mRect.top);
        path.close();

        path.moveTo(0, mRect.bottom);
        path.lineTo(mWidget, mRect.bottom);
        path.lineTo(mWidget, mHeight);
        path.lineTo(0, mHeight);
        path.lineTo(0, mRect.bottom);
        path.close();

        path.moveTo(0, mRect.top);
        path.lineTo(mRect.left, mRect.top);
        path.lineTo(mRect.left, mRect.bottom);
        path.lineTo(0, mRect.bottom);
        path.lineTo(0, mRect.top);
        path.close();

        canvas.drawPath(path, mPaint_CenterOther);
    }

    private void drawText(Canvas canvas) {
        String pointStr = getResources().getString(R.string.scan_text_point);
        int textLength = textSize * pointStr.split(" ")[0].length();
        StaticLayout layout = new StaticLayout(pointStr, mPaint_Text, textLength, Layout.Alignment.ALIGN_NORMAL, 1.0F, 0.0F, true);
        canvas.translate(mRect.centerX(),
                mRect.centerY() + mRect.height() / 2 + mHeight / 20);
        layout.draw(canvas);
    }

    @Override
    public void surfaceRedrawNeeded(SurfaceHolder surfaceHolder) {

    }

    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
        if (mWidget == 0) {
            mWidget = getWidth();
            mHeight = getHeight();
            int minWidget = mWidget < mHeight ? mWidget : mHeight;
            int widget = minWidget / 4;
            mRect = new Rect(mWidget / 2 - widget, mHeight / 2 - widget, mWidget / 2 + widget, mHeight / 2 + widget);
            ScanQRCode.mRect = new Rect(mRect);
            int widget_line = mRect.width() * 9 / 10;
            mRect_Line = new Rect(mRect.centerX() - widget_line / 2,
                    mRect.top + 20,
                    mRect.centerX() + widget_line / 2,
                    mRect.top + 20 + 5);
        }
        new Thread(this).start();
    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
        isDestroyed = true;
        Thread.interrupted();
    }

    @Override
    public void run() {
        while (!isDestroyed) {
            try {
                Canvas canvas = surfaceHolder.lockCanvas(new Rect(mRect));
                if(canvas != null) draw(canvas);
                surfaceHolder.unlockCanvasAndPost(canvas);
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                Thread.sleep(33);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}