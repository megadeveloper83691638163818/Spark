package sporemodder.view;

import io.github.emd4600.javafxribbon.RibbonTab;
import javafx.geometry.Bounds;
import javafx.geometry.Orientation;
import javafx.scene.control.Button;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.control.RadioMenuItem;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.Separator;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.FlowPane;
import javafx.scene.control.Tooltip;
import javafx.stage.Popup;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import sporemodder.DllModActions;
import sporemodder.EditorManager;
import sporemodder.GameManager;
import sporemodder.LanguageManager;
import sporemodder.ModeManager;
import sporemodder.ProjectManager;
import sporemodder.UIManager;
import sporemodder.WorkMode;
import sporemodder.view.dialogs.ModApiSearchDialog;
import sporemodder.view.dialogs.ProgramSettingsUI;
import sporemodder.view.dialogs.WorkModeDialog;
import sporemodder.view.editors.ItemEditor;
import sporemodder.view.editors.TextEditor;
import sporemodder.view.ribbons.ProgramMenu;

/**
 * The main action bar. It replaces the old ribbon: a File menu, the old ribbon tabs (which open a tool panel
 * under the bar, and close it when pressed again) and the most used actions.
 */
public class QuickBar extends FlowPane {
   private final MenuButton modeButton = new MenuButton();
   private Button MOD_API;
   private final ToggleGroup modeGroup = new ToggleGroup();
   private final ToggleButton fileButton = new ToggleButton("File");
   private final Popup filePopup = new Popup();
   private long filePopupHiddenAt;

   public QuickBar(DockLayout dock, RibbonDock ribbonDock, ProgramMenu programMenu, java.util.function.Function<String, RibbonTab> tabByName) {
      super(8, 6);
      this.getStyleClass().add("quick-bar");

      setUpFileMenu(programMenu);

      Button settings = action("Settings", "Change the program settings.", ProgramSettingsUI::show);
      Button save = action("Save", "Save the changes on the file (Ctrl+S)", () -> {
         EditorManager editors = EditorManager.get();
         if (editors.canSave()) {
            UIManager.get().tryAction(editors::saveActive, "Cannot save file.");
         }
      });
      Button exit = action("Exit", "Close the program", () -> {
         Stage stage = UIManager.get().getPrimaryStage();
         stage.fireEvent(new WindowEvent(stage, WindowEvent.WINDOW_CLOSE_REQUEST));
      });

      for (WorkMode mode : WorkMode.values()) {
         RadioMenuItem item = new RadioMenuItem(LanguageManager.tr(mode.getDisplayName()));
         item.setToggleGroup(modeGroup);
         item.setUserData(mode);
         item.setOnAction(e -> ModeManager.get().setMode(mode));
         modeButton.getItems().add(item);
      }

      MenuItem more = new MenuItem("Work mode...");
      more.setOnAction(e -> WorkModeDialog.show());
      modeButton.getItems().addAll(new SeparatorMenuItem(), more);
      updateMode(ModeManager.get().getMode());
      ModeManager.get().addListener((oldMode, newMode) -> updateMode(newMode));

      Button converter = action("Converter", "Number, hash and package converters (press again to close)", () -> ribbonDock.toggle(tabByName.apply("UTIL")));
      Button folder = action("Folder", "Opens the folder of the active project", DllModActions::openActiveFolder);
      Button compile = action("Compile", "Packs the mod, or builds the DLL in DLL mode", () -> {
         if (WorkMode.DLL_TOOLS.contains(ModeManager.get().getMode())) {
            DllModActions.buildActive();
         } else {
            ProjectManager.get().packActive(false);
         }
      });
      compile.getStyleClass().add("accent-button");
      MOD_API = action("ModAPI", "Find a ModAPI function and insert a detour (Ctrl+Shift+F in the code editor)", () -> {
         ItemEditor ACTIVE = EditorManager.get().getActiveEditor();
         ModApiSearchDialog.SHOW(ACTIVE instanceof TextEditor ? (TextEditor)ACTIVE : null);
      });
      Button run = action("Run game", "Run game", () -> UIManager.get().tryAction(() -> GameManager.get().runGame(), "Cannot run game."));

      MenuButton view = new MenuButton("View");
      view.getItems().addAll(
         panelItem("Project tree", dock, DockLayout.Panel.LEFT),
         panelItem("Inspector", dock, DockLayout.Panel.RIGHT),
         panelItem("Debug", dock, DockLayout.Panel.BOTTOM));
      MenuItem reset = new MenuItem("Reset layout");
      reset.setOnAction(e -> dock.resetLayout());
      view.getItems().addAll(new SeparatorMenuItem(), reset);
      dock.addVisibilityListener(() -> {
         for (MenuItem m : view.getItems()) {
            if (m instanceof CheckMenuItem && m.getUserData() instanceof DockLayout.Panel) {
               ((CheckMenuItem)m).setSelected(dock.isShown((DockLayout.Panel)m.getUserData()));
            }
         }
      });

      this.getChildren().addAll(fileButton, new Separator(Orientation.VERTICAL), ribbonDock.getTabStrip(), new Separator(Orientation.VERTICAL),
         settings, save, exit, new Separator(Orientation.VERTICAL), modeButton, new Separator(Orientation.VERTICAL), converter, folder, MOD_API, compile, run, new Separator(Orientation.VERTICAL), view);
      updateMode(ModeManager.get().getMode());
      SceneTranslator.translate(this);
   }

   private void setUpFileMenu(ProgramMenu programMenu) {
      fileButton.getStyleClass().add("top-tab");
      fileButton.setTooltip(new Tooltip("Projects, packing and unpacking"));
      filePopup.setAutoHide(true);
      programMenu.setCloseHandler(filePopup::hide);
      filePopup.getContent().add(programMenu.getMainNode());
      programMenu.getMainNode().getStyleClass().add("file-popup");
      filePopup.setOnHidden(e -> {
         filePopupHiddenAt = System.currentTimeMillis();
         fileButton.setSelected(false);
      });
      fileButton.setOnAction(e -> {
         // an auto-hide caused by this very click must not reopen the popup
         if (filePopup.isShowing() || System.currentTimeMillis() - filePopupHiddenAt < 250) {
            filePopup.hide();
            fileButton.setSelected(false);
            return;
         }

         Bounds b = fileButton.localToScreen(fileButton.getBoundsInLocal());
         SceneTranslator.translate(programMenu.getMainNode());
         filePopup.show(fileButton, b.getMinX(), b.getMaxY());
         fileButton.setSelected(true);
      });
   }

   private CheckMenuItem panelItem(String text, DockLayout dock, DockLayout.Panel panel) {
      CheckMenuItem item = new CheckMenuItem(text);
      item.setUserData(panel);
      item.setSelected(dock.isShown(panel));
      item.setOnAction(e -> dock.setShown(panel, item.isSelected()));
      return item;
   }

   private Button action(String text, String tooltip, Runnable action) {
      Button button = new Button(text);
      button.setTooltip(new Tooltip(tooltip));
      button.setOnAction(e -> action.run());
      return button;
   }

   private void updateMode(WorkMode mode) {
      if (MOD_API != null) {
         boolean DLL = WorkMode.DLL_TOOLS.contains(mode);
         MOD_API.setVisible(DLL);
         MOD_API.setManaged(DLL);
      }
      modeButton.setText(LanguageManager.tr("Work mode") + ": " + LanguageManager.tr(mode.getDisplayName()));
      for (MenuItem item : modeButton.getItems()) {
         if (item instanceof RadioMenuItem && item.getUserData() == mode) {
            ((RadioMenuItem)item).setSelected(true);
         }
      }
   }
}
