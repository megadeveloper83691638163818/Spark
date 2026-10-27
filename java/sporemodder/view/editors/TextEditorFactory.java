package sporemodder.view.editors;

import javafx.scene.Node;
import sporemodder.util.ProjectItem;

public class TextEditorFactory implements EditorFactory {
   public TextEditor createInstance() {
      TextEditor EDITOR = new TextEditor();
      ModApiCompletion.INSTALL(EDITOR);
      return EDITOR;
   }

   @Override
   public boolean isSupportedFile(ProjectItem var1) {
      return !var1.isFolder();
   }

   @Override
   public Node getIcon(ProjectItem var1) {
      return null;
   }
}
