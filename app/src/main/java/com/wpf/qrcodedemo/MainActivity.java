package com.wpf.qrcodedemo;

import android.Manifest;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.wpf.qrcodescanview.CreateQRImage;
import com.wpf.qrcodescanview.ScanQRCodeActivity;
import com.wpf.requestpermission.RequestPermissionActivity;
import com.wpf.requestpermission.RequestResult;

import static com.wpf.qrcodescanview.CreateQRImage.saveBitmapToSD;


public class MainActivity extends RequestPermissionActivity {

    private int requestCode = 100;
    private TextView textView;
    private ImageView imageView;
    private Button button;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        textView = (TextView) findViewById(R.id.textView);
        imageView = (ImageView) findViewById(R.id.imageView);
        button = (Button) findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivityForResult(new Intent(MainActivity.this, ScanQRCodeActivity.class),requestCode);
            }
        });
    }

    private void request(final Bitmap bitmap) {
        request(this, Manifest.permission.WRITE_EXTERNAL_STORAGE, 1, new RequestResult() {
            @Override
            public void onSuccess() {
                saveBitmapToSD(bitmap, Environment.getExternalStorageDirectory().getPath()+"/","QRCode.jpg");
            }

            @Override
            public void onFail(String[] failList) {

            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == this.requestCode && resultCode == RESULT_OK) {
            String result = data.getStringExtra("ResultString");
            textView.setText(result);
            new CreateQRImage(result) {
                @Override
                public void onFinish(Bitmap bitmap) {
                    imageView.setImageBitmap(bitmap);
                    if(bitmap != null) request(bitmap);
                }
            };
        }
    }
}
