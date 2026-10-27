package sporemodder.imageeditor.filters;

import java.awt.image.BufferedImage;

/** Wraps a filter to place it in another menu. */
public class CategorizedFilter implements ImageFilter {
   private final String category;
   private final ImageFilter inner;

   public CategorizedFilter(String category, ImageFilter inner) {
      this.category = category;
      this.inner = inner;
   }

   @Override
   public String getDisplayName() {
      return inner.getDisplayName();
   }

   @Override
   public void apply(BufferedImage image) {
      inner.apply(image);
   }

   @Override
   public String getCategory() {
      return category;
   }
}
