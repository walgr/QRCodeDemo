package com.wpf.qrcodedemo;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.wpf.qrcodescanview.ScanQRCodeActivity;


public class MainActivity extends AppCompatActivity {

    private int requestCode = 100;
    private Button button;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        button = (Button) findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivityForResult(new Intent(MainActivity.this, ScanQRCodeActivity.class),requestCode);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == this.requestCode && resultCode == RESULT_OK) {
            Bundle bundle = data.getBundleExtra("data");
            String result = bundle.getString("ResultString");
            Bitmap bitmap = bundle.getParcelable("Bitmap");
            Toast.makeText(this,result,Toast.LENGTH_LONG).show();
        }
    }
}
