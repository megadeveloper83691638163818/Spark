package sporemodder.view;

import java.util.List;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import sporemodder.DllModActions;
import sporemodder.DocumentationManager;
import sporemodder.MainApp;
import sporemodder.ModeManager;
import sporemodder.ProjectManager;
import sporemodder.UIManager;
import sporemodder.WorkMode;
import sporemodder.util.Project;
import sporemodder.view.dialogs.CreateProjectSimpleUI;
import sporemodder.view.dialogs.OpenProjectUI;
import sporemodder.view.dialogs.UnpackPackageUI;
import sporemodder.view.dialogs.UnpackPresetsUI;

public class IntroUI implements Controller {
   private static final int RECENT_COUNT = 10;
   @FXML
   private Node mainNode;
   @FXML
   private VBox documentationPane;
   @FXML
   private VBox recentProjectsList;
   @FXML
   private Button btnNew;
   @FXML
   private Button btnOpen;
   @FXML
   private Button btnPresets;
   @FXML
   private Button btnUnpack;
   @FXML
   private Button btnImport;
   @FXML
   private Button btnAddExternal;
   @FXML
   private Hyperlink modBrowserLink;
   @FXML
   private Hyperlink moddingServerLink;
   @FXML
   private Hyperlink officialServerLink;

   @Override
   public Node getMainNode() {
      return this.mainNode;
   }

   private Button createButton(String var1, Node var2) {
      Button var3 = new Button(var1, var2);
      var3.getStyleClass().add("artificial-menu-item");
      return var3;
   }

   private void setHyperlinkURL(Hyperlink var1, String var2) {
      var1.getStyleClass().add("inspector-docs-link");
      var1.setTooltip(new Tooltip(var2));
      var1.setOnAction(var1x -> MainApp.get().getHostServices().showDocument(var2));
   }

   private void updateForMode() {
      WorkMode mode = ModeManager.get().getMode();
      this.btnNew.setText(mode.text("New project"));
      this.btnNew.setTooltip(new Tooltip(mode.text("Creates a new empty project to start modding.")));
      this.btnOpen.setText(mode.text("Open project"));
      this.btnUnpack.setText(mode.text("Unpack mod"));
      this.btnUnpack.setTooltip(new Tooltip(mode.text("Unpacks the contents of a .package file into a new project.")));

      boolean regular = WorkMode.REGULAR_TOOLS.contains(mode);
      for (Button button : new Button[]{this.btnPresets, this.btnImport, this.btnAddExternal}) {
         button.setVisible(regular);
         button.setManaged(regular);
      }

      this.recentProjectsList.getChildren().clear();

      for (Button button : new Button[]{this.btnNew, this.btnOpen, this.btnUnpack}) {
         SceneTranslator.translate(button);
      }

      for (Project project : ModeManager.get().getRecentProjects(10)) {
         Button button = this.createButton(project.getName(), null);
         button.setMnemonicParsing(false);
         button.setOnAction(event -> ProjectManager.get().setActive(project));
         this.recentProjectsList.getChildren().add(button);
      }
   }

   @FXML
   private void initialize() {
      this.btnNew.setOnAction(var0 -> {
         ModeActions.newProject();
      });
      this.btnNew.setTooltip(new Tooltip("Creates a new empty project to start modding."));
      this.btnOpen.setOnAction(var0 -> OpenProjectUI.show());
      this.btnOpen.setTooltip(new Tooltip("Opens an existing project."));
      this.btnPresets.setOnAction(var0 -> {
         UnpackPresetsUI.show(null, false);
         UIManager.get().setOverlay(false);
      });
      this.btnPresets.setTooltip(new Tooltip("Unpacks groups of packages that are useful for investigation and modding."));
      this.btnUnpack.setOnAction(var0 -> {
         ModeActions.unpackMod();
      });
      this.btnUnpack.setTooltip(new Tooltip("Unpacks the contents of a .package file into a new project."));
      this.btnImport.setOnAction(var0 -> ProjectManager.get().importOldProject());
      this.btnImport.setTooltip(new Tooltip("Imports a project from older SporeModder versions, converting .prop.xml into the modern .prop.prop_t format."));
      this.btnAddExternal.setOnAction(var0 -> ProjectManager.get().addExternalProject());
      this.btnAddExternal.setTooltip(new Tooltip("Adds an external folder as a project, allowing to use Spark on it."));
      this.updateForMode();
      ModeManager.get().addListener((var1x, var2) -> this.updateForMode());
      Pane var1 = DocumentationManager.get().createDocumentationPane("main");
      if (var1 != null) {
         this.documentationPane.getChildren().add(var1);
      }

      this.setHyperlinkURL(this.modBrowserLink, "https://mods.sporecommunity.com/");
      this.setHyperlinkURL(this.moddingServerLink, "https://discord.gg/QR8CjQT");
      this.setHyperlinkURL(this.officialServerLink, "https://discord.gg/sporeofficial");
   }
}
