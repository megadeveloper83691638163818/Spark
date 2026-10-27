package sporemodder.imageeditor.model;

import java.awt.image.BufferedImage;

/** Per-pixel selection mask (0 = unselected, 255 = fully selected). */
public class Selection {
   private final int width;
   private final int height;
   private final byte[] mask;

   public Selection(int width, int height) {
      this.width = width;
      this.height = height;
      this.mask = new byte[width * height];
   }

   public int getWidth() {
      return width;
   }

   public int getHeight() {
      return height;
   }

   public int get(int x, int y) {
      return x < 0 || y < 0 || x >= width || y >= height ? 0 : mask[y * width + x] & 255;
   }

   public void set(int x, int y, int value) {
      if (x >= 0 && y >= 0 && x < width && y < height) {
         mask[y * width + x] = (byte) value;
      }
   }

   public static Selection all(int w, int h) {
      Selection s = new Selection(w, h);
      java.util.Arrays.fill(s.mask, (byte) 255);
      return s;
   }

   public static Selection rect(int w, int h, int x0, int y0, int x1, int y1) {
      Selection s = new Selection(w, h);
      for (int y = Math.max(0, Math.min(y0, y1)); y <= Math.min(h - 1, Math.max(y0, y1)); y++) {
         for (int x = Math.max(0, Math.min(x0, x1)); x <= Math.min(w - 1, Math.max(x0, x1)); x++) {
            s.mask[y * w + x] = (byte) 255;
         }
      }
      return s;
   }

   public static Selection ellipse(int w, int h, int x0, int y0, int x1, int y1) {
      Selection s = new Selection(w, h);
      double cx = (x0 + x1) / 2.0;
      double cy = (y0 + y1) / 2.0;
      double rx = Math.abs(x1 - x0) / 2.0 + 0.5;
      double ry = Math.abs(y1 - y0) / 2.0 + 0.5;
      for (int y = 0; y < h; y++) {
         for (int x = 0; x < w; x++) {
            double dx = (x - cx) / rx;
            double dy = (y - cy) / ry;
            if (dx * dx + dy * dy <= 1) {
               s.mask[y * w + x] = (byte) 255;
            }
         }
      }
      return s;
   }

   public Selection invert() {
      Selection s = new Selection(width, height);
      for (int i = 0; i < mask.length; i++) {
         s.mask[i] = (byte) (255 - (mask[i] & 255));
      }
      return s;
   }

   /** Soft edge using three box-blur passes (approximate Gaussian). */
   public Selection feather(int radius) {
      if (radius <= 0) {
         return this;
      }
      float[] a = new float[mask.length];
      for (int i = 0; i < a.length; i++) {
         a[i] = mask[i] & 255;
      }
      float[] tmp = new float[a.length];
      for (int pass = 0; pass < 3; pass++) {
         boxBlur(a, tmp, width, height, Math.max(1, radius / 2), true);
         boxBlur(tmp, a, width, height, Math.max(1, radius / 2), false);
      }
      Selection s = new Selection(width, height);
      for (int i = 0; i < a.length; i++) {
         s.mask[i] = (byte) Math.max(0, Math.min(255, Math.round(a[i])));
      }
      return s;
   }

   private static void boxBlur(float[] src, float[] dst, int w, int h, int r, boolean horizontal) {
      int len = horizontal ? w : h;
      int lines = horizontal ? h : w;
      for (int line = 0; line < lines; line++) {
         for (int i = 0; i < len; i++) {
            float sum = 0;
            int count = 0;
            for (int k = -r; k <= r; k++) {
               int j = i + k;
               if (j >= 0 && j < len) {
                  sum += horizontal ? src[line * w + j] : src[j * w + line];
                  count++;
               }
            }
            float v = sum / count;
            if (horizontal) {
               dst[line * w + i] = v;
            } else {
               dst[i * w + line] = v;
            }
         }
      }
   }

   /** Bounding box {x0, y0, x1, y1} (inclusive) of selected pixels, or null when empty. */
   public int[] bounds() {
      int x0 = width;
      int y0 = height;
      int x1 = -1;
      int y1 = -1;
      for (int y = 0; y < height; y++) {
         for (int x = 0; x < width; x++) {
            if (mask[y * width + x] != 0) {
               x0 = Math.min(x0, x);
               x1 = Math.max(x1, x);
               y0 = Math.min(y0, y);
               y1 = Math.max(y1, y);
            }
         }
      }
      return x1 < 0 ? null : new int[]{x0, y0, x1, y1};
   }

   /** Draws a dashed outline ("marching ants") onto a preview image. */
   public void drawOutline(BufferedImage img) {
      for (int y = 0; y < height && y < img.getHeight(); y++) {
         for (int x = 0; x < width && x < img.getWidth(); x++) {
            boolean in = get(x, y) >= 128;
            boolean edge = in != (get(x + 1, y) >= 128) || in != (get(x, y + 1) >= 128);
            if (edge) {
               img.setRGB(x, y, ((x + y) / 3 & 1) == 0 ? 0xFF000000 : 0xFFFFFFFF);
            }
         }
      }
   }

   /** Restores pixels of {@code img} outside the selection from {@code original} (soft edges blend). */
   public void restoreOutside(BufferedImage img, BufferedImage original) {
      int w = Math.min(img.getWidth(), original.getWidth());
      int h = Math.min(img.getHeight(), original.getHeight());
      for (int y = 0; y < h; y++) {
         for (int x = 0; x < w; x++) {
            int m = get(x, y);
            if (m == 255) {
               continue;
            }
            int o = original.getRGB(x, y);
            if (m == 0) {
               img.setRGB(x, y, o);
            } else {
               img.setRGB(x, y, mix(o, img.getRGB(x, y), m / 255.0));
            }
         }
      }
   }

   private static int mix(int a, int b, double t) {
      int out = 0;
      for (int shift = 0; shift <= 24; shift += 8) {
         int ca = a >>> shift & 255;
         int cb = b >>> shift & 255;
         out |= (int) Math.round(ca + (cb - ca) * t) << shift;
      }
      return out;
   }
}
