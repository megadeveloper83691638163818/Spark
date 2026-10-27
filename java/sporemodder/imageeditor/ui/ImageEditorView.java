package sporemodder.imageeditor.ui;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.control.Slider;
import javafx.scene.control.Spinner;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.ToolBar;
import javafx.scene.control.Tooltip;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import sporemodder.imageeditor.ImageEditorRegistry;
import sporemodder.imageeditor.filters.ImageFilter;
import sporemodder.imageeditor.filters.ParametricFilter;
import sporemodder.imageeditor.history.ImageHistory;
import sporemodder.imageeditor.history.LayerListCommand;
import sporemodder.imageeditor.history.LayerPixelsCommand;
import sporemodder.imageeditor.model.BlendMode;
import sporemodder.imageeditor.model.ImageDocument;
import sporemodder.imageeditor.model.ImageLayer;
import sporemodder.imageeditor.model.Selection;
import sporemodder.imageeditor.ops.DocumentOps;
import sporemodder.imageeditor.tools.ImageTool;
import sporemodder.imageeditor.tools.ToolContext;
import sporemodder.view.SceneTranslator;

public class ImageEditorView extends BorderPane {
   private static final double[] ZOOM_LEVELS = {0.25, 0.5, 1, 2, 4, 8, 16, 32};
   private static final int DEFAULT_ZOOM = 2;

   private final ImageEditorRegistry registry;
   private final ImageHistory history = new ImageHistory();
   private final ImageView imageView = new ImageView();
   private final ListView<ImageLayer> layerList = new ListView<>();
   private final ColorPicker colorPicker = new ColorPicker(Color.BLACK);
   private final ColorPicker secondaryPicker = new ColorPicker(Color.WHITE);
   private final Spinner<Integer> sizeSpinner = new Spinner<>(1, 256, 8);
   private final Slider toolOpacity = new Slider(1, 100, 100);
   private final Slider hardness = new Slider(0, 100, 80);
   private final Slider opacitySlider = new Slider(0, 100, 100);
   private final ComboBox<BlendMode> blendBox = new ComboBox<>();
   private final Label zoomLabel = new Label();
   private final Label positionLabel = new Label();
   private javafx.scene.control.ScrollPane sidePanel;
   private javafx.scene.control.SplitPane split;
   private ImageDocument document;
   private ImageTool activeTool;
   private int zoomIndex = DEFAULT_ZOOM;
   private boolean updatingLayerControls;
   private Runnable saveAction = () -> {
   };

   public ImageEditorView(ImageEditorRegistry registry) {
      this.registry = registry;
      this.imageView.setSmooth(false);
      this.imageView.setPreserveRatio(true);
      ScrollPane scroll = new ScrollPane(this.imageView);
      scroll.addEventFilter(javafx.scene.input.ScrollEvent.SCROLL, e -> {
         if (e.isControlDown() && e.getDeltaY() != 0) {
            setZoom(this.zoomIndex + (e.getDeltaY() > 0 ? 1 : -1));
            e.consume();
         }
      });
      this.sizeSpinner.setEditable(true);
      this.sizeSpinner.setPrefWidth(80);
      this.sizeSpinner.setTooltip(new Tooltip("Brush size"));
      this.toolOpacity.setPrefWidth(90);
      this.toolOpacity.setTooltip(new Tooltip("Brush opacity"));
      this.hardness.setPrefWidth(90);
      this.hardness.setTooltip(new Tooltip("Brush hardness (Magic Wand: low = more tolerance)"));
      this.blendBox.getItems().addAll(BlendMode.values());
      this.blendBox.setValue(BlendMode.NORMAL);
      this.setTop(new VBox(createActionBar(), createOptionsBar()));
      FlowPane tools = createToolPanel();
      tools.setPrefWrapLength(210);
      tools.setPrefWidth(220);
      VBox rightColumn = new VBox(tools, new Separator(), this.createLayerPanel());
      javafx.scene.control.ScrollPane rightScroll = new javafx.scene.control.ScrollPane(rightColumn);
      this.sidePanel = rightScroll;
      rightScroll.setFitToWidth(true);
      rightScroll.setHbarPolicy(javafx.scene.control.ScrollPane.ScrollBarPolicy.NEVER);
      rightScroll.setMinWidth(0);
      javafx.scene.control.SplitPane split = new javafx.scene.control.SplitPane(scroll, rightScroll);
      this.split = split;
      javafx.scene.control.SplitPane.setResizableWithParent(rightScroll, false);
      split.setDividerPositions(0.78);
      this.setCenter(split);
      this.setBottom(new HBox(this.positionLabel));
      this.installMouse();
      this.setOnKeyPressed(e -> {
         if (e.isControlDown() && e.getCode() == KeyCode.Z) {
            this.history.undo();
         } else if (e.isControlDown() && e.getCode() == KeyCode.Y) {
            this.history.redo();
         } else if (e.isControlDown() && e.getCode() == KeyCode.S) {
            this.saveAction.run();
         } else if (e.isControlDown() && e.getCode() == KeyCode.A) {
            this.selectAll();
         } else if (e.isControlDown() && e.getCode() == KeyCode.D) {
            this.deselect();
         }
      });
   }

