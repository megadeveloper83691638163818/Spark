package sporemodder.imageeditor.tools;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import sporemodder.imageeditor.history.LayerPixelsCommand;

/** Line / rectangle / ellipse: drag to preview, release to commit. */
public class ShapeTool implements ImageTool {
   public enum Kind { LINE, RECTANGLE, ELLIPSE }

   private final Kind kind;
   private BufferedImage before;
   private int startX;
   private int startY;

   public ShapeTool(Kind kind) {
      this.kind = kind;
   }

   @Override
   public String getId() {
      return kind.name().toLowerCase();
   }

   @Override
   public String getDisplayName() {
      switch (kind) {
         case LINE: return "Line";
         case RECTANGLE: return "Rectangle";
         default: return "Ellipse";
      }
   }

   @Override
   public void onPress(ToolContext ctx, int x, int y) {
      this.before = ctx.layer.copyPixels();
      this.startX = x;
      this.startY = y;
      draw(ctx, x, y);
   }

   @Override
   public void onDrag(ToolContext ctx, int x, int y) {
      if (this.before != null) {
         draw(ctx, x, y);
      }
   }

   @Override
   public void onRelease(ToolContext ctx, int x, int y) {
      if (this.before != null) {
         draw(ctx, x, y);
         ctx.history.push(new LayerPixelsCommand(getDisplayName(), ctx.document, ctx.layer, this.before));
         this.before = null;
      }
   }

   private void draw(ToolContext ctx, int x, int y) {
      BufferedImage restored = new BufferedImage(before.getWidth(), before.getHeight(), BufferedImage.TYPE_INT_ARGB);
      Graphics2D g = restored.createGraphics();
      g.drawImage(before, 0, 0, null);
      g.setColor(new Color(ctx.color, true));
      g.setStroke(new BasicStroke(ctx.size, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_MITER));
      int x0 = Math.min(startX, x);
      int y0 = Math.min(startY, y);
      int w = Math.abs(x - startX);
      int h = Math.abs(y - startY);
      switch (kind) {
         case LINE: g.drawLine(startX, startY, x, y); break;
         case RECTANGLE: g.drawRect(x0, y0, w, h); break;
         default: g.drawOval(x0, y0, w, h); break;
      }
      g.dispose();
      if (ctx.selection() != null) {
         ctx.selection().restoreOutside(restored, before);
      }
      ctx.layer.setPixels(restored);
      ctx.document.changed();
   }
}
