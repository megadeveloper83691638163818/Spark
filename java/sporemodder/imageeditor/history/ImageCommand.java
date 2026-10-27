package sporemodder.imageeditor.history;

public interface ImageCommand {
   String getName();

   void undo();

   void redo();
}
