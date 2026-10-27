package sporemodder.imageeditor.tools;

import java.awt.image.BufferedImage;
import sporemodder.imageeditor.history.LayerPixelsCommand;

/** Freehand tool: draws a square brush along the mouse path. */
public abstract class StrokeTool implements ImageTool {
   private BufferedImage before;
   private int lastX;
   private int lastY;

   /** Pixel value written by the brush (ARGB, written without blending). */
   protected abstract int brushColor(ToolContext ctx);

   @Override
   public void onPress(ToolContext ctx, int x, int y) {
      this.before = ctx.layer.copyPixels();
      this.lastX = x;
      this.lastY = y;
      drawLine(ctx, x, y, x, y, brushColor(ctx));
   }

   @Override
   public void onDrag(ToolContext ctx, int x, int y) {
      if (this.before != null) {
         drawLine(ctx, this.lastX, this.lastY, x, y, brushColor(ctx));
         this.lastX = x;
         this.lastY = y;
      }
   }

   @Override
   public void onRelease(ToolContext ctx, int x, int y) {
      if (this.before != null) {
         onDrag(ctx, x, y);
         ctx.history.push(new LayerPixelsCommand(getDisplayName(), ctx.document, ctx.layer, this.before));
         this.before = null;
      }
   }

   static void stamp(ToolContext ctx, BufferedImage img, int cx, int cy, int size, int argb) {
      int from = -(size - 1) / 2;
      for (int dy = 0; dy < size; dy++) {
         for (int dx = 0; dx < size; dx++) {
            int px = cx + from + dx;
            int py = cy + from + dy;
            if (px >= 0 && py >= 0 && px < img.getWidth() && py < img.getHeight() && ctx.selectionAt(px, py) >= 0.5) {
               img.setRGB(px, py, argb);
            }
         }
      }
   }

   static void drawLine(ToolContext ctx, int x0, int y0, int x1, int y1, int argb) {
      BufferedImage img = ctx.layer.getPixels();
      int dx = Math.abs(x1 - x0);
      int dy = -Math.abs(y1 - y0);
      int sx = x0 < x1 ? 1 : -1;
      int sy = y0 < y1 ? 1 : -1;
      int err = dx + dy;
      while (true) {
         stamp(ctx, img, x0, y0, ctx.size, argb);
         if (x0 == x1 && y0 == y1) {
            break;
         }
         int e2 = 2 * err;
         if (e2 >= dy) {
            err += dy;
            x0 += sx;
         }
         if (e2 <= dx) {
            err += dx;
            y0 += sy;
         }
      }
      ctx.document.changed();
   }
}
