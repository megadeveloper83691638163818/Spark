package sporemodder.imageeditor.tools;

import java.awt.image.BufferedImage;
import sporemodder.imageeditor.history.LayerPixelsCommand;

/** Round soft brush that places "dabs" along the stroke; subclasses decide what a dab does to a pixel. */
public abstract class DabTool implements ImageTool {
   private BufferedImage before;
   private double lastX;
   private double lastY;
   private double carry;

   /** Modify the pixel at (x, y). {@code amount} is the combined falloff * opacity * selection weight (0..1). */
   protected abstract void applyPixel(ToolContext ctx, BufferedImage img, int x, int y, double amount);

   @Override
   public void onPress(ToolContext ctx, int x, int y) {
      this.before = ctx.layer.copyPixels();
      this.lastX = x;
      this.lastY = y;
      this.carry = 0;
      dab(ctx, x, y);
   }

   @Override
   public void onDrag(ToolContext ctx, int x, int y) {
      if (this.before == null) {
         return;
      }
      double spacing = Math.max(1.0, ctx.size * 0.2);
      double dx = x - lastX;
      double dy = y - lastY;
      double dist = Math.hypot(dx, dy);
      double pos = spacing - carry;
      while (pos <= dist) {
         dab(ctx, lastX + dx * pos / dist, lastY + dy * pos / dist);
         pos += spacing;
      }
      carry = dist - (pos - spacing);
      lastX = x;
      lastY = y;
   }

   @Override
   public void onRelease(ToolContext ctx, int x, int y) {
      if (this.before != null) {
         onDrag(ctx, x, y);
         ctx.history.push(new LayerPixelsCommand(getDisplayName(), ctx.document, ctx.layer, this.before));
         this.before = null;
      }
   }

   private void dab(ToolContext ctx, double cx, double cy) {
      BufferedImage img = ctx.layer.getPixels();
      double r = Math.max(0.5, ctx.size / 2.0);
      int x0 = (int) Math.floor(cx - r);
      int x1 = (int) Math.ceil(cx + r);
      int y0 = (int) Math.floor(cy - r);
      int y1 = (int) Math.ceil(cy + r);
      for (int y = y0; y <= y1; y++) {
         for (int x = x0; x <= x1; x++) {
            if (x < 0 || y < 0 || x >= img.getWidth() || y >= img.getHeight()) {
               continue;
            }
            double t = Math.hypot(x + 0.5 - cx - 0.5, y + 0.5 - cy - 0.5) / r;
            if (t > 1) {
               continue;
            }
            double falloff = t <= ctx.hardness || ctx.hardness >= 1 ? 1 : 1 - (t - ctx.hardness) / (1 - ctx.hardness);
            double amount = falloff * ctx.opacity * ctx.selectionAt(x, y);
            if (amount > 0) {
               applyPixel(ctx, img, x, y, amount);
            }
         }
      }
      ctx.document.changed();
   }
}
