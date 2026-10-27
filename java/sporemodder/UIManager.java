package sporemodder;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Properties;
import javafx.animation.FadeTransition;
import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextArea;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelReader;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;
import javax.imageio.ImageIO;
import sporemodder.MessageManager.MessageType;
import sporemodder.view.Controller;
import sporemodder.view.ModeShortcut;
import sporemodder.view.SceneTranslator;
import sporemodder.view.UIUpdateListener;
import sporemodder.view.UserInterface;
import sporemodder.view.Controller.PlaceholderController;
import sporemodder.view.dialogs.ChooseStyleUI;

public class UIManager extends AbstractManager {
   public static final String APP_VERSION = "v0.001";

   private static final String PROPERTY_hasSelectedStyle = "hasSelectedStyle";
   private static final String PROPERTY_selectedStyle = "selectedStyle";
   private static final String PROPERTY_isFirstTime = "isFirstTime";
   private String currentStyle = "Default";
   private String selectedStyle = this.currentStyle;
   private boolean hasSelectedStyle = false;
   private boolean isShowingOverlay;
   private Stage primaryStage;
   private Scene mainScene;
   private final List<UIUpdateListener> updateListeners = new ArrayList<>();
   private UserInterface userInterface;
   private final List<String> stylesheetsToLoad = new ArrayList<>();
   private boolean showingIntroUI = true;
   private boolean isFirstTime = true;
   private ProgressBar taskbarProgress;
   private boolean isShowingTaskbarProgress;
   private double programProgress;
   private Image programIcon;
   private double dpiScaling;
   private String initializationError;
   private String titleInfo;
   private static final int MAX_STACKTRACE = 6;

   public static UIManager get() {
      return MainApp.get().getUIManager();
   }

   public Scene getScene() {
      return this.mainScene;
   }

   public Stage getPrimaryStage() {
      return this.primaryStage;
   }

   public void setInitializationError(String var1) {
      this.initializationError = var1;
   }

   public void showInitializationError() {
      if (this.initializationError != null) {
         get().showDialog(AlertType.ERROR, this.initializationError);
      }
   }

   public void initialize(Properties var1) {
      this.currentStyle = this.selectedStyle = var1.getProperty("selectedStyle", "Default");
      this.isFirstTime = Boolean.parseBoolean(var1.getProperty("isFirstTime", "true"));
      this.hasSelectedStyle = Boolean.parseBoolean(var1.getProperty("hasSelectedStyle", "false"));
      this.taskbarProgress = new ProgressBar();
      new Scene(this.taskbarProgress);
      this.taskbarProgress.setPrefWidth(32.0);
      this.taskbarProgress.setPrefHeight(32.0);
      this.programIcon = this.loadImage("program-icon.png");
   }

   public void saveSettings(Properties var1) {
      var1.put("selectedStyle", this.selectedStyle);
      var1.put("isFirstTime", Boolean.toString(this.isFirstTime));
      var1.put("hasSelectedStyle", Boolean.toString(this.hasSelectedStyle));
   }

   public void start(Stage var1) {
      this.primaryStage = var1;
      this.dpiScaling = Font.getDefault().getSize() / 12.0;
      this.restoreTitle();
      var1.getIcons().setAll(new Image[]{this.programIcon});
      boolean var2 = false;
      if (!this.hasSelectedStyle) {
         try {
            String var3 = ChooseStyleUI.show(this.programIcon);
            if (var3 != null) {
               this.currentStyle = var3;
               this.selectedStyle = var3;
               this.hasSelectedStyle = true;
               var2 = true;
            }
         } catch (Exception var4) {
            var4.printStackTrace();
         }
      }

      this.userInterface = new UserInterface();
      this.userInterface.initialize();
      if (this.mainScene == null) {
         this.mainScene = new Scene(this.userInterface, 1400.0, 700.0);
         var1.setScene(this.mainScene);
         this.mainScene.getStylesheets().add(PathManager.get().getStyleFile("basic.css").toURI().toString());
         this.mainScene.getStylesheets().add(PathManager.get().getStyleFile("syntax.css").toURI().toString());
         ModeShortcut.install(this.mainScene);
         String var5 = "Dark".equals(this.getCurrentStyle()) ? "resources/cpp-syntax-dark.css" : "resources/cpp-syntax-default.css";
         if (UIManager.class.getResource(var5) != null) {
            this.mainScene.getStylesheets().add(UIManager.class.getResource(var5).toExternalForm());
         }

         this.mainScene.getStylesheets().add(PathManager.get().getStyleFile("color-swatch.css").toURI().toString());
         if (UIManager.class.getResource("resources/spark-layout.css") != null) {
            this.mainScene.getStylesheets().add(UIManager.class.getResource("resources/spark-layout.css").toExternalForm());
         }

         if ("Dark".equals(this.getCurrentStyle()) && UIManager.class.getResource("resources/spark-dark.css") != null) {
            this.mainScene.getStylesheets().add(UIManager.class.getResource("resources/spark-dark.css").toExternalForm());
         }
         this.mainScene.getStylesheets().addAll(this.stylesheetsToLoad);
      }

      if (var2) {
         MainApp.get().saveSettings();
      }
   }

