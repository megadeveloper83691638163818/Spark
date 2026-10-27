package sporemodder.view;

import io.github.emd4600.javafxribbon.Ribbon;
import io.github.emd4600.javafxribbon.RibbonGroup;
import io.github.emd4600.javafxribbon.RibbonTab;
import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.function.Consumer;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Control;
import javafx.scene.control.Labeled;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextInputControl;
import javafx.scene.control.TitledPane;
import javafx.scene.control.Tooltip;
import sporemodder.LanguageManager;

public class SceneTranslator {
   private static final Map<Object, Map<String, SceneTranslator.Texts>> remembered = new WeakHashMap<>();
   private static Ribbon ribbon;

   private SceneTranslator() {
   }

   public static void setRibbon(Ribbon var0) {
      ribbon = var0;
      if (var0 != null) {
         for (RibbonTab var1 : var0.getTabs()) {
            hookRibbonTab(var1);
         }

         var0.getTabs().addListener((javafx.collections.ListChangeListener<RibbonTab>)var0x -> {
            while (var0x.next()) {
               if (var0x.wasAdded()) {
                  for (RibbonTab var2 : var0x.getAddedSubList()) {
                     hookRibbonTab(var2);
                  }
               }
            }
         });
      }
   }

   private static javafx.animation.AnimationTimer live;

   /** Re-checks all open windows a few times per second so texts set or created later are translated too. */
   public static void startLive() {
      if (live == null) {
         live = new javafx.animation.AnimationTimer() {
            private long last;

            public void handle(long var1) {
               if (var1 - this.last >= 400000000L) {
                  this.last = var1;

                  for (javafx.stage.Window var4 : new java.util.ArrayList<>(javafx.stage.Window.getWindows())) {
                     if (var4.isShowing() && var4.getScene() != null) {
                        SceneTranslator.translate(var4.getScene().getRoot());
                     }
                  }
               }
            }
         };
         live.start();
      }
   }

   private static final Map<Object, Boolean> hooked = new WeakHashMap<>();

   private static void hookRibbonTab(RibbonTab var0) {
      if (hooked.put(var0, Boolean.TRUE) == null) {
         var0.getGroups().addListener((javafx.collections.ListChangeListener<RibbonGroup>)var1 -> translateRibbonTab(var0));
      }

      translateRibbonTab(var0);
   }

   private static void translateRibbonTab(RibbonTab var0) {
      update(var0, "text", var0.getText(), var0::setText);
      translateTooltip(var0.getTooltip());

      for (RibbonGroup var2 : var0.getGroups()) {
         translate(var2);

         for (Node var4 : var2.getNodes()) {
            translate(var4);
         }
      }
   }

   /** Name shown for virtual project items (Effect Editor, Animation Editor...); real files keep their names. */
   public static boolean isTranslatableItem(Object var0) {
      return var0 instanceof sporemodder.view.editors.EffectEditorItem
         || var0 instanceof sporemodder.view.editors.AnimEditorItem
         || var0 instanceof sporemodder.util.ProjectNamesItem;
   }

   public static String itemName(sporemodder.util.ProjectItem var0) {
      return isTranslatableItem(var0) ? LanguageManager.tr(var0.getName()) : var0.getName();
   }

   public static void translate(Node var0) {
      if (var0 != null) {
         if (var0 instanceof Labeled) {
            Labeled var1 = (Labeled)var0;
            update(var1, "text", var1.getText(), var1::setText);
         }

         if (var0 instanceof TextInputControl) {
            TextInputControl var3 = (TextInputControl)var0;
            update(var3, "prompt", var3.getPromptText(), var3::setPromptText);
         }

         if (var0 instanceof Control) {
            Control var4 = (Control)var0;
            translateTooltip(var4.getTooltip());
            if (var4.getContextMenu() != null) {
               var4.getContextMenu().getItems().forEach(SceneTranslator::translateMenuItem);
            }
         }

         if (var0 instanceof MenuButton) {
            ((MenuButton)var0).getItems().forEach(SceneTranslator::translateMenuItem);
         }

         if (var0 instanceof TitledPane) {
            TitledPane var5 = (TitledPane)var0;
            update(var5, "text", var5.getText(), var5::setText);
            translate(var5.getContent());
         }

         if (var0 instanceof TabPane) {
            TabPane var9 = (TabPane)var0;
            if (hooked.put(var9, Boolean.TRUE) == null) {
               var9.getTabs().addListener((javafx.collections.ListChangeListener<Tab>)var1x -> {
                  while (var1x.next()) {
                     if (var1x.wasAdded()) {
                        for (Tab var3 : var1x.getAddedSubList()) {
                           translateTab(var3);
                        }
                     }
                  }
               });
            }

            for (Tab var2 : var9.getTabs()) {
               translateTab(var2);
            }
         }

         if (var0 instanceof Parent) {
            for (Node var8 : ((Parent)var0).getChildrenUnmodifiable()) {
               translate(var8);
            }
         }
      }
   }

   public static void translateRibbon() {
      if (ribbon != null) {
         for (RibbonTab var1 : ribbon.getTabs()) {
            hookRibbonTab(var1);
         }
      }
   }

   private static void translateTab(Tab var0) {
      boolean var1 = var0 instanceof sporemodder.view.EditorPaneUI.EditorTab
         ? isTranslatableItem(((sporemodder.view.EditorPaneUI.EditorTab)var0).item)
         : true;
      if (var1) {
         update(var0, "text", var0.getText(), var0::setText);
         if (hooked.put(var0.textProperty(), Boolean.TRUE) == null) {
            var0.textProperty().addListener((var1x, var2, var3) -> {
               if (isTranslatableTab(var0)) {
                  update(var0, "text", var0.getText(), var0::setText);
               }
            });
         }
      }

      translate(var0.getContent());
   }

   private static boolean isTranslatableTab(Tab var0) {
      return !(var0 instanceof sporemodder.view.EditorPaneUI.EditorTab) || isTranslatableItem(((sporemodder.view.EditorPaneUI.EditorTab)var0).item);
   }

   private static void translateTooltip(Tooltip var0) {
      if (var0 != null) {
         update(var0, "text", var0.getText(), var0::setText);
      }
   }

   private static void translateMenuItem(MenuItem var0) {
      update(var0, "text", var0.getText(), var0::setText);
      if (var0 instanceof Menu) {
         ((Menu)var0).getItems().forEach(SceneTranslator::translateMenuItem);
      }
   }

   private static void update(Object var0, String var1, String var2, Consumer<String> var3) {
      if (var2 != null && !var2.isEmpty()) {
         Map<String, SceneTranslator.Texts> var4 = remembered.computeIfAbsent(var0, var0x -> new HashMap<>());
         SceneTranslator.Texts var5 = var4.computeIfAbsent(var1, var0x -> new SceneTranslator.Texts());
         if (var5.original == null || !var2.equals(var5.applied)) {
            var5.original = var2;
         }

         String var6 = LanguageManager.tr(var5.original);
         var5.applied = var6;
         if (!var6.equals(var2)) {
            var3.accept(var6);
         }
      }
   }

   private static class Texts {
      String original;
      String applied;
   }
}
