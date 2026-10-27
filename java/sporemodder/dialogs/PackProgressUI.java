package sporemodder.view.dialogs;

import java.io.File;
import java.io.IOException;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.Alert.AlertType;
import javafx.stage.Modality;
import sporemodder.EditorManager;
import sporemodder.ProjectManager;
import sporemodder.BuildLog;
import sporemodder.UIManager;
import sporemodder.file.dbpf.DBPFPackingTask;
import sporemodder.util.Project;
import sporemodder.util.ProjectItem;
import sporemodder.view.Controller;

public class PackProgressUI implements Controller {
   @FXML
   private DialogPane mainNode;
   @FXML
   private ProgressBar progressBar;
   private Dialog<Void> dialog;
   private Thread taskThread;
   private boolean forceClose;

   public Node getMainNode() {
      return this.mainNode;
   }

   public ProgressBar getProgressBar() {
      return this.progressBar;
   }

   public Dialog<Void> getDialog() {
      return this.dialog;
   }

   private void showInternal(File var1, String var2, DBPFPackingTask var3) {
      this.dialog = new Dialog();
      this.dialog.setTitle("Packing '" + var2 + "'");
      this.dialog.setDialogPane(this.mainNode);
      this.dialog.setResizable(false);
      this.dialog.initModality(Modality.APPLICATION_MODAL);
      this.dialog.getDialogPane().getButtonTypes().add(ButtonType.CANCEL);
      this.dialog.setOnShowing(var2x -> {
         this.taskThread = new Thread(var3);
         this.taskThread.start();
      });
      this.dialog.setOnCloseRequest(var2x -> {
         if (!this.forceClose) {
            Alert var3x = new Alert(AlertType.WARNING, "Confirm cancellation", new ButtonType[]{ButtonType.NO, ButtonType.YES});
            var3x.setHeaderText("Are you sure you want to cancel?");
            var3x.setContentText("All the progress will be lost!");
            var3.pause();
            UIManager.get().showDialog(var3x, false).ifPresent(var2xx -> {
               if (var2xx == ButtonType.YES) {
                  var3.cancel();
                  this.forceClose = true;
               } else {
                  var3.resume();
               }
            });
            if (!this.forceClose) {
               var2x.consume();
            }
         }
      });
      var3.setOnSucceeded(var3x -> {
         this.forceClose = true;
         this.dialog.close();
         if (var3.getFailException() != null) {
            Platform.runLater(() -> this.showErrorDialog(var1, var3));
            String var4 = var3.getCurrentFile().getAbsolutePath().substring(var1.getAbsolutePath().length() + 1);
            ProjectItem var5 = ProjectManager.get().getItem(var4);
            EditorManager var6 = EditorManager.get();

            try {
               if (var6.hasItem(var5)) {
                  var6.loadFile(var5);
               } else {
                  var6.loadFile(var5);
                  var6.moveFileToNewTab(var5);
               }
            } catch (IOException var8) {
               var8.printStackTrace();
            }
         }
      });
      var3.setOnCancelled(var1x -> {
         this.forceClose = true;
         this.dialog.close();
      });
      this.progressBar.progressProperty().bind(var3.progressProperty());
      UIManager.get().setOverlay(true);
      UIManager.get().showDialog(this.dialog, false);
      UIManager.get().setOverlay(false);
   }

   private void showErrorDialog(File var1, DBPFPackingTask var2) {
      Alert var3 = new Alert(AlertType.ERROR, "The project was not packed", new ButtonType[]{ButtonType.OK});
      String var4;
      if (var2.getCurrentFile() != null) {
         String var5 = var2.getCurrentFile().getAbsolutePath().substring(var1.getAbsolutePath().length());
         var4 = "The project was not packed due to an error in file " + var5;
      } else {
         var4 = "The project was not packed";
      }

      var3.setContentText(var4 + ":\n" + var2.getFailException().getLocalizedMessage());
      var3.getDialogPane().setExpandableContent(UIManager.get().createExceptionArea(var2.getFailException()));
      UIManager.get().showDialog(var3, true);
   }

   private static boolean runLogged(String var0, File var1, File var2, DBPFPackingTask var3, boolean var4, boolean var5) {
      BuildLog.begin(var0 + (var4 ? " (with debug info)" : ""));
      BuildLog.log("Input folder: " + var1.getAbsolutePath());
      BuildLog.log("Output package: " + (var2 == null ? "(stream)" : var2.getAbsolutePath()));
      BuildLog.log("Package signature: " + var3.getPackageSignature());
      BuildLog.log("Debug information for ModCreatorKit: " + (var4 ? "yes" : "no"));
      long var6 = System.currentTimeMillis();
      Thread var8 = new Thread(() -> {
         File var1x = null;
         int var2x = 0;

         while (!var3.isDone()) {
            File var3x = var3.getCurrentFile();
            if (var3x != null && !var3x.equals(var1x)) {
               var1x = var3x;
               var2x++;
               String var4x = var3x.getAbsolutePath();
               if (var4x.startsWith(var1.getAbsolutePath())) {
                  var4x = var4x.substring(var1.getAbsolutePath().length());
               }

               BuildLog.log("Packing " + var4x);
            }

            try {
               Thread.sleep(20L);
            } catch (InterruptedException var6x) {
               return;
            }
         }

         BuildLog.log("Processed " + var2x + " files/folders");
      }, "BuildLogSampler");
      var8.setDaemon(true);
      var8.start();
      return false;
   }

   private static void finishLog(File var0, File var1, DBPFPackingTask var2, long var3) {
      long var5 = System.currentTimeMillis() - var3;
      if (var2.getFailException() != null) {
         File var7 = var2.getCurrentFile();
         BuildLog.error("Packing failed" + (var7 == null ? "" : " at " + var7.getAbsolutePath()), var2.getFailException());
      } else if (var2.isCancelled()) {
         BuildLog.log("Packing cancelled by the user");
      } else {
         BuildLog.log("Packing succeeded" + (var1 != null && var1.exists() ? ", " + var1.length() + " bytes" : ""));
      }

      BuildLog.log("Finished in " + var5 + " ms");
   }

   public static boolean show(Project var0, boolean var1) {
      PackProgressUI var2 = (PackProgressUI)UIManager.get().loadUI("dialogs/PackProgressUI");
      DBPFPackingTask var3 = new DBPFPackingTask(var0, var1);
      long var4 = System.currentTimeMillis();
      runLogged("Pack project '" + var0.getName() + "'", var0.getFolder(), var0.getOutputPackage(), var3, var1, false);
      var2.showInternal(var0.getFolder(), var0.getName(), var3);
      UIManager.get().notifyUIUpdate(false);
      finishLog(var0.getFolder(), var0.getOutputPackage(), var3, var4);
      return var3.getFailException() == null && !var3.isCancelled();
   }

   public static boolean show(File var0, File var1, String var2, boolean var3) {
      PackProgressUI var4 = (PackProgressUI)UIManager.get().loadUI("dialogs/PackProgressUI");
      DBPFPackingTask var5 = new DBPFPackingTask(var0, var1);
      long var6 = System.currentTimeMillis();
      runLogged("Pack '" + var2 + "'", var0, var1, var5, false, false);
      var4.showInternal(var0, var2, var5);
      UIManager.get().notifyUIUpdate(false);
      finishLog(var0, var1, var5, var6);
      return var5.getFailException() == null && !var5.isCancelled();
   }
}
