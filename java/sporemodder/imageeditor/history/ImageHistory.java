package sporemodder.imageeditor.history;

import java.util.ArrayDeque;
import java.util.Deque;

public class ImageHistory {
   private static final int MAX_COMMANDS = 50;
   private final Deque<ImageCommand> undoStack = new ArrayDeque<>();
   private final Deque<ImageCommand> redoStack = new ArrayDeque<>();

   public void push(ImageCommand var1) {
      this.undoStack.push(var1);
      this.redoStack.clear();

      while (this.undoStack.size() > 50) {
         this.undoStack.removeLast();
      }
   }

   public boolean canUndo() {
      return !this.undoStack.isEmpty();
   }

   public boolean canRedo() {
      return !this.redoStack.isEmpty();
   }

   public void undo() {
      if (this.canUndo()) {
         ImageCommand var1 = this.undoStack.pop();
         var1.undo();
         this.redoStack.push(var1);
      }
   }

   public void redo() {
      if (this.canRedo()) {
         ImageCommand var1 = this.redoStack.pop();
         var1.redo();
         this.undoStack.push(var1);
      }
   }
}
