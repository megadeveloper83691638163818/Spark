package sporemodder;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;
import sporemodder.file.shaders.FXCompiler;
import sporemodder.file.shaders.ShaderData;
import sporemodder.imageeditor.ImageEditorManager;

public class MainApp extends Application {
   private static MainApp instance;
   private UIManager uiManager;
   private MessageManager messageManager;
   private EditorManager editorManager;
   private PathManager pathManager;
   private HashManager hashManager;
   private ProjectManager projectManager;
   private GameManager gameManager;
   private DocumentationManager documentationManager;
   private FormatManager formatManager;
   private FileManager fileManager;
   private UpdateManager updateManager;
   private ModeManager modeManager;
   private LanguageManager languageManager;
   private ImageEditorManager imageEditorManager;
   private FXCompiler fxCompiler;
   private GitHubManager gitHubManager;
   private Properties settings;

   public UIManager getUIManager() {
      return this.uiManager;
   }

   public GitHubManager getGitHubManager() {
      return this.gitHubManager;
   }

   public MessageManager getMessageManager() {
      return this.messageManager;
   }

   public EditorManager getEditorManager() {
      return this.editorManager;
   }

   public PathManager getPathManager() {
      return this.pathManager;
   }

   public HashManager getHashManager() {
      return this.hashManager;
   }

   public ProjectManager getProjectManager() {
      return this.projectManager;
   }

   public GameManager getGameManager() {
      return this.gameManager;
   }

   public DocumentationManager getDocumentationManager() {
      return this.documentationManager;
   }

   public FormatManager getFormatManager() {
      return this.formatManager;
   }

   public FileManager getFileManager() {
      return this.fileManager;
   }

   public LanguageManager getLanguageManager() {
      return this.languageManager;
   }

   public ImageEditorManager getImageEditorManager() {
      return this.imageEditorManager;
   }

   public ModeManager getModeManager() {
      return this.modeManager;
   }

   public UpdateManager getUpdateManager() {
      return this.updateManager;
   }

   public FXCompiler getFXCompiler() {
      return this.fxCompiler;
   }

   public static MainApp get() {
      return instance;
   }

   public static void testInit() {
      new MainApp().init(true);
   }

   private void init(boolean var1) {
      instance = this;
      this.messageManager = new MessageManager();
      this.gameManager = new GameManager();
      this.pathManager = new PathManager();
      this.uiManager = new UIManager();
      if (!var1) {
         this.editorManager = new EditorManager();
      }

      this.hashManager = new HashManager();
      this.projectManager = new ProjectManager();
      if (!var1) {
         this.documentationManager = new DocumentationManager();
      }

      this.formatManager = new FormatManager();
      this.fileManager = new FileManager();
      this.updateManager = new UpdateManager();
      this.modeManager = new ModeManager();
      this.languageManager = new LanguageManager();
      this.imageEditorManager = new ImageEditorManager();
      this.fxCompiler = new FXCompiler();
      this.gitHubManager = new GitHubManager();
      this.pathManager.initialize(null);
      this.settings = new Properties();
      File var2 = this.pathManager.getProgramFile("config.properties");
      if (var2.exists()) {
         try (FileInputStream var3 = new FileInputStream(var2)) {
            this.settings.load(var3);
         } catch (IOException var8) {
            var8.printStackTrace();
         }
      }

      this.pathManager.loadSettings(this.settings);
      this.messageManager.initialize(this.settings);
      this.gameManager.initialize(this.settings);
      this.fileManager.initialize(this.settings);
      this.modeManager.initialize(this.settings);
      this.languageManager.initialize(this.settings);
      this.imageEditorManager.initialize(this.settings);
      if (!var1) {
         this.uiManager.initialize(this.settings);
      }

      if (!var1) {
         this.editorManager.initialize(this.settings);
      }

      this.hashManager.initialize(this.settings);
      this.projectManager.initialize(this.settings);
      if (!var1) {
         this.documentationManager.initialize(this.settings);
      }

      this.formatManager.initialize(this.settings);
      this.fxCompiler.initialize(this.settings);
      this.gitHubManager.initialize(this.settings);
      ShaderData.initialize();
      this.messageManager.postMessage(MessageManager.MessageType.OnSettingsLoad, this.settings);
   }

   public void saveSettings() {
      this.messageManager.saveSettings(this.settings);
      this.gameManager.saveSettings(this.settings);
      this.pathManager.saveSettings(this.settings);
      this.uiManager.saveSettings(this.settings);
      this.editorManager.saveSettings(this.settings);
      this.hashManager.saveSettings(this.settings);
      this.projectManager.saveSettings(this.settings);
      this.documentationManager.saveSettings(this.settings);
      this.formatManager.saveSettings(this.settings);
      this.fileManager.saveSettings(this.settings);
      this.modeManager.saveSettings(this.settings);
      this.languageManager.saveSettings(this.settings);
      this.imageEditorManager.saveSettings(this.settings);
      this.fxCompiler.saveSettings(this.settings);
      this.gitHubManager.saveSettings(this.settings);
      this.messageManager.postMessage(MessageManager.MessageType.OnSettingsSave, this.settings);

      try (FileOutputStream var1 = new FileOutputStream(this.pathManager.getProgramFile("config.properties"))) {
         this.settings.store(var1, null);
      } catch (IOException var6) {
         var6.printStackTrace();
      }
   }

   public void init() {
      this.init(false);
   }

   public void stop() {
      this.gitHubManager.dispose();
      this.fxCompiler.dispose();
      this.documentationManager.dispose();
      this.formatManager.dispose();
      this.editorManager.dispose();
      this.projectManager.dispose();
      this.hashManager.dispose();
      this.uiManager.dispose();
      this.fileManager.dispose();
      this.pathManager.dispose();
      this.gameManager.dispose();
   }

   public void start(Stage var1) {
      this.uiManager.start(var1);
      this.uiManager.notifyUIUpdate(true);
      this.uiManager.show();
      if (!this.updateManager.checkUpdate()) {
         Platform.exit();
      } else {
         this.uiManager.showInitializationError();
         this.gitHubManager.showFirstTimeDialog();
         if (this.uiManager.isFirstTime()) {
            this.gameManager.showFirstTimeDialog();
            this.projectManager.showFirstTimeDialog();
            this.uiManager.setOverlay(false);
            this.uiManager.setFirstTime(false);
            get().saveSettings();
         }
      }
   }

   public static void main(String[] var0) {
      launch(var0);
   }
}
