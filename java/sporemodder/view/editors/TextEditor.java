package sporemodder.view.editors;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.Skin;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyCombination.Modifier;
import javafx.stage.Popup;
import org.fxmisc.richtext.CodeArea;
import org.fxmisc.richtext.LineNumberFactory;
import org.fxmisc.richtext.event.MouseOverTextEvent;
import org.fxmisc.wellbehaved.event.EventPattern;
import org.fxmisc.wellbehaved.event.InputMap;
import org.fxmisc.wellbehaved.event.Nodes;
import sporemodder.EditorManager;
import sporemodder.FileManager;
import sporemodder.LanguageManager;
import sporemodder.ProjectManager;
import sporemodder.UIManager;
import sporemodder.file.BinaryPreview;
import sporemodder.file.DocumentFragment;
import sporemodder.file.DocumentStructure;
import sporemodder.file.TextReader;
import sporemodder.file.TextUtils;
import sporemodder.util.ProjectItem;
import sporemodder.view.UserInterface;
import sporemodder.view.ribbons.EditRibbonTab;
import sporemodder.view.syntax.SyntaxFormat;
import sporemodder.view.syntax.SyntaxFormatFactory;
import sporemodder.view.syntax.SyntaxHighlighter;

public class TextEditor extends AbstractEditableEditor implements ItemEditor, SearchableEditor, EditHistoryEditor {
   public static final String DEFAULT_STYLE_CLASS = "sporemodder-text-editor";
   private final CodeArea codeArea = new CodeArea();
   private boolean textEventsDisabled;
   private SyntaxFormat syntaxHighlighting;
   private DocumentStructure structure;
   private final List<TooltipFactory> tooltipFactories = new ArrayList<>();
   private final Popup popup = new Popup();
   private final Label popupMsg = new Label();
   private boolean isValidText = true;
   private String originalContents;
   private String searchedText;
   private final TreeSet<Integer> searchPositions = new TreeSet<>();

   public TextEditor() {
      this.getStyleClass().add("sporemodder-text-editor");
      this.codeArea.setPrefWidth(Double.MAX_VALUE);
      this.codeArea.setPrefHeight(Double.MAX_VALUE);
      this.codeArea.setParagraphGraphicFactory(LineNumberFactory.get(this.codeArea));
      this.codeArea.requestFollowCaret();
      this.codeArea.setOnKeyTyped(var1 -> {
         if (var1.getCharacter().getBytes()[0] == 10 || var1.getCharacter().getBytes()[0] == 13) {
            this.tabulate();
            this.codeArea.requestFollowCaret();
         }
      });
      Nodes.addInputMap(this.codeArea, InputMap.consume(EventPattern.keyPressed(KeyCode.TAB, new Modifier[0]), var1 -> {
         if (this.codeArea.getSelection().getLength() != 0) {
            this.tabulateSelection();
         } else {
            this.codeArea.replaceSelection("\t");
         }
      }));
      Nodes.addInputMap(this.codeArea, InputMap.consume(EventPattern.keyPressed(KeyCode.TAB, new Modifier[]{KeyCombination.SHIFT_DOWN}), var1 -> {
         if (this.codeArea.getSelection().getLength() != 0) {
            this.untabulateSelection();
         } else {
            this.codeArea.replaceSelection("\t");
         }
      }));
      Nodes.addInputMap(this.codeArea, InputMap.consume(EventPattern.keyPressed(KeyCode.F, new Modifier[]{KeyCombination.CONTROL_DOWN}), var1 -> {
         UserInterface.get().setActiveRibbonTab("EDIT");
         EditRibbonTab var2 = (EditRibbonTab)UserInterface.get().getRibbonTabController("EDIT");
         if (this.codeArea.getSelection().getLength() != 0) {
            var2.getTextActionsUI().getFindField().setText(this.codeArea.getSelectedText());
         }

         var2.getTextActionsUI().focusFindField();
      }));
      Nodes.addInputMap(this.codeArea, InputMap.consume(EventPattern.keyPressed(KeyCode.K, new Modifier[]{KeyCodeCombination.CONTROL_DOWN}), var1 -> {
         if (this.codeArea.getSelection().getLength() != 0) {
            int var2 = this.codeArea.getSelection().getStart();
            int var3 = this.codeArea.getSelection().getEnd();
            if (this.syntaxHighlighting == null || !this.syntaxHighlighting.toggleBlockComment(this, var2, var3)) {
               this.toggleBlockComment(var2, var3);
            }
         } else {
            int var4 = this.codeArea.getCaretPosition();
            if (this.syntaxHighlighting == null || !this.syntaxHighlighting.toggleLineComment(this, var4)) {
               this.toggleLineComment(var4);
            }
         }
      }));
      Nodes.addInputMap(this.codeArea, InputMap.consume(EventPattern.keyPressed(KeyCode.D, new Modifier[]{KeyCodeCombination.CONTROL_DOWN}), var1 -> {
         if (this.codeArea.getSelection().getLength() != 0) {
            int var2 = this.codeArea.getSelection().getEnd();
            this.codeArea.insertText(var2, this.codeArea.getSelectedText());
            this.codeArea.selectRange(var2, var2 + this.codeArea.getSelectedText().length());
         } else {
            String var6 = this.codeArea.getText();
            int var3 = this.codeArea.getCaretPosition();
            if (var3 >= var6.length() || TextUtils.isNewLine(var6, var3)) {
               var3--;
            }

            int var4 = TextUtils.scanLineStart(var6, var3);
            int var5 = TextUtils.scanLineEnd(var6, var3);
            this.codeArea.insertText(var5, "\n" + var6.substring(var4, var5));
         }
      }));
      this.popupMsg.getStyleClass().add("tooltip");
      this.popup.getContent().add(this.popupMsg);
      this.codeArea.setMouseOverTextDelay(Duration.ofMillis(500L));
      this.codeArea.addEventHandler(MouseOverTextEvent.MOUSE_OVER_TEXT_BEGIN, var1 -> {
         Object var2 = this.getTooltipObject(var1);
         if (var2 != null) {
            if (var2 instanceof String) {
               this.popup.getContent().set(0, this.popupMsg);
               this.popupMsg.setText((String)var2);
            } else {
               this.popup.getContent().set(0, (Node)var2);
            }

            Point2D var3 = var1.getScreenPosition();
            this.popup.show(this.codeArea, var3.getX(), var3.getY() + 10.0);
         }
      });
      this.codeArea.addEventHandler(MouseOverTextEvent.MOUSE_OVER_TEXT_END, var1 -> this.popup.hide());
      this.codeArea.textProperty().addListener((var1, var2, var3) -> {
         this.setIsSaved(false);
         if (!this.textEventsDisabled) {
            this.doSearch();
            this.updateSyntaxHighlighting();
         }

         UIManager.get().notifyUIUpdate(false);
      });
      this.codeArea.caretPositionProperty().addListener((var0, var1, var2) -> UIManager.get().notifyUIUpdate(false));
   }

