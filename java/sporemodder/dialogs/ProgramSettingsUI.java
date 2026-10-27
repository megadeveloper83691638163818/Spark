package sporemodder.view.dialogs;

import java.io.File;
import java.nio.file.Files;
import java.util.Map;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Control;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.control.Alert.AlertType;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import sporemodder.FileManager;
import sporemodder.GameManager;
import sporemodder.MainApp;
import sporemodder.PathManager;
import sporemodder.UIManager;
import sporemodder.GameManager.GameType;
import sporemodder.file.shaders.FXCompiler;
import sporemodder.util.SporeGame;
import sporemodder.util.GamePathConfiguration.GamePathType;
import sporemodder.view.Controller;

public class ProgramSettingsUI implements Controller {
   @FXML
   private Node mainNode;
   @FXML
   private ChoiceBox<String> stylesBox;
   @FXML
   private ChoiceBox<String> gamePathBox;
   @FXML
   private Button autoDetectButton;
   @FXML
   private TextField sporeField;
   @FXML
   private Button findSporeButton;
   @FXML
   private TextField gaField;
   @FXML
   private Button findGAButton;
   @FXML
   private TextField customPathField;
   @FXML
   private Button findCustomPathButton;
   @FXML
   private TextField commandLineField;
   @FXML
   private TextField fxcPathField;
   @FXML
   private Button findFXCButton;
   @FXML
   private Label styleWarning;
   @FXML
   private Node gameSettingsPanel;
   @FXML
   private Control commandLineLabel;
   @FXML
   private Control gameTypeLabel;
   @FXML
   private Control styleLabel;
   @FXML
   private Control fxcLabel;
   @FXML
   private TextField projectsFolderTextField;
   @FXML
   private Button findProjectsFolderButton;
   private Dialog<ButtonType> dialog;

   private void addInterfaceSection() {
      if (!(this.mainNode instanceof javafx.scene.layout.VBox)) {
         return;
      }

      javafx.scene.control.Button var1 = new javafx.scene.control.Button("Language...");
      var1.setOnAction(var0 -> sporemodder.view.dialogs.LanguageDialog.show());
      var1.setTooltip(new Tooltip("Choose the language of the program"));
      sporemodder.imageeditor.ImageEditorManager var2 = sporemodder.imageeditor.ImageEditorManager.get();
      javafx.scene.control.Button var3 = new javafx.scene.control.Button(
         var2.isEnabled() ? "Built-in image editor: on" : "Built-in image editor: off"
      );
      var3.setOnAction(var3x -> {
         var2.setEnabled(!var2.isEnabled());
         var3.setText(sporemodder.LanguageManager.tr(var2.isEnabled() ? "Built-in image editor: on" : "Built-in image editor: off"));
         UIManager.get().notifyUIUpdate(false);
      });
      var3.setTooltip(new Tooltip("Uses the built-in image editor (experimental) instead of the image viewer."));
      javafx.scene.layout.VBox var4 = new javafx.scene.layout.VBox(8.0, var1, var3);
      var4.setPadding(new javafx.geometry.Insets(8.0));
      javafx.scene.control.TitledPane var5 = new javafx.scene.control.TitledPane("Interface", var4);
      var5.setAnimated(false);
      var5.setCollapsible(false);
      javafx.scene.layout.VBox var6 = (javafx.scene.layout.VBox)this.mainNode;
      var6.getChildren().add(Math.min(1, var6.getChildren().size()), var5);
   }

   public Node getMainNode() {
      return this.mainNode;
   }

