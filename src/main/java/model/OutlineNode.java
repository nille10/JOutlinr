package model;

import javax.swing.tree.DefaultMutableTreeNode;

/**
 * Eine {@code OutlineNode} repräsentiert einen Knoten in der Outliner-Baumstruktur.
 *
 * <p>
 *  Die klasse erweitert {@link DefaultMutableTreeNode} um Eigenschaften wie Titel und Inhalt speichern zu können.
 *  Änderungen an der {@code OutlineNode} werden automatisch synchronisiert.
 * </p>
 */
public class OutlineNode extends DefaultMutableTreeNode {
  private String title;
  private String content;

  /**
   * Erstellt eine neue {@code OutlineNode} mit angegebenen Titel und leerem Textfeld.
   * @param title der Titel des Knotens
   */
  public OutlineNode(String title) {
    super(title);
    this.title = title;
    this.content = "";
  }

  /**
   * Liefert den Titel des Knotens zurück.
   *
   * @return den Titel als {@code String}
   */
  public String getTitle() {
    return title;
  }

  /**
   * Setzt den Titel des Knotens und aktualisiert das {@code UserObject}.
   *
   * @param title der neue Titel für den Knoten
   */
  public void setTitle(String title) {
    this.title = title;
    setUserObject(title);
  }

  /**
   * Aktualisiert das Benutzerelement ({@code userObject}) des Knotens und synchronisiert den titel.
   *
   * @param userObject das neue Objekt für den Knoten.
   */
  @Override
  public void setUserObject(Object userObject) {
    super.setUserObject(userObject);
    this.title = userObject != null ? userObject.toString() : "";
  }

  /**
   * Liefert den Inhalt des Textfensters zurück
   * @return Inhalt des Textfensters als {@code String}
   */
  public String getContent() {
    return content;
  }

  /**
   * Setzt den Inhalt des Textfensters auf den angegebenen content
   * @param content Inhalt des Textfensters
   */
  public void setContent(String content) {
    this.content = content;
  }

  /**
   * Liefert den Titel von dem aktuellen Knoten zurück.
   * @return Knotentitel als {@code String}
   */
  @Override
  public String toString() {
    return title;
  }

}