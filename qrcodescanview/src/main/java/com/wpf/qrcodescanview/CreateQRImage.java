package com.wpf.qrcodescanview;

import android.graphics.Bitmap;
import android.os.AsyncTask;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.Hashtable;

public abstract class CreateQRImage extends AsyncTask<String,Integer,Bitmap> {

	private int QR_WIDTH = 256, QR_HEIGHT = 256;

	private Bitmap createQRImage(String url, int QR_WIDTH, int QR_HEIGHT) {
		Bitmap bitmap = null;
		try {
			if (url == null || "".equals(url) || url.length() < 1) return null;
			Hashtable<EncodeHintType, String> hints = new Hashtable<>();
			hints.put(EncodeHintType.CHARACTER_SET, "utf-8");
			BitMatrix bitMatrix = new QRCodeWriter()
					.encode(url, BarcodeFormat.QR_CODE, QR_WIDTH, QR_HEIGHT, hints);
			int[] pixels = new int[QR_WIDTH * QR_HEIGHT];
			for (int y = 0; y < QR_HEIGHT; y++)
				for (int x = 0; x < QR_WIDTH; x++)
					pixels[y * QR_WIDTH + x] = bitMatrix.get(x, y)?0xff000000:0xffffffff;
			bitmap = Bitmap.createBitmap(QR_WIDTH, QR_HEIGHT, Bitmap.Config.ARGB_8888);
			bitmap.setPixels(pixels, 0, QR_WIDTH, 0, 0, QR_WIDTH, QR_HEIGHT);
		}
		catch (WriterException e) {
			e.printStackTrace();
		}
		return bitmap;
	}

	protected CreateQRImage(String url) {
		this.execute(url);
	}

	protected CreateQRImage(String url,int QR_WIDTH,int QR_HEIGHT) {
		this.QR_WIDTH = QR_WIDTH;
		this.QR_HEIGHT = QR_HEIGHT;
		this.execute(url);
	}

	@Override
	protected void onPostExecute(Bitmap bitmap) {
		onFinish(bitmap);
	}

	@Override
	protected Bitmap doInBackground(String... strings) {
		return createQRImage(strings[0],QR_WIDTH,QR_HEIGHT);
	}

	public static boolean saveBitmapToSD(Bitmap bitmap, String filePath, String fileName) {
		try {
			FileOutputStream fos = new FileOutputStream(filePath + fileName);
			bitmap.compress(Bitmap.CompressFormat.JPEG,100,fos);
			return true;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		return false;
	}

	public abstract void onFinish(Bitmap bitmap);
}
