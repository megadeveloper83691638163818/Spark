package sporemodder.imageeditor.tools;

public interface ImageTool {
   String getId();

   String getDisplayName();

   void onPress(ToolContext var1, int var2, int var3);

   void onDrag(ToolContext var1, int var2, int var3);

   void onRelease(ToolContext var1, int var2, int var3);
}
