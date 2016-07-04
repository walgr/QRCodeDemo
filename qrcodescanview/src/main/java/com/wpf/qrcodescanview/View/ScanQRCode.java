package com.wpf.qrcodescanview.View;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;

import com.wpf.qrcodescanview.Listener.OnFinishListener;

/**
 * Created by 王朋飞 on 6-20-0020.
 * 二维码扫描界面
 */

public class ScanQRCode extends FrameLayout {

    public static Rect mRect;
    private CameraView cameraView;
    private CameraView2 cameraView2;
    private View viewfinderView;
    private OnFinishListener onFinishListener;

    public ScanQRCode(Context context) {
        this(context,null);
    }

    public ScanQRCode(Context context, AttributeSet attrs) {
        this(context,attrs,0);
    }

    public ScanQRCode(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs, defStyleAttr);
    }

    private void init(Context context, AttributeSet attrs, int defStyleAttr) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            cameraView2 = new CameraView2(context, attrs, defStyleAttr) {
                @Override
                public void onSuccess(String result, Bitmap bitmap) {
                    if(onFinishListener != null) onFinishListener.onFinish(result,bitmap);
                }
            };
            addView(cameraView2);
        } else {
            addView(cameraView = new CameraView(context, attrs, defStyleAttr) {
                @Override
                public void onSuccess(String result, Bitmap bitmap) {
                    if(onFinishListener != null) onFinishListener.onFinish(result,bitmap);
                }
            });
        }
        addView(viewfinderView == null ?
                new ViewfinderView(context, attrs, defStyleAttr): viewfinderView);
    }

    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        cameraView2.onRequestPermissionsResult(requestCode,permissions,grantResults);
    }

    public CameraView getCameraView() {
        return cameraView;
    }

    public CameraView2 getCameraView2() {
        return cameraView2;
    }

    public ScanQRCode setOnFinishListener(OnFinishListener onFinishListener) {
        this.onFinishListener = onFinishListener;
        return this;
    }

    public ScanQRCode setViewfinderView(View viewfinderView) {
        this.viewfinderView = viewfinderView;
        return this;
    }
}
