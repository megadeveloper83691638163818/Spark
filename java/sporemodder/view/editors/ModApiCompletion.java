package sporemodder.view.editors;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javafx.application.Platform;
import javafx.geometry.Bounds;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.Popup;
import org.fxmisc.richtext.CodeArea;
import sporemodder.ModeManager;
import sporemodder.WorkMode;
import sporemodder.file.modapi.ModApiIndex;
import sporemodder.file.modapi.ModApiIndex.Entry;
import sporemodder.view.dialogs.ModApiSearchDialog;

public final class ModApiCompletion {
   private static final Set<String> EXTENSIONS = new HashSet<>(Arrays.asList(".cpp", ".cc", ".cxx", ".c", ".h", ".hpp", ".hh", ".hxx", ".inl"));
   private static final String[] KEYWORDS = {"auto", "bool", "break", "case", "catch", "char", "class", "const", "constexpr", "continue", "default", "delete", "do", "double",
      "else", "enum", "explicit", "extern", "false", "float", "for", "if", "inline", "int", "long", "namespace", "new", "nullptr", "override", "private", "protected",
      "public", "return", "short", "signed", "sizeof", "static", "static_cast", "struct", "switch", "template", "this", "true", "typedef", "typename", "union",
      "unsigned", "using", "virtual", "void", "volatile", "while", "uint32_t", "int32_t", "uint16_t", "uint8_t", "size_t", "string", "eastl::string", "eastl::vector"};
   private static final int MAX_ITEMS = 60;

   private static final class Item {
      final String NAME, DETAIL, INSERT;
      final Entry ENTRY;

      Item(String NAME, String DETAIL, String INSERT, Entry ENTRY) {
         this.NAME = NAME;
         this.DETAIL = DETAIL;
         this.INSERT = INSERT;
         this.ENTRY = ENTRY;
      }
   }

   private final TextEditor EDITOR;
   private final CodeArea AREA;
   private final Popup POPUP = new Popup();
   private final ListView<Item> LIST = new ListView<>();
   private boolean SWALLOW_TYPED;
   private boolean STYLES_COPIED;

   public static void INSTALL(TextEditor EDITOR) {
      new ModApiCompletion(EDITOR);
   }

   private ModApiCompletion(TextEditor EDITOR) {
      this.EDITOR = EDITOR;
      AREA = EDITOR.getCodeArea();

      LIST.setFocusTraversable(false);
      LIST.setPrefWidth(520);
      LIST.setCellFactory(V -> new ListCell<Item>() {
         @Override
         protected void updateItem(Item ITEM, boolean EMPTY) {
            super.updateItem(ITEM, EMPTY);
            setText(EMPTY || ITEM == null ? null : ITEM.NAME + (ITEM.DETAIL.isEmpty() ? "" : "    " + ITEM.DETAIL));
         }
      });
      LIST.setOnMouseClicked(E -> {
         if (E.getClickCount() >= 2) {
            ACCEPT();
         }
      });
      POPUP.getContent().add(LIST);

      AREA.addEventFilter(KeyEvent.KEY_PRESSED, this::ON_KEY_PRESSED);
      AREA.addEventFilter(KeyEvent.KEY_TYPED, E -> {
         if (SWALLOW_TYPED) {
            SWALLOW_TYPED = false;
            E.consume();
         }
      });
      AREA.addEventHandler(KeyEvent.KEY_TYPED, E -> {
         if (!IS_ACTIVE()) {
            return;
         }
         String CH = E.getCharacter();
         if (!CH.isEmpty() && (IS_IDENT_CHAR(CH.charAt(0)) || CH.charAt(0) == ':')) {
            Platform.runLater(() -> REFRESH(false));
         } else {
            POPUP.hide();
         }
      });
      AREA.focusedProperty().addListener((O, WAS, NOW) -> {
         if (!NOW) {
            POPUP.hide();
         }
      });
   }

   private boolean IS_ACTIVE() {
      if (!WorkMode.DLL_TOOLS.contains(ModeManager.get().getMode())) {
         return false;
      }
      File FILE = EDITOR.getFile();
      if (FILE == null) {
         return false;
      }
      String N = FILE.getName().toLowerCase(Locale.ROOT);
      int DOT = N.lastIndexOf('.');
      return DOT >= 0 && EXTENSIONS.contains(N.substring(DOT)) && AREA.isEditable();
   }

