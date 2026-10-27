package sporemodder.imageeditor;

import java.util.Properties;
import sporemodder.AbstractManager;
import sporemodder.MainApp;

public class ImageEditorManager extends AbstractManager {
   private static final String PROPERTY_enabled = "imageEditor.enabled";
   private final ImageEditorRegistry registry = new ImageEditorRegistry();
   private boolean enabled;

   public static ImageEditorManager get() {
      return MainApp.get().getImageEditorManager();
   }

   @Override
   public void initialize(Properties var1) {
      this.enabled = Boolean.parseBoolean(var1.getProperty("imageEditor.enabled", "false"));
      this.registry.addModule(new BuiltInImageModule());
   }

   @Override
   public void saveSettings(Properties var1) {
      var1.setProperty("imageEditor.enabled", Boolean.toString(this.enabled));
   }

   public ImageEditorRegistry getRegistry() {
      return this.registry;
   }

   public boolean isEnabled() {
      return this.enabled;
   }

   public void setEnabled(boolean var1) {
      this.enabled = var1;
   }
}
