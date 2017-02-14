package com.wpf.qrcodescanview.View;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.hardware.Camera;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.google.zxing.BinaryBitmap;
import com.google.zxing.ChecksumException;
import com.google.zxing.FormatException;
import com.google.zxing.NotFoundException;
import com.google.zxing.PlanarYUVLuminanceSource;
import com.google.zxing.Result;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.qrcode.QRCodeReader;
import com.wpf.requestpermission.RequestPermission;
import com.wpf.requestpermission.RequestResult;

import java.io.IOException;
import java.util.List;

/**
 * Created by wpf on 6-20-0020.
 * CameraView---5.0 down
 */

public abstract class CameraView extends SurfaceView implements
        SurfaceHolder.Callback2 ,
        Camera.PreviewCallback {

    private SurfaceHolder surfaceHolder;
    private Camera camera;
    private Camera.Parameters parameters;
    private Camera.Size size;
    private QRCodeReader qrCodeReader = new QRCodeReader();

    public CameraView(Context context) {
        this(context,null);
    }

    public CameraView(Context context, AttributeSet attrs) {
        this(context,attrs,0);
    }

    public CameraView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        surfaceHolder = getHolder();
        surfaceHolder.addCallback(this);
    }

    private void initCamera() {
        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) return;
        try {
            camera = Camera.open();
            if (camera == null) return;
            setParameters();
            camera.setDisplayOrientation(90);
            camera.setPreviewDisplay(surfaceHolder);
            camera.setPreviewCallback(this);
            camera.startPreview();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void setParameters() {
        parameters = camera.getParameters();
        size = getSupportedPreviewSizes();
        parameters.setPictureSize(size.width,size.height);
        parameters.setFocusMode("auto");
        camera.setParameters(parameters);
    }

    private Camera.Size getSupportedPreviewSizes() {
        List<Camera.Size> previewSize = camera.getParameters().getSupportedPreviewSizes();
        return previewSize.get(0);
    }

    private void scan(byte[] data) {
        if (ScanQRCode.mRect != null) {
            int imageWidth = size.width, imageHeight = size.height;
            int left = (imageWidth - ScanQRCode.mRect.width())/2;
            int top = (imageHeight - ScanQRCode.mRect.height())/2;
            int width = ScanQRCode.mRect.width();
            int height = ScanQRCode.mRect.height();
            PlanarYUVLuminanceSource source =
                    new PlanarYUVLuminanceSource(data, imageWidth, imageHeight,
                            left, top, width, height, false);
            BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));
            String rawResult = getDecodeResult(bitmap);
            if (!rawResult.isEmpty()) onSuccess(rawResult, null);
        }
    }

    private String getDecodeResult(BinaryBitmap bitmap) {
        Result rawResult = null;
        try {
            rawResult = qrCodeReader.decode(bitmap);
        } catch (NotFoundException | ChecksumException | FormatException ignored) {
        } finally {
            qrCodeReader.reset();
        }
        return rawResult == null ? "" : rawResult.getText();
    }

    private void close() {
        try {
            camera.reconnect();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void surfaceRedrawNeeded(SurfaceHolder surfaceHolder) {

    }

    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
        RequestPermission.request((AppCompatActivity) getContext(),
                new String[]{Manifest.permission.CAMERA}, 1, new RequestResult() {
                    @Override
                    public void onSuccess() {
                        initCamera();
                    }

                    @Override
                    public void onFail(String[] failList) {

                    }
                });
    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
        close();
    }

    @Override
    public void onPreviewFrame(byte[] bytes, Camera camera) {
        scan(bytes);
    }

    void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        RequestPermission.onRequestPermissionsResult(requestCode,permissions,grantResults);
    }

    public abstract void onSuccess(String result, Bitmap bitmap);
}
