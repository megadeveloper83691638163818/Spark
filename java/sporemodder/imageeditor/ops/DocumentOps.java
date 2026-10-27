package sporemodder.imageeditor.ops;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import sporemodder.imageeditor.history.DocumentSnapshotCommand;
import sporemodder.imageeditor.history.ImageHistory;
import sporemodder.imageeditor.model.ImageDocument;
import sporemodder.imageeditor.model.ImageLayer;
import sporemodder.imageeditor.model.Selection;

/** Whole-image operations: rotate, flip, resize, canvas size, crop, flatten, merge down. Each one is undoable. */
public final class DocumentOps {
   private DocumentOps() {
   }

   private interface LayerOp {
      BufferedImage apply(BufferedImage src);
   }

   private static void run(String name, ImageDocument doc, ImageHistory history, int newW, int newH, LayerOp op) {
      DocumentSnapshotCommand.Snapshot before = new DocumentSnapshotCommand.Snapshot(doc);
      for (ImageLayer layer : doc.getLayers()) {
         layer.setPixels(op.apply(layer.getPixels()));
      }
      doc.setSize(newW, newH);
      doc.setSelection(null);
      doc.changed();
      history.push(new DocumentSnapshotCommand(name, doc, before));
   }

   private static BufferedImage transformed(BufferedImage src, int w, int h, AffineTransform t) {
      BufferedImage out = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
      Graphics2D g = out.createGraphics();
      g.drawImage(src, t, null);
      g.dispose();
      return out;
   }

   public static void rotate(ImageDocument doc, ImageHistory history, int quarterTurnsCw) {
      int turns = ((quarterTurnsCw % 4) + 4) % 4;
      if (turns == 0) {
         return;
      }
      int w = doc.getWidth();
      int h = doc.getHeight();
      boolean swap = turns % 2 == 1;
      int nw = swap ? h : w;
      int nh = swap ? w : h;
      String name = turns == 1 ? "Rotate 90° CW" : (turns == 3 ? "Rotate 90° CCW" : "Rotate 180°");
      run(name, doc, history, nw, nh, src -> {
         AffineTransform t = new AffineTransform();
         t.translate(nw / 2.0, nh / 2.0);
         t.rotate(Math.toRadians(90 * turns));
         t.translate(-src.getWidth() / 2.0, -src.getHeight() / 2.0);
         return transformed(src, nw, nh, t);
      });
   }

   public static void flip(ImageDocument doc, ImageHistory history, boolean horizontal) {
      run(horizontal ? "Flip Image Horizontally" : "Flip Image Vertically", doc, history, doc.getWidth(), doc.getHeight(), src -> {
         AffineTransform t = horizontal ? new AffineTransform(-1, 0, 0, 1, src.getWidth(), 0) : new AffineTransform(1, 0, 0, -1, 0, src.getHeight());
         return transformed(src, src.getWidth(), src.getHeight(), t);
      });
   }

   public static void resizeImage(ImageDocument doc, ImageHistory history, int nw, int nh) {
      run("Resize Image", doc, history, nw, nh, src -> {
         BufferedImage out = new BufferedImage(nw, nh, BufferedImage.TYPE_INT_ARGB);
         Graphics2D g = out.createGraphics();
         g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
         g.drawImage(src, 0, 0, nw, nh, null);
         g.dispose();
         return out;
      });
   }

   public static void resizeCanvas(ImageDocument doc, ImageHistory history, int nw, int nh) {
      int ox = (nw - doc.getWidth()) / 2;
      int oy = (nh - doc.getHeight()) / 2;
      run("Canvas Size", doc, history, nw, nh, src -> {
         BufferedImage out = new BufferedImage(nw, nh, BufferedImage.TYPE_INT_ARGB);
         Graphics2D g = out.createGraphics();
         g.drawImage(src, ox, oy, null);
         g.dispose();
         return out;
      });
   }

   public static boolean cropToSelection(ImageDocument doc, ImageHistory history) {
      Selection sel = doc.getSelection();
      int[] b = sel == null ? null : sel.bounds();
      if (b == null) {
         return false;
      }
      int nw = b[2] - b[0] + 1;
      int nh = b[3] - b[1] + 1;
      run("Crop", doc, history, nw, nh, src -> {
         BufferedImage out = new BufferedImage(nw, nh, BufferedImage.TYPE_INT_ARGB);
         Graphics2D g = out.createGraphics();
         g.drawImage(src, -b[0], -b[1], null);
         g.dispose();
         return out;
      });
      return true;
   }

   public static void flatten(ImageDocument doc, ImageHistory history) {
      if (doc.getLayers().size() <= 1) {
         return;
      }
      DocumentSnapshotCommand.Snapshot before = new DocumentSnapshotCommand.Snapshot(doc);
      ImageLayer merged = new ImageLayer("Background", doc.flatten());
      List<ImageLayer> list = new ArrayList<>();
      list.add(merged);
      doc.setLayers(list);
      history.push(new DocumentSnapshotCommand("Flatten Image", doc, before));
   }

   public static void mergeDown(ImageDocument doc, ImageHistory history, ImageLayer layer) {
      List<ImageLayer> layers = new ArrayList<>(doc.getLayers());
      int i = layers.indexOf(layer);
      if (i <= 0) {
         return;
      }
      DocumentSnapshotCommand.Snapshot before = new DocumentSnapshotCommand.Snapshot(doc);
      ImageLayer below = layers.get(i - 1);
      ImageDocument tmp = new ImageDocument(doc.getWidth(), doc.getHeight());
      tmp.addLayer(new ImageLayer(below.getName(), below.getPixels()));
      tmp.getLayers().get(0).setOpacity(below.getOpacity());
      tmp.getLayers().get(0).setBlendMode(below.getBlendMode());
      ImageLayer top = new ImageLayer(layer.getName(), layer.getPixels());
      top.setOpacity(layer.getOpacity());
      top.setBlendMode(layer.getBlendMode());
      top.setVisible(layer.isVisible());
      tmp.addLayer(top);
      below.setPixels(tmp.flatten());
      below.setOpacity(1.0);
      below.setBlendMode(sporemodder.imageeditor.model.BlendMode.NORMAL);
      layers.remove(i);
      doc.setLayers(layers);
      history.push(new DocumentSnapshotCommand("Merge Down", doc, before));
   }
}
