package sporemodder.imageeditor.tools;

import java.awt.image.BufferedImage;

/** Eyedropper: copies the colour under the cursor into the colour picker. */
public class PickerTool implements ImageTool {
   @Override
   public String getId() {
      return "picker";
   }

   @Override
   public String getDisplayName() {
      return "Picker";
   }

   @Override
   public void onPress(ToolContext ctx, int x, int y) {
      pick(ctx, x, y);
   }

   @Override
   public void onDrag(ToolContext ctx, int x, int y) {
      pick(ctx, x, y);
   }

   @Override
   public void onRelease(ToolContext ctx, int x, int y) {
   }

   private void pick(ToolContext ctx, int x, int y) {
      BufferedImage img = ctx.document.flatten();
      if (ctx.colorSink != null && x >= 0 && y >= 0 && x < img.getWidth() && y < img.getHeight()) {
         ctx.colorSink.accept(img.getRGB(x, y));
      }
   }
}
