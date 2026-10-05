package model.columns;

/**
 * Die verfügbaren Typen von Zusatzspalten neben dem Baum.
 *
 * <p>Jeder Typ besitzt einen Anzeigenamen, eine kurze Beschreibung und einen
 * Standardwert, der verwendet wird, solange für einen Knoten noch kein eigener
 * Wert gesetzt wurde. Die Aufzählung ist erweiterbar: Neue Spaltentypen können
 * als weitere Konstanten hinzugefügt werden.</p>
 */
public enum ColumnType {

    /** Spalte mit einem einfachen Häkchen (Checkbox) pro Knoten. */
    CHECKBOX("Checkbox", "Einfaches Häkchen pro Knoten", Boolean.FALSE),

    /** Spalte mit einem einzeiligen Textfeld für eine kurze Notiz pro Knoten. */
    TEXTFIELD("Textfeld", "Kurze Notiz / einzeiligen Text pro Knoten", "");

    /** Der für Benutzer sichtbare Name des Spaltentyps. */
    private final String displayName;

    /** Eine kurze Beschreibung des Spaltentyps. */
    private final String description;

    /** Der Wert, der gilt, solange für einen Knoten noch kein eigener Wert gesetzt wurde. */
    private final Object defaultValue;

    /**
     * Erstellt einen Spaltentyp mit Anzeigenamen, Beschreibung und Standardwert.
     *
     * @param displayName  der für Benutzer sichtbare Name
     * @param description  eine kurze Beschreibung des Typs
     * @param defaultValue der Standardwert für Knoten ohne eigenen Wert
     */
    ColumnType(String displayName, String description, Object defaultValue) {
        this.displayName = displayName;
        this.description = description;
        this.defaultValue = defaultValue;
    }

    /**
     * Liefert den für Benutzer sichtbaren Namen des Spaltentyps.
     *
     * @return der Anzeigename
     */
    public String getDisplayName() { return displayName; }

    /**
     * Liefert die Beschreibung des Spaltentyps.
     *
     * @return die Beschreibung
     */
    public String getDescription() { return description; }

    /**
     * Liefert den Standardwert dieses Typs.
     *
     * <p>Wird von {@link Column#getValue} zurückgegeben, solange für einen Knoten
     * noch kein eigener Wert gesetzt wurde, z. B. {@link Boolean#FALSE} bei
     * {@link #CHECKBOX} oder ein leerer String bei {@link #TEXTFIELD}.</p>
     *
     * @return der Standardwert, niemals {@code null}
     */
    public Object getDefaultValue() { return defaultValue; }

    /**
     * Liefert den Anzeigenamen als Textdarstellung, sodass der Typ direkt in
     * Swing-Komponenten wie {@link javax.swing.JComboBox} sinnvoll angezeigt wird.
     *
     * @return der Anzeigename
     */
    @Override
    public String toString() { return displayName; }
}