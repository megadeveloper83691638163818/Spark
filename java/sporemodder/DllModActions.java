package sporemodder;

import java.awt.Desktop;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Alert.AlertType;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import sporemodder.file.modapi.DllModProjectGenerator;
import sporemodder.file.modapi.DllProjects;
import sporemodder.file.modapi.SporeModUnpacker;
import sporemodder.file.modapi.DllModBuilder.BuildResult;
import sporemodder.file.modapi.SporeModUnpacker.Summary;
import sporemodder.util.ModBundle;
import sporemodder.util.Project;
import sporemodder.view.dialogs.DllModDialogs;
import sporemodder.view.dialogs.DllModDialogs.NewDllMod;

public class DllModActions {
   private DllModActions() {
   }

   public static boolean isDllProject(Project var0) {
      if (var0 == null) {
         return false;
      } else if (DllProjects.isDllFolder(var0.getFolder())) {
         return true;
      } else {
         ModBundle var1 = var0.getParentModBundle();
         return var1 != null && DllProjects.isDllMod(var1.getFolder(), var1.getSrcFolder());
      }
   }

   public static List<Project> filterDllProjects(Collection<Project> var0) {
      ArrayList var1 = new ArrayList();

      for (Project var3 : var0) {
         if (isDllProject(var3)) {
            var1.add(var3);
         }
      }

      return var1;
   }

   private static File findBuildTarget(Project var0) {
      File var1 = DllProjects.findVcxproj(var0.getFolder());
      if (var1 == null && var0.getParentModBundle() != null) {
         var1 = DllProjects.findVcxproj(var0.getParentModBundle().getSrcFolder());
      }

      if (var1 == null) {
         return null;
      } else {
         String var2 = var1.getName().substring(0, var1.getName().length() - ".vcxproj".length());
         File var3 = new File(var1.getParentFile(), var2 + ".sln");
         return var3.isFile() ? var3 : var1;
      }
   }

   private static File askFolder(String var0, String var1) {
      DirectoryChooser var2 = new DirectoryChooser();
      var2.setTitle(var0);
      if (!var1.isEmpty() && new File(var1).isDirectory()) {
         var2.setInitialDirectory(new File(var1));
      }

      return var2.showDialog(UIManager.get().getPrimaryStage());
   }

   public static boolean chooseSdkPaths() {
      ModeManager var0 = ModeManager.get();
      File var1 = askFolder("Select the Spore ModAPI SDK folder", var0.getSdkPath());
      if (var1 == null) {
         return false;
      } else {
         var0.setSdkPath(var1.getAbsolutePath());
         File var2 = askFolder("Select the Spore ModAPI Launcher Kit folder", var0.getLauncherPath());
         if (var2 != null) {
            var0.setLauncherPath(var2.getAbsolutePath());
         }

         return true;
      }
   }

   public static void newProject() {
      Optional var0 = DllModDialogs.showNewDllMod(DllModActions::isNameTaken);
      if (var0.isPresent()) {
         NewDllMod var1 = (NewDllMod)var0.get();
         ModeManager var2 = ModeManager.get();
         var2.setSdkPath(var1.sdkPath);
         var2.setLauncherPath(var1.launcherPath);
         var2.setAuthor(var1.info.author);
         Project var3 = createProject(var1.name, "Cannot create the DLL mod.", var1x -> {
            DllModProjectGenerator.createInFolder(var1x, var1.name, var1.sdkPath, var1.launcherPath);
            var1.info.write(var1x);
         });
         if (var3 != null) {
            ProjectManager.get().setActive(var3);
         }
      }
   }

   public static void editModInfo() {
      Project var0 = ProjectManager.get().getActive();
      if (var0 == null) {
         showError("There is no active project.");
      } else {
         UIManager.get().tryAction(() -> DllModDialogs.showModInfo(var0.getFolder(), var0.getName()), "Cannot edit the mod info.");
      }
   }

   private static boolean isNameTaken(String var0) {
      return ProjectManager.get().hasProject(var0) || ProjectManager.get().hasModBundle(var0);
   }

   private static Project createProject(String var0, String var1, ProjectFiller var2) {
      Project var3 = new Project(var0);
      boolean var4 = UIManager.get().tryAction(() -> {
         ProjectManager.get().initializeProject(var3);
         var2.fill(var3.getFolder());
      }, var1);
      return var4 ? var3 : null;
   }

