package io.github.emd4600.javafxribbon;

import io.github.emd4600.javafxribbon.skin.RibbonSkin;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.css.CssMetaData;
import javafx.css.StyleConverter;
import javafx.css.StyleableDoubleProperty;
import javafx.css.StyleableProperty;
import javafx.css.converter.SizeConverter;
import javafx.scene.AccessibleAttribute;
import javafx.scene.control.Control;
import javafx.scene.control.SingleSelectionModel;
import javafx.scene.control.Skin;

public class Ribbon extends Control {
   public static final String DEFAULT_STYLE_CLASS = "ribbon";
   public static final double NO_FIXED_HEIGHT = -1.0;
   private static final double DEFAULT_TAB_MIN_WIDTH = 0.0;
   private static final double DEFAULT_TAB_MAX_WIDTH = Double.MAX_VALUE;
   private static final double DEFAULT_TAB_MIN_HEIGHT = 0.0;
   private static final double DEFAULT_TAB_MAX_HEIGHT = Double.MAX_VALUE;
   private ReadOnlyObjectWrapper<RibbonWindow> ribbonWindow;
   private ObjectProperty<RibbonProgramButton> programButton;
   private DoubleProperty contentHeight;
   private ObjectProperty<SingleSelectionModel<RibbonTab>> selectionModel = new SimpleObjectProperty(this, "selectionModel");
   private final ObservableList<RibbonTab> tabs = FXCollections.observableArrayList();
   private DoubleProperty tabMinWidth;
   private DoubleProperty tabMaxWidth;
   private DoubleProperty tabMinHeight;
   private DoubleProperty tabMaxHeight;
   private final ObjectProperty<Ribbon.RibbonState> ribbonState = new SimpleObjectProperty(this, "ribbonState", Ribbon.RibbonState.EXPANDED);

   public Ribbon() {
      this.getStyleClass().setAll(new String[]{"ribbon"});
      this.setSelectionModel(new Ribbon.RibbonSelectionModel(this));
      this.tabs.addListener((ListChangeListener.Change<? extends RibbonTab> c) -> {
         while (c.next()) {
            for (RibbonTab tab : c.getRemoved()) {
               if (tab != null && !this.getTabs().contains(tab)) {
                  tab.setRibbon(null);
               }
            }

            for (RibbonTab tabx : c.getAddedSubList()) {
               tabx.setRibbon(this);
            }
         }
      });
      this.setPrefWidth(Double.MAX_VALUE);
   }

   final void setRibbonWindow(RibbonWindow value) {
      this.ribbonWindowPropertyImpl().set(value);
   }

   public final RibbonWindow getRibbonWindow() {
      return this.ribbonWindow == null ? null : (RibbonWindow)this.ribbonWindow.get();
   }

   public final ReadOnlyObjectProperty<RibbonWindow> ribbonProperty() {
      return this.ribbonWindowPropertyImpl().getReadOnlyProperty();
   }

   private ReadOnlyObjectWrapper<RibbonWindow> ribbonWindowPropertyImpl() {
      if (this.ribbonWindow == null) {
         this.ribbonWindow = new ReadOnlyObjectWrapper(this, "ribbonWindow");
      }

      return this.ribbonWindow;
   }

   public final ObjectProperty<RibbonProgramButton> programButtonProperty() {
      if (this.programButton == null) {
         this.programButton = new SimpleObjectProperty(this, "programButton");
      }

      return this.programButton;
   }

   public final RibbonProgramButton getProgramButton() {
      return this.programButton == null ? null : (RibbonProgramButton)this.programButton.get();
   }

   public final void setProgramButton(RibbonProgramButton value) {
      this.programButtonProperty().set(value);
   }

   public final DoubleProperty contentHeightProperty() {
      if (this.contentHeight == null) {
         this.contentHeight = new SimpleDoubleProperty(this, "contentHeight", -1.0);
      }

      return this.contentHeight;
   }

   public final double getContentHeight() {
      return this.contentHeight == null ? -1.0 : this.contentHeight.get();
   }

   public final void setContentHeight(double value) {
      this.contentHeightProperty().set(value);
   }

   public final void setSelectionModel(SingleSelectionModel<RibbonTab> value) {
      this.selectionModel.set(value);
   }

   public final SingleSelectionModel<RibbonTab> getSelectionModel() {
      return (SingleSelectionModel<RibbonTab>)this.selectionModel.get();
   }

   public final ObjectProperty<SingleSelectionModel<RibbonTab>> selectionModelProperty() {
      return this.selectionModel;
   }

   public final ObservableList<RibbonTab> getTabs() {
      return this.tabs;
   }

   public final void setTabMinWidth(double value) {
      this.tabMinWidthProperty().setValue(value);
   }

   public final double getTabMinWidth() {
      return this.tabMinWidth == null ? 0.0 : this.tabMinWidth.getValue();
   }

