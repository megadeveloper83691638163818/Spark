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
package sporemodder.view.ribbons.util;

import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import sporemodder.DllModActions;
import sporemodder.view.Controller;

/** The group of the DLL ribbon tab, with the actions of a DLL mod. Creating a mod is in the program menu. */
public class DllModUI implements Controller {

	@FXML
	private Node mainNode;
	@FXML
	private Button btnBuild;
	@FXML
	private Button btnOpenFolder;
	@FXML
	private Button btnModInfo;
	@FXML
	private Button btnSdkPaths;

	@Override
	public Node getMainNode() {
		return mainNode;
	}

	@FXML
	protected void initialize() {
		btnBuild.setOnAction(e -> DllModActions.buildActive());
		btnOpenFolder.setOnAction(e -> DllModActions.openActiveFolder());
		btnModInfo.setOnAction(e -> DllModActions.editModInfo());
		btnSdkPaths.setOnAction(e -> DllModActions.chooseSdkPaths());
	}
}
