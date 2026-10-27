package sporemodder.imageeditor.io;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import javax.imageio.ImageIO;
import sporemodder.imageeditor.model.ImageDocument;

public class JavaImageFormat implements ImageFormat {
   private static final Set<String> EXTENSIONS = Collections.unmodifiableSet(new HashSet<>(Arrays.asList("png", "jpg", "jpeg", "bmp", "gif")));

   @Override
   public Set<String> getExtensions() {
      return EXTENSIONS;
   }

   @Override
   public ImageDocument read(File var1) throws IOException {
      BufferedImage var2 = ImageIO.read(var1);
      if (var2 == null) {
         throw new IOException("Not a supported image: " + var1.getName());
      } else {
         return ImageDocument.fromImage(var2, var1);
      }
   }

   @Override
   public void write(ImageDocument var1, File var2) throws IOException {
      String var3 = getExtension(var2);
      BufferedImage var4 = var1.flatten();
      if (var3.equals("jpg") || var3.equals("jpeg") || var3.equals("bmp")) {
         BufferedImage var5 = new BufferedImage(var4.getWidth(), var4.getHeight(), 1);
         var5.createGraphics().drawImage(var4, 0, 0, Color.WHITE, null);
         var4 = var5;
      }

      if (!ImageIO.write(var4, var3.equals("jpeg") ? "jpg" : var3, var2)) {
         throw new IOException("Cannot write the type '" + var3 + "'.");
      }
   }

   private static String getExtension(File var0) {
      String var1 = var0.getName().toLowerCase();
      return var1.substring(var1.lastIndexOf(46) + 1);
   }
}