   public double scaleByDpi(double var1) {
      return var1 * this.dpiScaling;
   }

   public void show() {
      this.restoreTitle();
      this.primaryStage.setMaximized(true);
      this.primaryStage.show();
      Platform.runLater(() -> LanguageManager.get().translateAll());
   }

   public void showMainUI() {
      if (this.showingIntroUI) {
         this.userInterface.showMainUI();
         this.showingIntroUI = true;
      }
   }

   public void restoreTitle() {
      this.titleInfo = null;
      this.refreshTitle();
   }

   public void refreshTitle() {
      StringBuilder var1 = new StringBuilder("Spark " + APP_VERSION);
      if (this.titleInfo != null) {
         var1.append(" - ").append(this.titleInfo);
      }

      ModeManager var2 = MainApp.get().getModeManager();
      if (var2 != null && !var2.is(WorkMode.STANDARD)) {
         var1.append(" [").append(LanguageManager.tr(var2.getMode().getDisplayName())).append("]");
      }

      this.primaryStage.setTitle(var1.toString());
   }

   public void setTitleInfo(String var1) {
      this.titleInfo = var1;
      this.refreshTitle();
   }

   public <T extends Controller> T loadUI(String var1) {
      try {
         FXMLLoader var2 = new FXMLLoader();
         var2.setLocation(UIManager.class.getResource("/sporemodder/view/" + var1 + ".fxml"));
         Node var3 = (Node)var2.load();
         Object var4 = (Controller)var2.getController();
         if (var4 == null) {
            var4 = new PlaceholderController(var3);
         }

         UILoadMessage var5 = new UILoadMessage();
         var5.name = var1;
         var5.controller = (Controller)var4;
         MessageManager.get().postMessage(MessageType.OnUILoad, var5);
         return (T)var4;
      } catch (Exception var6) {
         var6.printStackTrace();
         return null;
      }
   }

   public <T extends Controller> T loadUI(URL var1) {
      try {
         FXMLLoader var2 = new FXMLLoader();
         var2.setLocation(var1);
         Node var3 = (Node)var2.load();
         Controller var4 = (Controller)var2.getController();
         return (T)(var4 != null ? var4 : new PlaceholderController(var3));
      } catch (IOException var5) {
         var5.printStackTrace();
         return null;
      }
   }

   public ImageView loadIcon(String var1, double var2, double var4, boolean var6) {
      try {
         Image var7 = this.loadImage(var1);
         ImageView var8 = new ImageView();
         var8.setImage(var7);
         var8.setFitWidth(this.scaleByDpi(var2));
         var8.setFitHeight(this.scaleByDpi(var4));
         var8.setPreserveRatio(var6);
         return var8;
      } catch (Exception var9) {
         return new ImageView();
      }
   }

   public ImageView loadIcon(String var1) {
      return this.loadIcon(var1, 0.0, 0.0, true);
   }

   public Image loadImage(String var1) {
      if ("program-icon.png".equals(var1)) {
         java.io.InputStream var2 = UIManager.class.getResourceAsStream("/sporemodder/resources/spark-icon.png");
         if (var2 != null) {
            return new Image(var2);
         }
      }

      return new Image(PathManager.get().getStyleFile(var1).toURI().toString());
   }

   public UserInterface getUserInterface() {
      return this.userInterface;
   }

   public void addStylesheets(List<String> var1) {
      this.stylesheetsToLoad.addAll(var1);
      if (this.mainScene != null) {
         this.mainScene.getStylesheets().addAll(this.stylesheetsToLoad);
      }
   }