   public static void unpackSporemod() {
      FileChooser var0 = new FileChooser();
      var0.setTitle("Select the .sporemod files to unpack");
      var0.getExtensionFilters()
         .addAll(
            new ExtensionFilter[]{
               new ExtensionFilter("Spore mod (*.sporemod)", new String[]{"*.sporemod"}), new ExtensionFilter("All files", new String[]{"*.*"})
            }
         );
      List<File> var1 = var0.showOpenMultipleDialog(UIManager.get().getPrimaryStage());
      if (var1 != null && !var1.isEmpty()) {
         StringBuilder var2 = new StringBuilder();
         Project var3 = null;

         for (File var5 : var1) {
            Summary[] var6 = new Summary[1];
            Project var7 = createProject(uniqueProjectName(var5), "Cannot unpack " + var5.getName() + ".", var2x -> {
               var6[0] = SporeModUnpacker.unpack(var5, var2x);
               DllProjects.writeMarker(var2x);
            });
            if (var7 != null) {
               var3 = var7;
               var2.append(var7.getName()).append(": ").append(describe(var6[0])).append('\n');
            }
         }

         if (var3 != null) {
            ProjectManager.get().setActive(var3);
            Alert var8 = new Alert(AlertType.INFORMATION, var2.toString(), new ButtonType[]{ButtonType.OK});
            var8.setTitle("Mod unpacked");
            UIManager.get().showDialog(var8);
         }
      }
   }

   private static String describe(Summary var0) {
      return var0.packages.size() + " package(s), " + var0.dlls.size() + " DLL(s), " + (var0.others.size() + (var0.modInfo == null ? 0 : 1)) + " other file(s)";
   }

   private static String uniqueProjectName(File var0) {
      StringBuilder var1 = new StringBuilder();

      for (char var5 : var0.getName().replaceAll("(?i)\\.sporemod$", "").toCharArray()) {
         boolean var6 = "/\\[]{}".indexOf(var5) >= 0 || var5 < ' ';
         var1.append(var6 ? '_' : var5);
      }

      String var7 = var1.toString().trim().isEmpty() ? "UnpackedMod" : var1.toString().trim();
      String var8 = var7;

      for (int var9 = 2; isNameTaken(var8); var9++) {
         var8 = var7 + "_" + var9;
      }

      return var8;
   }

   public static void buildActive() {
      Project var0 = ProjectManager.get().getActive();
      if (var0 == null) {
         showError("There is no active project.");
      } else {
         File var1 = findBuildTarget(var0);
         if (var1 == null) {
            showError("This project has no Visual Studio project (.vcxproj), so there is nothing to build.");
         } else {
            build(var1);
         }
      }
   }

   private static void build(File var0) {
      BuildTask var1 = new BuildTask(var0);
      var1.setOnSucceeded(var1x -> {
         BuildResult var2x = (BuildResult)var1.getValue();
         String var3 = var2x.success ? "Build succeeded.\n\n" : "Build failed.\n\n";
         Alert var4 = new Alert(var2x.success ? AlertType.INFORMATION : AlertType.ERROR, var3 + var2x.output, new ButtonType[]{ButtonType.OK});
         var4.setTitle("Build DLL");
         UIManager.get().showDialog(var4);
      });
      var1.setOnFailed(var1x -> {
         BuildLog.error("Build failed with an exception", var1.getException());
         UIManager.get().showErrorDialog(var1.getException(), "Cannot build the project.", true);
      });
      Thread var2 = new Thread(var1, "DllModBuilder");
      var2.setDaemon(true);
      var2.start();
   }

   public static void openActiveFolder() {
      Project var0 = ProjectManager.get().getActive();
      if (var0 != null) {
         UIManager.get().tryAction(() -> Desktop.getDesktop().open(var0.getFolder()), "Cannot open the folder.");
      }
   }

   private static void showError(String var0) {
      UIManager.get().showDialog(new Alert(AlertType.ERROR, var0, new ButtonType[]{ButtonType.OK}));
   }

   private static class BuildTask extends javafx.concurrent.Task<sporemodder.file.modapi.DllModBuilder.BuildResult> {
      private final File projectFile;

      BuildTask(File var1) {
         this.projectFile = var1;
      }

      protected sporemodder.file.modapi.DllModBuilder.BuildResult call() throws Exception {
         BuildLog.begin("Build DLL '" + this.projectFile.getName() + "'");
         BuildLog.log("Project file: " + this.projectFile.getAbsolutePath());
         BuildLog.log("Configuration: Release");
         long var1 = System.currentTimeMillis();

         try {
            sporemodder.file.modapi.DllModBuilder.BuildResult var3 = sporemodder.file.modapi.DllModBuilder.build(this.projectFile, "Release");
            BuildLog.log((var3.success ? "Build succeeded" : "Build FAILED") + " in " + (System.currentTimeMillis() - var1) + " ms");
            return var3;
         } catch (Exception var6) {
            BuildLog.error("MSBuild could not be run", var6);
            throw var6;
         }
      }
   }

   @FunctionalInterface
   interface ProjectFiller {
      void fill(File var1) throws Exception;
   }
}