   @FXML
   private void initialize() {
      this.addInterfaceSection();
      this.fxcLabel.setTooltip(new Tooltip("The path to fxc.exe, usually inside DirectX SDK or Microsoft SDK."));
      this.findFXCButton.setOnAction(var1x -> {
         FileChooser var2x = new FileChooser();
         var2x.getExtensionFilters().add(FileManager.FILEFILTER_EXE);
         var2x.getExtensionFilters().add(FileManager.FILEFILTER_ALL);
         if (!this.fxcPathField.getText().isEmpty()) {
            var2x.setInitialDirectory(new File(this.fxcPathField.getText()).getParentFile());
         }

         File var3x = var2x.showOpenDialog(UIManager.get().getScene().getWindow());
         if (var3x != null) {
            this.fxcPathField.setText(var3x.getAbsolutePath());
         }
      });
      GameManager var1 = GameManager.get();
      Tooltip.install(this.gameSettingsPanel, new Tooltip("The game settings used by default"));
      this.autoDetectButton.setOnAction(var1x -> this.autoDetect());
      this.findSporeButton.setOnAction(var2x -> {
         DirectoryChooser var3x = new DirectoryChooser();
         if (!this.sporeField.getText().isEmpty()) {
            var3x.setInitialDirectory(new File(this.sporeField.getText()));
         }

         File var4 = var3x.showDialog(UIManager.get().getScene().getWindow());
         if (var4 != null) {
            var4 = var1.findInstallationFolder("SporeBin", var4);
            if (var4 != null) {
               this.sporeField.setText(var4.getAbsolutePath());
            } else {
               Alert var5 = new Alert(AlertType.WARNING, "The selected folder is not a valid Spore installation folder.", new ButtonType[]{ButtonType.OK});
               var5.setTitle("Invalid Folder");
               UIManager.get().showDialog(var5);
            }
         }
      });
      this.findGAButton
         .setOnAction(
            var2x -> {
               DirectoryChooser var3x = new DirectoryChooser();
               if (!this.gaField.getText().isEmpty()) {
                  var3x.setInitialDirectory(new File(this.gaField.getText()));
               }

               File var4 = var3x.showDialog(UIManager.get().getScene().getWindow());
               if (var4 != null) {
                  var4 = var1.findInstallationFolder("SporebinEP1", var4);
                  if (var4 != null) {
                     this.gaField.setText(var4.getAbsolutePath());
                  } else {
                     Alert var5 = new Alert(
                        AlertType.WARNING, "The selected folder is not a valid Galactic Adventures installation folder.", new ButtonType[]{ButtonType.OK}
                     );
                     var5.setTitle("Invalid Folder");
                     UIManager.get().showDialog(var5);
                  }
               }
            }
         );
      this.findCustomPathButton.setOnAction(var1x -> {
         FileChooser var2x = new FileChooser();
         if (!this.customPathField.getText().isEmpty()) {
            File var3x = new File(this.customPathField.getText());
            var2x.setInitialDirectory(var3x.getParentFile());
            var2x.setInitialFileName(var3x.getName());
         }

         var2x.getExtensionFilters().add(FileManager.FILEFILTER_ALL);
         var2x.getExtensionFilters().add(FileManager.FILEFILTER_EXE);
         File var4 = var2x.showOpenDialog(UIManager.get().getScene().getWindow());
         if (var4 != null) {
            this.customPathField.setText(var4.getAbsolutePath());
         }
      });
      if (var1.hasSpore()) {
         this.sporeField.setText(var1.getSpore().getInstallFolder().getAbsolutePath());
      }

      if (var1.hasGalacticAdventures()) {
         this.gaField.setText(var1.getGalacticAdventures().getInstallFolder().getAbsolutePath());
      }

      if (var1.getAlternativeExecutable() != null) {
         this.customPathField.setText(var1.getAlternativeExecutable().getAbsolutePath());
      }

      this.commandLineField.setText(GameManager.get().getCommandLineArguments());
      this.commandLineLabel.setTooltip(new Tooltip("The command line arguments that are used when executing the game."));
      this.gameTypeLabel.setTooltip(new Tooltip("Which game should SporeModder execute: Spore, Galactic Adventures, or a custom executable."));
      this.gamePathBox.getItems().setAll(new String[]{"Spore", "Galactic Adventures", "Custom"});
      GamePathType var2 = var1.getGameToExecute();
      byte var3 = 1;
      if (var2 == GamePathType.SPORE) {
         var3 = 0;
      } else if (var2 == GamePathType.GALACTIC_ADVENTURES) {
         var3 = 1;
      } else if (var2 == GamePathType.CUSTOM) {
         var3 = 2;
      }

      this.gamePathBox.getSelectionModel().select(var3);
      this.styleLabel.setTooltip(new Tooltip("The visual styling used in the program. Changes will be applied on restart."));
      this.stylesBox.getItems().setAll(UIManager.get().getAvailableStyles());
      this.stylesBox.getSelectionModel().select(UIManager.get().getSelectedStyle());
      this.styleWarning.setGraphic(UIManager.get().getAlertIcon(AlertType.WARNING, 18, 18));
      this.styleWarning.setVisible(false);
      this.stylesBox.getSelectionModel().selectedItemProperty().addListener((var1x, var2x, var3x) -> {
         if (!UIManager.get().getSelectedStyle().equals(var3x)) {
            this.styleWarning.setVisible(true);
         } else {
            this.styleWarning.setVisible(false);
         }
      });
      if (FXCompiler.get().getFXCFile() != null) {
         this.fxcPathField.setText(FXCompiler.get().getFXCFile().getAbsolutePath());
      }

      this.findProjectsFolderButton.setOnAction(var1x -> {
         DirectoryChooser var2x = new DirectoryChooser();
         File var3x;
         if (this.projectsFolderTextField.getText().isBlank()) {
            var3x = PathManager.get().getProjectsFolder();
         } else {
            var3x = new File(this.projectsFolderTextField.getText());
         }

         var2x.setInitialDirectory(var3x);
         File var4 = var2x.showDialog(UIManager.get().getScene().getWindow());
         if (var4 != null) {
            this.projectsFolderTextField.setText(var4.getAbsolutePath());
         }
      });
      this.projectsFolderTextField.setText(PathManager.get().getProjectsFolderStringForSettings());
   }

