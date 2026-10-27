package sporemodder.imageeditor.tools;

import java.awt.image.BufferedImage;
import sporemodder.imageeditor.history.LayerPixelsCommand;

/** Linear gradient from the primary to the secondary colour; drag to preview, release to apply. */
public class GradientTool implements ImageTool {
   private BufferedImage before;
   private int startX;
   private int startY;

   @Override
   public String getId() {
      return "gradient";
   }

   @Override
   public String getDisplayName() {
      return "Gradient";
   }

   @Override
   public void onPress(ToolContext ctx, int x, int y) {
      before = ctx.layer.copyPixels();
      startX = x;
      startY = y;
   }

   @Override
   public void onDrag(ToolContext ctx, int x, int y) {
      if (before != null) {
         render(ctx, x, y);
      }
   }

   @Override
   public void onRelease(ToolContext ctx, int x, int y) {
      if (before != null) {
         if (x != startX || y != startY) {
            render(ctx, x, y);
            ctx.history.push(new LayerPixelsCommand(getDisplayName(), ctx.document, ctx.layer, before));
         } else {
            ctx.layer.setPixels(before);
            ctx.document.changed();
         }
         before = null;
      }
   }

   private void render(ToolContext ctx, int x, int y) {
      BufferedImage out = new BufferedImage(before.getWidth(), before.getHeight(), BufferedImage.TYPE_INT_ARGB);
      double dx = x - startX;
      double dy = y - startY;
      double len2 = dx * dx + dy * dy;
      for (int py = 0; py < out.getHeight(); py++) {
         for (int px = 0; px < out.getWidth(); px++) {
            double t = len2 == 0 ? 0 : ((px - startX) * dx + (py - startY) * dy) / len2;
            t = Math.max(0, Math.min(1, t));
            int g = 0;
            for (int shift = 24; shift >= 0; shift -= 8) {
               double a = ctx.color >>> shift & 255;
               double b = ctx.secondaryColor >>> shift & 255;
               g |= (int) Math.round(a + (b - a) * t) << shift;
            }
            int src = before.getRGB(px, py);
            double k = ctx.opacity * ctx.selectionAt(px, py) * (g >>> 24) / 255.0;
            int res = 0;
            double sa = k;
            double da = (src >>> 24) / 255.0;
            double oa = sa + da * (1 - sa);
            if (oa > 0) {
               res = (int) Math.round(oa * 255) << 24;
               for (int shift = 16; shift >= 0; shift -= 8) {
                  double c = ((g >>> shift & 255) * sa + (src >>> shift & 255) * da * (1 - sa)) / oa;
                  res |= (int) Math.round(Math.max(0, Math.min(255, c))) << shift;
               }
            }
            out.setRGB(px, py, res);
         }
      }
      ctx.layer.setPixels(out);
      ctx.document.changed();
   }
}
