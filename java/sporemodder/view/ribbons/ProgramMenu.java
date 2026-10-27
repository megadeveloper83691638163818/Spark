package sporemodder.view.ribbons;

import io.github.emd4600.javafxribbon.Ribbon;
import io.github.emd4600.javafxribbon.RibbonCustomMenu;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.control.Tooltip;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyCombination.Modifier;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import sporemodder.DllModActions;
import sporemodder.GameManager;
import sporemodder.ModeManager;
import sporemodder.ProjectManager;
import sporemodder.BuildLog;
import sporemodder.UIManager;
import sporemodder.WorkMode;
import sporemodder.imageeditor.ImageEditorManager;
import sporemodder.util.Project;
import sporemodder.view.ModeActions;
import sporemodder.view.SceneTranslator;
import sporemodder.view.UIUpdateListener;
import sporemodder.view.dialogs.LanguageDialog;
import sporemodder.view.dialogs.OpenProjectUI;
import sporemodder.view.dialogs.ProgramSettingsUI;
import sporemodder.view.dialogs.UnpackPresetsUI;
import sporemodder.view.dialogs.WorkModeDialog;

public class ProgramMenu implements UIUpdateListener {
   private static final int RECENT_COUNT = 10;
   private Runnable closeHandler = () -> {
   };
   private HBox mainNode;
   private Button newProjectButton;
   private Button openProjectButton;
   private Button unpackPresetsButton;
   private Button importOldProject;
   private Button addExternalProject;
   private Button packButton;
   private Button unpackButton;
   private Button packRunButton;
   private Button runButton;
   private Button packDebugButton;
   private Button buildDllButton;
   private Button workModeButton;
   private final Map<Node, Set<WorkMode>> itemModes = new LinkedHashMap<>();
   private Button languageButton;
   private Button imageEditorButton;
   private Button settingsButton;
   private VBox recentProjectsList;
   private VBox vbox;

   /** Called after any menu button is pressed (the host closes its popup). */
   public void setCloseHandler(Runnable var1) {
      this.closeHandler = var1;
   }

   public Node getMainNode() {
      return this.mainNode;
   }

   public Pane getRecentProjectsPane() {
      return this.recentProjectsList;
   }

   public Pane getItemsPane() {
      return this.vbox;
   }

   private ImageView createIcon(String var1) {
      return this.createIcon(var1, 32, 32);
   }

   private ImageView createIcon(String var1, int var2, int var3) {
      ImageView var4 = UIManager.get().loadIcon(var1);
      var4.setFitWidth((double)var2);
      var4.setFitWidth((double)var3);
      return var4;
   }

   private static void setItemText(Button var0, String var1) {
      if (var0.getGraphic() instanceof BorderPane && ((BorderPane)var0.getGraphic()).getCenter() instanceof HBox) {
         HBox var2 = (HBox)((BorderPane)var0.getGraphic()).getCenter();
         Node var3 = (Node)var2.getChildren().get(var2.getChildren().size() - 1);
         if (var3 instanceof Label) {
            ((Label)var3).setText(var1);
         }
      } else {
         var0.setText(var1);
      }
   }

   private static void setItemVisible(Node var0, boolean var1) {
      var0.setVisible(var1);
      var0.setManaged(var1);
   }

   private Button createButton(String var1, String var2) {
      return this.createButton(var1, var2, null);
   }

   public Button createButton(String var1, String var2, Node var3) {
      if (var2 == null) {
         Button var7 = new Button(var1, var3);
         var7.getStyleClass().add("artificial-menu-item");
         var7.addEventFilter(ActionEvent.ACTION, var1x -> this.closeHandler.run());
         return var7;
      } else {
         HBox var4 = new HBox();
         var4.setAlignment(Pos.CENTER_LEFT);
         if (var3 != null) {
            var4.getChildren().add(var3);
         }

         var4.getChildren().add(new Label(var1));
         var4.setPrefWidth(Double.MAX_VALUE);
         BorderPane var5 = new BorderPane();
         var5.setCenter(var4);
         var5.setRight(new Label(var2));
         BorderPane.setAlignment(var5.getRight(), Pos.CENTER_RIGHT);
         Button var6 = new Button();
         var6.setGraphic(var5);
         var6.getStyleClass().add("artificial-menu-item");
         var6.addEventFilter(ActionEvent.ACTION, var1x -> this.closeHandler.run());
         return var6;
      }
   }

