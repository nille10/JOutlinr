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
 * Beispiel:
 *
 * +-----------+-----------+-----------+
 * | Status    | Notiz     | Priorität |
 * +-----------+-----------+-----------+
 * | Checkbox  | Text      | Text      |
 * | Checkbox  | Text      | Text      |
 * +-----------+-----------+-----------+
 *
 */
public class ColumnPanel extends JPanel {


  private final List<OutlineColumn> columns =
      new ArrayList<>();


  private OutlineNode[] currentNodes =
      new OutlineNode[0];


  private int currentRowHeight = 24;



  public ColumnPanel() {

    setLayout(
        new BoxLayout(
            this,
            BoxLayout.X_AXIS));


    setBackground(
        new Color(248,248,252));

    setOpaque(true);
  }



  /**
   * Erstellt und fügt eine neue Spalte hinzu.
   */
  public OutlineColumn addColumn(
      String title,
      ColumnType type) {


    OutlineColumn column =
        new OutlineColumn(title,type);


    columns.add(column);

    add(column);


    // vorhandene Tree-Zeilen sofort anzeigen
    column.rebuildRows(
        currentNodes,
        currentRowHeight,
        0);


    revalidate();
    repaint();


    return column;
  }




  /**
   * Entfernt eine Spalte.
   */
  public void removeColumn(
      OutlineColumn column) {


    if(columns.remove(column)) {

      remove(column);

      revalidate();
      repaint();
    }
  }




  /**
   * Aktualisiert alle Spalten passend zum Tree.
   */
  public void syncWithTree(
      OutlineNode[] nodes,
      int rowHeight) {


    currentNodes = nodes;

    currentRowHeight = rowHeight;


    for(OutlineColumn column : columns) {

      column.rebuildRows(
          nodes,
          rowHeight,
          0);
    }


    revalidate();
    repaint();
  }





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