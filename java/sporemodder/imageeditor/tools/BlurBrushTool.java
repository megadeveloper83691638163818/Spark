package sporemodder.imageeditor.tools;

import java.awt.image.BufferedImage;

/** Blur brush: mixes each pixel with the average of its neighbours. */
public class BlurBrushTool extends DabTool {
   @Override
   public String getId() {
      return "blurbrush";
   }

   @Override
   public String getDisplayName() {
      return "Blur Brush";
   }

   @Override
   protected void applyPixel(ToolContext ctx, BufferedImage img, int x, int y, double amount) {
      double[] sum = new double[4];
      int n = 0;
      for (int dy = -1; dy <= 1; dy++) {
         for (int dx = -1; dx <= 1; dx++) {
            int px = Math.max(0, Math.min(img.getWidth() - 1, x + dx));
            int py = Math.max(0, Math.min(img.getHeight() - 1, y + dy));
            int p = img.getRGB(px, py);
            for (int c = 0; c < 4; c++) {
               sum[c] += p >>> (24 - c * 8) & 255;
            }
            n++;
         }
      }
      int cur = img.getRGB(x, y);
      double k = Math.min(1, amount);
      int out = 0;
      for (int c = 0; c < 4; c++) {
         int shift = 24 - c * 8;
         double v = (cur >>> shift & 255) * (1 - k) + sum[c] / n * k;
         out |= (int) Math.round(Math.max(0, Math.min(255, v))) << shift;
      }
      img.setRGB(x, y, out);
   }
}
