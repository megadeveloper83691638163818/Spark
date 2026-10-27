package sporemodder.imageeditor.tools;

import java.awt.image.BufferedImage;
import java.util.ArrayDeque;
import sporemodder.imageeditor.history.LayerPixelsCommand;

/** Flood fill of the contiguous area of identical colour. */
public class FillTool implements ImageTool {
   @Override
   public String getId() {
      return "fill";
   }

   @Override
   public String getDisplayName() {
      return "Fill";
   }

   @Override
   public void onPress(ToolContext ctx, int x, int y) {
      BufferedImage img = ctx.layer.getPixels();
      int w = img.getWidth();
      int h = img.getHeight();
      if (x < 0 || y < 0 || x >= w || y >= h) {
         return;
      }
      int target = img.getRGB(x, y);
      if (target == ctx.color) {
         return;
      }
      BufferedImage before = ctx.layer.copyPixels();
      ArrayDeque<int[]> stack = new ArrayDeque<>();
      stack.push(new int[]{x, y});
      while (!stack.isEmpty()) {
         int[] p = stack.pop();
         int px = p[0];
         int py = p[1];
         if (py < 0 || py >= h || img.getRGB(px, py) != target) {
            continue;
         }
         int left = px;
         while (left > 0 && img.getRGB(left - 1, py) == target) {
            left--;
         }
         int right = px;
         while (right < w - 1 && img.getRGB(right + 1, py) == target) {
            right++;
         }
         for (int i = left; i <= right; i++) {
            img.setRGB(i, py, ctx.color);
         }
         for (int i = left; i <= right; i++) {
            if (py > 0 && img.getRGB(i, py - 1) == target) {
               stack.push(new int[]{i, py - 1});
            }
            if (py < h - 1 && img.getRGB(i, py + 1) == target) {
               stack.push(new int[]{i, py + 1});
            }
         }
      }
      if (ctx.selection() != null) {
         ctx.selection().restoreOutside(img, before);
      }
      ctx.history.push(new LayerPixelsCommand(getDisplayName(), ctx.document, ctx.layer, before));
      ctx.document.changed();
   }

   @Override
   public void onDrag(ToolContext ctx, int x, int y) {
   }

   @Override
   public void onRelease(ToolContext ctx, int x, int y) {
   }
}