   protected void toggleBlockComment(int var1, int var2) {
   }

   protected void toggleLineComment(int var1) {
   }

   private void tabulateSelection() {
      int var1 = this.codeArea.getSelection().getStart();
      int var2 = this.codeArea.getSelection().getEnd();
      String var3 = this.codeArea.getText();
      ArrayList var4 = new ArrayList();

      for (int var5 = TextUtils.scanLineStart(var3, var1); var5 < var2; var5 = TextUtils.scanLineStart(var3, TextUtils.scanLineEnd(var3, var5) + 1)) {
         var4.add(var5);
      }

      for (int var6 = 0; var6 < var4.size(); var6++) {
         this.codeArea.insertText((Integer)var4.get(var6) + var6, "\t");
      }
   }

   private void untabulateSelection() {
      int var1 = this.codeArea.getSelection().getStart();
      int var2 = this.codeArea.getSelection().getEnd();
      String var3 = this.codeArea.getText();
      ArrayList var4 = new ArrayList();

      for (int var5 = TextUtils.scanLineStart(var3, var1); var5 < var2; var5 = TextUtils.scanLineStart(var3, TextUtils.scanLineEnd(var3, var5) + 1)) {
         var4.add(var5);
      }

      int var6 = 0;

      for (int var7 = 0; var7 < var4.size(); var7++) {
         int var8 = (Integer)var4.get(var7) - var6;
         if (var3.charAt(var8) == '\t') {
            this.codeArea.replaceText(var8, var8 + 1, "");
            var3 = this.codeArea.getText();
            var6++;
         }
      }
   }

   @Override
   public void loadFile(ProjectItem var1) throws IOException {
      if (var1 != null) {
         this.item = var1;
         this.load(this.getFile(var1));
      } else {
         this.item = null;
         this.file = null;
      }
   }

   public void loadFile(File var1) throws IOException {
      if (var1 != null) {
         this.load(var1);
      } else {
         this.file = null;
      }
   }