   public final DoubleProperty tabMinWidthProperty() {
      if (this.tabMinWidth == null) {
         this.tabMinWidth = new StyleableDoubleProperty(0.0) {
            public CssMetaData<Ribbon, Number> getCssMetaData() {
               return Ribbon.StyleableProperties.TAB_MIN_WIDTH;
            }

            public Object getBean() {
               return Ribbon.this;
            }

            public String getName() {
               return "tabMinWidth";
            }
         };
      }

      return this.tabMinWidth;
   }

   public final void setTabMaxWidth(double value) {
      this.tabMaxWidthProperty().setValue(value);
   }

   public final double getTabMaxWidth() {
      return this.tabMaxWidth == null ? Double.MAX_VALUE : this.tabMaxWidth.getValue();
   }

   public final DoubleProperty tabMaxWidthProperty() {
      if (this.tabMaxWidth == null) {
         this.tabMaxWidth = new StyleableDoubleProperty(Double.MAX_VALUE) {
            public CssMetaData<Ribbon, Number> getCssMetaData() {
               return Ribbon.StyleableProperties.TAB_MAX_WIDTH;
            }

            public Object getBean() {
               return Ribbon.this;
            }

            public String getName() {
               return "tabMaxWidth";
            }
         };
      }

      return this.tabMaxWidth;
   }

   public final void setTabMinHeight(double value) {
      this.tabMinHeightProperty().setValue(value);
   }

   public final double getTabMinHeight() {
      return this.tabMinHeight == null ? 0.0 : this.tabMinHeight.getValue();
   }

   public final DoubleProperty tabMinHeightProperty() {
      if (this.tabMinHeight == null) {
         this.tabMinHeight = new StyleableDoubleProperty(0.0) {
            public CssMetaData<Ribbon, Number> getCssMetaData() {
               return Ribbon.StyleableProperties.TAB_MIN_HEIGHT;
            }

            public Object getBean() {
               return Ribbon.this;
            }

            public String getName() {
               return "tabMinHeight";
            }
         };
      }

      return this.tabMinHeight;
   }

   public final void setTabMaxHeight(double value) {
      this.tabMaxHeightProperty().setValue(value);
   }

   public final double getTabMaxHeight() {
      return this.tabMaxHeight == null ? Double.MAX_VALUE : this.tabMaxHeight.getValue();
   }

   public final DoubleProperty tabMaxHeightProperty() {
      if (this.tabMaxHeight == null) {
         this.tabMaxHeight = new StyleableDoubleProperty(Double.MAX_VALUE) {
            public CssMetaData<Ribbon, Number> getCssMetaData() {
               return Ribbon.StyleableProperties.TAB_MAX_HEIGHT;
            }

            public Object getBean() {
               return Ribbon.this;
            }

            public String getName() {
               return "tabMaxHeight";
            }
         };
      }

      return this.tabMaxHeight;
   }

   public final ObjectProperty<Ribbon.RibbonState> ribbonStateProperty() {
      return this.ribbonState;
   }

   public final Ribbon.RibbonState getRibbonState() {
      return (Ribbon.RibbonState)this.ribbonState.get();
   }

   public final void setRibbonState(Ribbon.RibbonState state) {
      this.ribbonState.set(state);
   }

   protected Skin<?> createDefaultSkin() {
      return new RibbonSkin(this);
   }

   static class RibbonSelectionModel extends SingleSelectionModel<RibbonTab> {
      private final Ribbon ribbon;

      public RibbonSelectionModel(Ribbon t) {
         if (t == null) {
            throw new NullPointerException("TabPane can not be null");
         } else {
            this.ribbon = t;
            ListChangeListener<RibbonTab> itemsContentObserver = c -> {
               while (c.next()) {
                  for (RibbonTab tab : c.getRemoved()) {
                     if (tab != null && !this.ribbon.getTabs().contains(tab) && tab.isSelected()) {
                        tab.setSelected(false);
                        int tabIndex = c.getFrom();
                        this.findNearestAvailableTab(tabIndex, true);
                     }
                  }

                  if ((c.wasAdded() || c.wasRemoved()) && this.getSelectedIndex() != this.ribbon.getTabs().indexOf(this.getSelectedItem())) {
                     this.clearAndSelect(this.ribbon.getTabs().indexOf(this.getSelectedItem()));
                  }
               }

               if (this.getSelectedIndex() == -1 && this.getSelectedItem() == null && this.ribbon.getTabs().size() > 0) {
                  this.findNearestAvailableTab(0, true);
               } else if (this.ribbon.getTabs().isEmpty()) {
                  this.clearSelection();
               }
            };
            if (this.ribbon.getTabs() != null) {
               this.ribbon.getTabs().addListener(itemsContentObserver);
            }
         }
      }

