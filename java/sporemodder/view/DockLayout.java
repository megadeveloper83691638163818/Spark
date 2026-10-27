package sporemodder.view;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.prefs.Preferences;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.control.SplitPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Rectangle;

/**
 * Blender-style layout. The right panel runs the full window height; the action bar, project tree, editors
 * and the debug panel share the rest. Every divider can be dragged and every panel hidden; sizes are remembered.
 */
public class DockLayout {
   public enum Panel {
      LEFT, RIGHT, BOTTOM
   }

   private static final Preferences PREFS = Preferences.userRoot().node("spark/layout3");
   private static final double DEFAULT_LEFT = 0.20;
   private static final double DEFAULT_RIGHT = 0.22;
   private static final double DEFAULT_BOTTOM = 0.24;

   private final SplitPane outer = new SplitPane();
   private final SplitPane inner = new SplitPane();
   private final SplitPane vertical = new SplitPane();
   private final BorderPane mainColumn = new BorderPane();
   private final StackPane mainWrap;
   private final StackPane leftWrap;
   private final StackPane centerWrap;
   private final StackPane rightHolder = new StackPane();
   private final StackPane rightWrap;
   private final StackPane bottomWrap;
   private final Map<Panel, Boolean> shown = new EnumMap<>(Panel.class);
   private final List<Runnable> visibilityListeners = new ArrayList<>();
   private final List<Object[]> attached = new ArrayList<>();
   private double leftFrac;
   private double rightFrac;
   private double bottomFrac;
   private boolean applying;
   private boolean inspectorOnly;

   public DockLayout(Node header, Node left, Node center, Node right, Node bottom) {
      this.leftWrap = wrap(left);
      this.centerWrap = wrap(center);
      this.rightHolder.getChildren().setAll(right);
      this.rightWrap = wrap(rightHolder);
      this.bottomWrap = wrap(bottom);
      this.vertical.setOrientation(Orientation.VERTICAL);
      this.mainColumn.setCenter(vertical);
      this.mainWrap = wrap(mainColumn);
      for (SplitPane pane : new SplitPane[]{outer, inner, vertical}) {
         pane.getStyleClass().add("dock-split");
      }

      leftWrap.setPrefWidth(240);
      rightWrap.setPrefWidth(300);
      bottomWrap.setPrefHeight(150);
      loadPrefs();
      captureOnRelease();
      rebuild();
   }

   private static StackPane wrap(Node node) {
      StackPane pane = new StackPane(node);
      pane.setMinSize(0, 0);
      pane.setAlignment(javafx.geometry.Pos.TOP_LEFT);
      pane.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
      Rectangle clip = new Rectangle();
      clip.widthProperty().bind(pane.widthProperty());
      clip.heightProperty().bind(pane.heightProperty());
      pane.setClip(clip);
      return pane;
   }

   public Node getRoot() {
      return outer;
   }

   /** Replaces what the right panel shows (inspector or the image editor tools and layers). */
   public void setRightContent(Node node) {
      if (rightHolder.getChildren().size() != 1 || rightHolder.getChildren().get(0) != node) {
         rightHolder.getChildren().setAll(node);
      }
   }

   public boolean isShown(Panel panel) {
      return shown.get(panel);
   }

   public void setShown(Panel panel, boolean visible) {
      if (shown.get(panel) != visible) {
         shown.put(panel, visible);
         PREFS.putBoolean("shown." + panel, visible);
         rebuild();
         visibilityListeners.forEach(Runnable::run);
      }
   }

   public void addVisibilityListener(Runnable listener) {
      visibilityListeners.add(listener);
   }

   /** Shows only the right panel (used by editors that need the whole window). */
   public void setInspectorOnly(boolean only) {
      this.inspectorOnly = only;
      rebuild();
   }

   public void resetLayout() {
      leftFrac = DEFAULT_LEFT;
      rightFrac = DEFAULT_RIGHT;
      bottomFrac = DEFAULT_BOTTOM;
      for (Panel p : Panel.values()) {
         shown.put(p, true);
         PREFS.putBoolean("shown." + p, true);
      }

      savePrefs();
      rebuild();
      visibilityListeners.forEach(Runnable::run);
   }

