package sporemodder.imageeditor;

import java.io.File;
import java.io.IOException;
import javafx.scene.Node;
import sporemodder.UIManager;
import sporemodder.imageeditor.io.ImageFormat;
import sporemodder.imageeditor.model.ImageDocument;
import sporemodder.imageeditor.ui.ImageEditorView;
import sporemodder.util.ProjectItem;
import sporemodder.view.editors.EditorFactory;
import sporemodder.view.editors.ItemEditor;

public class ImageEditor implements ItemEditor {
   private final ImageEditorView view = new ImageEditorView(ImageEditorManager.get().getRegistry());
   private ImageDocument document;
   private File file;

   private ImageEditor() {
      this.view.setSaveAction(this::save);
   }

   @Override
   public void loadFile(ProjectItem var1) throws IOException {
      if (var1 == null) {
         this.document = null;
      } else {
         this.file = var1.getFile();
         ImageFormat var2 = ImageEditorManager.get().getRegistry().findFormat(var1.getSpecificExtension());
         if (var2 == null) {
            throw new IOException("Unsupported image type: " + var1.getSpecificExtension());
         } else {
            this.document = var2.read(this.file);
            this.view.setDocument(this.document);
            this.document.setModified(false);
         }
      }
   }

   @Override
   public void setDestinationFile(File var1) {
      this.file = var1;
   }

   @Override
   public void setActive(boolean var1) {
   }

   @Override
   public Node getUI() {
      return this.view;
   }

   @Override
   public void save() {
      if (this.document != null && this.file != null && this.document.isModified()) {
         String var1 = this.file.getName();
         ImageFormat var2 = ImageEditorManager.get().getRegistry().findFormat(var1.substring(var1.lastIndexOf(46) + 1));
         UIManager.get().tryAction(() -> {
            var2.write(this.document, this.file);
            this.document.setModified(false);
         }, "Cannot save the image.");
      }
   }

   @Override
   public boolean isEditable() {
      return true;
   }

   @Override
   public boolean supportsSearching() {
      return false;
   }

   @Override
   public boolean supportsEditHistory() {
      return false;
   }

   public static class Factory implements EditorFactory {
      @Override
      public ItemEditor createInstance() {
         return new ImageEditor();
      }

      @Override
      public boolean isSupportedFile(ProjectItem var1) {
         ImageEditorManager var2 = ImageEditorManager.get();
         return var2.isEnabled() && !var1.isFolder() && var2.getRegistry().isSupportedExtension(var1.getSpecificExtension());
      }

      @Override
      public Node getIcon(ProjectItem var1) {
         return null;
      }
   }
}
