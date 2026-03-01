package com.exory550.exorymusic.util.color;

import android.app.Notification;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.BackgroundColorSpan;
import android.text.style.CharacterStyle;
import android.text.style.ForegroundColorSpan;
import android.text.style.TextAppearanceSpan;
import android.util.Log;
import android.util.Pair;

import androidx.annotation.ColorInt;
import androidx.annotation.FloatRange;
import androidx.annotation.IntRange;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import java.util.Locale;
import java.util.WeakHashMap;

public class NotificationColorUtil {

  private static final String TAG = "NotificationColorUtil";
  private static final boolean DEBUG = false;

  private static final Object sLock = new Object();
  private static NotificationColorUtil sInstance;

  private final ImageUtils mImageUtils = new ImageUtils();
  private final WeakHashMap<Bitmap, Pair<Boolean, Integer>> mGrayscaleBitmapCache =
          new WeakHashMap<>();

  private final int mGrayscaleIconMaxSize;

  private NotificationColorUtil(Context context) {
    mGrayscaleIconMaxSize =
        context.getResources().getDimensionPixelSize(androidx.core.R.dimen.notification_large_icon_width);
  }

  public static NotificationColorUtil getInstance(Context context) {
    synchronized (sLock) {
      if (sInstance == null) {
        sInstance = new NotificationColorUtil(context);
      }
      return sInstance;
    }
  }

  public static CharSequence clearColorSpans(CharSequence charSequence) {
    if (charSequence instanceof Spanned) {
      Spanned ss = (Spanned) charSequence;
      Object[] spans = ss.getSpans(0, ss.length(), Object.class);
      SpannableStringBuilder builder = new SpannableStringBuilder(ss.toString());
      for (Object span : spans) {
        Object resultSpan = span;
        if (resultSpan instanceof CharacterStyle) {
          resultSpan = ((CharacterStyle) span).getUnderlying();
        }
        if (resultSpan instanceof TextAppearanceSpan) {
          TextAppearanceSpan originalSpan = (TextAppearanceSpan) resultSpan;
          if (originalSpan.getTextColor() != null) {
            resultSpan =
                new TextAppearanceSpan(
                    originalSpan.getFamily(),
                    originalSpan.getTextStyle(),
                    originalSpan.getTextSize(),
                    null,
                    originalSpan.getLinkTextColor());
          }
        } else if (resultSpan instanceof ForegroundColorSpan
            || (resultSpan instanceof BackgroundColorSpan)) {
          continue;
        } else {
          resultSpan = span;
        }
        builder.setSpan(
            resultSpan, ss.getSpanStart(span), ss.getSpanEnd(span), ss.getSpanFlags(span));
      }
      return builder;
    }
    return charSequence;
  }

  public static int findContrastColor(int color, int other, boolean findFg, double minRatio) {
    int fg = findFg ? color : other;
    int bg = findFg ? other : color;
    if (ColorUtilsFromCompat.calculateContrast(fg, bg) >= minRatio) {
      return color;
    }

    double[] lab = new double[3];
    ColorUtilsFromCompat.colorToLAB(findFg ? fg : bg, lab);

    double low = 0, high = lab[0];
    final double a = lab[1], b = lab[2];
    for (int i = 0; i < 15 && high - low > 0.00001; i++) {
      final double l = (low + high) / 2;
      if (findFg) {
        fg = ColorUtilsFromCompat.LABToColor(l, a, b);
      } else {
        bg = ColorUtilsFromCompat.LABToColor(l, a, b);
      }
      if (ColorUtilsFromCompat.calculateContrast(fg, bg) > minRatio) {
        low = l;
      } else {
        high = l;
      }
    }
    return ColorUtilsFromCompat.LABToColor(low, a, b);
  }

  public static int findAlphaToMeetContrast(int color, int backgroundColor, double minRatio) {
    int fg = color;
    int bg = backgroundColor;
    if (ColorUtilsFromCompat.calculateContrast(fg, bg) >= minRatio) {
      return color;
    }
    int startAlpha = Color.alpha(color);
    int r = Color.red(color);
    int g = Color.green(color);
    int b = Color.blue(color);

    int low = startAlpha, high = 255;
    for (int i = 0; i < 15 && high - low > 0; i++) {
      final int alpha = (low + high) / 2;
      fg = Color.argb(alpha, r, g, b);
      if (ColorUtilsFromCompat.calculateContrast(fg, bg) > minRatio) {
        high = alpha;
      } else {
        low = alpha;
      }
    }
    return Color.argb(high, r, g, b);
  }