   protected File getFile(ProjectItem var1) {
      return var1.getFile();
   }

   public void load(File var1) throws IOException {
      this.setDestinationFile(var1);
      SyntaxFormatFactory var2 = EditorManager.get().getSyntaxHighlighting(var1);
      if (var2 != null) {
         this.syntaxHighlighting = var2;
      }

      try (FileInputStream var3 = new FileInputStream(var1)) {
         TextReader var4 = new TextReader();
         var4.read(var3);
         this.isValidText = var4.isValidText();
         String var5 = var4.getText();
         if (BinaryPreview.containsNul(var5)) {
            this.isValidText = false;
            this.syntaxHighlighting = null;
            var5 = BinaryPreview.describe(
               var1,
               LanguageManager.tr("This file is binary (it contains NUL characters), so it cannot be shown or edited as text."),
               LanguageManager.tr("Size (bytes):"),
               LanguageManager.tr("First bytes (hex):")
            );
         }

         this.loadContents(var5);
      }
   }

   public void loadContents(String var1) throws IOException {
      this.originalContents = var1;
      this.codeArea.replaceText(this.originalContents);
      this.codeArea.moveTo(0);
      this.codeArea.scrollToPixel(0.0, 0.0);
      if (this.item != null && this.item.isMod() && ProjectManager.get().getActive().isReadOnly()) {
         this.codeArea.setEditable(false);
      } else {
         this.codeArea.setEditable(this.isValidText);
      }

      this.setIsSaved(true);
      this.updateSyntaxHighlighting();
      this.codeArea.getUndoManager().forgetHistory();
   }

   protected Skin<?> createDefaultSkin() {
      return new TextEditorSkin(this);
   }

   public CodeArea getCodeArea() {
      return this.codeArea;
   }

   @Override
   public ProjectItem getItem() {
      return this.item;
   }

   public boolean isTextEventsDisabled() {
      return this.textEventsDisabled;
   }

   public void setTextEventsDisabled(boolean var1) {
      this.textEventsDisabled = var1;
   }

   public void eventlessEdit(TextEditor.EditAction var1) {
      this.textEventsDisabled = true;
      var1.edit(this.codeArea);
      this.textEventsDisabled = false;
      this.updateSyntaxHighlighting();
   }

   public SyntaxFormat getSyntaxHighlighting() {
      return this.syntaxHighlighting;
   }

   public void setSyntaxHighlighting(SyntaxFormat var1) {
      this.syntaxHighlighting = var1;
      this.updateSyntaxHighlighting();
   }

   public void updateSyntaxHighlighting() {
      SyntaxHighlighter var1 = new SyntaxHighlighter();
      var1.setText(this.codeArea.getText(), null);
      if (this.syntaxHighlighting != null) {
         this.syntaxHighlighting.generateStyle(this.codeArea.getText(), var1);
      }

      if (!this.searchPositions.isEmpty()) {
         int var2 = this.searchedText.length();

         for (Integer var4 : this.searchPositions) {
            var1.addExtra(var4, var2, "text-searched-word", false);
         }
      }

      try {
         this.codeArea.setStyleSpans(0, var1.generateStyleSpans());
      } catch (Exception var5) {
         var5.printStackTrace();
      }
   }

   public void replaceText(DocumentFragment var1, String var2) {
      this.codeArea.replaceText(var1.getStart(), var1.getEnd(), var2);
      this.structure.setLength(var1, var2.length());
      this.structure.setText(this.codeArea.getText());
   }

   public DocumentStructure getStructure() {
      return this.structure;
   }

   public void setStructure(DocumentStructure var1) {
      this.structure = var1;
   }

   public void tabulate() {
      String var1 = this.codeArea.getCaretPosition() == 0 ? "" : this.codeArea.getText().substring(0, this.codeArea.getCaretPosition() - 1);
      int var2 = var1.lastIndexOf("\n");
      if (var2 == -1) {
         var2 = 0;
      }

      int var3 = 0;

      while (var2 < var1.length()) {
         char var4 = var1.charAt(var2++);
         if (var4 != '\r' && var4 != '\n') {
            if (var4 != '\t') {
               break;
            }

            var3++;
         }
      }

      if (var3 != 0) {
         StringBuilder var6 = new StringBuilder();

         for (int var5 = 0; var5 < var3; var5++) {
            var6.append('\t');
         }

         this.codeArea.insertText(this.codeArea.getCaretPosition(), var6.toString());
      }
   }

