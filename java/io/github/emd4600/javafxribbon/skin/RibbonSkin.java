package io.github.emd4600.javafxribbon.skin;

import javafx.beans.Observable;
import io.github.emd4600.javafxribbon.Ribbon;
import io.github.emd4600.javafxribbon.RibbonGroup;
import io.github.emd4600.javafxribbon.RibbonMenu;
import io.github.emd4600.javafxribbon.RibbonProgramButton;
import io.github.emd4600.javafxribbon.RibbonTab;
import javafx.geometry.Bounds;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Separator;
import javafx.scene.control.SkinBase;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;

public class RibbonSkin extends SkinBase<Ribbon> {
   private final BorderPane pane = new BorderPane();
   private final HBox tabsPane = new HBox(0.0);
   private Pane tabsPaneOutline;
   private final BorderPane contentPane = new BorderPane();
   private final HBox groupsContainer = new HBox();
   private ImageView expandImage;
   private ImageView minimizeImage;
   private final Button expandButton = new Button();

   public RibbonSkin(Ribbon control) {
      super(control);
      this.construct();
   }

   private void handleRibbonState(Ribbon.RibbonState state) {
      if (state == Ribbon.RibbonState.DROPDOWN) {
         ((Ribbon)this.getSkinnable()).getRibbonWindow().getChildren().add(this.contentPane);
         this.contentPane.setTranslateY(this.tabsPane.getBoundsInParent().getMaxY());
         this.expandButton.setGraphic(this.expandImage);
      } else {
         ((Ribbon)this.getSkinnable()).getRibbonWindow().getChildren().remove(this.contentPane);
         this.contentPane.setTranslateY(0.0);
         if (state == Ribbon.RibbonState.EXPANDED) {
            this.pane.setCenter(this.contentPane);
            this.updateContentHeight(((Ribbon)this.getSkinnable()).getContentHeight());
            this.expandButton.setGraphic(this.minimizeImage);
         } else if (state == Ribbon.RibbonState.MINIMIZED) {
            this.pane.setCenter(null);
         }
      }
   }

   private void constructTabsPane() {
      this.tabsPane.getChildren().clear();
      this.createProgramButton();

      for (RibbonTab tab : ((Ribbon)this.getSkinnable()).getTabs()) {
         this.createTabHeader(tab);
      }

      this.tabsPane.getChildren().add(this.tabsPaneOutline);
   }

   private void construct() {
      this.pane.setTop(this.tabsPane);
      this.contentPane.setCenter(this.groupsContainer);
      this.contentPane.getStyleClass().add("ribbon-content-area");
      this.groupsContainer.maxHeightProperty().bind(this.contentPane.maxHeightProperty());
      this.groupsContainer.setAlignment(Pos.CENTER_LEFT);
      this.updateContentHeight(((Ribbon)this.getSkinnable()).getContentHeight());
      this.handleRibbonState(((Ribbon)this.getSkinnable()).getRibbonState());
      this.tabsPaneOutline = new Pane();
      this.tabsPaneOutline.getStyleClass().add("ribbon-tab-header-outline");
      HBox.setHgrow(this.tabsPaneOutline, Priority.SOMETIMES);
      this.constructTabsPane();
      this.setContent((RibbonTab)((Ribbon)this.getSkinnable()).getSelectionModel().getSelectedItem());
      this.getChildren().add(this.pane);
      this.expandImage = new ImageView(new Image(((Ribbon)this.getSkinnable()).getRibbonWindow().getResource("ribbon-expand.png")));
      this.minimizeImage = new ImageView(new Image(((Ribbon)this.getSkinnable()).getRibbonWindow().getResource("ribbon-minimize.png")));
      this.expandButton.getStyleClass().add("ribbon-button");
      this.expandButton.setPadding(new Insets(0.0));
      this.expandButton.setOnAction(event -> {
         if (((Ribbon)this.getSkinnable()).getRibbonState() == Ribbon.RibbonState.EXPANDED) {
            ((Ribbon)this.getSkinnable()).setRibbonState(Ribbon.RibbonState.MINIMIZED);
         } else if (((Ribbon)this.getSkinnable()).getRibbonState() == Ribbon.RibbonState.DROPDOWN) {
            ((Ribbon)this.getSkinnable()).setRibbonState(Ribbon.RibbonState.EXPANDED);
         }
      });
      this.expandButton.setGraphic(this.minimizeImage);
      this.contentPane.setRight(this.expandButton);
      BorderPane.setAlignment(this.expandButton, Pos.BOTTOM_RIGHT);
      ((Ribbon)this.getSkinnable()).getTabs().addListener((Observable c) -> this.constructTabsPane());
      ((Ribbon)this.getSkinnable()).getSelectionModel().selectedItemProperty().addListener((obs, oldValue, newValue) -> {
         if (newValue != null) {
            this.setContent(newValue);
         }
      });
      ((Ribbon)this.getSkinnable()).contentHeightProperty().addListener((obs, oldValue, newValue) -> this.updateContentHeight(newValue.doubleValue()));
      ((Ribbon)this.getSkinnable()).ribbonStateProperty().addListener((obs, oldValue, newValue) -> this.handleRibbonState(newValue));
      ((Ribbon)this.getSkinnable())
         .setOnScroll(
            event -> {
               if (event.getDeltaY() < 0.0
                  && ((Ribbon)this.getSkinnable()).getSelectionModel().getSelectedIndex() < ((Ribbon)this.getSkinnable()).getTabs().size() - 1) {
                  ((Ribbon)this.getSkinnable()).getSelectionModel().select(((Ribbon)this.getSkinnable()).getSelectionModel().getSelectedIndex() + 1);
               } else if (event.getDeltaY() > 0.0 && ((Ribbon)this.getSkinnable()).getSelectionModel().getSelectedIndex() > 0) {
                  ((Ribbon)this.getSkinnable()).getSelectionModel().select(((Ribbon)this.getSkinnable()).getSelectionModel().getSelectedIndex() - 1);
               }
            }
         );
      ((Ribbon)this.getSkinnable()).getScene().addEventFilter(MouseEvent.MOUSE_PRESSED, event -> {
         if (((Ribbon)this.getSkinnable()).getRibbonState() == Ribbon.RibbonState.DROPDOWN) {
            if (this.contentPane.contains(this.contentPane.sceneToLocal(event.getSceneX(), event.getSceneY()))) {
               return;
            }

            for (Node node : this.tabsPane.getChildren()) {
               if (node.contains(node.sceneToLocal(event.getSceneX(), event.getSceneY())) && node != this.tabsPaneOutline) {
                  return;
               }
            }

            ((Ribbon)this.getSkinnable()).setRibbonState(Ribbon.RibbonState.MINIMIZED);
         }
      });
   }