   private void ON_KEY_PRESSED(KeyEvent E) {
      if (!IS_ACTIVE()) {
         return;
      }
      if (E.isControlDown() && E.isShiftDown() && E.getCode() == KeyCode.F) {
         E.consume();
         ModApiSearchDialog.SHOW(EDITOR);
         return;
      }
      if (E.isControlDown() && E.getCode() == KeyCode.SPACE) {
         E.consume();
         REFRESH(true);
         return;
      }
      if (!POPUP.isShowing()) {
         return;
      }

      int SEL = LIST.getSelectionModel().getSelectedIndex();
      switch (E.getCode()) {
         case DOWN:
            E.consume();
            LIST.getSelectionModel().select(Math.min(LIST.getItems().size() - 1, SEL + 1));
            LIST.scrollTo(LIST.getSelectionModel().getSelectedIndex());
            break;
         case UP:
            E.consume();
            LIST.getSelectionModel().select(Math.max(0, SEL - 1));
            LIST.scrollTo(LIST.getSelectionModel().getSelectedIndex());
            break;
         case ENTER:
         case TAB:
            E.consume();
            SWALLOW_TYPED = true;
            Platform.runLater(() -> SWALLOW_TYPED = false);
            ACCEPT();
            break;
         case ESCAPE:
            E.consume();
            POPUP.hide();
            break;
         case BACK_SPACE:
            Platform.runLater(() -> REFRESH(false));
            break;
         case LEFT:
         case RIGHT:
         case HOME:
         case END:
         case PAGE_UP:
         case PAGE_DOWN:
            POPUP.hide();
            break;
         default:
            break;
      }
   }

   private static boolean IS_IDENT_CHAR(char C) {
      return Character.isLetterOrDigit(C) || C == '_';
   }

   private int PREFIX_START() {
      String TEXT = AREA.getText();
      int START = AREA.getCaretPosition();
      while (START > 0 && IS_IDENT_CHAR(TEXT.charAt(START - 1))) {
         START--;
      }
      return START;
   }

   private void REFRESH(boolean FORCE) {
      if (!IS_ACTIVE()) {
         POPUP.hide();
         return;
      }
      ModApiIndex.ENSURE_INDEXED(false, null);

      String TEXT = AREA.getText();
      int START = PREFIX_START();
      String PREFIX = TEXT.substring(START, AREA.getCaretPosition());
      if (PREFIX.length() < 2 && !FORCE) {
         POPUP.hide();
         return;
      }
      // not in comments and strings
      String BEFORE = TEXT.substring(TEXT.lastIndexOf('\n', Math.max(0, START - 1)) + 1, START);
      if (BEFORE.contains("//") || (BEFORE.length() - BEFORE.replace("\"", "").length()) % 2 == 1) {
         POPUP.hide();
         return;
      }

      String OWNER = null;
      Matcher OM = Pattern.compile("(\\w+)::$").matcher(BEFORE);
      if (OM.find()) {
         OWNER = OM.group(1);
      }

      String LOW = PREFIX.toLowerCase(Locale.ROOT);
      Map<String, Item> ITEMS = new LinkedHashMap<>();

      if (OWNER == null) {
         for (Entry S : ModApiIndex.SNIPPETS()) {
            if (MATCHES(S.NAME, LOW)) {
               ITEMS.putIfAbsent("s:" + S.NAME, new Item(S.NAME, S.DESCRIPTION, S.INSERT_TEXT, S));
            }
         }
         for (String K : KEYWORDS) {
            if (MATCHES(K, LOW) && !K.equals(PREFIX)) {
               ITEMS.putIfAbsent("k:" + K, new Item(K, "", K, null));
            }
         }
         Matcher WM = Pattern.compile("[A-Za-z_]\\w{2,}").matcher(TEXT);
         while (WM.find() && ITEMS.size() < MAX_ITEMS * 2) {
            if (WM.start() != START && MATCHES(WM.group(), LOW) && !WM.group().equals(PREFIX)) {
               ITEMS.putIfAbsent("w:" + WM.group(), new Item(WM.group(), "", WM.group(), null));
            }
         }
      }

      int FOUND = 0;
      for (Entry E : ModApiIndex.ENTRIES()) {
         if (FOUND >= MAX_ITEMS * 3) {
            break;
         }
         if (OWNER != null && !OWNER.equals(E.OWNER) && !(E.OWNER.isEmpty() && OWNER.equals(LAST_NAMESPACE(E.NAMESPACE)))) {
            continue;
         }
         if (!MATCHES(E.NAME, LOW)) {
            continue;
         }
         String KEY = "e:" + E.KIND + E.QUALIFIED_OWNER() + "::" + E.NAME + "(" + E.PARAMS + ")";
         if (ITEMS.putIfAbsent(KEY, new Item(E.NAME, E.SIGNATURE() + "   [" + E.LOCATION() + "]", E.NAME, E)) == null) {
            FOUND++;
         }
      }

      List<Item> SORTED = new ArrayList<>(ITEMS.values());
      SORTED.sort((A, B) -> {
         int D = Integer.compare(RANK(A, LOW), RANK(B, LOW));
         return D != 0 ? D : A.NAME.compareToIgnoreCase(B.NAME);
      });
      if (SORTED.size() > MAX_ITEMS) {
         SORTED = new ArrayList<>(SORTED.subList(0, MAX_ITEMS));
      }
      if (SORTED.isEmpty() || (SORTED.size() == 1 && SORTED.get(0).NAME.equals(PREFIX))) {
         POPUP.hide();
         return;
      }

      LIST.getItems().setAll(SORTED);
      LIST.getSelectionModel().select(0);
      LIST.scrollTo(0);
      LIST.setPrefHeight(Math.min(10, SORTED.size()) * 24 + 4);
      SHOW();
   }