  public static int findContrastColorAgainstDark(
      int color, int other, boolean findFg, double minRatio) {
    int fg = findFg ? color : other;
    int bg = findFg ? other : color;
    if (ColorUtilsFromCompat.calculateContrast(fg, bg) >= minRatio) {
      return color;
    }

    float[] hsl = new float[3];
    ColorUtilsFromCompat.colorToHSL(findFg ? fg : bg, hsl);

    float low = hsl[2], high = 1;
    for (int i = 0; i < 15 && high - low > 0.00001; i++) {
      final float l = (low + high) / 2;
      hsl[2] = l;
      if (findFg) {
        fg = ColorUtilsFromCompat.HSLToColor(hsl);
      } else {
        bg = ColorUtilsFromCompat.HSLToColor(hsl);
      }
      if (ColorUtilsFromCompat.calculateContrast(fg, bg) > minRatio) {
        high = l;
      } else {
        low = l;
      }
    }
    return findFg ? fg : bg;
  }

  public static int ensureTextContrastOnBlack(int color) {
    return findContrastColorAgainstDark(color, Color.BLACK, true, 12);
  }

  public static int ensureLargeTextContrast(int color, int bg, boolean isBgDarker) {
    return isBgDarker
        ? findContrastColorAgainstDark(color, bg, true, 3)
        : findContrastColor(color, bg, true, 3);
  }

  private static int ensureTextContrast(int color, int bg, boolean isBgDarker) {
    return isBgDarker
        ? findContrastColorAgainstDark(color, bg, true, 4.5)
        : findContrastColor(color, bg, true, 4.5);
  }

  public static int ensureTextBackgroundColor(int color, int textColor, int hintColor) {
    color = findContrastColor(color, hintColor, false, 3.0);
    return findContrastColor(color, textColor, false, 4.5);
  }

  private static String contrastChange(int colorOld, int colorNew, int bg) {
    return String.format(Locale.ROOT,
        "from %.2f:1 to %.2f:1",
        ColorUtilsFromCompat.calculateContrast(colorOld, bg),
        ColorUtilsFromCompat.calculateContrast(colorNew, bg));
  }

  public static int changeColorLightness(int baseColor, int amount) {
    final double[] result = ColorUtilsFromCompat.getTempDouble3Array();
    ColorUtilsFromCompat.colorToLAB(baseColor, result);
    result[0] = Math.max(Math.min(100, result[0] + amount), 0);
    return ColorUtilsFromCompat.LABToColor(result[0], result[1], result[2]);
  }

  public static int resolvePrimaryColor(Context context, int backgroundColor) {
    boolean useDark = shouldUseDark(backgroundColor);
    return ContextCompat.getColor(context, android.R.color.primary_text_light);
  }

  public static int resolveSecondaryColor(Context context, int backgroundColor) {
    boolean useDark = shouldUseDark(backgroundColor);
    if (useDark) {
      return ContextCompat.getColor(context, android.R.color.secondary_text_light);
    } else {
      return ContextCompat.getColor(context, android.R.color.secondary_text_dark);
    }
  }

  public static int resolveActionBarColor(Context context, int backgroundColor) {
    if (backgroundColor == Notification.COLOR_DEFAULT) {
      return Color.BLACK;
    }
    return getShiftedColor(backgroundColor, 7);
  }

  public static int resolveColor(Context context, int color) {
    if (color == Notification.COLOR_DEFAULT) {
      return ContextCompat.getColor(context, android.R.color.background_dark);
    }
    return color;
  }

  public static int getShiftedColor(int color, int amount) {
    final double[] result = ColorUtilsFromCompat.getTempDouble3Array();
    ColorUtilsFromCompat.colorToLAB(color, result);
    if (result[0] >= 4) {
      result[0] = Math.max(0, result[0] - amount);
    } else {
      result[0] = Math.min(100, result[0] + amount);
    }
    return ColorUtilsFromCompat.LABToColor(result[0], result[1], result[2]);
  }

  public static int resolveAmbientColor(Context context, int notificationColor) {
    final int resolvedColor = resolveColor(context, notificationColor);

    int color = resolvedColor;
    color = ensureTextContrastOnBlack(color);

    if (color != resolvedColor) {
      if (DEBUG) {
        Log.w(
            TAG,
            String.format(
                "Ambient contrast of notification for %s is %s (over black)"
                    + " by changing #%s to #%s",
                context.getPackageName(),
                contrastChange(resolvedColor, color, Color.BLACK),
                Integer.toHexString(resolvedColor),
                Integer.toHexString(color)));
      }
    }
    return color;
  }

