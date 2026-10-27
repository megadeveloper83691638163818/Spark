package sporemodder;

import javafx.collections.ListChangeListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.ListIterator;
import java.util.Properties;
import java.util.Set;
import javafx.scene.Node;
import javafx.scene.control.Tab;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import sporemodder.imageeditor.ImageEditor;
import sporemodder.util.Project;
import sporemodder.util.ProjectItem;
import sporemodder.view.EditorPaneUI;
import sporemodder.view.StatusBar;
import sporemodder.view.UIUpdateListener;
import sporemodder.view.UserInterface;
import sporemodder.view.editors.AnimTextEditorFactory;
import sporemodder.view.editors.ArithmeticaEditorFactory;
import sporemodder.view.editors.CellBackgroundMapEditorFactory;
import sporemodder.view.editors.CellEffectMapEditorFactory;
import sporemodder.view.editors.CellFileEditorFactory;
import sporemodder.view.editors.CellGlobalsEditorFactory;
import sporemodder.view.editors.CellLookAlgorithmEditorFactory;
import sporemodder.view.editors.CellLookTableEditorFactory;
import sporemodder.view.editors.CellLootTableEditorFactory;
import sporemodder.view.editors.CellPopulateEditorFactory;
import sporemodder.view.editors.CellPowersEditorFactory;
import sporemodder.view.editors.CellRandomCreatureEditorFactory;
import sporemodder.view.editors.CellStructureEditorFactory;
import sporemodder.view.editors.CellWorldEditorFactory;
import sporemodder.view.editors.CnvEditorFactory;
import sporemodder.view.editors.CompiledShaderViewerFactory;
import sporemodder.view.editors.EditHistoryEditor;
import sporemodder.view.editors.EditorFactory;
import sporemodder.view.editors.GMDLModelViewer;
import sporemodder.view.editors.GaitEditorFactory;
import sporemodder.view.editors.ImageViewer;
import sporemodder.view.editors.ItemEditor;
import sporemodder.view.editors.PctpEditorFactory;
import sporemodder.view.editors.PfxEditorFactory;
import sporemodder.view.editors.PollenMetadataEditor;
import sporemodder.view.editors.PropEditorFactory;
import sporemodder.view.editors.RWModelViewer;
import sporemodder.view.editors.ScnEditorFactory;
import sporemodder.view.editors.SearchableEditor;
import sporemodder.view.editors.ShaderBuilderEditorFactory;
import sporemodder.view.editors.ShaderFragmentEditorFactory;
import sporemodder.view.editors.SmtTextEditorFactory;
import sporemodder.view.editors.SpuiEditorFactory;
import sporemodder.view.editors.SummaryEditor;
import sporemodder.view.editors.TextEditorFactory;
import sporemodder.view.editors.TlsaEditorFactory;
import sporemodder.view.editors.XmlEditorFactory;
import sporemodder.view.syntax.CppSyntax;
import sporemodder.view.syntax.HlslSyntax;
import sporemodder.view.syntax.SyntaxFormatFactory;
import sporemodder.view.syntax.XmlSyntax;

public class EditorManager extends AbstractManager implements UIUpdateListener {
   private final List<EditorFactory> editorFactories = new ArrayList<>();
   private EditorFactory defaultEditorFactory;
   private EditorPaneUI.EditorTab activeEditorTab;
   private EditorPaneUI.EditorTab mainEditorTab;
   private final List<SyntaxFormatFactory> syntaxHighlighters = new ArrayList<>();
   private Image folderIcon;
   private EditorPaneUI paneUI;
   private boolean disableTabEvents = false;
   private EditorPaneUI.EditorTab previousMainEditorTab;
   private SearchableEditor searchableEditor;
   private EditHistoryEditor editHistoryEditor;

   public static EditorManager get() {
      return MainApp.get().getEditorManager();
   }

