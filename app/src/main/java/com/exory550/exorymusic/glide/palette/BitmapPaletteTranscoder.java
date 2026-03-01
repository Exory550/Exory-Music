package com.exory550.exorymusic.glide.palette;

import android.graphics.Bitmap;

import androidx.annotation.NonNull;

import com.bumptech.glide.load.Options;
import com.bumptech.glide.load.engine.Resource;
import com.bumptech.glide.load.resource.transcode.ResourceTranscoder;

import com.exory550.exorymusic.util.RetroColorUtil;

public class BitmapPaletteTranscoder implements ResourceTranscoder<Bitmap, BitmapPaletteWrapper> {

    @Override
  public Resource<BitmapPaletteWrapper> transcode(@NonNull Resource<Bitmap> toTranscode, @NonNull Options options) {
    Bitmap bitmap = toTranscode.get();
    BitmapPaletteWrapper bitmapPaletteWrapper =
            new BitmapPaletteWrapper(bitmap, RetroColorUtil.generatePalette(bitmap));
    return new BitmapPaletteResource(bitmapPaletteWrapper);
  }
}