  private static boolean shouldUseDark(int backgroundColor) {
    boolean useDark = backgroundColor == Notification.COLOR_DEFAULT;
    if (!useDark) {
      useDark = ColorUtilsFromCompat.calculateLuminance(backgroundColor) > 0.5;
    }
    return useDark;
  }

  public static double calculateLuminance(int backgroundColor) {
    return ColorUtilsFromCompat.calculateLuminance(backgroundColor);
  }

  public static double calculateContrast(int foregroundColor, int backgroundColor) {
    return ColorUtilsFromCompat.calculateContrast(foregroundColor, backgroundColor);
  }

  public static boolean satisfiesTextContrast(int backgroundColor, int foregroundColor) {
    return calculateContrast(foregroundColor, backgroundColor) >= 4.5;
  }

  public static int compositeColors(int foreground, int background) {
    return ColorUtilsFromCompat.compositeColors(foreground, background);
  }

  public static boolean isColorLight(int backgroundColor) {
    return calculateLuminance(backgroundColor) > 0.5f;
  }

  public boolean isGrayscaleIcon(Bitmap bitmap) {
    if (bitmap.getWidth() > mGrayscaleIconMaxSize || bitmap.getHeight() > mGrayscaleIconMaxSize) {
      return false;
    }

    synchronized (sLock) {
      Pair<Boolean, Integer> cached = mGrayscaleBitmapCache.get(bitmap);
      if (cached != null) {
        if (cached.second == bitmap.getGenerationId()) {
          return cached.first;
        }
      }
    }
    boolean result;
    int generationId;
    synchronized (mImageUtils) {
      result = mImageUtils.isGrayscale(bitmap);
      generationId = bitmap.getGenerationId();
    }
    synchronized (sLock) {
      mGrayscaleBitmapCache.put(bitmap, Pair.create(result, generationId));
    }
    return result;
  }

  private int processColor(int color) {
    return Color.argb(
        Color.alpha(color),
        255 - Color.red(color),
        255 - Color.green(color),
        255 - Color.blue(color));
  }

  private static class ColorUtilsFromCompat {
    private static final double XYZ_WHITE_REFERENCE_X = 95.047;
    private static final double XYZ_WHITE_REFERENCE_Y = 100;
    private static final double XYZ_WHITE_REFERENCE_Z = 108.883;
    private static final double XYZ_EPSILON = 0.008856;
    private static final double XYZ_KAPPA = 903.3;

    private static final int MIN_ALPHA_SEARCH_MAX_ITERATIONS = 10;
    private static final int MIN_ALPHA_SEARCH_PRECISION = 1;

    private static final ThreadLocal<double[]> TEMP_ARRAY = new ThreadLocal<>();

    private ColorUtilsFromCompat() {}

    public static int compositeColors(@ColorInt int foreground, @ColorInt int background) {
      int bgAlpha = Color.alpha(background);
      int fgAlpha = Color.alpha(foreground);
      int a = compositeAlpha(fgAlpha, bgAlpha);

      int r = compositeComponent(Color.red(foreground), fgAlpha, Color.red(background), bgAlpha, a);
      int g =
          compositeComponent(Color.green(foreground), fgAlpha, Color.green(background), bgAlpha, a);
      int b =
          compositeComponent(Color.blue(foreground), fgAlpha, Color.blue(background), bgAlpha, a);

      return Color.argb(a, r, g, b);
    }

    private static int compositeAlpha(int foregroundAlpha, int backgroundAlpha) {
      return 0xFF - (((0xFF - backgroundAlpha) * (0xFF - foregroundAlpha)) / 0xFF);
    }

    private static int compositeComponent(int fgC, int fgA, int bgC, int bgA, int a) {
      if (a == 0) return 0;
      return ((0xFF * fgC * fgA) + (bgC * bgA * (0xFF - fgA))) / (a * 0xFF);
    }

    @FloatRange(from = 0.0, to = 1.0)
    public static double calculateLuminance(@ColorInt int color) {
      final double[] result = getTempDouble3Array();
      colorToXYZ(color, result);
      return result[1] / 100;
    }

    public static double calculateContrast(@ColorInt int foreground, @ColorInt int background) {
      if (Color.alpha(background) != 255) {
        Log.wtf(TAG, "background can not be translucent: #" + Integer.toHexString(background));
      }
      if (Color.alpha(foreground) < 255) {
        foreground = compositeColors(foreground, background);
      }

      final double luminance1 = calculateLuminance(foreground) + 0.05;
      final double luminance2 = calculateLuminance(background) + 0.05;

      return Math.max(luminance1, luminance2) / Math.min(luminance1, luminance2);
    }