   private void createProgramButton() {
      if (((Ribbon)this.getSkinnable()).getProgramButton() != null) {
         RibbonProgramButton button = ((Ribbon)this.getSkinnable()).getProgramButton();
         button.setOnAction(event -> {
            if (button.getRibbonMenu().isShowing()) {
               button.getRibbonMenu().hide();
               button.setSelected(false);
            } else {
               Bounds rect = button.localToScreen(button.getBoundsInLocal());
               button.getRibbonMenu().show(this.getSkinnable(), rect.getMinX(), rect.getMaxY());
               button.setSelected(true);
            }
         });
         button.sceneProperty().addListener((obs, oldValue, newValue) -> {
            if (newValue != null) {
               newValue.addEventFilter(MouseEvent.MOUSE_PRESSED, event -> {
                  RibbonMenu menu = button.getRibbonMenu();
                  if (menu.isShowing() && !menu.contains(event.getScreenX(), event.getScreenY())) {
                     menu.hide();
                     button.setSelected(false);
                     if (button.contains(button.screenToLocal(event.getScreenX(), event.getScreenY()))) {
                        event.consume();
                     }
                  }
               });
            }
         });
         this.tabsPane.getChildren().add(button);
      }
   }

   private void updateContentHeight(double newValue) {
      if (newValue == -1.0) {
         this.contentPane.setPrefHeight(-1.0);
         this.contentPane.setMaxHeight(-1.0);
         this.contentPane.setMinHeight(-1.0);
      } else {
         this.contentPane.setPrefHeight(newValue);
         this.contentPane.setMaxHeight(newValue);
         this.contentPane.setMinHeight(newValue);
      }
   }

   private void createTabHeader(RibbonTab tab) {
      RibbonHeader button = new RibbonHeader(tab);
      this.tabsPane.getChildren().add(button);
      button.setOnMouseClicked(event -> {
         if (event.getButton() == MouseButton.PRIMARY) {
            Ribbon.RibbonState state = ((Ribbon)this.getSkinnable()).getRibbonState();
            if (state == Ribbon.RibbonState.EXPANDED) {
               if (tab.isSelected() && event.getClickCount() == 2) {
                  ((Ribbon)this.getSkinnable()).setRibbonState(Ribbon.RibbonState.MINIMIZED);
               } else {
                  this.select(tab);
               }
            } else if (state == Ribbon.RibbonState.MINIMIZED) {
               if (event.getClickCount() == 2) {
                  this.select(tab);
                  ((Ribbon)this.getSkinnable()).setRibbonState(Ribbon.RibbonState.EXPANDED);
               } else {
                  this.select(tab);
                  ((Ribbon)this.getSkinnable()).setRibbonState(Ribbon.RibbonState.DROPDOWN);
               }
            } else if (state == Ribbon.RibbonState.DROPDOWN) {
               if (event.getClickCount() == 2) {
                  this.select(tab);
                  ((Ribbon)this.getSkinnable()).setRibbonState(Ribbon.RibbonState.EXPANDED);
               } else {
                  this.select(tab);
               }
            }
         }
      });
   }

   private void select(RibbonTab tab) {
      tab.getRibbon().getSelectionModel().select(tab);
   }

   private void setContent(RibbonTab tab) {
      this.groupsContainer.getChildren().clear();

      for (RibbonGroup group : tab.getGroups()) {
         group.prefHeightProperty().bind(((Ribbon)this.getSkinnable()).contentHeightProperty());
         this.groupsContainer.getChildren().add(group);
         Separator separator = new Separator(Orientation.VERTICAL);
         separator.getStyleClass().add("ribbon-group-separator");
         separator.setMaxHeight(((Ribbon)this.getSkinnable()).getContentHeight() - 6.0);
         this.groupsContainer.getChildren().add(separator);
      }
   }
}
