package sporemodder.imageeditor.tools;

import sporemodder.imageeditor.model.Selection;

/** Rectangular or elliptical selection. A click without dragging clears the selection. */
public class MarqueeTool implements ImageTool {
   private final boolean ellipse;
   private int startX;
   private int startY;
   private boolean active;

   public MarqueeTool(boolean ellipse) {
      this.ellipse = ellipse;
   }

   @Override
   public String getId() {
      return ellipse ? "marquee-ellipse" : "marquee-rect";
   }

   @Override
   public String getDisplayName() {
      return ellipse ? "Ellipse Select" : "Rect Select";
   }

   @Override
   public void onPress(ToolContext ctx, int x, int y) {
      startX = x;
      startY = y;
      active = true;
   }

   @Override
   public void onDrag(ToolContext ctx, int x, int y) {
      if (active) {
         int w = ctx.document.getWidth();
         int h = ctx.document.getHeight();
         ctx.document.setSelection(ellipse ? Selection.ellipse(w, h, startX, startY, x, y) : Selection.rect(w, h, startX, startY, x, y));
      }
   }

   @Override
   public void onRelease(ToolContext ctx, int x, int y) {
      if (active) {
         active = false;
         if (x == startX && y == startY) {
            ctx.document.setSelection(null);
         } else {
            onDragFinal(ctx, x, y);
         }
      }
   }

   private void onDragFinal(ToolContext ctx, int x, int y) {
      active = true;
      onDrag(ctx, x, y);
      active = false;
   }
}
