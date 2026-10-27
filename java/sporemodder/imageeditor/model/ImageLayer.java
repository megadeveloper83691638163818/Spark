package sporemodder.imageeditor.model;

import java.awt.image.BufferedImage;

public class ImageLayer {
   private String name;
   private boolean visible = true;
   private double opacity = 1.0;
   private BlendMode blendMode = BlendMode.NORMAL;
   private BufferedImage pixels;

   public ImageLayer(String name, BufferedImage pixels) {
      this.name = name;
      this.pixels = toArgb(pixels);
   }

   public static ImageLayer createEmpty(String name, int w, int h) {
      return new ImageLayer(name, new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB));
   }

   public String getName() {
      return name;
   }

   public void setName(String name) {
      this.name = name;
   }

   public boolean isVisible() {
      return visible;
   }

   public void setVisible(boolean visible) {
      this.visible = visible;
   }

   public double getOpacity() {
      return opacity;
   }

   public void setOpacity(double opacity) {
      this.opacity = Math.max(0.0, Math.min(1.0, opacity));
   }

   public BlendMode getBlendMode() {
      return blendMode;
   }

   public void setBlendMode(BlendMode mode) {
      this.blendMode = mode == null ? BlendMode.NORMAL : mode;
   }

   public int getWidth() {
      return pixels.getWidth();
   }

   public int getHeight() {
      return pixels.getHeight();
   }

   public BufferedImage getPixels() {
      return pixels;
   }

   public void setPixels(BufferedImage img) {
      this.pixels = toArgb(img);
   }

   public BufferedImage copyPixels() {
      BufferedImage copy = new BufferedImage(pixels.getWidth(), pixels.getHeight(), BufferedImage.TYPE_INT_ARGB);
      java.awt.Graphics2D g = copy.createGraphics();
      g.setComposite(java.awt.AlphaComposite.Src);
      g.drawImage(pixels, 0, 0, null);
      g.dispose();
      return copy;
   }

   private static BufferedImage toArgb(BufferedImage img) {
      if (img.getType() == BufferedImage.TYPE_INT_ARGB) {
         return img;
      }
      BufferedImage out = new BufferedImage(img.getWidth(), img.getHeight(), BufferedImage.TYPE_INT_ARGB);
      java.awt.Graphics2D g = out.createGraphics();
      g.drawImage(img, 0, 0, null);
      g.dispose();
      return out;
   }
}
