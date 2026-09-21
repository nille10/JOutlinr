package view.columns;

/**
 * Die verfügbaren Typen von Zusatzspalten neben dem Baum.
 *
 * <p>Jeder Typ besitzt einen Anzeigenamen und eine kurze Beschreibung, die etwa in
 * Auswahldialogen verwendet werden können. Die Aufzählung ist erweiterbar: Neue
 * Spaltentypen können als weitere Konstanten hinzugefügt werden.</p>
 */
public enum ColumnType {

  /** Spalte mit einem einfachen Häkchen (Checkbox) pro Knoten. */
  CHECKBOX("Checkbox", "Einfaches Häkchen pro Knoten"),

  /** Spalte mit einem einzeiligen Textfeld für eine kurze Notiz pro Knoten. */
  TEXTFIELD("Textfeld", "Kurze Notiz / einzeiligen Text pro Knoten");

  /** Der für Benutzer sichtbare Name des Spaltentyps. */
  private final String displayName;

  /** Eine kurze Beschreibung des Spaltentyps. */
  private final String description;

  /**
   * Erstellt einen Spaltentyp mit Anzeigenamen und Beschreibung.
   *
   * @param displayName der für Benutzer sichtbare Name
   * @param description eine kurze Beschreibung des Typs
   */
  ColumnType(String displayName, String description) {
    this.displayName = displayName;
    this.description = description;
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
   * Liefert den Anzeigenamen als Textdarstellung, sodass der Typ direkt in
   * Swing-Komponenten sinnvoll angezeigt wird.
   *
   * @return der Anzeigename
   */
  @Override
  public String toString() { return displayName; }
}