   private static String LAST_NAMESPACE(String NS) {
      int I = NS.lastIndexOf("::");
      return I < 0 ? NS : NS.substring(I + 2);
   }

   private static boolean MATCHES(String NAME, String LOW) {
      return LOW.isEmpty() || NAME.toLowerCase(Locale.ROOT).startsWith(LOW);
   }

   private static int RANK(Item ITEM, String LOW) {
      int BASE = 1;
      if (ITEM.ENTRY != null) {
         BASE = ITEM.ENTRY.KIND == ModApiIndex.Kind.SNIPPET ? 0 : ITEM.ENTRY.KIND == ModApiIndex.Kind.FUNCTION ? 2 : ITEM.ENTRY.KIND == ModApiIndex.Kind.CLASS ? 3 : 4;
      }
      return ITEM.NAME.equalsIgnoreCase(LOW) ? BASE - 1 : BASE;
   }

   private void SHOW() {
      Optional<Bounds> CARET = AREA.getCaretBounds();
      if (!CARET.isPresent() || AREA.getScene() == null || AREA.getScene().getWindow() == null) {
         return;
      }
      if (!STYLES_COPIED) {
         LIST.getStylesheets().addAll(AREA.getScene().getStylesheets());
         STYLES_COPIED = true;
      }
      Bounds B = CARET.get();
      if (POPUP.isShowing()) {
         POPUP.setX(B.getMinX());
         POPUP.setY(B.getMaxY() + 2);
      } else {
         POPUP.show(AREA.getScene().getWindow(), B.getMinX(), B.getMaxY() + 2);
      }
   }

   private void ACCEPT() {
      Item ITEM = LIST.getSelectionModel().getSelectedItem();
      POPUP.hide();
      if (ITEM == null) {
         return;
      }
      int START = PREFIX_START();
      String TEXT = ITEM.INSERT.replace("${", "").replace("}", "");
      int MARK = TEXT.indexOf("$0");
      AREA.replaceText(START, AREA.getCaretPosition(), TEXT.replace("$0", ""));
      if (MARK >= 0) {
         AREA.moveTo(START + MARK);
      }
      AREA.requestFollowCaret();
   }

   // definition goes above AttachDetours, the attach line inside it
   public static boolean INSERT_DETOUR(TextEditor EDITOR, Entry ENTRY) {
      if (EDITOR == null || !EDITOR.getCodeArea().isEditable()) {
         return false;
      }
      CodeArea AREA = EDITOR.getCodeArea();
      String TEXT = AREA.getText();
      String DEFINITION = ENTRY.DETOUR_DEFINITION() + "\n";
      String ATTACH = ENTRY.DETOUR_ATTACH();

      Matcher M = Pattern.compile("(?m)^\\s*void\\s+AttachDetours\\s*\\(\\s*\\)\\s*\\{?").matcher(TEXT);
      if (M.find() && !TEXT.contains(ATTACH.split(" //")[0])) {
         int BODY = TEXT.indexOf('{', M.start());
         if (BODY >= 0) {
            int NL = TEXT.indexOf('\n', BODY);
            AREA.insertText(NL < 0 ? BODY + 1 : NL + 1, "\t" + ATTACH + "\n");
            AREA.insertText(M.start(), DEFINITION);
            AREA.moveTo(M.start());
            AREA.requestFollowCaret();
            return true;
         }
      }
      AREA.replaceSelection(DEFINITION + "// in AttachDetours():\n// " + ATTACH + "\n");
      return true;
   }
}
