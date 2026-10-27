package sporemodder.imageeditor.history;

import java.awt.image.BufferedImage;
import sporemodder.imageeditor.model.ImageDocument;
import sporemodder.imageeditor.model.ImageLayer;

public class LayerPixelsCommand implements ImageCommand {
   private final String name;
   private final ImageDocument document;
   private final ImageLayer layer;
   private final BufferedImage before;
   private final BufferedImage after;

   public LayerPixelsCommand(String var1, ImageDocument var2, ImageLayer var3, BufferedImage var4) {
      this.name = var1;
      this.document = var2;
      this.layer = var3;
      this.before = var4;
      this.after = var3.copyPixels();
   }

   @Override
   public String getName() {
      return this.name;
   }

   @Override
   public void undo() {
      this.layer.setPixels(this.before);
      this.document.changed();
   }

   @Override
   public void redo() {
      this.layer.setPixels(this.after);
      this.document.changed();
   }
}
