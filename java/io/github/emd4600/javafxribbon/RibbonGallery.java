package io.github.emd4600.javafxribbon;

import io.github.emd4600.javafxribbon.skin.RibbonGallerySkin;
import java.util.function.Consumer;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Control;
import javafx.scene.control.Skin;

public class RibbonGallery extends Control {
   public static final String DEFAULT_STYLE_CLASS = "ribbon-gallery";
   private final ObjectProperty<RibbonGallery.GalleryItemDisplay> displayPriority = new SimpleObjectProperty(RibbonGallery.GalleryItemDisplay.TEXT_PRIORITY);
   private final Ribbon ribbon;
   private final ObservableList<RibbonGalleryItem> items = FXCollections.observableArrayList();
   private final IntegerProperty displayedRow = new SimpleIntegerProperty(0);
   private final BooleanProperty showingPopup = new SimpleBooleanProperty(false);
   private final IntegerProperty columnCount = new SimpleIntegerProperty(5);
   private Consumer<RibbonGalleryItem> onItemAction;

   public RibbonGallery(Ribbon ribbon) {
      this.getStyleClass().add("ribbon-gallery");
      this.ribbon = ribbon;
   }

   public Ribbon getRibbon() {
      return this.ribbon;
   }

   public void setOnItemAction(Consumer<RibbonGalleryItem> action) {
      this.onItemAction = action;
   }

   public Consumer<RibbonGalleryItem> getOnItemAction() {
      return this.onItemAction;
   }

   public final ObservableList<RibbonGalleryItem> getItems() {
      return this.items;
   }

   public final ObjectProperty<RibbonGallery.GalleryItemDisplay> displayPriorityProperty() {
      return this.displayPriority;
   }

   public final RibbonGallery.GalleryItemDisplay getDisplayPriority() {
      return (RibbonGallery.GalleryItemDisplay)this.displayPriority.get();
   }

   public final void setDisplayPriority(RibbonGallery.GalleryItemDisplay value) {
      this.displayPriority.set(value);
   }

   public final IntegerProperty displayedRowProperty() {
      return this.displayedRow;
   }

   public final int getDisplayedRow() {
      return this.displayedRow.get();
   }

   public final void setDisplayedRow(int value) {
      this.displayedRow.set(value);
   }

   public final BooleanProperty showingPopupProperty() {
      return this.showingPopup;
   }

   public final boolean isShowingPopup() {
      return this.showingPopup.get();
   }

   public final void setShowingPopup(boolean value) {
      this.showingPopup.set(value);
   }

   public final IntegerProperty columnCountProperty() {
      return this.columnCount;
   }

   public final int getColumnCount() {
      return this.columnCount.get();
   }

   public final void setColumnCount(int value) {
      this.columnCount.set(value);
   }

   protected Skin<?> createDefaultSkin() {
      return new RibbonGallerySkin(this);
   }

   public static enum GalleryItemDisplay {
      GRAPHIC_PRIORITY,
      TEXT_PRIORITY;
   }
}