   /** Tools and layers; the main window can show this panel in its own right-hand panel. */
   public javafx.scene.Node getSidePanel() {
      return this.sidePanel;
   }

   public void setSidePanelDocked(boolean docked) {
      if (docked) {
         this.split.getItems().remove(this.sidePanel);
      } else if (!this.split.getItems().contains(this.sidePanel)) {
         this.split.getItems().add(this.sidePanel);
         this.split.setDividerPositions(0.78);
      }
   }

   public void setSaveAction(Runnable action) {
      this.saveAction = action;
   }

   public ImageHistory getHistory() {
      return this.history;
   }

   public void setDocument(ImageDocument doc) {
      this.document = doc;
      doc.addChangeListener(this::onDocumentChanged);
      refreshLayers(null);
      repaint();
      SceneTranslator.translate(this);
   }

   // ---------------------------------------------------------------- toolbars

   private ToolBar createActionBar() {
      Button save = new Button("Save");
      save.setOnAction(e -> this.saveAction.run());
      Button undo = new Button("Undo");
      undo.setOnAction(e -> this.history.undo());
      Button redo = new Button("Redo");
      redo.setOnAction(e -> this.history.redo());

      ToolBar bar = new ToolBar(save, undo, redo, new Separator());
      bar.getItems().add(createImageMenu());
      bar.getItems().add(createSelectMenu());

      Map<String, MenuButton> menus = new LinkedHashMap<>();
      menus.put("Adjustments", new MenuButton("Adjustments"));
      menus.put("Filters", new MenuButton("Filters"));
      for (ImageFilter filter : this.registry.getFilters()) {
         MenuButton menu = menus.computeIfAbsent(filter.getCategory(), MenuButton::new);
         boolean dialog = filter instanceof ParametricFilter;
         MenuItem item = new MenuItem(filter.getDisplayName() + (dialog ? "..." : ""));
         item.setOnAction(e -> this.applyFilter(filter));
         menu.getItems().add(item);
      }
      bar.getItems().addAll(menus.values());

      Button zoomOut = new Button("-");
      zoomOut.setOnAction(e -> this.setZoom(this.zoomIndex - 1));
      Button zoomIn = new Button("+");
      zoomIn.setOnAction(e -> this.setZoom(this.zoomIndex + 1));
      Button zoomReset = new Button("1:1");
      zoomReset.setOnAction(e -> this.setZoom(DEFAULT_ZOOM));
      updateZoomLabel();
      bar.getItems().addAll(new Separator(), zoomOut, this.zoomLabel, zoomIn, zoomReset);
      return bar;
   }

   private MenuButton createImageMenu() {
      MenuButton menu = new MenuButton("Image");
      menu.getItems().addAll(
         item("Rotate 90° CW", () -> DocumentOps.rotate(document, history, 1)),
         item("Rotate 90° CCW", () -> DocumentOps.rotate(document, history, 3)),
         item("Rotate 180°", () -> DocumentOps.rotate(document, history, 2)),
         item("Flip Image Horizontally", () -> DocumentOps.flip(document, history, true)),
         item("Flip Image Vertically", () -> DocumentOps.flip(document, history, false)),
         item("Resize Image...", () -> askSize("Resize Image", true)),
         item("Canvas Size...", () -> askSize("Canvas Size", false)),
         item("Crop to Selection", () -> DocumentOps.cropToSelection(document, history)),
         item("Flatten Image", () -> DocumentOps.flatten(document, history)));
      return menu;
   }

