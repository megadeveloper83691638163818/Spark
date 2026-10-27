package sporemodder.imageeditor.model;

import java.awt.AlphaComposite;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ImageDocument {
   private int width;
   private int height;
   private final List<ImageLayer> layers = new ArrayList<>();
   private final List<Runnable> changeListeners = new ArrayList<>();
   private File file;
   private boolean modified;
   private Selection selection;

   public ImageDocument(int width, int height) {
      this.width = width;
      this.height = height;
   }

   public static ImageDocument fromImage(BufferedImage img, File file) {
      ImageDocument doc = new ImageDocument(img.getWidth(), img.getHeight());
      doc.layers.add(new ImageLayer("Background", img));
      doc.file = file;
      return doc;
   }

   public int getWidth() {
      return width;
   }

   public int getHeight() {
      return height;
   }

   public void setSize(int width, int height) {
      this.width = width;
      this.height = height;
   }

   public File getFile() {
      return file;
   }

   public void setFile(File file) {
      this.file = file;
   }

   public List<ImageLayer> getLayers() {
      return Collections.unmodifiableList(layers);
   }

   public void addLayer(ImageLayer layer) {
      layers.add(layer);
      changed();
   }

   public void removeLayer(ImageLayer layer) {
      if (layers.size() > 1 && layers.remove(layer)) {
         changed();
      }
   }

   public void setLayers(List<ImageLayer> list) {
      layers.clear();
      layers.addAll(list);
      changed();
   }

   public void insertLayer(int index, ImageLayer layer) {
      layers.add(Math.max(0, Math.min(layers.size(), index)), layer);
      changed();
   }

   public boolean moveLayer(ImageLayer layer, int delta) {
      int from = layers.indexOf(layer);
      int to = from + delta;
      if (from < 0 || to < 0 || to >= layers.size()) {
         return false;
      }
      layers.remove(from);
      layers.add(to, layer);
      changed();
      return true;
   }

   public Selection getSelection() {
      return selection;
   }

   /** Changes the selection (does not mark the document as modified). */
   public void setSelection(Selection selection) {
      this.selection = selection;
      fire();
   }

   public boolean isModified() {
      return modified;
   }

   public void setModified(boolean modified) {
      this.modified = modified;
   }

   public void addChangeListener(Runnable listener) {
      changeListeners.add(listener);
   }

   public void changed() {
      modified = true;
      fire();
   }

   private void fire() {
      for (Runnable r : new ArrayList<>(changeListeners)) {
         r.run();
      }
   }

   public BufferedImage flatten() {
      BufferedImage out = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
      for (ImageLayer layer : layers) {
         if (!layer.isVisible()) {
            continue;
         }
         if (layer.getBlendMode() == BlendMode.NORMAL) {
            Graphics2D g = out.createGraphics();
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, (float) layer.getOpacity()));
            g.drawImage(layer.getPixels(), 0, 0, null);
            g.dispose();
         } else {
            blendInto(out, layer);
         }
      }
      return out;
   }

   private void blendInto(BufferedImage dst, ImageLayer layer) {
      BufferedImage src = layer.getPixels();
      BlendMode mode = layer.getBlendMode();
      int w = Math.min(width, src.getWidth());
      int h = Math.min(height, src.getHeight());
      for (int y = 0; y < h; y++) {
         for (int x = 0; x < w; x++) {
            int s = src.getRGB(x, y);
            double as = (s >>> 24) / 255.0 * layer.getOpacity();
            if (as <= 0) {
               continue;
            }
            int d = dst.getRGB(x, y);
            double ab = (d >>> 24) / 255.0;
            double ao = as + ab * (1 - as);
            int out = (int) Math.round(ao * 255) << 24;
            for (int shift = 16; shift >= 0; shift -= 8) {
               double cs = (s >>> shift & 255) / 255.0;
               double cb = (d >>> shift & 255) / 255.0;
               double cs2 = (1 - ab) * cs + ab * mode.blend(cs, cb);
               double co = (as * cs2 + (1 - as) * ab * cb) / ao;
               out |= (int) Math.round(Math.max(0, Math.min(1, co)) * 255) << shift;
            }
            dst.setRGB(x, y, out);
         }
      }
   }
}