   @Override
   public void initialize(Properties var1) {
      this.defaultEditorFactory = new TextEditorFactory();
      this.editorFactories.add(this.defaultEditorFactory);
      this.editorFactories.add(new PollenMetadataEditor.Factory());
      this.editorFactories.add(new SummaryEditor.Factory());
      this.editorFactories.add(new ArithmeticaEditorFactory());
      this.editorFactories.add(new CellLookTableEditorFactory());
      this.editorFactories.add(new CellEffectMapEditorFactory());
      this.editorFactories.add(new CellPopulateEditorFactory());
      this.editorFactories.add(new CellStructureEditorFactory());
      this.editorFactories.add(new CellRandomCreatureEditorFactory());
      this.editorFactories.add(new CellFileEditorFactory());
      this.editorFactories.add(new CellLootTableEditorFactory());
      this.editorFactories.add(new CellPowersEditorFactory());
      this.editorFactories.add(new CellBackgroundMapEditorFactory());
      this.editorFactories.add(new CellWorldEditorFactory());
      this.editorFactories.add(new CellGlobalsEditorFactory());
      this.editorFactories.add(new CellLookAlgorithmEditorFactory());
      this.editorFactories.add(new GaitEditorFactory());
      this.editorFactories.add(new ScnEditorFactory());
      this.editorFactories.add(new CompiledShaderViewerFactory());
      this.editorFactories.add(new ShaderBuilderEditorFactory());
      this.editorFactories.add(new ShaderFragmentEditorFactory());
      this.editorFactories.add(new SmtTextEditorFactory());
      this.editorFactories.add(new XmlEditorFactory());
      this.editorFactories.add(new AnimTextEditorFactory());
      this.editorFactories.add(new CnvEditorFactory());
      this.editorFactories.add(new PctpEditorFactory());
      this.editorFactories.add(new TlsaEditorFactory());
      this.editorFactories.add(new SpuiEditorFactory());
      this.editorFactories.add(new ImageViewer.Factory());
      this.editorFactories.add(new RWModelViewer.Factory());
      this.editorFactories.add(new GMDLModelViewer.Factory());
      this.editorFactories.add(new PfxEditorFactory());
      this.editorFactories.add(new PropEditorFactory());
      this.editorFactories.add(new ImageEditor.Factory());
      this.syntaxHighlighters.add(new XmlSyntax());
      this.syntaxHighlighters.add(new HlslSyntax());
      this.syntaxHighlighters.add(new CppSyntax());
      this.folderIcon = UIManager.get().loadImage("item-icon-folder.png");
      UIManager.get().addListener(this);
   }

   public List<EditorFactory> getEditorFactories() {
      return this.editorFactories;
   }

   public List<SyntaxFormatFactory> getSyntaxHighlighters() {
      return this.syntaxHighlighters;
   }

   @Override
   public void dispose() {
      try {
         for (Tab var2 : this.paneUI.getTabPane().getTabs()) {
            EditorPaneUI.EditorTab var3 = (EditorPaneUI.EditorTab)var2;
            if (var3.editor != null) {
               var3.editor.loadFile(null);
            }
         }
      } catch (Exception var4) {
         var4.printStackTrace();
      }
   }

   public ProjectItem getActiveItem() {
      return this.activeEditorTab == null ? null : this.activeEditorTab.item;
   }

   public boolean hasItem(ProjectItem var1) {
      for (Tab var3 : this.paneUI.getTabPane().getTabs()) {
         EditorPaneUI.EditorTab var4 = (EditorPaneUI.EditorTab)var3;
         if (var4.item == var1) {
            return true;
         }
      }

      return true;
   }

