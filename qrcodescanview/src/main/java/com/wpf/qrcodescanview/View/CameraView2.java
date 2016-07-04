package com.wpf.qrcodescanview.View;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.ImageFormat;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraDevice.StateCallback;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.Image;
import android.media.ImageReader;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.AttributeSet;
import android.util.Size;
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
import com.wpf.qrcodescanview.View.Util.CompareSizesByArea;
import com.wpf.requestpermission.RequestPermission;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Collections;

/**
 * Created by 王朋飞 on 6-20-0020.
 * 摄像头View---5.0以上
 */

@TargetApi(Build.VERSION_CODES.LOLLIPOP)
abstract class CameraView2 extends SurfaceView implements
        SurfaceHolder.Callback2 {

    private RequestPermission requestPermission;
    private SurfaceHolder surfaceHolder;
    private CameraManager mCameraManager;
    private CameraDevice mCameraDevice;
    private CaptureRequest.Builder previewBuilder;
    private Handler handler = new Handler(Looper.getMainLooper());
    private CameraCaptureSession mSession;
    private QRCodeReader qrCodeReader = new QRCodeReader();
    private ImageReader mImageReader;
    private CameraDevice.StateCallback stateCallback = new StateCallback() {
        @Override
        public void onOpened(@NonNull CameraDevice cameraDevice) {
            mCameraDevice = cameraDevice;
            createCameraCaptureSession();
        }

        @Override
        public void onDisconnected(@NonNull CameraDevice cameraDevice) {
            cameraDevice.close();
            mCameraDevice = null;
        }

        @Override
        public void onError(@NonNull CameraDevice cameraDevice, int i) {

        }
    };
    private CameraCaptureSession.StateCallback mSessionPreviewStateCallback = new
            CameraCaptureSession.StateCallback() {

                @Override
                public void onConfigured(@NonNull CameraCaptureSession session) {
                    try {
                        mSession = session;
                        previewBuilder.set(CaptureRequest.CONTROL_AF_MODE,
                                CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);
                        previewBuilder.set(CaptureRequest.CONTROL_AE_MODE,
                                CaptureRequest.CONTROL_AE_MODE_ON_AUTO_FLASH);
                        session.setRepeatingRequest(previewBuilder.build(), mSessionCaptureCallback, handler);
                    } catch (CameraAccessException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onConfigureFailed(@NonNull CameraCaptureSession cameraCaptureSession) {
                    cameraCaptureSession.close();
                }
            };
    private CameraCaptureSession.CaptureCallback mSessionCaptureCallback =
            new CameraCaptureSession.CaptureCallback() {
                @Override
                public void onCaptureCompleted(@NonNull CameraCaptureSession session,
                                               @NonNull CaptureRequest request,
                                               @NonNull TotalCaptureResult result) {
                    mSession = session;
                }
            };
    private ImageReader.OnImageAvailableListener onImageAvailableListener =
            new ImageReader.OnImageAvailableListener() {
                @Override
                public void onImageAvailable(ImageReader imageReader) {
                    scan(imageReader);
                }
            };

    private void scan(ImageReader imageReader) {
        Image image = imageReader.acquireLatestImage();
        if (ScanQRCode.mRect != null) {
            if (image == null) return;
            Image.Plane[] planes = image.getPlanes();
            if (planes == null) return;
            if (planes.length >= 1 && planes[0] == null) return;
            ByteBuffer byteBuffer = planes[0].getBuffer();
            byte[] data = new byte[byteBuffer.capacity()];
            byteBuffer.get(data);
            int imageWidth = image.getWidth(), imageHeight = image.getHeight();
            int left = ScanQRCode.mRect.left * imageWidth / getWidth();
            int top = ScanQRCode.mRect.top * imageHeight / getHeight();
            int width = (int) (ScanQRCode.mRect.width() * imageWidth * 1.5) / getWidth();
            int height = (int) (ScanQRCode.mRect.height() * imageHeight * 1.5) / getHeight();
            PlanarYUVLuminanceSource source =
                    new PlanarYUVLuminanceSource(data, imageWidth, imageHeight,
                            left, top, width, height, false);
            BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));
            String rawResult = getDecodeResult(bitmap);
            if (!rawResult.isEmpty()) onSuccess(rawResult, null);
        }
        image.close();
        try {
            if (mSession != null) {
                previewBuilder.removeTarget(mImageReader.getSurface());
                mSession.setRepeatingRequest(previewBuilder.build(),
                        new CameraCaptureSession.CaptureCallback() {
                        }, handler);
            }
        } catch (CameraAccessException e) {
            e.printStackTrace();
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

//    private Bitmap getBitmap(byte[] yuvData,int dataWidth,int left,int top,PlanarYUVLuminanceSource source) {
//        int width = getWidth();
//        int height = getHeight();
//        int[] pixels = new int[width * height];
//        byte[] yuv = yuvData;
//        int inputOffset = top * dataWidth + left;
//
//        for (int y = 0; y < height; y++) {
//            int outputOffset = y * width;
//            for (int x = 0; x < width; x++) {
//                int grey = yuv[inputOffset + x] & 0xff;
//                pixels[outputOffset + x] = 0xFF000000 | (grey * 0x00010101);
//            }
//            inputOffset += dataWidth;
//        }
//
//        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
//        bitmap.setPixels(pixels, 0, width, 0, 0, width, height);
//        return bitmap;
//    }

    public CameraView2(Context context) {
        this(context, null);
    }

    public CameraView2(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CameraView2(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void onDestroy() {
        if (mSession != null) {
            mSession.close();
            mSession = null;
        }
        if (mCameraDevice != null) {
            mCameraDevice.close();
            mCameraDevice = null;
        }
    }

    private void init() {
        surfaceHolder = getHolder();
        surfaceHolder.addCallback(this);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void initCamera() {
        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) return;
        try {
            mCameraManager = (CameraManager) getContext().getSystemService(Context.CAMERA_SERVICE);
            Size imageSize = getImageSize();
            if (imageSize == null) return;
            mImageReader = ImageReader.newInstance(imageSize.getWidth(), imageSize.getHeight(),
                    ImageFormat.YUV_420_888, 7);
            mImageReader.setOnImageAvailableListener(onImageAvailableListener, handler);
            mCameraManager.openCamera(String.valueOf(CameraCharacteristics.LENS_FACING_FRONT),
                    stateCallback, handler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private void createCameraCaptureSession() {
        try {
            previewBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            previewBuilder.addTarget(surfaceHolder.getSurface());
            previewBuilder.set(CaptureRequest.JPEG_ORIENTATION,90);
            mCameraDevice.createCaptureSession(
                    Arrays.asList(surfaceHolder.getSurface(),mImageReader.getSurface()),
                    mSessionPreviewStateCallback, handler);
            scamImage();
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private Size getImageSize() {
        Size largest = null;
        try {
            String cameraId = String.valueOf(CameraCharacteristics.LENS_FACING_FRONT);
            CameraCharacteristics characteristics = mCameraManager.getCameraCharacteristics(cameraId);
            StreamConfigurationMap map = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
            assert map != null;
            largest = Collections.max(Arrays.asList(map.getOutputSizes(ImageFormat.YUV_420_888)), new CompareSizesByArea());
        } catch (CameraAccessException | IllegalArgumentException e) {
            e.printStackTrace();
            new AlertDialog.Builder(getContext())
                    .setMessage("当前摄像头不可用,请退出")
                    .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            ((AppCompatActivity)getContext()).finish();
                        }
                    }).show();
        }
        return largest;
    }

    private void scamImage() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (mCameraDevice != null) {
                    try {
                        Thread.sleep(300);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    try {
                        if(mSession != null) {
                            previewBuilder.addTarget(mImageReader.getSurface());
                            mSession.setRepeatingRequest(previewBuilder.build(),
                                    new CameraCaptureSession.CaptureCallback() {}, handler);
                        }
                    } catch (CameraAccessException e) {
                        e.printStackTrace();
                    }
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }

    @Override
    public void surfaceRedrawNeeded(SurfaceHolder surfaceHolder) {

    }

    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
        requestPermission = new RequestPermission((AppCompatActivity) getContext(),
                new String[]{Manifest.permission.CAMERA,
                Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1) {

            @Override
            public void onSuccess() {
                initCamera();
            }

            @Override
            public void onFail(String[] strings) {

            }
        };
    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
        onDestroy();
    }

    void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        requestPermission.onRequestPermissionsResult(requestCode,permissions,grantResults);
    }

    public abstract void onSuccess(String result, Bitmap bitmap);
}
