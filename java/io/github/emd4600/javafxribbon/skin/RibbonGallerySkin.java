package io.github.emd4600.javafxribbon.skin;

import javafx.beans.Observable;
import io.github.emd4600.javafxribbon.RibbonGallery;
import io.github.emd4600.javafxribbon.RibbonGalleryItem;
import java.util.function.Consumer;
import javafx.application.Platform;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.SkinBase;
import javafx.scene.control.Tooltip;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.TextAlignment;
import javafx.stage.Popup;

public class RibbonGallerySkin extends SkinBase<RibbonGallery> {
   private final BorderPane mainPane = new BorderPane();
   private final VBox buttonsPane = new VBox();
   private StackPane upButton;
   private StackPane downButton;
   private StackPane popupButton;
   private final Popup popup = new Popup();
   private final GalleryPane itemsPane = new GalleryPane();
   private final GalleryPane popupPane = new GalleryPane();
   private final Pane popupMainPane = new Pane();

   public RibbonGallerySkin(RibbonGallery control) {
      super(control);
      this.buttonsPane.getStyleClass().add("gallery-buttons-pane");
      this.createButtons(this.buttonsPane);
      this.itemsPane.getStyleClass().add("items-pane");
      this.itemsPane.columnCountProperty().bind(control.columnCountProperty());
      this.itemsPane.displayedRowProperty().bind(control.displayedRowProperty());
      this.itemsPane.setPrefHeight(Double.MAX_VALUE);
      this.itemsPane.setMinWidth(Double.NEGATIVE_INFINITY);
      this.popupPane.getStyleClass().add("items-pane");
      this.popupPane.columnCountProperty().bind(control.columnCountProperty());
      this.popupPane.setPrefHeight(-1.0);
      this.popupPane.setMinWidth(Double.NEGATIVE_INFINITY);
      this.popupPane.setMinHeight(Double.NEGATIVE_INFINITY);
      this.popupPane.setExpanded(true);
      this.mainPane.setCenter(this.itemsPane);
      this.mainPane.setRight(this.buttonsPane);
      this.getChildren().add(this.mainPane);
      this.generateItems();
      control.getItems().addListener((Observable c) -> this.generateItems());
      control.displayedRowProperty().addListener((obs, oldValue, newValue) -> this.changeDisplayRow(newValue.intValue()));
      control.showingPopupProperty().addListener((obs, oldValue, newValue) -> this.showPopup(newValue));
      this.itemsPane.visibleRowsProperty().addListener((obs, oldValue, newValue) -> {
         if (newValue.intValue() != 0) {
            this.updateButtons();
         }
      });
      this.popupMainPane.getChildren().add(this.popupPane);
      this.popupMainPane.getStylesheets().addAll(new String[]{control.getRibbon().getRibbonWindow().getUserAgentStylesheet()});
      this.popup.getContent().add(this.popupMainPane);
      Platform.runLater(() -> control.getScene().addEventFilter(MouseEvent.MOUSE_CLICKED, event -> {
            if (!this.popupMainPane.contains(this.popupMainPane.screenToLocal(event.getX(), event.getY()))) {
               control.setShowingPopup(false);
            }
         }));
   }

   private void showPopup(boolean show) {
      if (show) {
         this.popupMainPane.getStyleClass().setAll(((RibbonGallery)this.getSkinnable()).getStyleClass());
         this.popupMainPane.getStyleClass().add("ribbon-gallery-popup");
         this.popupMainPane.setPrefWidth(((RibbonGallery)this.getSkinnable()).getWidth());
         Point2D coords = ((RibbonGallery)this.getSkinnable()).localToScreen(0.0, 0.0);
         this.popup.show(this.getSkinnable(), coords.getX(), coords.getY());
      } else {
         this.popup.hide();
      }
   }

