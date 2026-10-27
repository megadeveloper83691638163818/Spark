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
package sporemodder.view.ribbons;

import io.github.emd4600.javafxribbon.Ribbon;
import io.github.emd4600.javafxribbon.RibbonGroup;
import io.github.emd4600.javafxribbon.RibbonTab;
import sporemodder.UIManager;
import sporemodder.WorkMode;
import sporemodder.view.ribbons.util.DllModUI;

/** The ribbon tab with the actions of {@link WorkMode#MODE_2}. It is only shown in that mode (see {@link ModeLayout}). */
public class DllRibbonTab extends RibbonTabController {

	public static final String ID = "DLL";

	private DllModUI dllMod;

	/** Creates the tab but does not insert it in the ribbon; {@link ModeLayout} decides when it is shown. */
	public void addTab(Ribbon ribbon) {
		tab = new RibbonTab("DLL Mod");

		dllMod = UIManager.get().loadUI("ribbons/util/DllModUI");
		tab.getGroups().add((RibbonGroup) dllMod.getMainNode());
	}

	public DllModUI getDllModUI() {
		return dllMod;
	}
}
