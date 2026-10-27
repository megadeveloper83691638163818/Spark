package sporemodder.view.dialogs;

import java.util.List;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import sporemodder.DllModActions;
import sporemodder.LanguageManager;
import sporemodder.MainApp;
import sporemodder.UIManager;
import sporemodder.file.modapi.ModApiIndex;
import sporemodder.file.modapi.ModApiIndex.Entry;
import sporemodder.view.editors.ModApiCompletion;
import sporemodder.view.editors.TextEditor;

public final class ModApiSearchDialog {
   private static Stage STAGE;
   private static TextEditor TARGET;
   private static TextField QUERY;
   private static ListView<Entry> RESULTS;
   private static TextArea PREVIEW;
   private static Label STATUS;
   private static Button BTN_NAME, BTN_DETOUR, BTN_COPY, BTN_HEADER;

   private ModApiSearchDialog() {
   }

   public static void SHOW(TextEditor EDITOR) {
      TARGET = EDITOR;
      if (STAGE == null) {
         BUILD();
      }
      STAGE.show();
      STAGE.toFront();
      QUERY.requestFocus();
      QUERY.selectAll();
      ModApiIndex.ENSURE_INDEXED(false, ModApiSearchDialog::UPDATE);
      UPDATE();
   }

   private static void BUILD() {
      STAGE = new Stage();
      STAGE.setTitle(LanguageManager.tr("ModAPI function search"));
      Stage MAIN = UIManager.get().getPrimaryStage();
      if (MAIN != null) {
         STAGE.initOwner(MAIN);
      }

      QUERY = new TextField();
      QUERY.setPromptText(LanguageManager.tr("Class or function, for example: cViewer SetRenderType"));
      HBox.setHgrow(QUERY, Priority.ALWAYS);
      Button SDK = new Button(LanguageManager.tr("SDK folder..."));
      SDK.setOnAction(E -> {
         if (DllModActions.chooseSdkPaths()) {
            ModApiIndex.ENSURE_INDEXED(true, ModApiSearchDialog::UPDATE);
            UPDATE();
         }
      });
      Button RELOAD = new Button(LanguageManager.tr("Reload"));
      RELOAD.setOnAction(E -> {
         ModApiIndex.ENSURE_INDEXED(true, ModApiSearchDialog::UPDATE);
         UPDATE();
      });
      HBox TOP = new HBox(8, QUERY, RELOAD, SDK);
      TOP.setPadding(new Insets(10, 10, 6, 10));

      STATUS = new Label();
      STATUS.setPadding(new Insets(0, 10, 6, 10));

      RESULTS = new ListView<>();
      RESULTS.setCellFactory(V -> new ListCell<Entry>() {
         @Override
         protected void updateItem(Entry ITEM, boolean EMPTY) {
            super.updateItem(ITEM, EMPTY);
            if (EMPTY || ITEM == null) {
               setText(null);
            } else {
               setText((ITEM.OWNER.isEmpty() ? "" : ITEM.OWNER + "::") + ITEM.NAME + "    " + ITEM.KIND.name().toLowerCase() + "   " + ITEM.LOCATION());
            }
         }
      });
      PREVIEW = new TextArea();
      PREVIEW.setEditable(false);
      PREVIEW.setStyle("-fx-font-family: 'Consolas', 'Monospaced';");
      SplitPane SPLIT = new SplitPane(RESULTS, PREVIEW);
      SPLIT.setDividerPositions(0.45);

      BTN_NAME = new Button(LanguageManager.tr("Insert name"));
      BTN_DETOUR = new Button(LanguageManager.tr("Insert detour"));
      BTN_COPY = new Button(LanguageManager.tr("Copy detour"));
      BTN_HEADER = new Button(LanguageManager.tr("Open header"));
      Button CLOSE = new Button(LanguageManager.tr("Close"));
      BTN_NAME.setOnAction(E -> INSERT_NAME());
      BTN_DETOUR.setOnAction(E -> INSERT_DETOUR());
      BTN_COPY.setOnAction(E -> COPY_DETOUR());
      BTN_HEADER.setOnAction(E -> OPEN_HEADER());
      CLOSE.setOnAction(E -> STAGE.hide());
      HBox BUTTONS = new HBox(8, BTN_NAME, BTN_DETOUR, BTN_COPY, BTN_HEADER, CLOSE);
      BUTTONS.setPadding(new Insets(8, 10, 10, 10));

      BorderPane ROOT = new BorderPane();
      ROOT.setTop(new VBox(TOP, STATUS));
      ROOT.setCenter(SPLIT);
      ROOT.setBottom(BUTTONS);

      Scene SCENE = new Scene(ROOT, 980, 560);
      if (MAIN != null && MAIN.getScene() != null) {
         SCENE.getStylesheets().addAll(MAIN.getScene().getStylesheets());
      }
      SCENE.setOnKeyPressed(E -> {
         if (E.getCode() == KeyCode.ESCAPE) {
            STAGE.hide();
         }
      });
      STAGE.setScene(SCENE);

      QUERY.textProperty().addListener((O, A, B) -> UPDATE());
      QUERY.setOnKeyPressed(E -> {
         if (E.getCode() == KeyCode.DOWN) {
            RESULTS.requestFocus();
            RESULTS.getSelectionModel().select(Math.max(0, RESULTS.getSelectionModel().getSelectedIndex()));
         } else if (E.getCode() == KeyCode.ENTER) {
            INSERT_DETOUR();
         }
      });
      RESULTS.getSelectionModel().selectedItemProperty().addListener((O, A, B) -> UPDATE_PREVIEW());
      RESULTS.setOnMouseClicked(E -> {
         if (E.getClickCount() >= 2) {
            INSERT_NAME();
         }
      });
   }

