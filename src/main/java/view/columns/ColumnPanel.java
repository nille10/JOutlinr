/*
  KI generiert
*/
package view.columns;

import model.OutlineNode;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Container für alle Zusatzspalten rechts neben dem JTree.
 *
 * <p>Die Spalten werden horizontal (X-Achse) nebeneinander angeordnet. Das Panel
 * hält die aktuellen Knoten und die Zeilenhöhe des Baums vor, damit neu hinzugefügte
 * Spalten sofort mit den passenden Zeilen aufgebaut werden können. Die Gesamtbreite
 * des Panels entspricht stets der Summe der Spaltenbreiten.</p>
 */
public class ColumnPanel extends JPanel {

  /** Alle aktuell im Panel enthaltenen Spalten in ihrer Anzeigereihenfolge (von links nach rechts). */
  private final List<OutlineColumn> columns = new ArrayList<>();

  /** Die zuletzt per {@link #syncWithTree(OutlineNode[], int)} übergebenen Knoten des Baums. */
  private OutlineNode[] currentNodes  = new OutlineNode[0];

  /** Die zuletzt übergebene Zeilenhöhe des Baums in Pixeln (Standardwert: 24). */
  private int currentRowHeight        = 24;

  /**
   * Der gemeinsame {@link RowHoverManager} für alle Spalten.
   * Wird von OutlinePanel gesetzt und an jede neue Spalte weitergegeben.
   */
  private RowHoverManager hoverManager;


  /**
   * Erstellt ein neues, zunächst leeres Spaltenpanel.
   *
   * <p>Verwendet ein {@link BoxLayout} entlang der X-Achse und ist transparent,
   * sodass der Hintergrund des umgebenden Containers durchscheint.</p>
   */
  public ColumnPanel() {

    setLayout(new BoxLayout(this, BoxLayout.X_AXIS));

    setOpaque(false);
  }


  /**
   * Setzt den {@link RowHoverManager} für alle aktuellen und zukünftigen Spalten.
   *
   * <p>Wird von OutlinePanel einmalig im Konstruktor aufgerufen. Bereits vorhandene
   * Spalten werden ebenfalls angeschlossen; neue Spalten erhalten den Manager
   * beim Hinzufügen über {@link #addColumn(String, ColumnType)}.</p>
   *
   * @param manager der gemeinsam zu verwendende Hover-Manager
   */
  public void setHoverManager(RowHoverManager manager) {
    this.hoverManager = manager;
    for (OutlineColumn col : columns) {
      col.setHoverManager(manager);
    }
  }


  /**
   * Callback, der aufgerufen wird, wenn sich Daten in einer der Spalten ändern.
   * Wird an jede neu hinzugefügte Spalte weitergegeben.
   */
  private Runnable onDataChanged;

  /**
   * Setzt den Callback, der bei Datenänderungen in den Spalten aufgerufen wird.
   *
   * <p>Der Callback wird nur an Spalten weitergegeben, die <em>nach</em> diesem
   * Aufruf über {@link #addColumn(String, ColumnType)} hinzugefügt werden.
   * Bereits vorhandene Spalten behalten den zuvor gesetzten Callback.</p>
   *
   * @param callback der auszuführende Callback, oder {@code null}
   */
  public void setOnDataChanged(Runnable callback) {
    this.onDataChanged = callback;
  }

  /**
   * Fügt eine neue Spalte am rechten Ende des Panels hinzu.
   *
   * <p>Die Spalte erhält den Daten-Callback und den Hover-Manager dieses Panels,
   * wird mit den aktuellen Knoten und der aktuellen Zeilenhöhe aufgebaut und das
   * Panel wird anschließend neu berechnet und gezeichnet.</p>
   *
   * @param title der Titel der neuen Spalte
   * @param type  der Typ der neuen Spalte
   * @return die neu erstellte und hinzugefügte Spalte
   */
  public OutlineColumn addColumn(String title, ColumnType type) {

    OutlineColumn column = new OutlineColumn(title, type);
    column.setOnDataChanged(onDataChanged);
    column.setHoverManager(hoverManager);

    columns.add(column);
    add(column);

    column.rebuildRows(currentNodes, currentRowHeight, 0);

    updatePreferredWidth();
    revalidate();
    repaint();

    return column;
  }

  /**
   * Entfernt die angegebene Spalte aus dem Panel.
   *
   * <p>Ist die Spalte im Panel enthalten, wird ihr Hover-Manager gelöst, sie wird aus
   * dem Layout entfernt und die Gesamtbreite neu berechnet. Ist sie nicht enthalten,
   * geschieht nichts.</p>
   *
   * @param column die zu entfernende Spalte
   */
  public void removeColumn(OutlineColumn column) {

    if (columns.remove(column)) {
      column.setHoverManager(null);
      remove(column);
      updatePreferredWidth();
      revalidate();
      repaint();
    }
  }


  /**
   * Setzt {@code preferredSize}, {@code minimumSize} und {@code maximumSize} auf die
   * Summe aller Spaltenbreiten.
   *
   * <p>Die Höhe bleibt dabei flexibel: Sie ist 0 für die bevorzugte und minimale Größe
   * und {@link Integer#MAX_VALUE} für die maximale Größe.</p>
   */
  private void updatePreferredWidth() {
    int totalW = columns.stream().mapToInt(OutlineColumn::getColumnWidth).sum();
    setPreferredSize(new Dimension(totalW, 0));
    setMaximumSize(new Dimension(totalW, Integer.MAX_VALUE));
    setMinimumSize(new Dimension(totalW, 0));
  }


  /**
   * Synchronisiert alle Spalten mit dem aktuellen Zustand des Baums.
   *
   * <p>Die übergebenen Knoten und die Zeilenhöhe werden gespeichert, damit später
   * hinzugefügte Spalten ebenfalls damit aufgebaut werden können. Anschließend
   * werden die Zeilen aller vorhandenen Spalten neu aufgebaut.</p>
   *
   * @param nodes     die aktuell sichtbaren Knoten des Baums in Zeilenreihenfolge
   * @param rowHeight die Zeilenhöhe des Baums in Pixeln
   */
  public void syncWithTree(OutlineNode[] nodes, int rowHeight) {

    currentNodes     = nodes;
    currentRowHeight = rowHeight;

    for (OutlineColumn col : columns) {
      col.rebuildRows(nodes, rowHeight, 0);
    }

    revalidate();
    repaint();
  }


  /**
   * Gibt an, ob mindestens eine Spalte vorhanden ist.
   *
   * @return {@code true}, wenn das Panel Spalten enthält, sonst {@code false}
   */
  public boolean hasColumns() {
    return !columns.isEmpty();
  }

  /**
   * Liefert die Anzahl der vorhandenen Spalten.
   *
   * @return die Anzahl der Spalten
   */
  public int getColumnCount() {
    return columns.size();
  }

  /**
   * Liefert eine schreibgeschützte Sicht auf die vorhandenen Spalten.
   *
   * <p>Die Liste spiegelt spätere Änderungen wider, kann aber vom Aufrufer nicht
   * verändert werden ({@link UnsupportedOperationException}).</p>
   *
   * @return unveränderbare Liste der Spalten in Anzeigereihenfolge
   */
  public List<OutlineColumn> getColumns() {
    return Collections.unmodifiableList(columns);
  }
}