package sporemodder.imageeditor.tools;

import java.awt.image.BufferedImage;

/** Soft round brush with opacity and hardness (normal alpha blending). */
public class BrushTool extends DabTool {
   @Override
   public String getId() {
      return "brush";
   }

   @Override
   public String getDisplayName() {
      return "Brush";
   }

   @Override
   protected void applyPixel(ToolContext ctx, BufferedImage img, int x, int y, double amount) {
      double sa = (ctx.color >>> 24) / 255.0 * amount;
      int d = img.getRGB(x, y);
      double da = (d >>> 24) / 255.0;
      double oa = sa + da * (1 - sa);
      if (oa <= 0) {
         return;
      }
      int out = (int) Math.round(oa * 255) << 24;
      for (int shift = 16; shift >= 0; shift -= 8) {
         double sc = ctx.color >>> shift & 255;
         double dc = d >>> shift & 255;
         double c = (sc * sa + dc * da * (1 - sa)) / oa;
         out |= (int) Math.round(Math.max(0, Math.min(255, c))) << shift;
      }
      img.setRGB(x, y, out);
   }
}
