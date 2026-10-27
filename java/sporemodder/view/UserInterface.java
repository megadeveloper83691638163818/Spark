package sporemodder.view;

import io.github.emd4600.javafxribbon.Ribbon;
import io.github.emd4600.javafxribbon.RibbonTab;
import io.github.emd4600.javafxribbon.Ribbon.RibbonState;
import io.github.emd4600.javafxribbon.RibbonGroup;
import io.github.emd4600.javafxribbon.RibbonProgramButton;
import io.github.emd4600.javafxribbon.RibbonWindow;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import sporemodder.DocumentationManager;
import sporemodder.ModeManager;
import sporemodder.PathManager;
import sporemodder.BuildLog;
import sporemodder.UIManager;
import sporemodder.WorkMode;
import sporemodder.util.ProjectItem;
import sporemodder.view.HideablePane.HideSide;
import sporemodder.view.ribbons.DllRibbonTab;
import sporemodder.view.ribbons.EditRibbonTab;
import sporemodder.view.ribbons.ModAndGitRibbonTab;
import sporemodder.view.ribbons.ModeLayout;
import sporemodder.view.ribbons.ProgramMenu;
import sporemodder.view.ribbons.ProjectRibbonTab;
import sporemodder.view.ribbons.RibbonTabController;
import sporemodder.view.ribbons.UtilRibbonTab;

public class UserInterface extends RibbonWindow {
   private IntroUI introUI;
   private FlowPane dialogOverlay;
   private BorderPane rootPane;
   private BorderPane SHELL;
   private Node HEADER;
   private ProjectTreeUI projectTree;
   private InspectorPaneUI inspectorPane;
   private EditorPaneUI editorPane;
   private StatusBar statusBar;
   private Label statusNameLabel;
   private Label statusTypeLabel;
   private ProgramMenu programMenu;
   private DockLayout dock;
   private QuickBar quickBar;
   private RibbonDock ribbonDock;
   private final Ribbon tabModel = new Ribbon();
   private sporemodder.imageeditor.ui.ImageEditorView dockedImageView;
   private final Map<String, RibbonTabController> ribbonTabs = new HashMap<>();
   private String activeRibbonTab;

   public String getUserAgentStylesheet() {
      return PathManager.get().getStyleFile("ribbonstyle.css").toURI().toString();
   }

   public InputStream getResource(String var1) {
      try {
         return new FileInputStream(PathManager.get().getStyleFile(var1));
      } catch (FileNotFoundException var3) {
         var3.printStackTrace();
         return null;
      }
   }

   public static UserInterface get() {
      return UIManager.get().getUserInterface();
   }

