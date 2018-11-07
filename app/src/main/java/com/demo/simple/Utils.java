package com.demo.simple;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Parcel;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.view.View;

/**
 * Created by Lisen.Liu on 2018/11/2.
 */

public class Utils {

    private static Canvas sCanvas = new Canvas();
    public static Bitmap getViewSnapshot(View v, int alpha) {
        if (v == null) {
            return null;
        }
        v.setDrawingCacheEnabled(true);
        v.buildDrawingCache();
        Bitmap bitmap = v.getDrawingCache();
        //return Bitmap.createBitmap(bitmap);

        Bitmap result = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        sCanvas.setBitmap(result);
        Paint paint = new Paint();
        paint.setAlpha(alpha);
        sCanvas.drawBitmap(bitmap, 0, 0, paint);
        return result;
    }

    public static void vibrate(Context context) {
        Vibrator vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
        vibrator.vibrate(new long[] {100, 100}, -1);
    }
}