   private void applyChanges() {
      GameManager var1 = GameManager.get();
      var1.setCommandLineArguments(this.commandLineField.getText());
      String var2 = (String)this.gamePathBox.getSelectionModel().getSelectedItem();
      switch (var2) {
         case "Spore":
            var1.setGameToExecute(GamePathType.SPORE);
            break;
         case "Galactic Adventures":
            var1.setGameToExecute(GamePathType.GALACTIC_ADVENTURES);
            break;
         default:
            var1.setGameToExecute(GamePathType.CUSTOM);
      }

      var2 = this.sporeField.getText();
      if (var2.isEmpty()) {
         var1.setSpore(null);
      } else if (!var1.hasSpore() || !var2.equals(var1.getSpore().getInstallFolder().getAbsolutePath())) {
         var1.setSpore(var1.createSpore(var2));
      }

      var2 = this.gaField.getText();
      if (var2.isEmpty()) {
         var1.setGalacticAdventures(null);
      } else if (!var1.hasGalacticAdventures() || !var2.equals(var1.getGalacticAdventures().getInstallFolder().getAbsolutePath())) {
         var1.setGalacticAdventures(var1.createGA(var2));
      }

      var2 = this.customPathField.getText();
      if (var2.isEmpty()) {
         var1.setAlternativeExecutable(null);
      } else {
         var1.setAlternativeExecutable(new File(var2));
      }

      UIManager.get().setSelectedStyle((String)this.stylesBox.getSelectionModel().getSelectedItem());
      var2 = this.fxcPathField.getText();
      if (var2.isEmpty()) {
         FXCompiler.get().setFXCFile(null);
      } else {
         FXCompiler.get().setFXCFile(new File(var2));
      }

      String var9 = null;
      if (!this.projectsFolderTextField.getText().isBlank()) {
         File var4 = new File(this.projectsFolderTextField.getText());
         if (!Files.isDirectory(var4.toPath())) {
            UIManager.get().showDialog(AlertType.ERROR, "The Projects folder does not exist: " + this.projectsFolderTextField.getText());
         } else {
            var9 = this.projectsFolderTextField.getText();
         }
      } else if (!PathManager.get().isDefaultProjectsFolder()) {
         var9 = "";
      }

      if (var9 != null) {
         UIManager.get().showDialog("The Projects folder has been changed. The changes won't apply until you restart Spark.");
         PathManager.get().setNextProjectsFolder(var9);
      }

      MainApp.get().saveSettings();
   }

   private void autoDetect() {
      Map var1 = GameManager.get().autoDetectPaths();
      if (var1.containsKey(GameType.SPORE)) {
         this.sporeField.setText(((SporeGame)var1.get(GameType.SPORE)).getInstallFolder().getAbsolutePath());
      }

      if (var1.containsKey(GameType.GA)) {
         this.gaField.setText(((SporeGame)var1.get(GameType.GA)).getInstallFolder().getAbsolutePath());
      }

      if (FXCompiler.get().autoDetectPath()) {
         this.fxcPathField.setText(FXCompiler.get().getFXCFile().getAbsolutePath());
      }
   }

   private void showInternal() {
      this.dialog.setTitle("Spark Settings");
      this.dialog.getDialogPane().setContent(this.mainNode);
      this.dialog.getDialogPane().getButtonTypes().setAll(new ButtonType[]{ButtonType.CANCEL, ButtonType.APPLY});
      ((Button)this.dialog.getDialogPane().lookupButton(ButtonType.APPLY)).setDefaultButton(true);
      UIManager.get().showDialog(this.dialog).ifPresent(var1 -> {
         if (var1 == ButtonType.APPLY) {
            this.applyChanges();
         }
      });
   }

   public static void show() {
      ProgramSettingsUI var0 = (ProgramSettingsUI)UIManager.get().loadUI("dialogs/ProgramSettingsUI");
      var0.dialog = new Dialog();
      var0.showInternal();
   }
}