    public static void colorToLAB(@ColorInt int color, @NonNull double[] outLab) {
      RGBToLAB(Color.red(color), Color.green(color), Color.blue(color), outLab);
    }

    public static void RGBToLAB(
        @IntRange(from = 0x0, to = 0xFF) int r,
        @IntRange(from = 0x0, to = 0xFF) int g,
        @IntRange(from = 0x0, to = 0xFF) int b,
        @NonNull double[] outLab) {
      RGBToXYZ(r, g, b, outLab);
      XYZToLAB(outLab[0], outLab[1], outLab[2], outLab);
    }

    public static void colorToXYZ(@ColorInt int color, @NonNull double[] outXyz) {
      RGBToXYZ(Color.red(color), Color.green(color), Color.blue(color), outXyz);
    }

    public static void RGBToXYZ(
        @IntRange(from = 0x0, to = 0xFF) int r,
        @IntRange(from = 0x0, to = 0xFF) int g,
        @IntRange(from = 0x0, to = 0xFF) int b,
        @NonNull double[] outXyz) {
      if (outXyz.length != 3) {
        throw new IllegalArgumentException("outXyz must have a length of 3.");
      }

      double sr = r / 255.0;
      sr = sr < 0.04045 ? sr / 12.92 : Math.pow((sr + 0.055) / 1.055, 2.4);
      double sg = g / 255.0;
      sg = sg < 0.04045 ? sg / 12.92 : Math.pow((sg + 0.055) / 1.055, 2.4);
      double sb = b / 255.0;
      sb = sb < 0.04045 ? sb / 12.92 : Math.pow((sb + 0.055) / 1.055, 2.4);

      outXyz[0] = 100 * (sr * 0.4124 + sg * 0.3576 + sb * 0.1805);
      outXyz[1] = 100 * (sr * 0.2126 + sg * 0.7152 + sb * 0.0722);
      outXyz[2] = 100 * (sr * 0.0193 + sg * 0.1192 + sb * 0.9505);
    }

    public static void XYZToLAB(
        @FloatRange(from = 0f, to = XYZ_WHITE_REFERENCE_X) double x,
        @FloatRange(from = 0f, to = XYZ_WHITE_REFERENCE_Y) double y,
        @FloatRange(from = 0f, to = XYZ_WHITE_REFERENCE_Z) double z,
        @NonNull double[] outLab) {
      if (outLab.length != 3) {
        throw new IllegalArgumentException("outLab must have a length of 3.");
      }
      x = pivotXyzComponent(x / XYZ_WHITE_REFERENCE_X);
      y = pivotXyzComponent(y / XYZ_WHITE_REFERENCE_Y);
      z = pivotXyzComponent(z / XYZ_WHITE_REFERENCE_Z);
      outLab[0] = Math.max(0, 116 * y - 16);
      outLab[1] = 500 * (x - y);
      outLab[2] = 200 * (y - z);
    }

    public static void LABToXYZ(
        @FloatRange(from = 0f, to = 100) final double l,
        @FloatRange(from = -128, to = 127) final double a,
        @FloatRange(from = -128, to = 127) final double b,
        @NonNull double[] outXyz) {
      final double fy = (l + 16) / 116;
      final double fx = a / 500 + fy;
      final double fz = fy - b / 200;

      double tmp = Math.pow(fx, 3);
      final double xr = tmp > XYZ_EPSILON ? tmp : (116 * fx - 16) / XYZ_KAPPA;
      final double yr = l > XYZ_KAPPA * XYZ_EPSILON ? Math.pow(fy, 3) : l / XYZ_KAPPA;

      tmp = Math.pow(fz, 3);
      final double zr = tmp > XYZ_EPSILON ? tmp : (116 * fz - 16) / XYZ_KAPPA;

      outXyz[0] = xr * XYZ_WHITE_REFERENCE_X;
      outXyz[1] = yr * XYZ_WHITE_REFERENCE_Y;
      outXyz[2] = zr * XYZ_WHITE_REFERENCE_Z;
    }

    @ColorInt
    public static int XYZToColor(
        @FloatRange(from = 0f, to = XYZ_WHITE_REFERENCE_X) double x,
        @FloatRange(from = 0f, to = XYZ_WHITE_REFERENCE_Y) double y,
        @FloatRange(from = 0f, to = XYZ_WHITE_REFERENCE_Z) double z) {
      double r = (x * 3.2406 + y * -1.5372 + z * -0.4986) / 100;
      double g = (x * -0.9689 + y * 1.8758 + z * 0.0415) / 100;
      double b = (x * 0.0557 + y * -0.2040 + z * 1.0570) / 100;

      r = r > 0.0031308 ? 1.055 * Math.pow(r, 1 / 2.4) - 0.055 : 12.92 * r;
      g = g > 0.0031308 ? 1.055 * Math.pow(g, 1 / 2.4) - 0.055 : 12.92 * g;
      b = b > 0.0031308 ? 1.055 * Math.pow(b, 1 / 2.4) - 0.055 : 12.92 * b;

      return Color.rgb(
          constrain((int) Math.round(r * 255), 0, 255),
          constrain((int) Math.round(g * 255), 0, 255),
          constrain((int) Math.round(b * 255), 0, 255));
    }

