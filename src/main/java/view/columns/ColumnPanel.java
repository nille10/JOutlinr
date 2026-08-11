package view.columns;

import model.OutlineNode;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Container für alle Zusatzspalten rechts neben dem JTree.
 */
public class ColumnPanel extends JPanel {


  private final List<OutlineColumn> columns = new ArrayList<>();

  private OutlineNode[] currentNodes  = new OutlineNode[0];
  private int currentRowHeight        = 24;

  /** Wird von OutlinePanel gesetzt und an jede neue Spalte weitergegeben. */
  private RowHoverManager hoverManager;



  public ColumnPanel() {

    setLayout(new BoxLayout(this, BoxLayout.X_AXIS));

    setOpaque(false);
  }



  // ── HoverManager ─────────────────────────────────────────────────────────

  /**
   * Setzt den RowHoverManager für alle aktuellen und zukünftigen Spalten.
   * Wird von OutlinePanel einmalig im Konstruktor aufgerufen.
   */
  public void setHoverManager(RowHoverManager manager) {
    this.hoverManager = manager;
    // Bestehende Spalten ebenfalls anschließen
    for (OutlineColumn col : columns) {
      col.setHoverManager(manager);
    }
  }



  // ── Spalten verwalten ────────────────────────────────────────────────────

  private Runnable onDataChanged;

  public void setOnDataChanged(Runnable callback) {
    this.onDataChanged = callback;
  }

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

  public void removeColumn(OutlineColumn column) {

    if (columns.remove(column)) {
      column.setHoverManager(null);
      remove(column);
      updatePreferredWidth();
      revalidate();
      repaint();
    }
  }


  /** Setzt preferredSize und maximumSize auf die Summe aller Column-Breiten. */
  private void updatePreferredWidth() {
    int totalW = columns.stream().mapToInt(OutlineColumn::getColumnWidth).sum();
    setPreferredSize(new Dimension(totalW, 0));
    setMaximumSize(new Dimension(totalW, Integer.MAX_VALUE));
    setMinimumSize(new Dimension(totalW, 0));
  }


  public void syncWithTree(OutlineNode[] nodes, int rowHeight) {

    currentNodes     = nodes;
    currentRowHeight = rowHeight;

    for (OutlineColumn col : columns) {
      col.rebuildRows(nodes, rowHeight, 0);
    }

    revalidate();
    repaint();
  }



  // ── Getter ────────────────────────────────────────────────────────────────

  public boolean hasColumns() {
    return !columns.isEmpty();
  }

  public int getColumnCount() {
    return columns.size();
  }

  public List<OutlineColumn> getColumns() {
    return Collections.unmodifiableList(columns);
  }
}