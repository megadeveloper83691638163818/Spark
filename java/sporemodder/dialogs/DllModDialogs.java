package sporemodder.view.dialogs;

import java.io.File;
import java.io.IOException;
import java.util.Optional;
import java.util.function.Predicate;
import javafx.application.Platform;
import javafx.beans.Observable;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.stage.DirectoryChooser;
import sporemodder.ModeManager;
import sporemodder.UIManager;
import sporemodder.file.modapi.DllModProjectGenerator;
import sporemodder.file.modapi.ModInfoXml;
import sporemodder.util.ModBundle;

public class DllModDialogs {
   private DllModDialogs() {
   }

   public static Optional<DllModDialogs.NewDllMod> showNewDllMod(Predicate<String> var0) {
      ModeManager var1 = ModeManager.get();
      TextField var2 = new TextField("MyDllMod");
      TextField var3 = new TextField("1.0.0");
      TextField var4 = new TextField(var1.getAuthor());
      TextField var5 = new TextField(var1.getSdkPath());
      TextField var6 = new TextField(var1.getLauncherPath());
      Label var7 = new Label();
      var7.setStyle("-fx-text-fill: #cc3333;");
      GridPane var8 = createGrid();
      addRow(var8, 0, "Name:", var2, null);
      addRow(var8, 1, "Version:", var3, null);
      addRow(var8, 2, "Author:", var4, null);
      addRow(var8, 3, "Spore ModAPI SDK:", var5, createBrowseButton("Select the Spore ModAPI SDK folder", var5));
      addRow(var8, 4, "Launcher Kit:", var6, createBrowseButton("Select the Spore ModAPI Launcher Kit folder", var6));
      var8.add(var7, 1, 5, 2, 1);
      Dialog var9 = new Dialog();
      var9.setTitle("New DLL mod");
      var9.setHeaderText("Create a new Spore ModAPI DLL mod");
      var9.getDialogPane().setContent(var8);
      var9.getDialogPane().getButtonTypes().setAll(new ButtonType[]{ButtonType.CANCEL, ButtonType.OK});
      Node var10 = var9.getDialogPane().lookupButton(ButtonType.OK);
      BooleanBinding var11 = Bindings.createBooleanBinding(() -> {
         String var3x = findProblem(var2.getText().trim(), var0);
         var7.setText(var3x == null ? "" : var3x);
         return var3x != null;
      }, new Observable[]{var2.textProperty()});
      var10.disableProperty().bind(var11);
      Platform.runLater(var2::requestFocus);
      Optional var12 = UIManager.get().showDialog(var9);
      if (var12.isPresent() && var12.get() == ButtonType.OK) {
         DllModDialogs.NewDllMod var13 = new DllModDialogs.NewDllMod();
         var13.name = var2.getText().trim();
         var13.info = newModInfo(var13.name, var3.getText().trim(), var4.getText().trim());
         var13.sdkPath = var5.getText().trim();
         var13.launcherPath = var6.getText().trim();
         return Optional.of(var13);
      } else {
         return Optional.empty();
      }
   }

   private static String findProblem(String var0, Predicate<String> var1) {
      if (!DllModProjectGenerator.isValidName(var0)) {
         return "Use letters, digits and '_', starting with a letter.";
      } else {
         return var1.test(var0) ? "A project with this name already exists." : null;
      }
   }

   private static ModInfoXml newModInfo(String var0, String var1, String var2) {
      ModInfoXml var3 = new ModInfoXml();
      var3.displayName = var0;
      var3.unique = ModBundle.generateUniqueTagFromName(var0);
      var3.version = var1;
      var3.author = var2;
      var3.dlls.add(var0 + ".dll");
      return var3;
   }

   public static void showModInfo(File var0, String var1) throws IOException {
      ModInfoXml var2 = ModInfoXml.read(var0);
      if (var2 == null) {
         var2 = newModInfo(var1, "1.0.0", ModeManager.get().getAuthor());
      }

      TextField var3 = new TextField(var2.displayName);
      TextField var4 = new TextField(var2.unique);
      TextField var5 = new TextField(var2.version);
      TextField var6 = new TextField(var2.author);
      TextField var7 = new TextField(var2.description);
      TextField var8 = new TextField(var2.dllsBuild);
      GridPane var9 = createGrid();
      addRow(var9, 0, "Display name:", var3, null);
      addRow(var9, 1, "Unique name:", var4, null);
      addRow(var9, 2, "Version:", var5, null);
      addRow(var9, 3, "Author:", var6, null);
      addRow(var9, 4, "Description:", var7, null);
      addRow(var9, 5, "DLLs build:", var8, null);
      Dialog var10 = new Dialog();
      var10.setTitle("Mod info");
      var10.setHeaderText("Information stored in ModInfo.xml");
      var10.getDialogPane().setContent(var9);
      var10.getDialogPane().getButtonTypes().setAll(new ButtonType[]{ButtonType.CANCEL, ButtonType.OK});
      Optional var11 = UIManager.get().showDialog(var10);
      if (var11.isPresent() && var11.get() == ButtonType.OK) {
         var2.displayName = var3.getText();
         var2.unique = var4.getText();
         var2.version = var5.getText();
         var2.author = var6.getText();
         var2.description = var7.getText();
         var2.dllsBuild = var8.getText();
         var2.write(var0);
         ModeManager.get().setAuthor(var2.author);
      }
   }

   private static GridPane createGrid() {
      GridPane var0 = new GridPane();
      var0.setHgap(10.0);
      var0.setVgap(8.0);
      var0.setPadding(new Insets(10.0));
      ColumnConstraints var1 = new ColumnConstraints();
      ColumnConstraints var2 = new ColumnConstraints(260.0);
      var2.setHgrow(Priority.ALWAYS);
      var0.getColumnConstraints().addAll(new ColumnConstraints[]{var1, var2, new ColumnConstraints()});
      return var0;
   }

   private static void addRow(GridPane var0, int var1, String var2, TextField var3, Button var4) {
      var0.add(new Label(var2), 0, var1);
      var0.add(var3, 1, var1);
      if (var4 != null) {
         var0.add(var4, 2, var1);
      }
   }

   private static Button createBrowseButton(String var0, TextField var1) {
      Button var2 = new Button("Browse...");
      var2.setOnAction(var2x -> {
         DirectoryChooser var3 = new DirectoryChooser();
         var3.setTitle(var0);
         File var4 = new File(var1.getText().trim());
         if (var4.isDirectory()) {
            var3.setInitialDirectory(var4);
         }

         File var5 = var3.showDialog(UIManager.get().getPrimaryStage());
         if (var5 != null) {
            var1.setText(var5.getAbsolutePath());
         }
      });
      return var2;
   }

   public static class NewDllMod {
      public String name;
      public ModInfoXml info;
      public String sdkPath;
      public String launcherPath;
   }
}
