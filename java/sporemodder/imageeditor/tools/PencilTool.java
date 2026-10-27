package sporemodder.imageeditor.tools;

public class PencilTool extends StrokeTool {
   @Override
   public String getId() {
      return "pencil";
   }

   @Override
   public String getDisplayName() {
      return "Pencil";
   }

   @Override
   protected int brushColor(ToolContext ctx) {
      return ctx.color;
   }
}
