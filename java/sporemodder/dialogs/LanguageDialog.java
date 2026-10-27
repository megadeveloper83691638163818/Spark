package sporemodder.view.dialogs;

import javafx.geometry.Insets;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.VBox;
import sporemodder.Language;
import sporemodder.LanguageManager;
import sporemodder.UIManager;

public class LanguageDialog {
   private LanguageDialog() {
   }

   public static void show() {
      ToggleGroup var0 = new ToggleGroup();
      VBox var1 = new VBox(10.0);
      var1.setPadding(new Insets(10.0));

      for (Language var5 : LanguageManager.get().getLanguages()) {
         RadioButton var6 = new RadioButton(var5.getDisplayName());
         var6.setToggleGroup(var0);
         var6.setUserData(var5);
         var6.setSelected(LanguageManager.get().getLanguage() == var5);
         var1.getChildren().add(var6);
      }

      Dialog var7 = new Dialog();
      var7.setTitle("Language");
      var7.setHeaderText("Choose the language of the program\n(buttons like OK and Cancel change after a restart)");
      var7.getDialogPane().setContent(var1);
      var7.getDialogPane().getButtonTypes().setAll(ButtonType.CANCEL, ButtonType.OK);
      UIManager.get().<ButtonType>showDialog(var7).ifPresent(var1x -> {
         if (var1x == ButtonType.OK && var0.getSelectedToggle() != null) {
            LanguageManager.get().setLanguage((Language)var0.getSelectedToggle().getUserData());
         }
      });
   }
}
