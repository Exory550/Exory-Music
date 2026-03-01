package com.exory550.exorymusic.util;

import android.graphics.Bitmap;

import androidx.annotation.NonNull;

public class ImageUtil {

  private ImageUtil() {}

  public static Bitmap resizeBitmap(@NonNull Bitmap src, int maxForSmallerSize) {
    int width = src.getWidth();
    int height = src.getHeight();

    final int dstWidth;
    final int dstHeight;

    if (width < height) {
      if (maxForSmallerSize >= width) {
        return src;
      }
      float ratio = (float) height / width;
      dstWidth = maxForSmallerSize;
      dstHeight = Math.round(maxForSmallerSize * ratio);
    } else {
      if (maxForSmallerSize >= height) {
        return src;
      }
      float ratio = (float) width / height;
      dstWidth = Math.round(maxForSmallerSize * ratio);
      dstHeight = maxForSmallerSize;
    }

    return Bitmap.createScaledBitmap(src, dstWidth, dstHeight, false);
  }

  public static int calculateInSampleSize(int width, int height, int reqWidth) {
    if (width < height) {
      reqWidth = (height / width) * reqWidth;
    } else {
      reqWidth = (width / height) * reqWidth;
    }

    int inSampleSize = 1;

    if (height > reqWidth || width > reqWidth) {
      final int halfHeight = height / 2;
      final int halfWidth = width / 2;

      while ((halfHeight / inSampleSize) > reqWidth && (halfWidth / inSampleSize) > reqWidth) {
        inSampleSize *= 2;
      }
    }

    return inSampleSize;
  }
}
