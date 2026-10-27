package sporemodder.imageeditor.history;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import sporemodder.imageeditor.model.ImageDocument;
import sporemodder.imageeditor.model.ImageLayer;

/** Undo/redo of whole-image operations (rotate, resize, crop...): size plus all layer pixels. */
public class DocumentSnapshotCommand implements ImageCommand {
   public static class Snapshot {
      final int width;
      final int height;
      final List<ImageLayer> layers = new ArrayList<>();
      final List<BufferedImage> pixels = new ArrayList<>();

      public Snapshot(ImageDocument doc) {
         this.width = doc.getWidth();
         this.height = doc.getHeight();
         for (ImageLayer layer : doc.getLayers()) {
            layers.add(layer);
            pixels.add(layer.copyPixels());
         }
      }

      void restore(ImageDocument doc) {
         doc.setSize(width, height);
         for (int i = 0; i < layers.size(); i++) {
            BufferedImage copy = new BufferedImage(pixels.get(i).getWidth(), pixels.get(i).getHeight(), BufferedImage.TYPE_INT_ARGB);
            java.awt.Graphics2D g = copy.createGraphics();
            g.setComposite(java.awt.AlphaComposite.Src);
            g.drawImage(pixels.get(i), 0, 0, null);
            g.dispose();
            layers.get(i).setPixels(copy);
         }
         doc.setSelection(null);
         doc.setLayers(layers);
      }
   }

   private final String name;
   private final ImageDocument document;
   private final Snapshot before;
   private final Snapshot after;

   public DocumentSnapshotCommand(String name, ImageDocument document, Snapshot before) {
      this.name = name;
      this.document = document;
      this.before = before;
      this.after = new Snapshot(document);
   }

   @Override
   public String getName() {
      return name;
   }

   @Override
   public void undo() {
      before.restore(document);
   }

   @Override
   public void redo() {
      after.restore(document);
   }
}
