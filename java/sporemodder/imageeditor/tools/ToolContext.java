package sporemodder.imageeditor.tools;

import java.util.function.IntConsumer;
import sporemodder.imageeditor.history.ImageHistory;
import sporemodder.imageeditor.model.ImageDocument;
import sporemodder.imageeditor.model.ImageLayer;
import sporemodder.imageeditor.model.Selection;

public class ToolContext {
   public final ImageDocument document;
   public final ImageLayer layer;
   public final ImageHistory history;
   public final int color;
   public final int secondaryColor;
   public final int size;
   public final double opacity;
   public final double hardness;
   public final IntConsumer colorSink;

   public ToolContext(ImageDocument document, ImageLayer layer, ImageHistory history, int color, int secondaryColor,
                      int size, double opacity, double hardness, IntConsumer colorSink) {
      this.document = document;
      this.layer = layer;
      this.history = history;
      this.color = color;
      this.secondaryColor = secondaryColor;
      this.size = Math.max(1, size);
      this.opacity = Math.max(0, Math.min(1, opacity));
      this.hardness = Math.max(0, Math.min(1, hardness));
      this.colorSink = colorSink;
   }

   public Selection selection() {
      return document.getSelection();
   }

   /** Selection weight (0..1) of a pixel; 1 when nothing is selected. */
   public double selectionAt(int x, int y) {
      Selection s = document.getSelection();
      return s == null ? 1.0 : s.get(x, y) / 255.0;
   }
}
