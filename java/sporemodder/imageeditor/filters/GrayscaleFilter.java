package sporemodder.imageeditor.filters;

import java.awt.image.BufferedImage;

public class GrayscaleFilter implements ImageFilter {
   @Override
   public String getDisplayName() {
      return "Grayscale";
   }

   @Override
   public void apply(BufferedImage var1) {
      for (int var2 = 0; var2 < var1.getHeight(); var2++) {
         for (int var3 = 0; var3 < var1.getWidth(); var3++) {
            int var4 = var1.getRGB(var3, var2);
            int var5 = var4 >> 16 & 0xFF;
            int var6 = var4 >> 8 & 0xFF;
            int var7 = var4 & 0xFF;
            int var8 = (int)Math.round(0.299 * var5 + 0.587 * var6 + 0.114 * var7);
            var1.setRGB(var3, var2, var4 & 0xFF000000 | var8 << 16 | var8 << 8 | var8);
         }
      }
   }
}