   private static void UPDATE() {
      if (STAGE == null) {
         return;
      }
      if (ModApiIndex.IS_RUNNING()) {
         STATUS.setText(LanguageManager.tr("Reading the SDK headers..."));
         RESULTS.getItems().clear();
      } else if (!ModApiIndex.IS_READY()) {
         String SDK = ModApiIndex.SDK_PATH();
         STATUS.setText(SDK.isEmpty()
            ? LanguageManager.tr("The ModAPI SDK folder is not set. Press \"SDK folder...\" and select the folder that contains \"Spore ModAPI\".")
            : LanguageManager.tr("No headers found in the SDK folder: ") + SDK);
         RESULTS.getItems().clear();
      } else {
         List<Entry> FOUND = ModApiIndex.SEARCH(QUERY.getText(), 300);
         RESULTS.getItems().setAll(FOUND);
         STATUS.setText(QUERY.getText().trim().isEmpty()
            ? LanguageManager.tr("Declarations indexed: ") + ModApiIndex.ENTRIES().size()
            : LanguageManager.tr("Found: ") + FOUND.size());
         if (!FOUND.isEmpty()) {
            RESULTS.getSelectionModel().select(0);
         }
      }
      UPDATE_PREVIEW();
   }

   private static void UPDATE_PREVIEW() {
      Entry E = RESULTS.getSelectionModel().getSelectedItem();
      boolean FUNC = E != null && E.KIND == ModApiIndex.Kind.FUNCTION;
      BTN_NAME.setDisable(E == null);
      BTN_DETOUR.setDisable(!FUNC);
      BTN_COPY.setDisable(!FUNC);
      BTN_HEADER.setDisable(E == null || E.FILE == null);
      if (E == null) {
         PREVIEW.clear();
         return;
      }
      String TEXT = E.SIGNATURE() + "\n" + LanguageManager.tr("Header: ") + E.FILE + ":" + E.LINE + "\n";
      if (FUNC) {
         TEXT += "\n" + E.DETOUR_DEFINITION() + "\n// AttachDetours():\n" + E.DETOUR_ATTACH() + "\n";
      }
      PREVIEW.setText(TEXT);
   }

   private static boolean HAS_EDITOR() {
      return TARGET != null && TARGET.getCodeArea().isEditable() && TARGET.getCodeArea().getScene() != null;
   }

   private static void INSERT_NAME() {
      Entry E = RESULTS.getSelectionModel().getSelectedItem();
      if (E == null) {
         return;
      }
      if (HAS_EDITOR()) {
         String TEXT = E.OWNER.isEmpty() || E.KIND == ModApiIndex.Kind.CLASS ? E.NAME : E.OWNER + "::" + E.NAME;
         TARGET.getCodeArea().replaceSelection(TEXT);
         TARGET.getCodeArea().requestFocus();
         STATUS.setText(LanguageManager.tr("Inserted: ") + TEXT);
      } else {
         COPY(E.NAME);
         STATUS.setText(LanguageManager.tr("No open C++ file, the name was copied to the clipboard."));
      }
   }

   private static void INSERT_DETOUR() {
      Entry E = RESULTS.getSelectionModel().getSelectedItem();
      if (E == null || E.KIND != ModApiIndex.Kind.FUNCTION) {
         return;
      }
      if (HAS_EDITOR() && ModApiCompletion.INSERT_DETOUR(TARGET, E)) {
         STATUS.setText(LanguageManager.tr("Detour inserted into the file."));
         Platform.runLater(() -> TARGET.getCodeArea().requestFocus());
      } else {
         COPY(E.DETOUR_DEFINITION() + "\n// AttachDetours():\n" + E.DETOUR_ATTACH() + "\n");
         STATUS.setText(LanguageManager.tr("No open C++ file, the detour was copied to the clipboard."));
      }
   }

   private static void COPY_DETOUR() {
      Entry E = RESULTS.getSelectionModel().getSelectedItem();
      if (E != null && E.KIND == ModApiIndex.Kind.FUNCTION) {
         COPY(E.DETOUR_DEFINITION() + "\n// AttachDetours():\n" + E.DETOUR_ATTACH() + "\n");
         STATUS.setText(LanguageManager.tr("Copied to the clipboard."));
      }
   }

   private static void OPEN_HEADER() {
      Entry E = RESULTS.getSelectionModel().getSelectedItem();
      if (E != null && E.FILE != null) {
         MainApp.get().getHostServices().showDocument(E.FILE.toUri().toString());
      }
   }

   private static void COPY(String TEXT) {
      ClipboardContent CONTENT = new ClipboardContent();
      CONTENT.putString(TEXT);
      Clipboard.getSystemClipboard().setContent(CONTENT);
   }
}