   public void loadFile(ProjectItem var1) throws IOException {
      UIManager var2 = UIManager.get();
      this.previousMainEditorTab = this.mainEditorTab;
      String var3 = var1.getRelativePath();
      EditorPaneUI.EditorTab var4 = this.paneUI.getTab(var3);
      if (var4 == null) {
         ItemEditor var5 = var1.createEditor();
         if (this.activeEditorTab != null && this.activeEditorTab.editor != null) {
            this.activeEditorTab.editor.setActive(false);
            MessageManager.get().postMessage(MessageManager.MessageType.OnEditorUnsetAsActive, this.activeEditorTab.editor);
         }

         this.disableTabEvents = true;
         if (this.mainEditorTab == null) {
            this.activeEditorTab = var4 = this.mainEditorTab = this.paneUI.createMainTab(var5, var1, var3, true);
         } else {
            this.activeEditorTab = var4 = this.mainEditorTab = this.paneUI.createMainTab(var5, var1, var3, false);
            this.paneUI.getTabPane().getTabs().set(0, var4);
         }

         this.loadFile(this.mainEditorTab, var1);
         if (var5 != null && var5.supportsSearching() && ProjectManager.get().isShowingSearch()) {
            List<String> var6 = ProjectManager.get().getSearchedWords();
            SearchableEditor var7 = (SearchableEditor)var5;

            for (String var9 : var6) {
               if (var7.find(var9)) {
                  break;
               }
            }
         }

         this.disableTabEvents = false;
      }

      this.paneUI.getTabPane().getSelectionModel().select(var4);
      this.setActiveEditor_impl(this.paneUI.getTabPane().getTabs().indexOf(var4));
      this.updateFileLabel(var3);
      var2.notifyUIUpdate(false);
      MessageManager.get().postMessage(MessageManager.MessageType.OnFileLoad, var1);
   }

   private void loadFile(EditorPaneUI.EditorTab var1, ProjectItem var2) throws IOException {
      if (var1.editor != null) {
         var1.editor.loadFile(var2);
      }

      var1.setText(var2.getName());
      if (var1.editor != null) {
         var1.setContent(var1.editor.getUI());
      } else {
         var1.setContent(null);
      }
   }

   public boolean moveFileToNewTab(ProjectItem var1) {
      if (this.mainEditorTab != null && var1.getRelativePath().equals(this.mainEditorTab.relativePath)) {
         this.disableTabEvents = true;
         EditorPaneUI.EditorTab var2 = new EditorPaneUI.EditorTab(this.mainEditorTab.editor, this.mainEditorTab.item, this.mainEditorTab.relativePath);
         var2.setText(this.mainEditorTab.getText());
         var2.setContent(this.mainEditorTab.getContent());
         this.paneUI.getTabPane().getTabs().add(var2);
         this.paneUI.getTabPane().getTabs().remove(0);
         if (this.previousMainEditorTab != null) {
            this.paneUI.getTabPane().getTabs().add(0, this.previousMainEditorTab);
         }

         this.mainEditorTab = this.previousMainEditorTab;
         this.previousMainEditorTab = null;
         this.disableTabEvents = false;
         this.paneUI.getTabPane().getSelectionModel().select(var2);
         UIManager.get().notifyUIUpdate(false);
         return true;
      } else {
         return false;
      }
   }

   public ItemEditor getEditor(String var1) {
      for (Tab var3 : this.paneUI.getTabPane().getTabs()) {
         EditorPaneUI.EditorTab var4 = (EditorPaneUI.EditorTab)var3;
         if (var1.equals(var4.relativePath)) {
            return var4.editor;
         }
      }

      return null;
   }

   public boolean removeEditor(String var1) {
      for (Tab var3 : this.paneUI.getTabPane().getTabs()) {
         if (var1.equals(((EditorPaneUI.EditorTab)var3).relativePath)) {
            this.paneUI.getTabPane().getTabs().remove(var3);
            return true;
         }
      }

      return false;
   }

   public boolean reloadEditor(String var1) throws IOException {
      ArrayList var2 = new ArrayList();

      for (Tab var4 : this.paneUI.getTabPane().getTabs()) {
         EditorPaneUI.EditorTab var5 = (EditorPaneUI.EditorTab)var4;
         if (var1.equals(var5.relativePath)) {
            this.reloadEditor(var5, var2);
            return true;
         }
      }

      this.paneUI.getTabPane().getTabs().removeAll(var2);
      return false;
   }

   private void reloadEditor(EditorPaneUI.EditorTab var1, List<Tab> var2) throws IOException {
      ProjectItem var3 = ProjectManager.get().getItem(var1.relativePath);
      if (var3 == null) {
         var2.add(var1);
      } else if (var1.item != var3) {
         var1.item = var3;
         if (var1.editor != null) {
            if (var1.item.isMod()) {
               var1.editor.save();
            }

            var1.editor.loadFile(var1.item);
         }
      }
   }