   private MenuButton createSelectMenu() {
      MenuButton menu = new MenuButton("Select");
      menu.getItems().addAll(
         item("Select All", this::selectAll),
         item("Deselect", this::deselect),
         item("Invert Selection", () -> {
            Selection s = document.getSelection();
            document.setSelection(s == null ? Selection.all(document.getWidth(), document.getHeight()) : s.invert());
         }),
         item("Feather...", this::featherSelection));
      return menu;
   }

   private MenuItem item(String text, Runnable action) {
      MenuItem item = new MenuItem(text);
      item.setOnAction(e -> {
         if (document != null) {
            action.run();
         }
      });
      return item;
   }

   private FlowPane createToolPanel() {
      FlowPane panel = new FlowPane(4, 4);
      panel.setPadding(new Insets(6));
      ToggleGroup group = new ToggleGroup();
      for (ImageTool tool : this.registry.getTools()) {
         ToggleButton button = new ToggleButton(tool.getDisplayName());
         button.setToggleGroup(group);
         button.setOnAction(e -> this.activeTool = button.isSelected() ? tool : null);
         panel.getChildren().add(button);
         if (this.activeTool == null) {
            button.setSelected(true);
            this.activeTool = tool;
         }
      }
      return panel;
   }

   private ToolBar createOptionsBar() {
      return new ToolBar(new Label("Color:"), this.colorPicker, new Label("Secondary:"), this.secondaryPicker, new Separator(),
         new Label("Size:"), this.sizeSpinner, new Label("Opacity:"), this.toolOpacity, new Label("Hardness:"), this.hardness);
   }

   // ------------------------------------------------------------------ layers

   private VBox createLayerPanel() {
      this.layerList.setCellFactory(lv -> new ListCell<ImageLayer>() {
         @Override
         protected void updateItem(ImageLayer layer, boolean empty) {
            super.updateItem(layer, empty);
            if (!empty && layer != null) {
               CheckBox box = new CheckBox(layer.getName());
               box.setSelected(layer.isVisible());
               box.setOnAction(e -> {
                  layer.setVisible(box.isSelected());
                  ImageEditorView.this.document.changed();
               });
               this.setGraphic(box);
               this.setOnMouseClicked(e -> {
                  if (e.getClickCount() == 2) {
                     ImageEditorView.this.renameLayer(layer);
                  }
               });
            } else {
               this.setGraphic(null);
               this.setOnMouseClicked(null);
            }
         }
      });
      this.layerList.getSelectionModel().selectedItemProperty().addListener((o, a, layer) -> syncLayerControls(layer));
      this.layerList.setPrefHeight(260);

      this.opacitySlider.valueProperty().addListener((o, a, value) -> {
         ImageLayer layer = selectedLayer();
         if (!updatingLayerControls && layer != null && document != null) {
            layer.setOpacity(value.doubleValue() / 100.0);
            document.changed();
         }
      });
      this.blendBox.valueProperty().addListener((o, a, mode) -> {
         ImageLayer layer = selectedLayer();
         if (!updatingLayerControls && layer != null && document != null) {
            layer.setBlendMode(mode);
            document.changed();
         }
      });

      Button add = new Button("Add");
      add.setOnAction(e -> this.addLayer());
      Button duplicate = new Button("Copy");
      duplicate.setOnAction(e -> this.duplicateLayer());
      Button remove = new Button("Delete");
      remove.setOnAction(e -> this.removeLayer());
      Button up = new Button("Up");
      up.setOnAction(e -> this.moveLayer(1));
      Button down = new Button("Down");
      down.setOnAction(e -> this.moveLayer(-1));
      Button merge = new Button("Merge Down");
      merge.setOnAction(e -> {
         ImageLayer layer = selectedLayer();
         if (document != null && layer != null) {
            DocumentOps.mergeDown(document, history, layer);
         }
      });
      HBox row1 = new HBox(4, add, duplicate, remove);
      HBox row2 = new HBox(4, up, down, merge);

      VBox box = new VBox(6, new Label("Layers"), this.layerList, row1, row2, new Label("Blend mode"), this.blendBox,
         new Label("Opacity"), this.opacitySlider);
      box.setPadding(new Insets(6));
      box.setPrefWidth(220);
      return box;
   }

