package sporemodder;

/**
 * An interface language: the id of its dictionary file (<code>en</code>, <code>ru</code>, ...) and the name shown
 * in the language list. Languages are discovered by {@link LanguageManager}; adding one needs no code change.
 */
public final class Language {

	/** The language of the texts in the code; it needs no dictionary. */
	public static final Language ENGLISH = new Language("en", "English");

	private final String id;
	private final String displayName;

	Language(String id, String displayName) {
		this.id = id;
		this.displayName = displayName;
	}

	public String getId() {
		return id;
	}

	public String getDisplayName() {
		return displayName;
	}

	/** Returns the available language with that id, or {@link #ENGLISH} if there is none. */
	public static Language fromId(String id) {
		return LanguageManager.get().findLanguage(id);
	}

	@Override
	public String toString() {
		return displayName;
	}
}
