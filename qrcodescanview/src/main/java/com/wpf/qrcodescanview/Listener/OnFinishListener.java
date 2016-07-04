package com.wpf.qrcodescanview.Listener;

import android.graphics.Bitmap;

/**
 * Created by 王朋飞 on 6-30-0030.
 * 返回信息接口
 */

public interface OnFinishListener {
    void onFinish(String result, Bitmap bitmap);
}