   public void initialize() {
      this.dialogOverlay = new FlowPane();
      this.dialogOverlay.getStyleClass().add("dialog-overlay-pane");
      this.dialogOverlay.setMouseTransparent(true);
      this.dialogOverlay.setOpacity(0.0);
      this.getChildren().add(this.dialogOverlay);
      this.introUI = (IntroUI)UIManager.get().loadUI("IntroUI");
      this.rootPane = new BorderPane();
      this.projectTree = (ProjectTreeUI)UIManager.get().loadUI("ProjectTreeUI");
      this.inspectorPane = (InspectorPaneUI)UIManager.get().loadUI("InspectorPaneUI");
      this.editorPane = (EditorPaneUI)UIManager.get().loadUI("EditorPaneUI");
      this.statusBar = new StatusBar();
      this.rootPane.setBottom(this.statusBar);
      this.statusNameLabel = new Label();
      this.statusTypeLabel = new Label();
      this.statusBar.getLeftNodes().add(this.statusNameLabel);
      this.statusBar.getRightNodes().add(this.statusTypeLabel);
      this.inspectorPane.reset();
      this.loadRibbon();
      RibbonTab var99 = new RibbonTab(" ");
      this.getRibbon().getTabs().add(var99);
      this.getRibbon().getSelectionModel().select(var99);
      this.getRibbon().setVisible(false);
      this.getRibbon().setManaged(false);
      this.getRibbon().setMinHeight(0.0);
      this.getRibbon().setPrefHeight(0.0);
      this.getRibbon().setMaxHeight(0.0);
      this.ribbonDock = new RibbonDock(this.tabModel);
      this.dock = new DockLayout(
         null,
         this.projectTree.getMainNode(),
         this.editorPane.getMainNode(),
         this.inspectorPane.getMainNode(),
         BuildLog.getPanel()
      );
      BuildLog.setDockHandler(() -> this.dock.setShown(DockLayout.Panel.BOTTOM, true));
      this.quickBar = new QuickBar(this.dock, this.ribbonDock, this.programMenu, var1 -> this.ribbonTabs.get(var1.toUpperCase()).getTab());
      this.HEADER = new VBox(this.quickBar, this.ribbonDock.getPanel());
      this.rootPane.setCenter(this.dock.getRoot());
      // bar stays on top of both screens
      this.SHELL = new BorderPane();
      this.SHELL.setTop(this.HEADER);
      this.SHELL.setCenter(this.introUI.getMainNode());
      this.setContent(this.SHELL);
      this.editorPane.getTabPane().getSelectionModel().selectedItemProperty().addListener((var1, var2, var3) -> this.updateRightPanel(var3));
      SceneTranslator.startLive();
   }

   private void updateRightPanel(javafx.scene.control.Tab var1) {
      sporemodder.imageeditor.ui.ImageEditorView var2 = null;
      if (var1 instanceof EditorPaneUI.EditorTab && ((EditorPaneUI.EditorTab)var1).editor != null) {
         Node var3 = ((EditorPaneUI.EditorTab)var1).editor.getUI();
         if (var3 instanceof sporemodder.imageeditor.ui.ImageEditorView) {
            var2 = (sporemodder.imageeditor.ui.ImageEditorView)var3;
         }
      }

      if (this.dockedImageView != null && this.dockedImageView != var2) {
         this.dockedImageView.setSidePanelDocked(false);
         this.dockedImageView = null;
      }

      if (var2 != null) {
         var2.setSidePanelDocked(true);
         this.dockedImageView = var2;
         this.dock.setRightContent(var2.getSidePanel());
      } else {
         this.dock.setRightContent(this.inspectorPane.getMainNode());
      }
   }

   public void showMainUI() {
      this.SHELL.setCenter(this.rootPane);
   }

   public void showIntroUI() {
      this.SHELL.setCenter(this.introUI.getMainNode());
   }

   private void loadRibbon() {
      Ribbon var1 = this.tabModel;
      var1.setContentHeight(UIManager.get().scaleByDpi(92.0));
      ProjectRibbonTab var2 = new ProjectRibbonTab();
      var2.addTab(var1);
      this.ribbonTabs.put("PROJECT".toUpperCase(), var2);
      EditRibbonTab var3 = new EditRibbonTab();
      var3.addTab(var1);
      this.ribbonTabs.put("EDIT".toUpperCase(), var3);
      UtilRibbonTab var4 = new UtilRibbonTab();
      var4.addTab(var1);
      this.ribbonTabs.put("UTIL".toUpperCase(), var4);
      ModAndGitRibbonTab var5 = new ModAndGitRibbonTab();
      var5.addTab(var1);
      this.ribbonTabs.put("MOD_AND_GIT".toUpperCase(), var5);
      DllRibbonTab var6 = new DllRibbonTab();
      var6.addTab(var1);
      this.ribbonTabs.put("DLL".toUpperCase(), var6);
      SceneTranslator.setRibbon(var1);
      this.setUpModeLayout(var1, var2, var3, var4, var5, var6);
      this.programMenu = new ProgramMenu();
      this.programMenu.initialize(var1);

   }

