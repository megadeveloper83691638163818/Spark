package sporemodder.view.editors;

import sporemodder.file.lvl.LevelDefinition;
import sporemodder.view.UserInterface;

public class LvlEditor extends ArgScriptEditor<LevelDefinition> {
   public LvlEditor() {
      LevelDefinition unit = new LevelDefinition();
      this.stream = unit.generateStream();
   }

   @Override
   protected void onStreamParse() {
      this.stream.getData().clear();
   }

   @Override
   protected void showInspector(boolean show) {
      if (show) {
         UserInterface.get().getInspectorPane().configureDefault("Level Definition (LVL)", "LVL", null);
      } else {
         UserInterface.get().getInspectorPane().reset();
      }
   }

   @Override
   public void setActive(boolean isActive) {
      super.setActive(isActive);
      this.showInspector(isActive);
   }
}
