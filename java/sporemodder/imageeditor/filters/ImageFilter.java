package sporemodder.imageeditor.filters;

import java.awt.image.BufferedImage;

public interface ImageFilter {
   String getDisplayName();

   void apply(BufferedImage image);

   /** Menu this filter is listed in. */
   default String getCategory() {
      return "Filters";
   }
}
