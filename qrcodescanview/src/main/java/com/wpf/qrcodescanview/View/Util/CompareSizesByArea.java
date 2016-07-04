package com.wpf.qrcodescanview.View.Util;

import android.annotation.TargetApi;
import android.os.Build;
import android.util.Size;

import java.util.Comparator;

@TargetApi(Build.VERSION_CODES.LOLLIPOP)
public class CompareSizesByArea implements Comparator<Size> {
    @Override
    public int compare(Size lhs, Size rhs) {
        // We cast here to ensure the multiplications won't overflow
        return Long.signum((long) lhs.getWidth() * lhs.getHeight() -
                (long) rhs.getWidth() * rhs.getHeight());
    }

}