   private void setUpModeLayout(Ribbon var1, ProjectRibbonTab var2, EditRibbonTab var3, UtilRibbonTab var4, ModAndGitRibbonTab var5, DllRibbonTab var6) {
      ModeLayout var7 = new ModeLayout(var1);
      var7.addTab(var2.getTab(), EnumSet.allOf(WorkMode.class));
      var7.addTab(var3.getTab(), EnumSet.allOf(WorkMode.class));
      var7.addTab(var4.getTab(), EnumSet.allOf(WorkMode.class));
      var7.addTab(var5.getTab(), WorkMode.REGULAR_TOOLS);
      var7.addTab(var6.getTab(), WorkMode.DLL_TOOLS);
      var7.restrictGroups(
         WorkMode.REGULAR_TOOLS,
         new RibbonGroup[]{
            (RibbonGroup)var2.getModdingActionsUI().getMainNode(),
            (RibbonGroup)var2.getOtherProjectRibbonUI().getMainNode(),
            (RibbonGroup)var4.getNumberConverterUI().getMainNode(),
            (RibbonGroup)var4.getHashConverterUI().getMainNode()
         }
      );
      ModeManager.get().addListener((var1x, var2x) -> var7.apply(var2x));
      var7.apply(ModeManager.get().getMode());
   }

   public void setRibbonTabController(String var1, RibbonTabController var2) {
      this.ribbonTabs.put(var1, var2);
   }

   public RibbonTabController getRibbonTabController(String var1) {
      return this.ribbonTabs.get(var1.toUpperCase());
   }

   public void setActiveRibbonTab(String var1) {
      if (var1 != null && this.ribbonTabs.containsKey(var1)) {
         this.activeRibbonTab = var1;
         this.tabModel.getSelectionModel().select(this.ribbonTabs.get(var1).getTab());
      } else {
         throw new IllegalArgumentException();
      }
   }

   public String getActiveRibbonTab() {
      return this.activeRibbonTab;
   }

   public void toggleInspectorOnly(boolean var1) {
      this.dock.setInspectorOnly(var1);
      this.HEADER.setVisible(!var1);
      this.HEADER.setManaged(!var1);
   }

   public DockLayout getDock() {
      return this.dock;
   }

   public void setStatusFile(ProjectItem var1) {
      if (var1 == null) {
         this.statusNameLabel.setText("");
         this.statusTypeLabel.setText("");
      } else {
         String var2 = var1.getName();
         this.statusNameLabel.setText(var2);
         int var3 = var2.indexOf(".");
         if (var3 != -1) {
            String var4 = var2.substring(var3);
            String var5 = DocumentationManager.get().getDocumentation("type_names", var4);
            if (var5.equals(var4)) {
               var4 = var2.substring(var2.lastIndexOf("."));
               var5 = DocumentationManager.get().getDocumentation("type_names", var4);
            }

            if (var5.equals(var4)) {
               this.statusTypeLabel.setText(var5);
            } else {
               this.statusTypeLabel.setText(var5 + " (" + var4 + ")");
            }
         } else {
            this.statusTypeLabel.setText("");
         }
      }
   }

   public void setStatusInfo(Node var1) {
      if (var1 == null) {
         if (this.statusBar.getLeftNodes().size() > 1) {
            this.statusBar.getLeftNodes().remove(1);
         }
      } else if (this.statusBar.getLeftNodes().size() == 1) {
         this.statusBar.getLeftNodes().add(var1);
      } else {
         this.statusBar.getLeftNodes().set(1, var1);
      }
   }

   public ProgramMenu getProgramMenu() {
      return this.programMenu;
   }

   public EditorPaneUI getEditorPane() {
      return this.editorPane;
   }

   public ProjectTreeUI getProjectTree() {
      return this.projectTree;
   }

   public InspectorPaneUI getInspectorPane() {
      return this.inspectorPane;
   }

   public StatusBar getStatusBar() {
      return this.statusBar;
   }

   public Pane getDialogOverlay() {
      return this.dialogOverlay;
   }

   public IntroUI getIntroUI() {
      return this.introUI;
   }
}