   private ImageLayer selectedLayer() {
      return this.layerList.getSelectionModel().getSelectedItem();
   }

   private void syncLayerControls(ImageLayer layer) {
      this.updatingLayerControls = true;
      this.opacitySlider.setValue(layer == null ? 100 : layer.getOpacity() * 100.0);
      this.blendBox.setValue(layer == null ? BlendMode.NORMAL : layer.getBlendMode());
      this.updatingLayerControls = false;
   }

   /** Layers are shown top-most first. */
   private List<ImageLayer> displayOrder() {
      List<ImageLayer> list = new ArrayList<>(this.document.getLayers());
      java.util.Collections.reverse(list);
      return list;
   }

   private void refreshLayers(ImageLayer select) {
      ImageLayer keep = select != null ? select : selectedLayer();
      this.layerList.getItems().setAll(displayOrder());
      if (keep != null && this.layerList.getItems().contains(keep)) {
         this.layerList.getSelectionModel().select(keep);
      } else {
         this.layerList.getSelectionModel().selectFirst();
      }
      this.layerList.refresh();
   }

   private void onDocumentChanged() {
      if (!displayOrder().equals(this.layerList.getItems())) {
         refreshLayers(null);
      }
      repaint();
   }

   private void addLayer() {
      if (document == null) {
         return;
      }
      List<ImageLayer> before = new ArrayList<>(document.getLayers());
      ImageLayer layer = ImageLayer.createEmpty("Layer " + (before.size() + 1), document.getWidth(), document.getHeight());
      document.addLayer(layer);
      history.push(new LayerListCommand("Add layer", document, before));
      refreshLayers(layer);
   }

   private void duplicateLayer() {
      ImageLayer src = selectedLayer();
      if (document == null || src == null) {
         return;
      }
      List<ImageLayer> before = new ArrayList<>(document.getLayers());
      ImageLayer copy = new ImageLayer(src.getName() + " copy", src.copyPixels());
      copy.setOpacity(src.getOpacity());
      copy.setVisible(src.isVisible());
      copy.setBlendMode(src.getBlendMode());
      document.insertLayer(before.indexOf(src) + 1, copy);
      history.push(new LayerListCommand("Duplicate layer", document, before));
      refreshLayers(copy);
   }

   private void removeLayer() {
      ImageLayer layer = selectedLayer();
      if (document == null || layer == null || document.getLayers().size() <= 1) {
         return;
      }
      List<ImageLayer> before = new ArrayList<>(document.getLayers());
      document.removeLayer(layer);
      history.push(new LayerListCommand("Delete layer", document, before));
      refreshLayers(null);
   }

   private void moveLayer(int delta) {
      ImageLayer layer = selectedLayer();
      if (document == null || layer == null) {
         return;
      }
      List<ImageLayer> before = new ArrayList<>(document.getLayers());
      if (document.moveLayer(layer, delta)) {
         history.push(new LayerListCommand("Move layer", document, before));
         refreshLayers(layer);
      }
   }

   private void renameLayer(ImageLayer layer) {
      TextInputDialog dialog = new TextInputDialog(layer.getName());
      dialog.setTitle("Rename layer");
      dialog.setHeaderText(null);
      dialog.setContentText("Name:");
      dialog.showAndWait().ifPresent(name -> {
         if (!name.trim().isEmpty()) {
            layer.setName(name.trim());
            this.layerList.refresh();
         }
      });
   }

   // --------------------------------------------------------------- selection

   private void selectAll() {
      if (document != null) {
         document.setSelection(Selection.all(document.getWidth(), document.getHeight()));
      }
   }

   private void deselect() {
      if (document != null) {
         document.setSelection(null);
      }
   }