    @ColorInt
    public static int LABToColor(
        @FloatRange(from = 0f, to = 100) final double l,
        @FloatRange(from = -128, to = 127) final double a,
        @FloatRange(from = -128, to = 127) final double b) {
      final double[] result = getTempDouble3Array();
      LABToXYZ(l, a, b, result);
      return XYZToColor(result[0], result[1], result[2]);
    }

    private static int constrain(int amount, int low, int high) {
      return amount < low ? low : (Math.min(amount, high));
    }

    private static float constrain(float amount, float low, float high) {
      return amount < low ? low : (Math.min(amount, high));
    }

    private static double pivotXyzComponent(double component) {
      return component > XYZ_EPSILON
          ? Math.pow(component, 1 / 3.0)
          : (XYZ_KAPPA * component + 16) / 116;
    }

    public static double[] getTempDouble3Array() {
      double[] result = TEMP_ARRAY.get();
      if (result == null) {
        result = new double[3];
        TEMP_ARRAY.set(result);
      }
      return result;
    }

    @ColorInt
    public static int HSLToColor(@NonNull float[] hsl) {
      final float h = hsl[0];
      final float s = hsl[1];
      final float l = hsl[2];

      final float c = (1f - Math.abs(2 * l - 1f)) * s;
      final float m = l - 0.5f * c;
      final float x = c * (1f - Math.abs((h / 60f % 2f) - 1f));

      final int hueSegment = (int) h / 60;

      int r = 0, g = 0, b = 0;

      switch (hueSegment) {
        case 0:
          r = Math.round(255 * (c + m));
          g = Math.round(255 * (x + m));
          b = Math.round(255 * m);
          break;
        case 1:
          r = Math.round(255 * (x + m));
          g = Math.round(255 * (c + m));
          b = Math.round(255 * m);
          break;
        case 2:
          r = Math.round(255 * m);
          g = Math.round(255 * (c + m));
          b = Math.round(255 * (x + m));
          break;
        case 3:
          r = Math.round(255 * m);
          g = Math.round(255 * (x + m));
          b = Math.round(255 * (c + m));
          break;
        case 4:
          r = Math.round(255 * (x + m));
          g = Math.round(255 * m);
          b = Math.round(255 * (c + m));
          break;
        case 5:
        case 6:
          r = Math.round(255 * (c + m));
          g = Math.round(255 * m);
          b = Math.round(255 * (x + m));
          break;
      }

      r = constrain(r, 0, 255);
      g = constrain(g, 0, 255);
      b = constrain(b, 0, 255);

      return Color.rgb(r, g, b);
    }

    public static void colorToHSL(@ColorInt int color, @NonNull float[] outHsl) {
      RGBToHSL(Color.red(color), Color.green(color), Color.blue(color), outHsl);
    }

    public static void RGBToHSL(
        @IntRange(from = 0x0, to = 0xFF) int r,
        @IntRange(from = 0x0, to = 0xFF) int g,
        @IntRange(from = 0x0, to = 0xFF) int b,
        @NonNull float[] outHsl) {
      final float rf = r / 255f;
      final float gf = g / 255f;
      final float bf = b / 255f;

      final float max = Math.max(rf, Math.max(gf, bf));
      final float min = Math.min(rf, Math.min(gf, bf));
      final float deltaMaxMin = max - min;

      float h, s;
      float l = (max + min) / 2f;

      if (max == min) {
        h = s = 0f;
      } else {
        if (max == rf) {
          h = ((gf - bf) / deltaMaxMin) % 6f;
        } else if (max == gf) {
          h = ((bf - rf) / deltaMaxMin) + 2f;
        } else {
          h = ((rf - gf) / deltaMaxMin) + 4f;
        }

        s = deltaMaxMin / (1f - Math.abs(2f * l - 1f));
      }

      h = (h * 60f) % 360f;
      if (h < 0) {
        h += 360f;
      }

      outHsl[0] = constrain(h, 0f, 360f);
      outHsl[1] = constrain(s, 0f, 1f);
      outHsl[2] = constrain(l, 0f, 1f);
    }
  }
}
