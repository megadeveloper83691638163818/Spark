package sporemodder.view;

import io.github.emd4600.javafxribbon.Ribbon;
import io.github.emd4600.javafxribbon.RibbonGroup;
import io.github.emd4600.javafxribbon.RibbonTab;
import javafx.collections.ListChangeListener;
import javafx.geometry.Insets;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.HBox;

/**
 * Replacement for the old ribbon header: the tabs are buttons in the action bar, and the groups of the chosen
 * tab appear in a panel under it. Pressing the same tab again closes the panel.
 */
public class RibbonDock {
   private final Ribbon model;
   private final HBox tabStrip = new HBox(4);
   private final HBox groupsBox = new HBox(10);
   private final ScrollPane panel = new ScrollPane(groupsBox);
   private final ListChangeListener<RibbonGroup> groupsListener = c -> refresh();
   private RibbonTab shownTab;
   private boolean open;

   public RibbonDock(Ribbon model) {
      this.model = model;
      this.tabStrip.getStyleClass().add("top-tabs");
      this.groupsBox.getStyleClass().add("ribbon-panel-content");
      this.groupsBox.setPadding(new Insets(6, 8, 6, 8));
      this.panel.getStyleClass().add("ribbon-panel");
      this.panel.setFitToHeight(true);
      this.panel.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
      this.panel.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
      this.panel.setVisible(false);
      this.panel.setManaged(false);
      model.getTabs().addListener((ListChangeListener<RibbonTab>)c -> rebuildButtons());
      model.getSelectionModel().selectedItemProperty().addListener((o, a, b) -> {
         refresh();
         updateButtons();
      });
      rebuildButtons();
   }

   public HBox getTabStrip() {
      return tabStrip;
   }

   public ScrollPane getPanel() {
      return panel;
   }

   public boolean isOpen() {
      return open;
   }

   /** Opens the tab, or closes the panel when this tab is already open. */
   public void toggle(RibbonTab tab) {
      if (tab == null) {
         return;
      }

      if (open && model.getSelectionModel().getSelectedItem() == tab) {
         close();
      } else {
         model.getSelectionModel().select(tab);
         open = true;
         refresh();
         updateButtons();
      }
   }

   public void close() {
      open = false;
      refresh();
      updateButtons();
   }

   private void rebuildButtons() {
      tabStrip.getChildren().clear();
      for (RibbonTab tab : model.getTabs()) {
         ToggleButton button = new ToggleButton(tab.getText());
         button.getStyleClass().add("top-tab");
         button.setUserData(tab);
         tab.textProperty().addListener((o, a, b) -> button.setText(b));
         button.setOnAction(e -> toggle(tab));
         tabStrip.getChildren().add(button);
      }

      if (open && !model.getTabs().contains(model.getSelectionModel().getSelectedItem())) {
         open = false;
      }

      refresh();
      updateButtons();
   }

   private void updateButtons() {
      RibbonTab selected = model.getSelectionModel().getSelectedItem();
      for (javafx.scene.Node n : tabStrip.getChildren()) {
         ((ToggleButton)n).setSelected(open && n.getUserData() == selected);
      }
   }

   private void refresh() {
      RibbonTab selected = model.getSelectionModel().getSelectedItem();
      if (shownTab != selected) {
         if (shownTab != null) {
            shownTab.getGroups().removeListener(groupsListener);
         }

         shownTab = selected;
         if (shownTab != null) {
            shownTab.getGroups().addListener(groupsListener);
         }
      }

      groupsBox.getChildren().clear();
      boolean visible = open && selected != null;
      if (visible) {
         groupsBox.getChildren().setAll(selected.getGroups());
      }

      panel.setVisible(visible);
      panel.setManaged(visible);
   }
}
