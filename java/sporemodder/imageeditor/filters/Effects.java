package sporemodder.imageeditor.filters;

import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.Random;
import sporemodder.imageeditor.filters.ParametricFilter.Param;

/** Blur, noise, pixelate, edge and emboss filters. */
public final class Effects {
   private Effects() {
   }

   private static ParametricFilter make(String name, Param[] params, java.util.function.BiConsumer<BufferedImage, double[]> body) {
      return new ParametricFilter("Filters", name, Arrays.asList(params)) {
         @Override
         public void apply(BufferedImage img, double[] v) {
            body.accept(img, v);
         }
      };
   }

   private static int pack(double a, double r, double g, double b) {
      return Adjustments.clamp(a) << 24 | Adjustments.clamp(r) << 16 | Adjustments.clamp(g) << 8 | Adjustments.clamp(b);
   }

   public static ParametricFilter gaussianBlur() {
      return make("Gaussian Blur", new Param[]{new Param("Radius", 0.5, 30, 2)}, (img, v) -> {
         int radius = (int) Math.ceil(v[0] * 2);
         double[] kernel = new double[radius * 2 + 1];
         double sigma = Math.max(0.1, v[0]);
         double sum = 0;
         for (int i = -radius; i <= radius; i++) {
            kernel[i + radius] = Math.exp(-(i * i) / (2 * sigma * sigma));
            sum += kernel[i + radius];
         }
         for (int i = 0; i < kernel.length; i++) {
            kernel[i] /= sum;
         }
         int w = img.getWidth();
         int h = img.getHeight();
         double[][] buf = new double[4][w * h];
         double[][] tmp = new double[4][w * h];
         int[] px = img.getRGB(0, 0, w, h, null, 0, w);
         for (int i = 0; i < px.length; i++) {
            double a = (px[i] >>> 24) / 255.0;
            buf[0][i] = a * 255;
            buf[1][i] = (px[i] >> 16 & 255) * a;
            buf[2][i] = (px[i] >> 8 & 255) * a;
            buf[3][i] = (px[i] & 255) * a;
         }
         for (int c = 0; c < 4; c++) {
            for (int y = 0; y < h; y++) {
               for (int x = 0; x < w; x++) {
                  double acc = 0;
                  for (int k = -radius; k <= radius; k++) {
                     acc += buf[c][y * w + Math.max(0, Math.min(w - 1, x + k))] * kernel[k + radius];
                  }
                  tmp[c][y * w + x] = acc;
               }
            }
            for (int y = 0; y < h; y++) {
               for (int x = 0; x < w; x++) {
                  double acc = 0;
                  for (int k = -radius; k <= radius; k++) {
                     acc += tmp[c][Math.max(0, Math.min(h - 1, y + k)) * w + x] * kernel[k + radius];
                  }
                  buf[c][y * w + x] = acc;
               }
            }
         }
         for (int i = 0; i < px.length; i++) {
            double a = buf[0][i] / 255.0;
            px[i] = a <= 0 ? 0 : pack(buf[0][i], buf[1][i] / a, buf[2][i] / a, buf[3][i] / a);
         }
         img.setRGB(0, 0, w, h, px, 0, w);
      });
   }

