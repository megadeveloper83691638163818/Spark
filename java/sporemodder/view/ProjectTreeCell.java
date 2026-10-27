package sporemodder.view;

import java.awt.Toolkit;
import java.util.regex.Pattern;
import javafx.animation.PauseTransition;
import javafx.geometry.Point2D;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.util.Duration;
import sporemodder.EditorManager;
import sporemodder.ProjectManager;
import sporemodder.util.ProjectItem;

public class ProjectTreeCell extends TreeCell<ProjectItem> {
   private final TextField textField = new TextField();
   private Tooltip warningTooltip;
   private static final Pattern ILLEGAL_CHARACTERS = Pattern.compile("[\\s@*%;,\\(\\)'{}<>\\[\\]|\"^]");

   public ProjectTreeCell(boolean var1) {
      this.addEventFilter(MouseEvent.MOUSE_PRESSED, var2 -> {
         if (this.isSelected() && var2.getButton() == MouseButton.PRIMARY && !this.getDisclosureNode().getBoundsInParent().contains(var2.getX(), var2.getY())) {
            var2.consume();
         }

         if (var1 && var2.getButton() == MouseButton.SECONDARY && !this.isEmpty() && this.getItem() != null) {
            var2.consume();
         }
      });
      if (var1) {
         this.addEventHandler(MouseEvent.MOUSE_RELEASED, var1x -> {
            ProjectManager var2 = ProjectManager.get();
            if (var1x.getButton() == MouseButton.SECONDARY && !this.isEmpty() && this.getItem() != null) {
               var2.getContextMenu().hide();
               var2.generateContextMenu((ProjectItem)this.getItem());
               var2.getContextMenu().show(this, var1x.getScreenX(), var1x.getScreenY());
               var1x.consume();
            } else {
               var2.getContextMenu().hide();
            }
         });
      }

      this.setOnMouseClicked(var2 -> {
         if (!this.isEmpty() && var2.getButton() == MouseButton.PRIMARY && var2.getClickCount() == 2) {
            ProjectItem var3 = (ProjectItem)this.getItem();
            if (var3.isFolder()) {
               this.getTreeItem().setExpanded(!this.getTreeItem().isExpanded());
            } else if (var1) {
               EditorManager.get().moveFileToNewTab(var3);
            }

            var2.consume();
         }
      });
      this.textField.setOnAction(var1x -> {
         if (this.warningTooltip != null && this.warningTooltip.isShowing()) {
            this.warningTooltip.hide();
         }

         this.commitEdit((ProjectItem)this.getItem());
         var1x.consume();
      });
      this.textField.textProperty().addListener((var1x, var2, var3) -> {
         if (this.warningTooltip != null && this.warningTooltip.isShowing()) {
            this.warningTooltip.hide();
         }
      });
   }

   public void startEdit() {
      if (this.isEditable() && this.getTreeView().isEditable()) {
         super.startEdit();
         if (this.isEditing()) {
            this.textField.setText(this.getItem() == null ? "" : ((ProjectItem)this.getItem()).getName());
            this.setText(null);
            this.setGraphic(this.textField);
            this.textField.requestFocus();
            this.textField.selectRange(0, this.textField.getText().split("\\.", 2)[0].length());
         }
      }
   }

   public void cancelEdit() {
      super.cancelEdit();
      this.restoreItem((ProjectItem)this.getItem());
   }

   private boolean hasNonConvertibleExtension(ProjectItem var1, String var2) {
      return var1.getName().endsWith(var2) && var1.getName().substring(var1.getName().indexOf(46)).equals(var2);
   }