   public void reloadEditors() throws IOException {
      ArrayList var1 = new ArrayList();

      for (Tab var3 : this.paneUI.getTabPane().getTabs()) {
         this.reloadEditor((EditorPaneUI.EditorTab)var3, var1);
      }

      this.paneUI.getTabPane().getTabs().removeAll(var1);
   }

   public ItemEditor getActiveEditor() {
      return this.activeEditorTab == null ? null : this.activeEditorTab.editor;
   }

   public void setActiveEditor(int var1) {
      this.paneUI.getTabPane().getSelectionModel().select(var1);
   }

   private void updateSearchableEditor() {
      if (this.activeEditorTab != null && this.activeEditorTab.editor != null && this.activeEditorTab.editor.supportsSearching()) {
         this.setSearchableEditor((SearchableEditor)this.activeEditorTab.editor);
      } else {
         this.setSearchableEditor(null);
      }

      if (this.activeEditorTab != null && this.activeEditorTab.editor != null && this.activeEditorTab.editor.supportsEditHistory()) {
         this.setEditHistoryEditor((EditHistoryEditor)this.activeEditorTab.editor);
      } else {
         this.setEditHistoryEditor(null);
      }
   }

   private void setActiveEditor_impl(int var1) {
      if (this.activeEditorTab != null) {
         int var2 = this.paneUI.getTabPane().getTabs().indexOf(this.activeEditorTab);
         if (var2 == var1) {
            this.updateSearchableEditor();
            this.updateStatusLabel();
            if (this.activeEditorTab.editor != null) {
               this.activeEditorTab.editor.setActive(true);
               MessageManager.get().postMessage(MessageManager.MessageType.OnEditorSetAsActive, this.activeEditorTab.editor);
            }

            return;
         }

         if (this.activeEditorTab.editor != null) {
            this.activeEditorTab.editor.setActive(false);
            MessageManager.get().postMessage(MessageManager.MessageType.OnEditorUnsetAsActive, this.activeEditorTab.editor);
         }
      }

      if (var1 != -1) {
         this.activeEditorTab = (EditorPaneUI.EditorTab)this.paneUI.getTabPane().getTabs().get(var1);
         if (this.activeEditorTab != null && this.activeEditorTab.relativePath != null) {
            this.updateFileLabel(this.activeEditorTab.relativePath);
         }

         this.updateStatusLabel();
      }

      if (this.activeEditorTab != null && this.activeEditorTab.editor != null) {
         this.activeEditorTab.editor.setActive(true);
         MessageManager.get().postMessage(MessageManager.MessageType.OnEditorSetAsActive, this.activeEditorTab.editor);
      }

      this.updateSearchableEditor();
      UIManager.get().notifyUIUpdate(false);
   }

   private void updateStatusLabel() {
      UserInterface var1 = UIManager.get().getUserInterface();
      var1.setStatusFile(this.activeEditorTab == null ? null : this.activeEditorTab.item);
      var1.setStatusInfo(null);
      var1.getStatusBar().setStatus(StatusBar.Status.DEFAULT);
   }

   private void updateFileLabel(String var1) {
      if (var1 != null) {
         if (var1.startsWith("..")) {
            this.paneUI.setFileLabel(null, var1);
         } else {
            Project var2 = ProjectManager.get().getProjectByFile(var1);
            if (var2 != null) {
               this.paneUI.setFileLabel(var2.getName(), var1);
            }
         }
      }
   }

   public void loadFixedTabs(Collection<String> var1) {
      this.disableTabEvents = true;

      for (String var3 : var1) {
         ProjectItem var4 = ProjectManager.get().getItem(var3);
         if (var4 != null) {
            EditorPaneUI.EditorTab var5 = new EditorPaneUI.EditorTab(var4.createEditor(), var4, var3);

            try {
               this.loadFile(var5, var4);
               this.paneUI.getTabPane().getTabs().add(var5);
            } catch (IOException var7) {
               var7.printStackTrace();
            }
         }
      }

      this.disableTabEvents = false;
      if (!this.paneUI.getTabPane().getTabs().isEmpty()) {
         this.setActiveEditor(0);
         this.setActiveEditor_impl(0);
      }
   }