   public void initialize(Ribbon var1) {
      this.recentProjectsList = new VBox(5.0);
      this.vbox = new VBox(5.0);
      Separator var2 = new Separator(Orientation.VERTICAL);
      var2.setPrefHeight(240.0);
      this.mainNode = new HBox();
      this.mainNode.getStyleClass().add("context-menu");
      this.mainNode.getChildren().addAll(new Node[]{this.vbox, new Separator(Orientation.VERTICAL), this.recentProjectsList});
      HBox.setMargin(this.vbox, new Insets(5.0));
      HBox.setHgrow(this.recentProjectsList, Priority.ALWAYS);
      this.vbox.setMinWidth(325.0);
      this.vbox.setMaxWidth(325.0);
      this.mainNode.setMaxWidth(650.0);
      this.mainNode.setMaxHeight(250.0);
      this.newProjectButton = this.createButton("New project", null);
      this.newProjectButton.setOnAction(var0 -> ModeActions.newProject());
      this.newProjectButton.setTooltip(new Tooltip("Creates a new empty project to start modding."));
      this.openProjectButton = this.createButton("Open project...", "Ctrl+O");
      this.openProjectButton.setOnAction(var0 -> OpenProjectUI.show());
      this.openProjectButton.setTooltip(new Tooltip("Opens an existing project."));
      this.unpackPresetsButton = this.createButton("Unpack Presets", null);
      this.unpackPresetsButton.setOnAction(var0 -> {
         UnpackPresetsUI.show(null, false);
         UIManager.get().setOverlay(false);
      });
      this.unpackPresetsButton.setTooltip(new Tooltip("Unpacks groups of packages that are useful for investigation and modding."));
      this.importOldProject = this.createButton("Import old project", null);
      this.importOldProject.setOnAction(var0 -> ProjectManager.get().importOldProject());
      this.importOldProject
         .setTooltip(new Tooltip("Imports a project from older SporeModder versions, converting .prop.xml into the modern .prop.prop_t format."));
      this.addExternalProject = this.createButton("Add external project", null);
      this.addExternalProject.setOnAction(var0 -> ProjectManager.get().addExternalProject());
      this.addExternalProject.setTooltip(new Tooltip("Adds an external folder as a project, allowing to use Spark on it."));
      this.vbox
         .getChildren()
         .addAll(new Node[]{this.newProjectButton, this.openProjectButton, this.unpackPresetsButton, this.importOldProject, this.addExternalProject});
      this.vbox.getChildren().add(new Separator(Orientation.HORIZONTAL));
      this.packRunButton = this.createButton("Pack mod and run game", "F9", this.createIcon("pack-and-run.png", 48, 32));
      this.packRunButton.setOnAction(var0 -> {
         if (ProjectManager.get().packActive(false)) {
            UIManager.get().tryAction(() -> GameManager.get().runGame(), "Cannot run game.");
         }
      });
      this.packRunButton.setTooltip(new Tooltip("Packs the contents of the projects into .package file and runs Spore."));
      this.packButton = this.createButton("Pack mod", "Ctrl+P", this.createIcon("pack.png"));
      this.packButton.setOnAction(var0 -> ProjectManager.get().packActive(false));
      this.packButton.setTooltip(new Tooltip("Packs the contents of the projects into .package file."));
      this.runButton = this.createButton("Run game", "F7", this.createIcon("run-without-pack.png"));
      this.runButton.setOnAction(var0 -> UIManager.get().tryAction(() -> GameManager.get().runGame(), "Cannot run game."));
      this.runButton.setTooltip(new Tooltip("Runs Spore."));
      this.unpackButton = this.createButton("Unpack mod", "Ctrl+U", this.createIcon("unpack.png"));
      this.unpackButton.setOnAction(var0 -> ModeActions.unpackMod());
      this.unpackButton.setTooltip(new Tooltip("Unpacks the contents of a .package file into a new project."));
      this.packDebugButton = this.createButton("Pack mod with debug info", null, this.createIcon("debug-pack.png"));
      this.packDebugButton.setOnAction(var0 -> ProjectManager.get().packActive(true));
      this.packDebugButton.setTooltip(new Tooltip("Packs the contents of the projects into .package file, with debug information for the ModCreatorKit"));
      this.buildDllButton = this.createButton("Build DLL", "Ctrl+B", this.createIcon("pack.png"));
      this.buildDllButton.setOnAction(var0 -> DllModActions.buildActive());
      this.buildDllButton.setTooltip(new Tooltip("Builds the Spore ModAPI DLL of the active project with MSBuild."));
      this.vbox
         .getChildren()
         .addAll(new Node[]{this.packRunButton, this.packButton, this.runButton, this.unpackButton, this.packDebugButton, this.buildDllButton});
      this.workModeButton = this.createButton("Work mode...", null, this.createIcon("config.png"));
      this.workModeButton.setOnAction(var0 -> WorkModeDialog.show());
      this.workModeButton.setTooltip(new Tooltip("Choose how the program works: standard, DLL mods, ..."));
      this.languageButton = this.createButton("Language...", null, this.createIcon("config.png"));
      this.languageButton.setOnAction(var0 -> LanguageDialog.show());
      this.languageButton.setTooltip(new Tooltip("Choose the language of the program"));
      this.imageEditorButton = this.createButton("Built-in image editor: off", null, this.createIcon("config.png"));
      this.imageEditorButton.setOnAction(var0 -> {
         ImageEditorManager var1x = ImageEditorManager.get();
         var1x.setEnabled(!var1x.isEnabled());
         UIManager.get().notifyUIUpdate(false);
      });
      this.imageEditorButton.setTooltip(new Tooltip("Uses the built-in image editor (experimental) instead of the image viewer."));
      this.settingsButton = this.createButton("Settings", null, this.createIcon("config.png"));
      this.settingsButton.setOnAction(var0 -> ProgramSettingsUI.show());
      this.settingsButton.setTooltip(new Tooltip("Change the program settings."));
      this.vbox.getChildren().add(new Separator(Orientation.HORIZONTAL));
      Button buildLogButton = this.createButton("Build log", null, this.createIcon("config.png"));
      buildLogButton.setOnAction(var0 -> BuildLog.show());
      buildLogButton.setTooltip(new Tooltip("Shows debug information of the last project builds."));
      this.vbox.getChildren().addAll(new Node[]{this.workModeButton, buildLogButton, this.settingsButton});
      UIManager.get().addListener(this);

   }