   public void addListener(UIUpdateListener var1) {
      this.updateListeners.add(var1);
   }

   public void notifyUIUpdate(boolean var1) {
      for (UIUpdateListener var3 : this.updateListeners) {
         var3.onUIUpdate(var1);
      }
   }

   public String getCurrentStyle() {
      return this.currentStyle;
   }

   public String getSelectedStyle() {
      return this.selectedStyle;
   }

   public void setSelectedStyle(String var1) {
      this.selectedStyle = var1;
   }

   public List<String> getAvailableStyles() {
      ArrayList var1 = new ArrayList();

      for (File var5 : PathManager.get().getProgramFile("Styles").listFiles()) {
         if (var5.isDirectory()) {
            var1.add(var5.getName());
         }
      }

      return var1;
   }

   public void setOverlay(boolean var1) {
      Pane var2 = this.userInterface.getDialogOverlay();
      if (!var1 || !this.isShowingOverlay) {
         if (var1 || this.isShowingOverlay) {
            var2.setMouseTransparent(!var1);
            FadeTransition var3 = new FadeTransition(Duration.millis(200.0), var2);
            var3.setAutoReverse(false);
            var3.setCycleCount(1);
            if (var1) {
               var3.setFromValue(0.0);
               var3.setToValue(1.0);
            } else {
               var3.setFromValue(1.0);
               var3.setToValue(0.0);
            }

            var3.play();
            this.isShowingOverlay = var1;
         }
      }
   }

   public boolean isShowingOverlay() {
      return this.isShowingOverlay;
   }

   public <R> Optional<R> showDialog(Dialog<R> var1, boolean var2) {
      boolean var3 = this.isShowingOverlay;
      this.setOverlay(true);
      var1.initModality(Modality.APPLICATION_MODAL);
      var1.initOwner(this.primaryStage);
      this.translateDialog(var1);
      Optional var4 = var1.showAndWait();
      if (var2 && !var3) {
         this.setOverlay(false);
      }

      return var4;
   }

   private void translateDialog(Dialog<?> var1) {
      LanguageManager var2 = LanguageManager.get();
      if (var2 != null) {
         if (var1.getTitle() != null) {
            var1.setTitle(var2.translate(var1.getTitle()));
         }

         if (var1.getHeaderText() != null) {
            String[] var3 = var1.getHeaderText().split("\\n", -1);

            for (int var4 = 0; var4 < var3.length; var4++) {
               var3[var4] = var2.translate(var3[var4]);
            }

            var1.setHeaderText(String.join("\n", var3));
         }

         SceneTranslator.translate(var1.getDialogPane());
      }
   }

   public <R> Optional<R> showDialog(Dialog<R> var1) {
      return this.showDialog(var1, true);
   }

   public void showDialog(AlertType var1, String var2) {
      Label var3 = new Label();
      var3.setWrapText(true);
      var3.setText(var2);
      Alert var4 = new Alert(var1, null, new ButtonType[]{ButtonType.OK});
      var4.getDialogPane().setContent(var3);
      this.showDialog(var4);
   }

   public void showDialog(String var1) {
      this.showDialog(AlertType.NONE, var1);
   }

   public boolean isShowingTaskbarProgress() {
      return this.isShowingTaskbarProgress;
   }

   private void setProgramProgress(double var1) {
      this.programProgress = var1;
      if (var1 == 1.0) {
         this.isShowingTaskbarProgress = false;
         this.restoreTaskbarProgress();
      } else {
         this.isShowingTaskbarProgress = true;
      }
   }

   private double getTaskbarProgress() {
      return this.taskbarProgress.getProgress();
   }