   public void clearTabs() {
      this.paneUI.getTabPane().getTabs().clear();
      this.mainEditorTab = null;
      this.activeEditorTab = null;
      this.previousMainEditorTab = null;
   }

   public void setUI(EditorPaneUI var1) {
      this.paneUI = var1;
      var1.getTabPane().getSelectionModel().selectedItemProperty().addListener((var2, var3, var4) -> {
         if (!this.disableTabEvents && var4 != var3) {
            this.setActiveEditor_impl(var1.getTabPane().getTabs().indexOf(var4));
         }
      });
      var1.getTabPane().getTabs().addListener((ListChangeListener.Change<? extends Tab> var1x) -> {
         while (var1x.next()) {
            for (Tab var6 : var1x.getRemoved()) {
               ItemEditor var7 = ((EditorPaneUI.EditorTab)var6).editor;
               if (var7 != null) {
                  var7.setActive(false);
                  MessageManager.get().postMessage(MessageManager.MessageType.OnEditorUnsetAsActive, this.activeEditorTab.editor);
               }

               if (var6 == this.mainEditorTab) {
                  this.mainEditorTab = null;
               }
            }
         }

         if (!this.disableTabEvents) {
            Set var2 = ProjectManager.get().getActive().getFixedTabPaths();
            var2.clear();

            for (Tab var4 : var1x.getList()) {
               if (var4 != this.mainEditorTab) {
                  var2.add(((EditorPaneUI.EditorTab)var4).relativePath);
               }
            }
         }
      });
   }

   public SyntaxFormatFactory getSyntaxHighlighting(File var1) {
      ListIterator var2 = this.syntaxHighlighters.listIterator(this.syntaxHighlighters.size());

      while (var2.hasPrevious()) {
         SyntaxFormatFactory var3 = (SyntaxFormatFactory)var2.previous();
         if (var3.isSupportedFile(var1)) {
            return var3;
         }
      }

      return null;
   }

   public EditorFactory getEditorFactory(ProjectItem var1) {
      ListIterator var2 = this.editorFactories.listIterator(this.editorFactories.size());

      while (var2.hasPrevious()) {
         EditorFactory var3 = (EditorFactory)var2.previous();
         if (var3.isSupportedFile(var1)) {
            return var3;
         }
      }

      return null;
   }

   public void setTitle(ItemEditor var1, String var2) {
      EditorPaneUI.EditorTab var3 = this.paneUI.getTab(var1);
      if (var3 != null) {
         var3.setText(var2);
      }
   }

   public Node getIcon(ProjectItem var1) {
      if (var1.isFolder() && !var1.isRoot()) {
         ImageView var5 = new ImageView(this.folderIcon);
         var5.setFitWidth(24.0);
         var5.setFitHeight(18.0);
         return var5;
      } else {
         for (EditorFactory var3 : this.editorFactories) {
            Node var4 = var3.getIcon(var1);
            if (var4 != null) {
               return var4;
            }
         }

         return null;
      }
   }

   public boolean canSave() {
      return this.activeEditorTab != null && this.activeEditorTab.editor != null && this.activeEditorTab.editor.isEditable();
   }

   public void saveActive() {
      if (this.canSave()) {
         this.activeEditorTab.editor.save();
         UIManager.get().notifyUIUpdate(false);
      }
   }

   @Override
   public void onUIUpdate(boolean var1) {
   }

   public boolean isAutosaveEnabled() {
      return false;
   }

   public SearchableEditor getSearchableEditor() {
      return this.searchableEditor;
   }

   private void setSearchableEditor(SearchableEditor var1) {
      this.searchableEditor = var1;
   }

   public EditHistoryEditor getEditHistoryEditor() {
      return this.editHistoryEditor;
   }

   private void setEditHistoryEditor(EditHistoryEditor var1) {
      this.editHistoryEditor = var1;
   }
}
