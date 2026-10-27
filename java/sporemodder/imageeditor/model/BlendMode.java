package sporemodder.imageeditor.model;

public enum BlendMode {
   NORMAL("Normal"), MULTIPLY("Multiply"), SCREEN("Screen"), OVERLAY("Overlay"), DARKEN("Darken"), LIGHTEN("Lighten"),
   COLOR_DODGE("Color Dodge"), COLOR_BURN("Color Burn"), HARD_LIGHT("Hard Light"), SOFT_LIGHT("Soft Light"),
   DIFFERENCE("Difference"), EXCLUSION("Exclusion"), ADD("Add"), SUBTRACT("Subtract");

   private final String displayName;

   BlendMode(String displayName) {
      this.displayName = displayName;
   }

   public String getDisplayName() {
      return displayName;
   }

   @Override
   public String toString() {
      return displayName;
   }

   /** Blend function on channel values in 0..1; s = layer (source), d = backdrop. */
   public double blend(double s, double d) {
      switch (this) {
         case MULTIPLY: return s * d;
         case SCREEN: return s + d - s * d;
         case OVERLAY: return HARD_LIGHT.blend(d, s);
         case DARKEN: return Math.min(s, d);
         case LIGHTEN: return Math.max(s, d);
         case COLOR_DODGE: return s >= 1 ? 1 : Math.min(1, d / (1 - s));
         case COLOR_BURN: return s <= 0 ? 0 : 1 - Math.min(1, (1 - d) / s);
         case HARD_LIGHT: return s <= 0.5 ? d * 2 * s : SCREEN.blend(2 * s - 1, d);
         case SOFT_LIGHT: {
            if (s <= 0.5) {
               return d - (1 - 2 * s) * d * (1 - d);
            }
            double g = d <= 0.25 ? ((16 * d - 12) * d + 4) * d : Math.sqrt(d);
            return d + (2 * s - 1) * (g - d);
         }
         case DIFFERENCE: return Math.abs(s - d);
         case EXCLUSION: return s + d - 2 * s * d;
         case ADD: return Math.min(1, s + d);
         case SUBTRACT: return Math.max(0, d - s);
         default: return s;
      }
   }
}
