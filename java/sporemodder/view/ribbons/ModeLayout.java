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

import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import io.github.emd4600.javafxribbon.Ribbon;
import io.github.emd4600.javafxribbon.RibbonGroup;
import io.github.emd4600.javafxribbon.RibbonTab;
import sporemodder.WorkMode;

/**
 * Decides which ribbon tabs and groups are shown in each work mode. Every tab (and optionally group) is registered
 * with the set of modes it is visible in, so adding a mode needs no change here.
 */
public class ModeLayout {

	private final Ribbon ribbon;
	/** Visible modes of each tab, in ribbon order. */
	private final Map<RibbonTab, Set<WorkMode>> tabModes = new LinkedHashMap<>();
	private final Map<RibbonTab, List<RibbonGroup>> originalGroups = new HashMap<>();
	private final Map<RibbonGroup, Set<WorkMode>> groupModes = new HashMap<>();

	public ModeLayout(Ribbon ribbon) {
		this.ribbon = ribbon;
	}

	/** Registers a tab, visible only in the given modes (pass {@link EnumSet#allOf} for all). */
	public void addTab(RibbonTab tab, Set<WorkMode> modes) {
		tabModes.put(tab, EnumSet.copyOf(modes));
		originalGroups.put(tab, new ArrayList<>(tab.getGroups()));
	}

	/** Groups that are only shown in the given modes; groups that are never restricted are shown in all of them. */
	public void restrictGroups(Set<WorkMode> modes, RibbonGroup... groups) {
		for (RibbonGroup group : groups) groupModes.put(group, EnumSet.copyOf(modes));
	}

	public void apply(WorkMode mode) {
		RibbonTab selected = ribbon.getSelectionModel().getSelectedItem();

		List<RibbonTab> visibleTabs = new ArrayList<>();
		for (Map.Entry<RibbonTab, Set<WorkMode>> entry : tabModes.entrySet()) {
			if (entry.getValue().contains(mode)) visibleTabs.add(entry.getKey());
		}
		ribbon.getTabs().setAll(visibleTabs);

		for (RibbonTab tab : visibleTabs) {
			tab.getGroups().setAll(visibleGroups(tab, mode));
		}

		// If the selected tab disappeared, go to the first one
		if (!visibleTabs.isEmpty()) {
			ribbon.getSelectionModel().select(visibleTabs.contains(selected) ? selected : visibleTabs.get(0));
		}
	}

	private Collection<RibbonGroup> visibleGroups(RibbonTab tab, WorkMode mode) {
		List<RibbonGroup> result = new ArrayList<>();
		for (RibbonGroup group : originalGroups.get(tab)) {
			Set<WorkMode> modes = groupModes.get(group);
			if (modes == null || modes.contains(mode)) result.add(group);
		}
		return result;
	}
}