   public static ParametricFilter motionBlur() {
      return make("Motion Blur", new Param[]{new Param("Distance", 1, 80, 10), new Param("Angle", 0, 180, 0)}, (img, v) -> {
         int w = img.getWidth();
         int h = img.getHeight();
         int[] src = img.getRGB(0, 0, w, h, null, 0, w);
         int[] dst = new int[src.length];
         int steps = Math.max(1, (int) Math.round(v[0]));
         double ca = Math.cos(Math.toRadians(v[1]));
         double sa = Math.sin(Math.toRadians(v[1]));
         for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
               double a = 0, r = 0, g = 0, b = 0;
               for (int i = 0; i < steps; i++) {
                  double t = i - (steps - 1) / 2.0;
                  int sx = Math.max(0, Math.min(w - 1, (int) Math.round(x + ca * t)));
                  int sy = Math.max(0, Math.min(h - 1, (int) Math.round(y + sa * t)));
                  int p = src[sy * w + sx];
                  double pa = (p >>> 24) / 255.0;
                  a += pa;
                  r += (p >> 16 & 255) * pa;
                  g += (p >> 8 & 255) * pa;
                  b += (p & 255) * pa;
               }
               dst[y * w + x] = a <= 0 ? 0 : pack(a / steps * 255, r / a, g / a, b / a);
            }
         }
         img.setRGB(0, 0, w, h, dst, 0, w);
      });
   }

   public static ParametricFilter pixelate() {
      return make("Pixelate", new Param[]{new Param("Cell size", 2, 64, 8)}, (img, v) -> {
         int cell = (int) Math.round(v[0]);
         int w = img.getWidth();
         int h = img.getHeight();
         for (int cy = 0; cy < h; cy += cell) {
            for (int cx = 0; cx < w; cx += cell) {
               double a = 0, r = 0, g = 0, b = 0;
               int n = 0;
               for (int y = cy; y < Math.min(h, cy + cell); y++) {
                  for (int x = cx; x < Math.min(w, cx + cell); x++) {
                     int p = img.getRGB(x, y);
                     double pa = (p >>> 24) / 255.0;
                     a += pa;
                     r += (p >> 16 & 255) * pa;
                     g += (p >> 8 & 255) * pa;
                     b += (p & 255) * pa;
                     n++;
                  }
               }
               int out = a <= 0 ? 0 : pack(a / n * 255, r / a, g / a, b / a);
               for (int y = cy; y < Math.min(h, cy + cell); y++) {
                  for (int x = cx; x < Math.min(w, cx + cell); x++) {
                     img.setRGB(x, y, out);
                  }
               }
            }
         }
      });
   }

   public static ParametricFilter addNoise() {
      return make("Add Noise", new Param[]{new Param("Amount", 1, 100, 20), new Param("Monochrome", 0, 1, 0)}, (img, v) -> {
         Random rnd = new Random(12345);
         for (int y = 0; y < img.getHeight(); y++) {
            for (int x = 0; x < img.getWidth(); x++) {
               int p = img.getRGB(x, y);
               double n = rnd.nextGaussian() * v[0] * 1.275;
               boolean mono = v[1] >= 0.5;
               double nr = mono ? n : rnd.nextGaussian() * v[0] * 1.275;
               double ng = mono ? n : rnd.nextGaussian() * v[0] * 1.275;
               img.setRGB(x, y, Adjustments.rgb(p, (p >> 16 & 255) + nr, (p >> 8 & 255) + ng, (p & 255) + n));
            }
         }
      });
   }

   public static ParametricFilter median() {
      return make("Median", new Param[]{new Param("Radius", 1, 4, 1)}, (img, v) -> {
         int r = (int) Math.round(v[0]);
         int w = img.getWidth();
         int h = img.getHeight();
         int[] src = img.getRGB(0, 0, w, h, null, 0, w);
         int[] dst = new int[src.length];
         int size = (2 * r + 1) * (2 * r + 1);
         int[][] ch = new int[4][size];
         for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
               int n = 0;
               for (int dy = -r; dy <= r; dy++) {
                  for (int dx = -r; dx <= r; dx++) {
                     int p = src[Math.max(0, Math.min(h - 1, y + dy)) * w + Math.max(0, Math.min(w - 1, x + dx))];
                     for (int c = 0; c < 4; c++) {
                        ch[c][n] = p >>> (24 - c * 8) & 255;
                     }
                     n++;
                  }
               }
               int out = 0;
               for (int c = 0; c < 4; c++) {
                  Arrays.sort(ch[c], 0, n);
                  out |= ch[c][n / 2] << (24 - c * 8);
               }
               dst[y * w + x] = out;
            }
         }
         img.setRGB(0, 0, w, h, dst, 0, w);
      });
   }

   private static double lum(int p) {
      return 0.299 * (p >> 16 & 255) + 0.587 * (p >> 8 & 255) + 0.114 * (p & 255);
   }

   public static ParametricFilter edgeDetect() {
      return make("Edge Detect", new Param[]{new Param("Strength", 0.5, 5, 1)}, (img, v) -> {
         int w = img.getWidth();
         int h = img.getHeight();
         double[] l = new double[w * h];
         for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
               l[y * w + x] = lum(img.getRGB(x, y));
            }
         }
         for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
               double[] n = new double[9];
               int i = 0;
               for (int dy = -1; dy <= 1; dy++) {
                  for (int dx = -1; dx <= 1; dx++) {
                     n[i++] = l[Math.max(0, Math.min(h - 1, y + dy)) * w + Math.max(0, Math.min(w - 1, x + dx))];
                  }
               }
               double gx = -n[0] - 2 * n[3] - n[6] + n[2] + 2 * n[5] + n[8];
               double gy = -n[0] - 2 * n[1] - n[2] + n[6] + 2 * n[7] + n[8];
               double m = Math.hypot(gx, gy) * v[0];
               img.setRGB(x, y, (img.getRGB(x, y) & 0xFF000000) | Adjustments.clamp(m) * 0x010101);
            }
         }
      });
   }

   public static ParametricFilter emboss() {
      return make("Emboss", new Param[]{new Param("Strength", 0.5, 5, 1)}, (img, v) -> {
         int w = img.getWidth();
         int h = img.getHeight();
         double[] l = new double[w * h];
         for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
               l[y * w + x] = lum(img.getRGB(x, y));
            }
         }
         for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
               double a = l[Math.max(0, y - 1) * w + Math.max(0, x - 1)];
               double b = l[Math.min(h - 1, y + 1) * w + Math.min(w - 1, x + 1)];
               img.setRGB(x, y, (img.getRGB(x, y) & 0xFF000000) | Adjustments.clamp(128 + (b - a) * v[0]) * 0x010101);
            }
         }
      });
   }
}