   @Override
   public void setActive(boolean var1) {
      super.setActive(var1);
      if (var1 && !this.isValidText && this.item != null) {
         Label var2 = new Label(LanguageManager.tr("This file cannot be read as text, therefore it cannot be edited."));
         var2.setGraphic(UIManager.get().getAlertIcon(AlertType.WARNING, 16, 16));
         UIManager.get().getUserInterface().setStatusInfo(var2);
      }

      if (var1) {
         this.updateSyntaxHighlighting();
      }

      this.showInspector(var1);
   }

   public final String getText() {
      return this.codeArea.getText();
   }

   protected void showInspector(boolean var1) {
      if (var1) {
         UserInterface.get().getInspectorPane().configureDefault(null, FileManager.getExtension(this.getFile().getName()), null);
      } else {
         UserInterface.get().getInspectorPane().reset();
      }
   }

   @Override
   protected void saveData() throws Exception {
      if (!this.file.exists()) {
         this.file.createNewFile();
      }

      try (PrintWriter var1 = new PrintWriter(new OutputStreamWriter(new FileOutputStream(this.file), StandardCharsets.UTF_8))) {
         var1.write(this.codeArea.getText());
         this.setIsSaved(true);
      }
   }

   @Override
   public boolean isEditable() {
      return this.isValidText;
   }

   public List<TooltipFactory> getTooltipFactories() {
      return this.tooltipFactories;
   }

   private Object getTooltipObject(MouseOverTextEvent var1) {
      Object var2 = null;
      String var3 = this.codeArea.getText();

      for (TooltipFactory var5 : this.tooltipFactories) {
         var2 = var5.getTooltip(var3, var1);
         if (var2 != null) {
            return var2;
         }
      }

      return null;
   }

   @Override
   public Node getUI() {
      return this;
   }

   @Override
   public boolean supportsSearching() {
      return true;
   }

   @Override
   public boolean supportsEditHistory() {
      return true;
   }

   @Override
   public boolean canFindUp() {
      int var1 = this.codeArea.getCaretPosition();
      return this.searchPositions.floor(var1 - 1) != null;
   }

   @Override
   public boolean canFindDown() {
      int var1 = this.codeArea.getCaretPosition();
      return this.searchPositions.ceiling(var1 + 1) != null;
   }

   @Override
   public void findUp() {
      int var1 = this.codeArea.getCaretPosition();
      Integer var2 = this.searchPositions.floor(var1 - 1);
      if (var2 != null) {
         this.codeArea.moveTo(var2);
         this.codeArea.selectRange(var2 + this.searchedText.length(), var2);
         this.codeArea.requestFollowCaret();
      }
   }

   @Override
   public void findDown() {
      int var1 = this.codeArea.getCaretPosition();
      Integer var2 = this.searchPositions.ceiling(var1 + 1);
      if (var2 != null) {
         this.codeArea.moveTo(var2);
         this.codeArea.selectRange(var2 + this.searchedText.length(), var2);
         this.codeArea.requestFollowCaret();
      }
   }

   private void doSearch() {
      this.searchPositions.clear();
      if (this.searchedText != null && !this.searchedText.isEmpty()) {
         String var1 = this.codeArea.getText().toLowerCase();
         String var2 = this.searchedText.toLowerCase();
         int var3 = -1;

         while ((var3 = var1.indexOf(var2, var3 + 1)) != -1) {
            this.searchPositions.add(var3);
         }
      }
   }

   @Override
   public boolean find(String var1) {
      if ((this.searchedText != null || var1 != null) && (this.searchedText == null || !this.searchedText.equals(var1))) {
         this.searchedText = var1;
         this.doSearch();
         this.updateSyntaxHighlighting();
         return !this.searchPositions.isEmpty();
      } else {
         return true;
      }
   }

   @Override
   public String getSearchedText() {
      return this.searchedText;
   }

   @Override
   public boolean canUndo() {
      return this.codeArea.isUndoAvailable();
   }

   @Override
   public boolean canRedo() {
      return this.codeArea.isRedoAvailable();
   }

   @Override
   public void undo() {
      this.codeArea.undo();
   }

   @Override
   public void redo() {
      this.codeArea.redo();
   }

   @Override
   protected void restoreContents() {
      this.codeArea.replaceText(this.originalContents);
   }

   @Override
   public List<? extends EditHistoryAction> getActions() {
      return null;
   }

   @Override
   public int getUndoRedoIndex() {
      return -1;
   }

   @FunctionalInterface
   public interface EditAction {
      void edit(CodeArea var1);
   }
}