   private void updateModeItems() {
      WorkMode var1 = ModeManager.get().getMode();
      if (this.itemModes.isEmpty()) {
         this.itemModes.put(this.unpackPresetsButton, WorkMode.REGULAR_TOOLS);
         this.itemModes.put(this.importOldProject, WorkMode.REGULAR_TOOLS);
         this.itemModes.put(this.addExternalProject, WorkMode.REGULAR_TOOLS);
         this.itemModes.put(this.packDebugButton, WorkMode.REGULAR_TOOLS);
         this.itemModes.put(this.buildDllButton, WorkMode.DLL_TOOLS);
      }

      setItemText(this.newProjectButton, var1.text("New project"));
      this.newProjectButton.setTooltip(new Tooltip(var1.text("Creates a new empty project to start modding.")));
      setItemText(this.openProjectButton, var1.text("Open project..."));
      setItemText(this.unpackButton, var1.text("Unpack mod"));
      this.unpackButton.setTooltip(new Tooltip(var1.text("Unpacks the contents of a .package file into a new project.")));

      for (Entry var3 : this.itemModes.entrySet()) {
         setItemVisible((Node)var3.getKey(), ((Set)var3.getValue()).contains(var1));
      }
   }

   private void updateEnabledItems() {
      Project var1 = ProjectManager.get().getActive();
      this.openProjectButton.setDisable(ModeManager.get().filterProjects(ProjectManager.get().getProjects()).isEmpty());
      this.packButton.setDisable(false);
      this.packDebugButton.setDisable(false);
      this.packRunButton.setDisable(!GameManager.get().canRunGame());
      this.runButton.setDisable(!GameManager.get().canRunGame());
      this.buildDllButton.setDisable(!DllModActions.isDllProject(var1));
         }

   private List<Project> getRecentProjectsForMode() {
      return ModeManager.get().getRecentProjects(10);
   }

   public void onUIUpdate(boolean var1) {
      if (var1) {
         Platform.runLater(
            () -> {
               Scene var1x = UIManager.get().getScene();
               var1x.getAccelerators().put(new KeyCodeCombination(KeyCode.F7, new Modifier[0]), (Runnable)() -> this.runButton.fire());
               var1x.getAccelerators().put(new KeyCodeCombination(KeyCode.F9, new Modifier[0]), (Runnable)() -> this.packRunButton.fire());
               var1x.getAccelerators()
                  .put(new KeyCodeCombination(KeyCode.O, new Modifier[]{KeyCombination.CONTROL_DOWN}), (Runnable)() -> this.openProjectButton.fire());
               var1x.getAccelerators()
                  .put(new KeyCodeCombination(KeyCode.U, new Modifier[]{KeyCombination.CONTROL_DOWN}), (Runnable)() -> this.unpackButton.fire());
               var1x.getAccelerators()
                  .put(new KeyCodeCombination(KeyCode.P, new Modifier[]{KeyCombination.CONTROL_DOWN}), (Runnable)() -> this.packButton.fire());
               var1x.getAccelerators()
                  .put(new KeyCodeCombination(KeyCode.B, new Modifier[]{KeyCombination.CONTROL_DOWN}), (Runnable)() -> this.buildDllButton.fire());
            }
         );
      }

      this.updateModeItems();
      this.updateEnabledItems();
      SceneTranslator.translate(this.mainNode);
      List<Project> var2 = this.getRecentProjectsForMode();
      this.recentProjectsList.getChildren().clear();

      for (Project var4 : var2) {
         Button var5 = this.createButton(var4.getName(), null);
         var5.setMnemonicParsing(false);
         var5.setOnAction(var1x -> ProjectManager.get().setActive(var4));
         this.recentProjectsList.getChildren().add(var5);
      }
   }
}