   private void featherSelection() {
      Selection s = document.getSelection();
      if (s == null) {
         return;
      }
      int[] v = askInts("Feather", new String[]{"Radius (px):"}, new int[]{4}, 1, 200);
      if (v != null) {
         document.setSelection(s.feather(v[0]));
      }
   }

   // ------------------------------------------------------------------- input

   private void installMouse() {
      this.imageView.addEventHandler(MouseEvent.MOUSE_PRESSED, e -> {
         this.requestFocus();
         this.useTool(e, ImageTool::onPress);
      });
      this.imageView.addEventHandler(MouseEvent.MOUSE_DRAGGED, e -> {
         this.showPosition(e);
         this.useTool(e, ImageTool::onDrag);
      });
      this.imageView.addEventHandler(MouseEvent.MOUSE_RELEASED, e -> this.useTool(e, ImageTool::onRelease));
      this.imageView.addEventHandler(MouseEvent.MOUSE_MOVED, this::showPosition);
   }

   private void showPosition(MouseEvent e) {
      int x = (int) Math.floor(e.getX() / ZOOM_LEVELS[zoomIndex]);
      int y = (int) Math.floor(e.getY() / ZOOM_LEVELS[zoomIndex]);
      this.positionLabel.setText(" " + x + ", " + y);
   }

   private interface ToolAction {
      void run(ImageTool tool, ToolContext ctx, int x, int y);
   }

   private void useTool(MouseEvent e, ToolAction action) {
      ImageLayer layer = selectedLayer();
      if (this.activeTool == null || this.document == null || layer == null) {
         return;
      }
      double zoom = ZOOM_LEVELS[this.zoomIndex];
      int x = (int) Math.floor(e.getX() / zoom);
      int y = (int) Math.floor(e.getY() / zoom);
      int size = this.sizeSpinner.getValue() == null ? 1 : this.sizeSpinner.getValue();
      ToolContext ctx = new ToolContext(this.document, layer, this.history, toArgb(this.colorPicker.getValue()),
         toArgb(this.secondaryPicker.getValue()), size, this.toolOpacity.getValue() / 100.0, this.hardness.getValue() / 100.0, this::setColor);
      action.run(this.activeTool, ctx, x, y);
   }

   private void setColor(int argb) {
      this.colorPicker.setValue(Color.rgb(argb >> 16 & 255, argb >> 8 & 255, argb & 255, (argb >>> 24) / 255.0));
   }

   private static int toArgb(Color c) {
      return (int) Math.round(c.getOpacity() * 255.0) << 24
         | (int) Math.round(c.getRed() * 255.0) << 16
         | (int) Math.round(c.getGreen() * 255.0) << 8
         | (int) Math.round(c.getBlue() * 255.0);
   }

   // ---------------------------------------------------------------- filters

   private static BufferedImage copyOf(BufferedImage src) {
      BufferedImage out = new BufferedImage(src.getWidth(), src.getHeight(), BufferedImage.TYPE_INT_ARGB);
      java.awt.Graphics2D g = out.createGraphics();
      g.setComposite(java.awt.AlphaComposite.Src);
      g.drawImage(src, 0, 0, null);
      g.dispose();
      return out;
   }

   /** Applies a filter to a copy of {@code before}, limited to the selection, and puts the result on the layer. */
   private void render(ImageLayer layer, BufferedImage before, ImageFilter filter, double[] values) {
      BufferedImage work = copyOf(before);
      if (filter instanceof ParametricFilter && values != null) {
         ((ParametricFilter) filter).apply(work, values);
      } else {
         filter.apply(work);
      }
      if (document.getSelection() != null) {
         document.getSelection().restoreOutside(work, before);
      }
      layer.setPixels(work);
      document.changed();
   }

   private void applyFilter(ImageFilter filter) {
      ImageLayer layer = selectedLayer();
      if (this.document == null || layer == null) {
         return;
      }
      BufferedImage before = layer.copyPixels();
      if (filter instanceof ParametricFilter) {
         if (!showParamDialog((ParametricFilter) filter, layer, before)) {
            layer.setPixels(before);
            document.changed();
            return;
         }
      } else {
         render(layer, before, filter, null);
      }
      this.history.push(new LayerPixelsCommand(filter.getDisplayName(), this.document, layer, before));
   }

