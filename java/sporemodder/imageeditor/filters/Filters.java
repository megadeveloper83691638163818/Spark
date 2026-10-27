package sporemodder.imageeditor.filters;

import java.awt.image.BufferedImage;
import java.util.function.IntUnaryOperator;

/** Factory for the extra built-in filters. */
public final class Filters {
   private Filters() {
   }

   private static int clamp(int v) {
      return v < 0 ? 0 : Math.min(v, 255);
   }

   private static ImageFilter perPixel(String name, IntUnaryOperator op) {
      return new ImageFilter() {
         @Override
         public String getDisplayName() {
            return name;
         }

         @Override
         public void apply(BufferedImage img) {
            for (int y = 0; y < img.getHeight(); y++) {
               for (int x = 0; x < img.getWidth(); x++) {
                  img.setRGB(x, y, op.applyAsInt(img.getRGB(x, y)));
               }
            }
         }
      };
   }

   private static int rgb(int argb, int r, int g, int b) {
      return (argb & 0xFF000000) | clamp(r) << 16 | clamp(g) << 8 | clamp(b);
   }

   public static ImageFilter brightness(String name, int delta) {
      return perPixel(name, p -> rgb(p, (p >> 16 & 255) + delta, (p >> 8 & 255) + delta, (p & 255) + delta));
   }

   public static ImageFilter contrast(String name, double factor) {
      return perPixel(name, p -> rgb(p,
         (int) Math.round(((p >> 16 & 255) - 128) * factor + 128),
         (int) Math.round(((p >> 8 & 255) - 128) * factor + 128),
         (int) Math.round(((p & 255) - 128) * factor + 128)));
   }

   public static ImageFilter sepia() {
      return perPixel("Sepia", p -> {
         int r = p >> 16 & 255;
         int g = p >> 8 & 255;
         int b = p & 255;
         return rgb(p, (int) (0.393 * r + 0.769 * g + 0.189 * b), (int) (0.349 * r + 0.686 * g + 0.168 * b), (int) (0.272 * r + 0.534 * g + 0.131 * b));
      });
   }

   public static ImageFilter threshold() {
      return perPixel("Threshold", p -> {
         int l = (int) Math.round(0.299 * (p >> 16 & 255) + 0.587 * (p >> 8 & 255) + 0.114 * (p & 255));
         int v = l >= 128 ? 255 : 0;
         return rgb(p, v, v, v);
      });
   }

   public static ImageFilter posterize() {
      return perPixel("Posterize", p -> {
         IntUnaryOperator q = c -> clamp(Math.round(c / 85f) * 85);
         return rgb(p, q.applyAsInt(p >> 16 & 255), q.applyAsInt(p >> 8 & 255), q.applyAsInt(p & 255));
      });
   }

   public static ImageFilter kernel(String name, float[] k, float divisor) {
      return new ImageFilter() {
         @Override
         public String getDisplayName() {
            return name;
         }

         @Override
         public void apply(BufferedImage img) {
            int w = img.getWidth();
            int h = img.getHeight();
            int[] src = img.getRGB(0, 0, w, h, null, 0, w);
            int[] dst = new int[src.length];
            for (int y = 0; y < h; y++) {
               for (int x = 0; x < w; x++) {
                  float r = 0, g = 0, b = 0;
                  for (int ky = -1; ky <= 1; ky++) {
                     for (int kx = -1; kx <= 1; kx++) {
                        int sx = Math.max(0, Math.min(w - 1, x + kx));
                        int sy = Math.max(0, Math.min(h - 1, y + ky));
                        int p = src[sy * w + sx];
                        float f = k[(ky + 1) * 3 + kx + 1];
                        r += f * (p >> 16 & 255);
                        g += f * (p >> 8 & 255);
                        b += f * (p & 255);
                     }
                  }
                  int a = src[y * w + x];
                  dst[y * w + x] = rgb(a, Math.round(r / divisor), Math.round(g / divisor), Math.round(b / divisor));
               }
            }
            img.setRGB(0, 0, w, h, dst, 0, w);
         }
      };
   }

   public static ImageFilter blur() {
      return kernel("Blur", new float[]{1, 2, 1, 2, 4, 2, 1, 2, 1}, 16f);
   }

   public static ImageFilter sharpen() {
      return kernel("Sharpen", new float[]{0, -1, 0, -1, 5, -1, 0, -1, 0}, 1f);
   }

   public static ImageFilter flipHorizontal() {
      return new ImageFilter() {
         @Override
         public String getDisplayName() {
            return "Flip horizontally";
         }

         @Override
         public void apply(BufferedImage img) {
            int w = img.getWidth();
            for (int y = 0; y < img.getHeight(); y++) {
               for (int x = 0; x < w / 2; x++) {
                  int a = img.getRGB(x, y);
                  img.setRGB(x, y, img.getRGB(w - 1 - x, y));
                  img.setRGB(w - 1 - x, y, a);
               }
            }
         }
      };
   }

   public static ImageFilter flipVertical() {
      return new ImageFilter() {
         @Override
         public String getDisplayName() {
            return "Flip vertically";
         }

         @Override
         public void apply(BufferedImage img) {
            int h = img.getHeight();
            for (int y = 0; y < h / 2; y++) {
               for (int x = 0; x < img.getWidth(); x++) {
                  int a = img.getRGB(x, y);
                  img.setRGB(x, y, img.getRGB(x, h - 1 - y));
                  img.setRGB(x, h - 1 - y, a);
               }
            }
         }
      };
   }
}