   public boolean hasAnyNonConvertibleExtension(ProjectItem var1) {
      return this.hasNonConvertibleExtension(var1, ".prop_t")
         || this.hasNonConvertibleExtension(var1, ".tsla_t")
         || this.hasNonConvertibleExtension(var1, ".pctp_t")
         || this.hasNonConvertibleExtension(var1, ".cnv_t")
         || this.hasNonConvertibleExtension(var1, ".animation_t")
         || this.hasNonConvertibleExtension(var1, ".gait_t")
         || this.hasNonConvertibleExtension(var1, ".globals_t")
         || this.hasNonConvertibleExtension(var1, ".world_t")
         || this.hasNonConvertibleExtension(var1, ".backgroundMap_t")
         || this.hasNonConvertibleExtension(var1, ".powers_t")
         || this.hasNonConvertibleExtension(var1, ".loottable_t")
         || this.hasNonConvertibleExtension(var1, ".cell_t")
         || this.hasNonConvertibleExtension(var1, ".randomcreature_t")
         || this.hasNonConvertibleExtension(var1, ".structure_t")
         || this.hasNonConvertibleExtension(var1, ".populate_t")
         || this.hasNonConvertibleExtension(var1, ".effectMap_t")
         || this.hasNonConvertibleExtension(var1, ".looktable_t")
         || this.hasNonConvertibleExtension(var1, ".arth_t")
         || this.hasNonConvertibleExtension(var1, ".summary_t")
         || this.hasNonConvertibleExtension(var1, ".pollen_metadata_t");
   }

   private void showWarning(String var1) {
      Point2D var2 = this.localToScreen(new Point2D(this.getWidth(), this.getHeight()));
      this.warningTooltip = new Tooltip(var1);
      this.warningTooltip.show(this, var2.getX(), var2.getY());
      Toolkit.getDefaultToolkit().beep();
      PauseTransition var3 = new PauseTransition(Duration.seconds(3.5));
      var3.setOnFinished(var1x -> this.warningTooltip.hide());
      var3.play();
   }

   public void commitEdit(ProjectItem var1) {
      String var2 = this.textField.getText();
      if (ILLEGAL_CHARACTERS.matcher(var2).find()) {
         this.showWarning("The name can only contain numbers, letters, -, _, ~,. and #.");
      } else {
         boolean var3 = false;

         for (TreeItem var5 : var1.getTreeItem().getParent().getChildren()) {
            if (var5 != this.getTreeItem() && ((ProjectItem)var5.getValue()).isMod() && var2.equals(((ProjectItem)var5.getValue()).getName())) {
               var3 = true;
               break;
            }
         }

         if (!var3) {
            var1.setName(this.textField.getText());
            super.commitEdit(var1);
         } else {
            this.showWarning("There's already a file with this name in the mod.");
         }
      }
   }

   public void updateItem(ProjectItem var1, boolean var2) {
      super.updateItem(var1, var2);
      if (var2 || var1 == null) {
         this.setText(null);
         this.setGraphic(null);
         this.getStyleClass().setAll(new String[]{"tree-cell", "project_item_not_mod", "project_item_not_source"});
      } else if (this.isEditing()) {
         this.textField.setText(var1.getName());
         this.setText(null);
         this.setGraphic(this.textField);
      } else {
         this.setText(sporemodder.view.SceneTranslator.itemName(var1));
         this.setGraphic(var1.getIcon());
         this.getStyleClass()
            .setAll(
               new String[]{
                  "tree-cell",
                  var1.isMod() ? "project_item_mod" : "project_item_not_mod",
                  var1.isSource() ? "project_item_source" : "project_item_not_source",
                  this.hasAnyNonConvertibleExtension(var1) ? "project_item_warning" : null
               }
            );
      }
   }

   private void restoreItem(ProjectItem var1) {
      if (var1 == null) {
         this.setText(null);
         this.setGraphic(null);
         this.getStyleClass().setAll(new String[]{"text-field-tree-cell", "project_item_not_mod", "project_item_not_source"});
      } else {
         this.setText(sporemodder.view.SceneTranslator.itemName(var1));
         this.setGraphic(var1.getIcon());
         this.getStyleClass()
            .setAll(
               new String[]{
                  "tree-cell",
                  var1.isMod() ? "project_item_mod" : "project_item_not_mod",
                  var1.isSource() ? "project_item_source" : "project_item_not_source",
                  this.hasAnyNonConvertibleExtension(var1) ? "project_item_warning" : null
               }
            );
      }
   }
}
