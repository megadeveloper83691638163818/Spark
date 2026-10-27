package sporemodder.imageeditor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import sporemodder.imageeditor.filters.ImageFilter;
import sporemodder.imageeditor.io.ImageFormat;
import sporemodder.imageeditor.tools.ImageTool;

public class ImageEditorRegistry {
   private final List<ImageTool> tools = new ArrayList<>();
   private final List<ImageFilter> filters = new ArrayList<>();
   private final List<ImageFormat> formats = new ArrayList<>();

   public void addModule(ImageEditorModule var1) {
      var1.register(this);
   }

   public void addTool(ImageTool var1) {
      this.tools.add(var1);
   }

   public void addFilter(ImageFilter var1) {
      this.filters.add(var1);
   }

   public void addFormat(ImageFormat var1) {
      this.formats.add(var1);
   }

   public List<ImageTool> getTools() {
      return Collections.unmodifiableList(this.tools);
   }

   public List<ImageFilter> getFilters() {
      return Collections.unmodifiableList(this.filters);
   }

   public ImageFormat findFormat(String var1) {
      String var2 = var1.toLowerCase();

      for (int var3 = this.formats.size() - 1; var3 >= 0; var3--) {
         if (this.formats.get(var3).getExtensions().contains(var2)) {
            return this.formats.get(var3);
         }
      }

      return null;
   }

   public boolean isSupportedExtension(String var1) {
      return this.findFormat(var1) != null;
   }
}