   /** Shows sliders with live preview; returns true if the user confirmed (result is left on the layer). */
   private boolean showParamDialog(ParametricFilter filter, ImageLayer layer, BufferedImage before) {
      Dialog<ButtonType> dialog = new Dialog<>();
      dialog.setTitle(filter.getDisplayName());
      dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
      GridPane grid = new GridPane();
      grid.setHgap(8);
      grid.setVgap(8);
      grid.setPadding(new Insets(10));
      List<ParametricFilter.Param> params = filter.getParams();
      double[] values = filter.defaults();
      for (int i = 0; i < params.size(); i++) {
         ParametricFilter.Param p = params.get(i);
         Slider slider = new Slider(p.min, p.max, p.def);
         slider.setPrefWidth(240);
         Label value = new Label(format(p.def));
         final int index = i;
         slider.valueProperty().addListener((o, a, v) -> {
            values[index] = v.doubleValue();
            value.setText(format(v.doubleValue()));
            render(layer, before, filter, values);
         });
         grid.add(new Label(p.name), 0, i);
         grid.add(slider, 1, i);
         grid.add(value, 2, i);
      }
      dialog.getDialogPane().setContent(grid);
      SceneTranslator.translate(dialog.getDialogPane());
      render(layer, before, filter, values);
      Optional<ButtonType> result = dialog.showAndWait();
      return result.isPresent() && result.get() == ButtonType.OK;
   }

   private static String format(double v) {
      return Math.abs(v - Math.rint(v)) < 0.005 ? String.valueOf((long) Math.rint(v)) : String.format("%.2f", v);
   }

   // ----------------------------------------------------------------- dialogs

   private int[] askInts(String title, String[] labels, int[] defaults, int min, int max) {
      Dialog<ButtonType> dialog = new Dialog<>();
      dialog.setTitle(title);
      dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
      GridPane grid = new GridPane();
      grid.setHgap(8);
      grid.setVgap(8);
      grid.setPadding(new Insets(10));
      List<Spinner<Integer>> spinners = new ArrayList<>();
      for (int i = 0; i < labels.length; i++) {
         Spinner<Integer> spinner = new Spinner<>(min, max, defaults[i]);
         spinner.setEditable(true);
         spinners.add(spinner);
         grid.add(new Label(labels[i]), 0, i);
         grid.add(spinner, 1, i);
      }
      dialog.getDialogPane().setContent(grid);
      SceneTranslator.translate(dialog.getDialogPane());
      Optional<ButtonType> result = dialog.showAndWait();
      if (!result.isPresent() || result.get() != ButtonType.OK) {
         return null;
      }
      int[] out = new int[labels.length];
      for (int i = 0; i < out.length; i++) {
         spinners.get(i).increment(0);
         out[i] = spinners.get(i).getValue();
      }
      return out;
   }

   private void askSize(String title, boolean resample) {
      int[] v = askInts(title, new String[]{"Width:", "Height:"}, new int[]{document.getWidth(), document.getHeight()}, 1, 16384);
      if (v == null) {
         return;
      }
      if (resample) {
         DocumentOps.resizeImage(document, history, v[0], v[1]);
      } else {
         DocumentOps.resizeCanvas(document, history, v[0], v[1]);
      }
   }

   // ----------------------------------------------------------------- display

   private void setZoom(int index) {
      this.zoomIndex = Math.max(0, Math.min(ZOOM_LEVELS.length - 1, index));
      this.updateZoomLabel();
      this.repaint();
   }

   private void updateZoomLabel() {
      this.zoomLabel.setText(Math.round(ZOOM_LEVELS[this.zoomIndex] * 100) + "%");
   }

   private void repaint() {
      if (this.document != null) {
         BufferedImage flat = this.document.flatten();
         if (this.document.getSelection() != null) {
            this.document.getSelection().drawOutline(flat);
         }
         this.imageView.setImage(SwingFXUtils.toFXImage(flat, null));
         this.imageView.setFitWidth(this.document.getWidth() * ZOOM_LEVELS[this.zoomIndex]);
      }
   }
}
