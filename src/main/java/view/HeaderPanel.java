package view;

import view.columns.ColumnHeader;
import view.columns.OutlineColumn;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Nicht scrollender Header-Bereich des Outliners.
 *
 * Layout: BorderLayout
 *
 *   CENTER = treeHeader   → nimmt den gesamten restlichen Platz
 *   EAST   = columnHeaders (JPanel, BoxLayout X_AXIS) → fix-breite Spalten
 *
 * Damit verhält sich der Tree-Header exakt wie der Tree selbst:
 * er füllt den Platz der übrig bleibt nachdem die Columns ihren
 * festen Platz eingenommen haben.
 *
 * updateTreeHeaderWidth() ist nicht mehr nötig und entfällt –
 * BorderLayout CENTER erledigt das automatisch.
 */
public class HeaderPanel extends JPanel {


  private final ColumnHeader treeHeader;

  /** Rechts-angedockter Container für alle Spalten-Header (fix-breit). */
  private final JPanel columnHeaders;

  private record ColumnEntry(ColumnHeader header, OutlineColumn column) {}
  private final List<ColumnEntry> entries = new ArrayList<>();

  private java.util.function.Consumer<OutlineColumn> onColumnRemove;



  public HeaderPanel() {

    setLayout(new BorderLayout());
    //setBackground(new Color(235, 235, 240));


    // ── Tree-Header (CENTER – nimmt restlichen Platz) ────────────────────
    treeHeader = new ColumnHeader("Outliner");
    // preferredSize Breite irrelevant – BorderLayout CENTER dehnt automatisch
    treeHeader.setPreferredSize(new Dimension(0, ColumnHeader.HEADER_HEIGHT));
    add(treeHeader, BorderLayout.CENTER);


    // ── Spalten-Header-Container (EAST – fix-breite Spalten) ─────────────
    columnHeaders = new JPanel();
    columnHeaders.setLayout(new BoxLayout(columnHeaders, BoxLayout.X_AXIS));
    columnHeaders.setOpaque(false);
    add(columnHeaders, BorderLayout.EAST);
  }



  // ── Callback ─────────────────────────────────────────────────────────────

  public void setOnColumnRemove(java.util.function.Consumer<OutlineColumn> callback) {
    this.onColumnRemove = callback;
  }



  // ── Spalten-Header verwalten ──────────────────────────────────────────────

  /**
   * Fügt einen neuen Spalten-Header hinzu.
   *
   * Der Header bekommt dieselbe feste Breite wie seine OutlineColumn
   * (column.getColumnWidth()) – dadurch fluchten Header und Zellen exakt.
   */
  public ColumnHeader addColumnHeader(String title, OutlineColumn column) {

    ColumnHeader header = new ColumnHeader(title);

    // Breite des Headers = Breite der Column (beide aus demselben computeWidth())
    int w = column.getColumnWidth();
    header.setPreferredSize(new Dimension(w, ColumnHeader.HEADER_HEIGHT));
    header.setMinimumSize(new Dimension(w, ColumnHeader.HEADER_HEIGHT));
    header.setMaximumSize(new Dimension(w, ColumnHeader.HEADER_HEIGHT));

    header.setOnRemove(() -> {
      removeEntry(header, column);
      if (onColumnRemove != null) {
        onColumnRemove.accept(column);
      }
    });

    header.setOnRename(newTitle -> column.setTitle(newTitle));

    entries.add(new ColumnEntry(header, column));
    columnHeaders.add(header);

    revalidate();
    repaint();

    return header;
  }


  private void removeEntry(ColumnHeader header, OutlineColumn column) {
    entries.removeIf(e -> e.header() == header);
    columnHeaders.remove(header);
    revalidate();
    repaint();
  }


  public void clearColumnHeaders() {
    entries.clear();
    columnHeaders.removeAll();
    revalidate();
    repaint();
  }


  // updateTreeHeaderWidth() entfällt – BorderLayout CENTER erledigt das



  // ── Getter ────────────────────────────────────────────────────────────────

  public ColumnHeader getTreeHeader() { return treeHeader; }

  public List<ColumnHeader> getHeaders() {
    return entries.stream().map(ColumnEntry::header).toList();
  }
}