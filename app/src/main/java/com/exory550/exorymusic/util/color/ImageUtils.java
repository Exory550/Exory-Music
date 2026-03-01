package com.exory550.exorymusic.util.color;

import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

public class ImageUtils {
  private static final int TOLERANCE = 20;
  private static final int ALPHA_TOLERANCE = 50;
  private static final int COMPACT_BITMAP_SIZE = 64;
  private final Matrix mTempMatrix = new Matrix();
  private int[] mTempBuffer;
  private Bitmap mTempCompactBitmap;
  private Canvas mTempCompactBitmapCanvas;
  private Paint mTempCompactBitmapPaint;

  public static boolean isGrayscale(int color) {
    int alpha = 0xFF & (color >> 24);
    if (alpha < ALPHA_TOLERANCE) {
      return true;
    }
    int r = 0xFF & (color >> 16);
    int g = 0xFF & (color >> 8);
    int b = 0xFF & color;
    return Math.abs(r - g) < TOLERANCE
        && Math.abs(r - b) < TOLERANCE
        && Math.abs(g - b) < TOLERANCE;
  }

  public static Bitmap buildScaledBitmap(Drawable drawable, int maxWidth, int maxHeight) {
    if (drawable == null) {
      return null;
    }
    int originalWidth = drawable.getIntrinsicWidth();
    int originalHeight = drawable.getIntrinsicHeight();
    if ((originalWidth <= maxWidth)
        && (originalHeight <= maxHeight)
        && (drawable instanceof BitmapDrawable)) {
      return ((BitmapDrawable) drawable).getBitmap();
    }
    if (originalHeight <= 0 || originalWidth <= 0) {
      return null;
    }
    float ratio =
        Math.min(
            (float) maxWidth / (float) originalWidth, (float) maxHeight / (float) originalHeight);
    ratio = Math.min(1.0f, ratio);
    int scaledWidth = (int) (ratio * originalWidth);
    int scaledHeight = (int) (ratio * originalHeight);
    Bitmap result = Bitmap.createBitmap(scaledWidth, scaledHeight, Config.ARGB_8888);
    Canvas canvas = new Canvas(result);
    drawable.setBounds(0, 0, scaledWidth, scaledHeight);
    drawable.draw(canvas);
    return result;
  }

  public boolean isGrayscale(Bitmap bitmap) {
    int height = bitmap.getHeight();
    int width = bitmap.getWidth();

    if (height > COMPACT_BITMAP_SIZE || width > COMPACT_BITMAP_SIZE) {
      if (mTempCompactBitmap == null) {
        mTempCompactBitmap =
            Bitmap.createBitmap(COMPACT_BITMAP_SIZE, COMPACT_BITMAP_SIZE, Config.ARGB_8888);
        mTempCompactBitmapCanvas = new Canvas(mTempCompactBitmap);
        mTempCompactBitmapPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mTempCompactBitmapPaint.setFilterBitmap(true);
      }
      mTempMatrix.reset();
      mTempMatrix.setScale(
          (float) COMPACT_BITMAP_SIZE / width, (float) COMPACT_BITMAP_SIZE / height, 0, 0);
      mTempCompactBitmapCanvas.drawColor(0, PorterDuff.Mode.SRC);
      mTempCompactBitmapCanvas.drawBitmap(bitmap, mTempMatrix, mTempCompactBitmapPaint);
      bitmap = mTempCompactBitmap;
      width = height = COMPACT_BITMAP_SIZE;
    }
    final int size = height * width;
    ensureBufferSize(size);
    bitmap.getPixels(mTempBuffer, 0, width, 0, 0, width, height);
    for (int i = 0; i < size; i++) {
      if (!isGrayscale(mTempBuffer[i])) {
        return false;
      }
    }
    return true;
  }

  private void ensureBufferSize(int size) {
    if (mTempBuffer == null || mTempBuffer.length < size) {
      mTempBuffer = new int[size];
    }
  }
}
