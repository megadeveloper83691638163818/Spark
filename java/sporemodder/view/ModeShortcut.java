package sporemodder.view;

import java.util.EnumSet;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.Popup;
import javafx.stage.Stage;
import javafx.util.Duration;
import sporemodder.LanguageManager;
import sporemodder.ModeManager;
import sporemodder.UIManager;
import sporemodder.WorkMode;

public class ModeShortcut {
   private static final KeyCode KEY_A = KeyCode.F3;
   private static final KeyCode KEY_B = KeyCode.F4;
   private static final double MESSAGE_SECONDS = 1.4;
   private static Popup message;

   private ModeShortcut() {
   }

   public static void install(Scene var0) {
      EnumSet var1 = EnumSet.noneOf(KeyCode.class);
      var0.addEventFilter(KeyEvent.KEY_PRESSED, var1x -> {
         KeyCode var2 = var1x.getCode();
         if (var2 == KEY_A || var2 == KEY_B) {
            boolean var3 = var1.add(var2);
            if (var3 && var1.contains(KEY_A) && var1.contains(KEY_B)) {
               switchToNextMode();
               var1x.consume();
            }
         }
      });
      var0.addEventFilter(KeyEvent.KEY_RELEASED, var1x -> var1.remove(var1x.getCode()));
      var0.windowProperty().addListener((var1x, var2, var3) -> {
         if (var3 != null) {
            var3.focusedProperty().addListener((var1xx, var2x, var3x) -> {
               if (!var3x) {
                  var1.clear();
               }
            });
         }
      });
   }

   public static void switchToNextMode() {
      WorkMode[] var0 = WorkMode.values();
      WorkMode var1 = var0[(ModeManager.get().getMode().ordinal() + 1) % var0.length];
      ModeManager.get().setMode(var1);
      showMessage(LanguageManager.tr("Work mode") + ": " + LanguageManager.tr(var1.getDisplayName()));
   }

   private static void showMessage(String var0) {
      Stage var1 = UIManager.get().getPrimaryStage();
      if (message != null) {
         message.hide();
      }

      Label var2 = new Label(var0);
      var2.setStyle(
         "-fx-background-color: rgba(30, 30, 30, 0.9); -fx-text-fill: white; -fx-font-size: 16px;-fx-padding: 10 20 10 20; -fx-background-radius: 6;"
      );
      Popup var3 = new Popup();
      var3.getContent().add(var2);
      var3.show(var1);
      Platform.runLater(() -> {
         var3.setAnchorX(var1.getX() + (var1.getWidth() - var3.getWidth()) / 2.0);
         var3.setAnchorY(var1.getY() + 90.0);
      });
      PauseTransition var4 = new PauseTransition(Duration.seconds(1.4));
      var4.setOnFinished(var1x -> var3.hide());
      var4.play();
      message = var3;
   }
}
