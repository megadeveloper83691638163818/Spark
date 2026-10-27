package sporemodder.imageeditor.filters;

import java.awt.image.BufferedImage;
import java.util.List;

/** A filter with adjustable parameters; the editor shows a dialog with live preview. */
public abstract class ParametricFilter implements ImageFilter {
   public static class Param {
      public final String name;
      public final double min;
      public final double max;
      public final double def;

      public Param(String name, double min, double max, double def) {
         this.name = name;
         this.min = min;
         this.max = max;
         this.def = def;
      }
   }

   private final String name;
   private final String category;
   private final List<Param> params;

   protected ParametricFilter(String category, String name, List<Param> params) {
      this.category = category;
      this.name = name;
      this.params = params;
   }

   public List<Param> getParams() {
      return params;
   }

   public double[] defaults() {
      double[] d = new double[params.size()];
      for (int i = 0; i < d.length; i++) {
         d[i] = params.get(i).def;
      }
      return d;
   }

   public abstract void apply(BufferedImage image, double[] values);

   @Override
   public void apply(BufferedImage image) {
      apply(image, defaults());
   }

   @Override
   public String getDisplayName() {
      return name;
   }

   @Override
   public String getCategory() {
      return category;
   }
}
