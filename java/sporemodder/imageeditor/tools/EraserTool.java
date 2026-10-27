package sporemodder.imageeditor.tools;

public class EraserTool extends StrokeTool {
   @Override
   public String getId() {
      return "eraser";
   }

   @Override
   public String getDisplayName() {
      return "Eraser";
   }

   @Override
   protected int brushColor(ToolContext ctx) {
      return 0;
   }
}
