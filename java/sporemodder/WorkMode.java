package sporemodder;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;

import sporemodder.util.Project;

/**
 * The working modes of the program. {@link #STANDARD} is the original Spark behaviour, {@link #MODE_2} is for creating
 * ModAPI DLL mods and {@link #DECOMP} ("DecompMode") is still a stub.
 * <p>
 * What changes between modes is described here instead of with <code>if (mode == ...)</code> in the interface:
 * the project filter, {@link #text(String)} for labels that are worded differently, and the sets
 * {@link #REGULAR_TOOLS} / {@link #DLL_TOOLS} that tabs, groups and menu items are registered with.
 * All texts are English; they are translated through {@link LanguageManager}.
 */
public enum WorkMode {
	STANDARD("standard", "Standard", "Regular Spark behaviour.", project -> true, Map.of()),
	MODE_2("mode2", "Mode 2: DLL mods", "Easy creation of Spore ModAPI DLL mods.", DllModActions::isDllProject, Map.of(
			"New project", "New DLL mod",
			"Creates a new empty project to start modding.", "Creates a new Spore ModAPI DLL mod.",
			"Open project...", "Open DLL mod...",
			"Open project", "Open DLL mod",
			"Unpack mod", "Unpack .sporemod",
			"Unpacks the contents of a .package file into a new project.", "Unpacks .sporemod files into new DLL mods.")),
	DECOMP("decomp", "DecompMode", "Decompilation work. Not implemented yet.", project -> true, Map.of());

	/** Modes that use the regular Spark tools (everything but the DLL mod tools). */
	public static final Set<WorkMode> REGULAR_TOOLS = Collections.unmodifiableSet(EnumSet.of(STANDARD, DECOMP));
	/** Modes that use the DLL mod tools. */
	public static final Set<WorkMode> DLL_TOOLS = Collections.unmodifiableSet(EnumSet.of(MODE_2));

	private final String id;
	private final String displayName;
	private final String description;
	private final Predicate<Project> projectFilter;
	private final Map<String, String> wording;

	WorkMode(String id, String displayName, String description, Predicate<Project> projectFilter, Map<String, String> wording) {
		this.id = id;
		this.displayName = displayName;
		this.description = description;
		this.projectFilter = projectFilter;
		this.wording = wording;
	}

	public String getId() { return id; }
	public String getDisplayName() { return displayName; }
	public String getDescription() { return description; }

	/** The English text as this mode words it (for example "New project" is "New DLL mod" in {@link #MODE_2}). */
	public String text(String english) {
		return wording.getOrDefault(english, english);
	}

	public boolean acceptsProject(Project project) {
		return projectFilter.test(project);
	}

	/** The projects that are shown in this mode. */
	public List<Project> filterProjects(Collection<Project> projects) {
		List<Project> result = new ArrayList<>();
		for (Project project : projects) {
			if (acceptsProject(project)) result.add(project);
		}
		return result;
	}

	public static WorkMode fromId(String id) {
		if ("mode3".equals(id)) return DECOMP;  // id used before this mode was named
		for (WorkMode mode : values()) {
			if (mode.id.equals(id)) return mode;
		}
		return STANDARD;
	}

	@Override
	public String toString() { return displayName; }
}
