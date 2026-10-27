package sporemodder.imageeditor.tools;

import java.awt.image.BufferedImage;
import java.util.ArrayDeque;
import sporemodder.imageeditor.model.Selection;

/** Magic wand: selects the contiguous area of similar colour (tolerance depends on the brush Hardness slider). */
public class WandTool implements ImageTool {
   @Override
   public String getId() {
      return "wand";
   }

   @Override
   public String getDisplayName() {
      return "Magic Wand";
   }

   @Override
   public void onPress(ToolContext ctx, int x, int y) {
      BufferedImage img = ctx.layer.getPixels();
      int w = img.getWidth();
      int h = img.getHeight();
      if (x < 0 || y < 0 || x >= w || y >= h) {
         return;
      }
      int tolerance = (int) Math.round((1 - ctx.hardness) * 128);
      int target = img.getRGB(x, y);
      Selection sel = new Selection(ctx.document.getWidth(), ctx.document.getHeight());
      boolean[] seen = new boolean[w * h];
      ArrayDeque<int[]> stack = new ArrayDeque<>();
      stack.push(new int[]{x, y});
      while (!stack.isEmpty()) {
         int[] p = stack.pop();
         int px = p[0];
         int py = p[1];
         if (px < 0 || py < 0 || px >= w || py >= h || seen[py * w + px]) {
            continue;
         }
         seen[py * w + px] = true;
         if (distance(img.getRGB(px, py), target) > tolerance) {
            continue;
         }
         sel.set(px, py, 255);
         stack.push(new int[]{px + 1, py});
         stack.push(new int[]{px - 1, py});
         stack.push(new int[]{px, py + 1});
         stack.push(new int[]{px, py - 1});
      }
      ctx.document.setSelection(sel);
   }

   private static int distance(int a, int b) {
      int d = 0;
      for (int shift = 0; shift <= 24; shift += 8) {
         d = Math.max(d, Math.abs((a >>> shift & 255) - (b >>> shift & 255)));
      }
      return d;
   }

   @Override
   public void onDrag(ToolContext ctx, int x, int y) {
   }

   @Override
   public void onRelease(ToolContext ctx, int x, int y) {
   }
}
