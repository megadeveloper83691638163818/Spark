package sporemodder.imageeditor.tools;

import java.awt.image.BufferedImage;

/** Dodge (lighten) and Burn (darken) brushes. */
public class DodgeBurnTool extends DabTool {
   private final boolean dodge;

   public DodgeBurnTool(boolean dodge) {
      this.dodge = dodge;
   }

   @Override
   public String getId() {
      return dodge ? "dodge" : "burn";
   }

   @Override
   public String getDisplayName() {
      return dodge ? "Dodge" : "Burn";
   }

   @Override
   protected void applyPixel(ToolContext ctx, BufferedImage img, int x, int y, double amount) {
      double k = amount * 0.25;
      int p = img.getRGB(x, y);
      int out = p & 0xFF000000;
      for (int shift = 16; shift >= 0; shift -= 8) {
         double c = p >>> shift & 255;
         c = dodge ? c + (255 - c) * k : c * (1 - k);
         out |= (int) Math.round(Math.max(0, Math.min(255, c))) << shift;
      }
      img.setRGB(x, y, out);
   }
}