   private Button createItem(RibbonGalleryItem item) {
      Button label = new Button(item.getText());
      label.setGraphic(item.getGraphic());
      label.setMinWidth(Double.NEGATIVE_INFINITY);
      label.getStyleClass().add("gallery-item");
      label.setTextAlignment(TextAlignment.LEFT);
      label.setAlignment(Pos.CENTER_LEFT);
      if (item.getGraphic() != null) {
         if (((RibbonGallery)this.getSkinnable()).getDisplayPriority() == RibbonGallery.GalleryItemDisplay.GRAPHIC_PRIORITY) {
            label.setContentDisplay(ContentDisplay.CENTER);
         } else {
            label.setContentDisplay(ContentDisplay.LEFT);
         }
      }

      if (item.getDescription() != null) {
         label.setTooltip(new Tooltip(item.getDescription()));
      }

      label.setOnAction(event -> {
         if (((RibbonGallery)this.getSkinnable()).isShowingPopup()) {
            ((RibbonGallery)this.getSkinnable()).setShowingPopup(false);
         }

         Consumer<RibbonGalleryItem> action = ((RibbonGallery)this.getSkinnable()).getOnItemAction();
         if (action != null) {
            action.accept(item);
         }
      });
      return label;
   }

   private void generateItems() {
      for (RibbonGalleryItem item : ((RibbonGallery)this.getSkinnable()).getItems()) {
         this.itemsPane.getChildren().add(this.createItem(item));
         this.popupPane.getChildren().add(this.createItem(item));
      }
   }

   private void createButtons(VBox buttonsPane) {
      this.upButton = new StackPane();
      this.upButton.getStyleClass().addAll(new String[]{"gallery-button"});
      this.upButton.setOnMouseReleased(event -> {
         if (!this.upButton.isDisabled()) {
            ((RibbonGallery)this.getSkinnable()).setDisplayedRow(((RibbonGallery)this.getSkinnable()).getDisplayedRow() - this.itemsPane.getVisibleRows());
         }
      });
      this.downButton = new StackPane();
      this.downButton.getStyleClass().addAll(new String[]{"gallery-button"});
      this.downButton.setOnMouseReleased(event -> {
         if (!this.downButton.isDisabled()) {
            ((RibbonGallery)this.getSkinnable()).setDisplayedRow(((RibbonGallery)this.getSkinnable()).getDisplayedRow() + this.itemsPane.getVisibleRows());
         }
      });
      this.popupButton = new StackPane();
      this.popupButton.getStyleClass().addAll(new String[]{"gallery-button"});
      this.popupButton.setOnMouseReleased(event -> {
         if (!this.popupButton.isDisabled()) {
            ((RibbonGallery)this.getSkinnable()).setShowingPopup(true);
         }
      });
      Region upButtonGraphic = new Region();
      upButtonGraphic.getStyleClass().setAll(new String[]{"up-button-graphic"});
      upButtonGraphic.setFocusTraversable(false);
      upButtonGraphic.setMaxHeight(Double.NEGATIVE_INFINITY);
      upButtonGraphic.setMaxWidth(Double.NEGATIVE_INFINITY);
      Region downButtonGraphic = new Region();
      downButtonGraphic.getStyleClass().setAll(new String[]{"down-button-graphic"});
      downButtonGraphic.setFocusTraversable(false);
      downButtonGraphic.setMaxWidth(Double.NEGATIVE_INFINITY);
      downButtonGraphic.setMaxHeight(Double.NEGATIVE_INFINITY);
      Region popupButtonGraphic = new Region();
      popupButtonGraphic.getStyleClass().setAll(new String[]{"popup-button-graphic"});
      popupButtonGraphic.setFocusTraversable(false);
      popupButtonGraphic.setMaxWidth(Double.NEGATIVE_INFINITY);
      popupButtonGraphic.setMaxHeight(Double.NEGATIVE_INFINITY);
      this.upButton.getChildren().add(upButtonGraphic);
      this.downButton.getChildren().add(downButtonGraphic);
      this.popupButton.getChildren().add(popupButtonGraphic);
      buttonsPane.getChildren().addAll(new Node[]{this.upButton, this.downButton, this.popupButton});
   }

   private void updateButtons() {
      int numVisibleRows = this.itemsPane.getVisibleRows();
      int numRows = this.itemsPane.getTotalRows();
      int currentRow = ((RibbonGallery)this.getSkinnable()).getDisplayedRow();
      setDisable(this.upButton, currentRow == 0);
      setDisable(this.downButton, currentRow >= numRows - numVisibleRows);
      setDisable(this.popupButton, numVisibleRows == numRows);
   }

   private static void setDisable(Node element, boolean isDisabled) {
      element.setDisable(isDisabled);
      if (isDisabled) {
         element.getStyleClass().add("disabled");
      } else {
         element.getStyleClass().remove("disabled");
      }
   }

   private void changeDisplayRow(int newRow) {
      this.updateButtons();
   }
}
