/****************************************************************************
* Copyright (C) 2018 Eric Mor
*
* This file is part of SporeModder FX.
*
* SporeModder FX is free software: you can redistribute it and/or modify
* it under the terms of the GNU General Public License as published by
* the Free Software Foundation, either version 3 of the License, or
* (at your option) any later version.
*
* This program is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU General Public License for more details.
*
* You should have received a copy of the GNU General Public License
* along with this program.  If not, see <http://www.gnu.org/licenses/>.
****************************************************************************/
package sporemodder.view.dialogs;

import javafx.geometry.Insets;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.VBox;
import sporemodder.ModeManager;
import sporemodder.UIManager;
import sporemodder.WorkMode;

/** Dialog (opened from the program menu) to choose the {@link WorkMode} of the program. */
public class WorkModeDialog {

	private WorkModeDialog() {
	}

	public static void show() {
		ToggleGroup group = new ToggleGroup();
		VBox content = new VBox(12);
		content.setPadding(new Insets(10));

		for (WorkMode mode : WorkMode.values()) {
			RadioButton radio = new RadioButton(mode.getDisplayName());
			radio.setToggleGroup(group);
			radio.setUserData(mode);
			radio.setSelected(ModeManager.get().is(mode));

			Label description = new Label(mode.getDescription());
			description.setWrapText(true);
			description.setPadding(new Insets(0, 0, 0, 24));
			description.setOpacity(0.7);

			content.getChildren().addAll(radio, description);
		}

		Dialog<ButtonType> dialog = new Dialog<>();
		dialog.setTitle("Work mode");
		dialog.setHeaderText("Choose how the program works");
		dialog.getDialogPane().setContent(content);
		dialog.getDialogPane().getButtonTypes().setAll(ButtonType.CANCEL, ButtonType.OK);

		UIManager.get().showDialog(dialog).ifPresent(result -> {
			if (result == ButtonType.OK && group.getSelectedToggle() != null) {
				ModeManager.get().setMode((WorkMode) group.getSelectedToggle().getUserData());
			}
		});
	}
}