   private void updateTaskbarProgress() {
      WritableImage var1 = new WritableImage((int)this.programIcon.getWidth(), (int)this.programIcon.getHeight());
      double var2 = 0.3607843137254902;
      double var4 = 0.6784313725490196;
      double var6 = 0.3176470588235294;
      double var8 = 0.9;
      double var10 = this.programIcon.getWidth() * this.programProgress;
      PixelReader var12 = this.programIcon.getPixelReader();
      PixelWriter var13 = var1.getPixelWriter();

      for (int var14 = 0; (double)var14 < var1.getWidth(); var14++) {
         for (int var15 = 0; (double)var15 < var1.getHeight(); var15++) {
            Color var16 = var12.getColor(var14, var15);
            if ((double)var14 <= var10) {
               double var17 = var16.getOpacity();
               double var19 = 1.0 - var17;
               if (var17 == 0.0) {
                  var13.setColor(var14, var15, new Color(var2, var4, var6, var8));
               } else {
                  var13.setColor(
                     var14,
                     var15,
                     new Color(var16.getRed() * var17 + var2 * var19, var16.getGreen() * var17 + var4 * var19, var16.getBlue() * var17 + var6 * var19, 1.0)
                  );
               }
            } else {
               var13.setColor(var14, var15, var16);
            }
         }
      }

      try {
         File var22 = File.createTempFile("temp-sporemodder-icon", ".png");
         ImageIO.write(SwingFXUtils.fromFXImage(var1, null), "PNG", var22);
         this.primaryStage.getIcons().setAll(new Image[]{new Image(var22.toURI().toString())});
         var22.delete();
      } catch (IOException var21) {
         var21.printStackTrace();
      }
   }

   private void restoreTaskbarProgress() {
      this.primaryStage.getIcons().setAll(new Image[]{this.programIcon});
   }

   public Node getAlertIcon(AlertType var1, int var2, int var3) {
      String var4 = null;
      if (var1 == AlertType.CONFIRMATION) {
         var4 = "dialog-confirmation";
      } else if (var1 == AlertType.ERROR) {
         var4 = "dialog-error";
      } else if (var1 == AlertType.INFORMATION) {
         var4 = "dialog-information";
      } else if (var1 == AlertType.WARNING) {
         var4 = "dialog-warning";
      }

      return this.loadIcon(var4 + ".png", (double)var2, (double)var3, true);
   }

   public boolean tryAction(SimpleAction var1, String var2) {
      try {
         var1.doAction();
         return true;
      } catch (Exception var4) {
         var4.printStackTrace();
         this.showErrorDialog(var4, var2, true);
         return false;
      }
   }

   public boolean tryAction(SimpleAction var1, String var2, boolean var3) {
      try {
         var1.doAction();
         return true;
      } catch (Exception var5) {
         var5.printStackTrace();
         this.showErrorDialog(var5, var2, var3);
         return false;
      }
   }

   public void showErrorDialog(Throwable var1, String var2, boolean var3) {
      Alert var4 = new Alert(AlertType.ERROR, var2 == null ? "" : var2 + " ", new ButtonType[]{ButtonType.OK});
      if (var1.getLocalizedMessage() != null) {
         var4.getDialogPane().setContentText(var4.getDialogPane().getContentText() + var1.getLocalizedMessage());
      }

      var4.setTitle("Error");
      var4.getDialogPane().setExpandableContent(this.createExceptionArea(var1));
      this.showDialog(var4, var3);
   }

   public Node createExceptionArea(Throwable var1) {
      TextArea var2 = new TextArea(getStackTraceString(var1, ""));
      var2.setEditable(false);
      var2.setWrapText(true);
      var2.getStyleClass().clear();
      var2.getStyleClass().add("label");
      var2.getStyleClass().add("dialog-exception-details");
      return var2;
   }

   private static String getStackTraceString(Throwable var0, String var1) {
      StringBuilder var2 = new StringBuilder();
      var2.append(var0.toString());
      var2.append("\n");
      StackTraceElement[] var3 = var0.getStackTrace();
      if (var3 != null) {
         for (int var4 = 0; var4 < 6 && var4 < var3.length; var4++) {
            StackTraceElement var5 = var3[var4];
            var2.append(var1);
            var2.append("\tat ");
            var2.append(var5.toString());
            var2.append("\n");
         }
      }

      Throwable[] var9 = var0.getSuppressed();
      if (var9 != null) {
         for (Throwable var8 : var9) {
            var2.append(var1);
            var2.append("\tSuppressed: ");
            var2.append(getStackTraceString(var8, var1 + "\t"));
         }
      }

      Throwable var11 = var0.getCause();
      if (var11 != null) {
         var2.append(var1);
         var2.append("Caused by: ");
         var2.append(getStackTraceString(var11, var1));
      }

      return var2.toString();
   }

   public boolean isFirstTime() {
      return this.isFirstTime;
   }

   public void setFirstTime(boolean var1) {
      this.isFirstTime = var1;
   }

   @FunctionalInterface
   public interface SimpleAction {
      void doAction() throws Exception;
   }

   public static class UILoadMessage {
      public String name;
      public Controller controller;
   }
}
