package sporemodder.imageeditor.filters;

import java.awt.image.BufferedImage;

public class InvertFilter implements ImageFilter {
   @Override
   public String getDisplayName() {
      return "Invert";
   }

   @Override
   public void apply(BufferedImage var1) {
      for (int var2 = 0; var2 < var1.getHeight(); var2++) {
         for (int var3 = 0; var3 < var1.getWidth(); var3++) {
            int var4 = var1.getRGB(var3, var2);
            var1.setRGB(var3, var2, var4 & 0xFF000000 | ~var4 & 16777215);
         }
      }
   }
}
