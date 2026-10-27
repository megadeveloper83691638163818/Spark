package sporemodder.imageeditor.filters;

import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.List;
import java.util.function.IntUnaryOperator;
import sporemodder.imageeditor.filters.ParametricFilter.Param;

/** Colour adjustments with parameters (Brightness/Contrast, Hue/Saturation, Levels, Vibrance...). */
public final class Adjustments {
   private Adjustments() {
   }

   static int clamp(double v) {
      return v < 0 ? 0 : (v > 255 ? 255 : (int) Math.round(v));
   }

   interface PixelOp {
      int apply(int argb, double[] v);
   }

   private static ParametricFilter make(String name, List<Param> params, PixelOp op) {
      return new ParametricFilter("Adjustments", name, params) {
         @Override
         public void apply(BufferedImage img, double[] v) {
            for (int y = 0; y < img.getHeight(); y++) {
               for (int x = 0; x < img.getWidth(); x++) {
                  img.setRGB(x, y, op.apply(img.getRGB(x, y), v));
               }
            }
         }
      };
   }

   static int rgb(int argb, double r, double g, double b) {
      return (argb & 0xFF000000) | clamp(r) << 16 | clamp(g) << 8 | clamp(b);
   }

   public static ParametricFilter brightnessContrast() {
      return make("Brightness/Contrast", Arrays.asList(new Param("Brightness", -100, 100, 0), new Param("Contrast", -100, 100, 0)), (p, v) -> {
         double offset = v[0] * 2.55;
         double factor = v[1] >= 0 ? 1 + v[1] / 100.0 * 3 : 1 + v[1] / 100.0;
         return rgb(p, ((p >> 16 & 255) - 128) * factor + 128 + offset, ((p >> 8 & 255) - 128) * factor + 128 + offset, ((p & 255) - 128) * factor + 128 + offset);
      });
   }

   public static ParametricFilter hueSaturation() {
      return make("Hue/Saturation", Arrays.asList(new Param("Hue", -180, 180, 0), new Param("Saturation", -100, 100, 0), new Param("Lightness", -100, 100, 0)), (p, v) -> {
         float[] hsb = java.awt.Color.RGBtoHSB(p >> 16 & 255, p >> 8 & 255, p & 255, null);
         float h = (float) ((hsb[0] + v[0] / 360.0) % 1.0);
         if (h < 0) {
            h += 1;
         }
         float s = (float) Math.max(0, Math.min(1, v[1] >= 0 ? hsb[1] * (1 + v[1] / 100.0) : hsb[1] * (1 + v[1] / 100.0)));
         float b = (float) (v[2] >= 0 ? hsb[2] + (1 - hsb[2]) * v[2] / 100.0 : hsb[2] * (1 + v[2] / 100.0));
         return (p & 0xFF000000) | (java.awt.Color.HSBtoRGB(h, s, Math.max(0, Math.min(1, b))) & 0xFFFFFF);
      });
   }

   public static ParametricFilter levels() {
      return make("Levels", Arrays.asList(new Param("Input black", 0, 254, 0), new Param("Input white", 1, 255, 255),
         new Param("Gamma", 0.1, 3.0, 1.0), new Param("Output black", 0, 255, 0), new Param("Output white", 0, 255, 255)), (p, v) -> {
         double inB = v[0];
         double inW = Math.max(inB + 1, v[1]);
         IntUnaryOperator f = c -> {
            double t = Math.max(0, Math.min(1, (c - inB) / (inW - inB)));
            t = Math.pow(t, 1.0 / v[2]);
            return clamp(v[3] + t * (v[4] - v[3]));
         };
         return (p & 0xFF000000) | f.applyAsInt(p >> 16 & 255) << 16 | f.applyAsInt(p >> 8 & 255) << 8 | f.applyAsInt(p & 255);
      });
   }

   public static ParametricFilter vibrance() {
      return make("Vibrance", Arrays.asList(new Param("Vibrance", -100, 100, 0)), (p, v) -> {
         double r = p >> 16 & 255;
         double g = p >> 8 & 255;
         double b = p & 255;
         double max = Math.max(r, Math.max(g, b));
         double min = Math.min(r, Math.min(g, b));
         double sat = max == 0 ? 0 : (max - min) / max;
         double amount = v[0] / 100.0 * (1 - sat);
         double avg = (r + g + b) / 3;
         return rgb(p, r + (r - avg) * amount, g + (g - avg) * amount, b + (b - avg) * amount);
      });
   }

   public static ParametricFilter colorBalance() {
      return make("Color Balance", Arrays.asList(new Param("Red", -100, 100, 0), new Param("Green", -100, 100, 0), new Param("Blue", -100, 100, 0)), (p, v) ->
         rgb(p, (p >> 16 & 255) + v[0] * 2.55, (p >> 8 & 255) + v[1] * 2.55, (p & 255) + v[2] * 2.55));
   }

   public static ParametricFilter temperature() {
      return make("Temperature", Arrays.asList(new Param("Warm/Cool", -100, 100, 0)), (p, v) ->
         rgb(p, (p >> 16 & 255) + v[0] * 0.5, p >> 8 & 255, (p & 255) - v[0] * 0.5));
   }

   public static ImageFilter equalize() {
      return new CategorizedFilter("Adjustments", new ImageFilter() {
         @Override
         public String getDisplayName() {
            return "Equalize";
         }

         @Override
         public void apply(BufferedImage img) {
            int[] hist = new int[256];
            int total = 0;
            for (int y = 0; y < img.getHeight(); y++) {
               for (int x = 0; x < img.getWidth(); x++) {
                  int p = img.getRGB(x, y);
                  if ((p >>> 24) == 0) {
                     continue;
                  }
                  hist[(int) Math.round(0.299 * (p >> 16 & 255) + 0.587 * (p >> 8 & 255) + 0.114 * (p & 255))]++;
                  total++;
               }
            }
            if (total == 0) {
               return;
            }
            int[] map = new int[256];
            int cum = 0;
            for (int i = 0; i < 256; i++) {
               cum += hist[i];
               map[i] = clamp(255.0 * cum / total);
            }
            for (int y = 0; y < img.getHeight(); y++) {
               for (int x = 0; x < img.getWidth(); x++) {
                  int p = img.getRGB(x, y);
                  double l = 0.299 * (p >> 16 & 255) + 0.587 * (p >> 8 & 255) + 0.114 * (p & 255);
                  double k = l <= 0 ? 1 : map[(int) Math.round(l)] / l;
                  img.setRGB(x, y, rgb(p, (p >> 16 & 255) * k, (p >> 8 & 255) * k, (p & 255) * k));
               }
            }
         }
      });
   }
}