   private void loadPrefs() {
      leftFrac = PREFS.getDouble("leftFrac", DEFAULT_LEFT);
      rightFrac = PREFS.getDouble("rightFrac", DEFAULT_RIGHT);
      bottomFrac = PREFS.getDouble("bottomFrac", DEFAULT_BOTTOM);
      for (Panel p : Panel.values()) {
         shown.put(p, PREFS.getBoolean("shown." + p, true));
      }
   }

   private void savePrefs() {
      PREFS.putDouble("leftFrac", leftFrac);
      PREFS.putDouble("rightFrac", rightFrac);
      PREFS.putDouble("bottomFrac", bottomFrac);
   }

   private void rebuild() {
      applying = true;
      inner.getItems().clear();
      vertical.getItems().clear();
      outer.getItems().clear();
      if (shown.get(Panel.LEFT)) {
         inner.getItems().add(leftWrap);
      }

      inner.getItems().add(centerWrap);
      vertical.getItems().add(inner);
      if (shown.get(Panel.BOTTOM)) {
         vertical.getItems().add(bottomWrap);
      }

      if (inspectorOnly) {
         outer.getItems().add(rightWrap);
      } else {
         outer.getItems().add(mainWrap);
         if (shown.get(Panel.RIGHT)) {
            outer.getItems().add(rightWrap);
         }
      }

      Platform.runLater(this::applyPositions);
      // SplitPane finishes its first layout after this point and may move the dividers, so apply again shortly after
      javafx.animation.PauseTransition later = new javafx.animation.PauseTransition(javafx.util.Duration.millis(250));
      later.setOnFinished(e -> applyPositions());
      later.play();
   }

   private void applyPositions() {
      if (outer.getWidth() <= 0 || outer.getHeight() <= 0) {
         // not laid out yet: try again once the window has a size
         outer.heightProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(javafx.beans.value.ObservableValue<? extends Number> o, Number a, Number b) {
               if (b.doubleValue() > 0) {
                  outer.heightProperty().removeListener(this);
                  Platform.runLater(DockLayout.this::applyPositions);
               }
            }
         });
         return;
      }

      applying = true;
      if (shown.get(Panel.LEFT)) {
         inner.setDividerPositions(leftFrac);
      }

      if (shown.get(Panel.BOTTOM)) {
         vertical.setDividerPositions(1 - bottomFrac);
      }

      if (!inspectorOnly && shown.get(Panel.RIGHT)) {
         outer.setDividerPositions(1 - rightFrac);
      }

      applying = false;
   }

   private boolean dragging;

   private static boolean isDivider(Object target) {
      javafx.scene.Node n = target instanceof javafx.scene.Node ? (javafx.scene.Node)target : null;
      while (n != null) {
         if (n.getStyleClass().contains("split-pane-divider")) {
            return true;
         }

         n = n.getParent();
      }

      return false;
   }

   /** Sizes are only remembered after the user releases a divider, never from automatic layout changes. */
   private void captureOnRelease() {
      for (SplitPane pane : new SplitPane[]{outer, inner, vertical}) {
         pane.addEventFilter(javafx.scene.input.MouseEvent.MOUSE_PRESSED, e -> dragging = isDivider(e.getTarget()));
         pane.addEventFilter(javafx.scene.input.MouseEvent.MOUSE_RELEASED, e -> {
            if (dragging) {
               dragging = false;
               capture();
            }
         });
      }
   }

   private void capture() {
      if (shown.get(Panel.LEFT) && !inner.getDividers().isEmpty()) {
         leftFrac = inner.getDividerPositions()[0];
      }

      if (shown.get(Panel.BOTTOM) && !vertical.getDividers().isEmpty()) {
         bottomFrac = 1 - vertical.getDividerPositions()[0];
      }

      if (!inspectorOnly && shown.get(Panel.RIGHT) && !outer.getDividers().isEmpty()) {
         rightFrac = 1 - outer.getDividerPositions()[0];
      }

      savePrefs();
   }
}
