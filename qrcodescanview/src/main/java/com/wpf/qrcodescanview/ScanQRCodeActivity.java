package com.wpf.qrcodescanview;

import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.wpf.qrcodescanview.Listener.OnFinishListener;
import com.wpf.qrcodescanview.View.ScanQRCode;

/**
 * onActivityResult
 * ResultString->Result
 * Bitmap->Bitmap
 */

public class ScanQRCodeActivity extends AppCompatActivity implements
        OnFinishListener {

    private ScanQRCode scanQRCode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sacn_qrcode);
        scanQRCode = (ScanQRCode) findViewById(R.id.cameraView);
        scanQRCode.setOnFinishListener(this);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        scanQRCode.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public void onFinish(String result, Bitmap bitmap) {
        Bundle bundle = new Bundle();
        bundle.putString("ResultString",result);
        bundle.putParcelable("Bitmap",bitmap);
        setResult(RESULT_OK,getIntent().putExtra("data",bundle));
        finish();
    }
}