      public void select(int index) {
         if (index >= 0
            && (this.getItemCount() <= 0 || index < this.getItemCount())
            && (index != this.getSelectedIndex() || !this.getModelItem(index).isSelected())) {
            if (this.getSelectedIndex() >= 0 && this.getSelectedIndex() < this.ribbon.getTabs().size()) {
               ((RibbonTab)this.ribbon.getTabs().get(this.getSelectedIndex())).setSelected(false);
            }

            this.setSelectedIndex(index);
            RibbonTab tab = this.getModelItem(index);
            if (tab != null) {
               this.setSelectedItem(tab);
            }

            if (this.getSelectedIndex() >= 0 && this.getSelectedIndex() < this.ribbon.getTabs().size()) {
               ((RibbonTab)this.ribbon.getTabs().get(this.getSelectedIndex())).setSelected(true);
            }

            this.ribbon.notifyAccessibleAttributeChanged(AccessibleAttribute.FOCUS_ITEM);
         }
      }

      public void select(RibbonTab tab) {
         int itemCount = this.getItemCount();

         for (int i = 0; i < itemCount; i++) {
            RibbonTab value = this.getModelItem(i);
            if (value != null && value.equals(tab)) {
               this.select(i);
               return;
            }
         }

         if (tab != null) {
            this.setSelectedItem(tab);
         }
      }

      protected RibbonTab getModelItem(int index) {
         ObservableList<RibbonTab> items = this.ribbon.getTabs();
         if (items == null) {
            return null;
         } else {
            return index >= 0 && index < items.size() ? (RibbonTab)items.get(index) : null;
         }
      }

      protected int getItemCount() {
         ObservableList<RibbonTab> items = this.ribbon.getTabs();
         return items == null ? 0 : items.size();
      }

      private RibbonTab findNearestAvailableTab(int tabIndex, boolean doSelect) {
         int tabCount = this.getItemCount();
         int i = 1;
         RibbonTab bestTab = null;

         while (true) {
            int downPos = tabIndex - i;
            if (downPos >= 0) {
               RibbonTab _tab = this.getModelItem(downPos);
               if (_tab != null && !_tab.isDisable()) {
                  bestTab = _tab;
                  break;
               }
            }

            int upPos = tabIndex + i - 1;
            if (upPos < tabCount) {
               RibbonTab _tab = this.getModelItem(upPos);
               if (_tab != null && !_tab.isDisable()) {
                  bestTab = _tab;
                  break;
               }
            }

            if (downPos < 0 && upPos >= tabCount) {
               break;
            }

            i++;
         }

         if (doSelect && bestTab != null) {
            this.select(bestTab);
         }

         return bestTab;
      }
   }

   public static enum RibbonState {
      EXPANDED,
      MINIMIZED,
      DROPDOWN;
   }

   private static class StyleableProperties {
      private static final CssMetaData<Ribbon, Number> TAB_MIN_WIDTH = new CssMetaData<Ribbon, Number>("-fx-tab-min-width", SizeConverter.getInstance(), 0.0) {
         public boolean isSettable(Ribbon n) {
            return n.tabMinWidth == null || !n.tabMinWidth.isBound();
         }

         public StyleableProperty<Number> getStyleableProperty(Ribbon n) {
            return (StyleableProperty<Number>)n.tabMinWidthProperty();
         }
      };
      private static final CssMetaData<Ribbon, Number> TAB_MAX_WIDTH = new CssMetaData<Ribbon, Number>(
         "-fx-tab-max-width", SizeConverter.getInstance(), Double.MAX_VALUE
      ) {
         public boolean isSettable(Ribbon n) {
            return n.tabMaxWidth == null || !n.tabMaxWidth.isBound();
         }

         public StyleableProperty<Number> getStyleableProperty(Ribbon n) {
            return (StyleableProperty<Number>)n.tabMaxWidthProperty();
         }
      };
      private static final CssMetaData<Ribbon, Number> TAB_MIN_HEIGHT = new CssMetaData<Ribbon, Number>("-fx-tab-min-height", SizeConverter.getInstance(), 0.0) {
         public boolean isSettable(Ribbon n) {
            return n.tabMinHeight == null || !n.tabMinHeight.isBound();
         }

         public StyleableProperty<Number> getStyleableProperty(Ribbon n) {
            return (StyleableProperty<Number>)n.tabMinHeightProperty();
         }
      };
      private static final CssMetaData<Ribbon, Number> TAB_MAX_HEIGHT = new CssMetaData<Ribbon, Number>(
         "-fx-tab-max-height", SizeConverter.getInstance(), Double.MAX_VALUE
      ) {
         public boolean isSettable(Ribbon n) {
            return n.tabMaxHeight == null || !n.tabMaxHeight.isBound();
         }

         public StyleableProperty<Number> getStyleableProperty(Ribbon n) {
            return (StyleableProperty<Number>)n.tabMaxHeightProperty();
         }
      };
   }
}
