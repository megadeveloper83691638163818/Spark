package sporemodder;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextArea;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import sporemodder.view.SceneTranslator;

/** Debug log window for project builds (packing in standard mode, DLL builds in DLL mode). */
public final class BuildLog {
   private static final int MAX_CHARS = 400_000;
   private static final StringBuilder buffer = new StringBuilder();
   private static final SimpleDateFormat TIME = new SimpleDateFormat("HH:mm:ss.SSS");
   private static boolean autoOpen = true;
   private static Stage stage;
   private static TextArea area;

   private BuildLog() {
   }

   public static void begin(String title) {
      if (buffer.length() > 0) {
         append("");
      }
      append("==== " + title + " ====");
      log("Spark " + UIManager.APP_VERSION + ", Java " + System.getProperty("java.version") + ", " + System.getProperty("os.name"));
      try {
         ModeManager modes = MainApp.get().getModeManager();
         if (modes != null) {
            log("Work mode: " + modes.getMode().getDisplayName());
         }
      } catch (RuntimeException ignored) {
         // mode manager not available yet
      }
      if (autoOpen) {
         show();
      }
   }

   public static void log(String message) {
      append("[" + time() + "] " + message);
   }

   public static void error(String message, Throwable t) {
      log("ERROR: " + message);
      if (t != null) {
         StringWriter sw = new StringWriter();
         t.printStackTrace(new PrintWriter(sw));
         append(sw.toString().trim());
      }
   }

   private static synchronized String time() {
      return TIME.format(new Date());
   }

   private static synchronized void append(String line) {
      buffer.append(line).append('\n');
      if (buffer.length() > MAX_CHARS) {
         buffer.delete(0, buffer.length() - MAX_CHARS / 2);
      }
      System.out.println(line);
      Platform.runLater(() -> {
         if (area != null) {
            area.appendText(line + "\n");
         }
      });
   }

   private static Runnable dockHandler;
   private static BorderPane root;

   /** Called by the main window: the debug panel is part of the dock layout. */
   public static void setDockHandler(Runnable handler) {
      dockHandler = handler;
   }

   public static void show() {
      if (dockHandler != null) {
         if (Platform.isFxApplicationThread()) {
            dockHandler.run();
         } else {
            Platform.runLater(dockHandler);
         }
      } else if (Platform.isFxApplicationThread()) {
         showNow();
      } else {
         Platform.runLater(BuildLog::showNow);
      }
   }

   /** The log panel (text area and buttons); the same node is used by the dock layout or the fallback window. */
   public static synchronized BorderPane getPanel() {
      if (root == null) {
         area = new TextArea();
         area.setEditable(false);
         area.getStyleClass().add("debug-area");
         area.setStyle("-fx-font-family: 'Consolas', 'Monospaced';");
         area.setText(buffer.toString());
         Button copy = new Button("Copy text");
         copy.setOnAction(e -> {
            ClipboardContent content = new ClipboardContent();
            content.putString(area.getText());
            Clipboard.getSystemClipboard().setContent(content);
         });
         Button clear = new Button("Clear");
         clear.setOnAction(e -> {
            synchronized (BuildLog.class) {
               buffer.setLength(0);
            }
            area.clear();
         });
         CheckBox open = new CheckBox("Open on build");
         open.setSelected(autoOpen);
         open.setOnAction(e -> autoOpen = open.isSelected());
         Button stop = new Button("Stop build");
         stop.setOnAction(e -> {
            if (!sporemodder.file.modapi.DllModBuilder.cancel()) {
               log("No DLL build is running");
            }
         });
         HBox buttons = new HBox(8, copy, clear, stop, open);
         buttons.setPadding(new Insets(6));
         root = new BorderPane(area);
         root.setBottom(buttons);
         root.setMinHeight(60);
         SceneTranslator.translate(root);
      }

      return root;
   }

   private static void showNow() {
      if (stage == null) {
         stage = new Stage();
         stage.setTitle(LanguageManager.tr("Build log"));
         stage.setScene(new Scene(getPanel(), 760, 420));
      }

      stage.show();
      stage.toFront();
   }
}
