package sporemodder.imageeditor.history;

import java.util.ArrayList;
import java.util.List;
import sporemodder.imageeditor.model.ImageDocument;
import sporemodder.imageeditor.model.ImageLayer;

/** Undo/redo of structural layer changes (add, remove, duplicate, reorder). */
public class LayerListCommand implements ImageCommand {
   private final String name;
   private final ImageDocument document;
   private final List<ImageLayer> before;
   private final List<ImageLayer> after;

   public LayerListCommand(String name, ImageDocument document, List<ImageLayer> before) {
      this.name = name;
      this.document = document;
      this.before = new ArrayList<>(before);
      this.after = new ArrayList<>(document.getLayers());
   }

   @Override
   public String getName() {
      return name;
   }

   @Override
   public void undo() {
      document.setLayers(before);
   }

   @Override
   public void redo() {
      document.setLayers(after);
   }
}
