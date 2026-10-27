package sporemodder;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Properties;
import java.util.function.BiConsumer;
import sporemodder.util.Project;

public class ModeManager extends AbstractManager {
   private static final String PROPERTY_workMode = "workMode";
   private static final String PROPERTY_sdkPath = "modapi.sdkPath";
   private static final String PROPERTY_launcherPath = "modapi.launcherPath";
   private static final String PROPERTY_author = "modapi.author";
   private WorkMode mode = WorkMode.STANDARD;
   private String sdkPath = "";
   private String launcherPath = "";
   private String author = "";
   private final List<BiConsumer<WorkMode, WorkMode>> listeners = new ArrayList<>();

   public static ModeManager get() {
      return MainApp.get().getModeManager();
   }

   @Override
   public void initialize(Properties var1) {
      this.mode = WorkMode.fromId(var1.getProperty("workMode", WorkMode.STANDARD.getId()));
      this.sdkPath = var1.getProperty("modapi.sdkPath", "");
      this.launcherPath = var1.getProperty("modapi.launcherPath", "");
      this.author = var1.getProperty("modapi.author", "");
   }

   @Override
   public void saveSettings(Properties var1) {
      var1.setProperty("workMode", this.mode.getId());
      var1.setProperty("modapi.sdkPath", this.sdkPath);
      var1.setProperty("modapi.launcherPath", this.launcherPath);
      var1.setProperty("modapi.author", this.author);
   }

   public WorkMode getMode() {
      return this.mode;
   }

   public String getSdkPath() {
      return this.sdkPath;
   }

   public void setSdkPath(String var1) {
      this.sdkPath = var1 == null ? "" : var1;
   }

   public String getLauncherPath() {
      return this.launcherPath;
   }

   public void setLauncherPath(String var1) {
      this.launcherPath = var1 == null ? "" : var1;
   }

   public String getAuthor() {
      return this.author;
   }

   public void setAuthor(String var1) {
      this.author = var1 == null ? "" : var1;
   }

   /** Only the projects that belong to the active mode (see {@link WorkMode#filterProjects}). */
   public Collection<Project> filterProjects(Collection<Project> projects) {
      return this.mode.filterProjects(projects);
   }

   /** The most recent projects of the active mode, at most <code>count</code>. */
   public List<Project> getRecentProjects(int count) {
      List<Project> projects = this.mode.filterProjects(ProjectManager.get().getRecentProjects(Integer.MAX_VALUE));
      return projects.subList(0, Math.min(count, projects.size()));
   }

   public boolean is(WorkMode var1) {
      return this.mode == var1;
   }

   public void setMode(WorkMode var1) {
      if (var1 != null && var1 != this.mode) {
         WorkMode var2 = this.mode;
         this.mode = var1;
         this.onModeChanged(var2, var1);

         for (BiConsumer var4 : new ArrayList<>(this.listeners)) {
            var4.accept(var2, var1);
         }

         UIManager.get().refreshTitle();
         UIManager.get().notifyUIUpdate(false);
      }
   }

   public void addListener(BiConsumer<WorkMode, WorkMode> var1) {
      this.listeners.add(var1);
   }

   public void removeListener(BiConsumer<WorkMode, WorkMode> var1) {
      this.listeners.remove(var1);
   }

   protected void onModeChanged(WorkMode var1, WorkMode var2) {
      switch (var2) {
         case MODE_2:
         case DECOMP:
         case STANDARD:
      }
   }
}
