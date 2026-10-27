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

package sporemodder.view;

import sporemodder.DllModActions;
import sporemodder.ModeManager;
import sporemodder.WorkMode;
import sporemodder.view.dialogs.CreateProjectSimpleUI;
import sporemodder.view.dialogs.UnpackPackageUI;

/**
 * What the main buttons do in the active work mode. The menu and the intro screen call this, so the choice between
 * the regular and the mode-specific action is made in one place.
 */
public final class ModeActions {

	private ModeActions() {
	}

	public static void newProject() {
		if (ModeManager.get().is(WorkMode.MODE_2)) DllModActions.newProject();
		else CreateProjectSimpleUI.show();
	}

	public static void unpackMod() {
		if (ModeManager.get().is(WorkMode.MODE_2)) DllModActions.unpackSporemod();
		else UnpackPackageUI.showChooser();
	}
}
