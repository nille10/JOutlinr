package view.columns;

//erweiterbar
public enum ColumnType {

  CHECKBOX("Checkbox", "Einfaches Häkchen pro Knoten"),
  TEXTFIELD("Textfeld", "Kurze Notiz / einzeiligen Text pro Knoten");

  private final String displayName;
  private final String description;

  ColumnType(String displayName, String description) {
    this.displayName = displayName;
    this.description = description;
  }

  public String getDisplayName() { return displayName; }
  public String getDescription() { return description; }

  @Override
  public String toString() { return displayName; }
}