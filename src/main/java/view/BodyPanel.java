package view;

import view.columns.ColumnPanel;

import javax.swing.*;
import java.awt.*;

/**
 * Scrollbarer Inhaltsbereich des Outliners.
 *
 * Aufbau:
 *
 * +------------------+------------------------+
 * | JTree  (WEST)    | ColumnPanel (CENTER)   |
 * |  feste Breite    |  bekommt Rest-Breite   |
 * |  wächst nach     |  schrumpft wenn Tree   |
 * |  rechts          |  breiter wird          |
 * +------------------+------------------------+
 *
 * Layout: BorderLayout
 *   CENTER = treeContainer  → bekommt den gesamten restlichen Platz
 *   EAST   = columnPanel    → fix-breit (Summe aller Column-Breiten),
 *                             schrumpft nicht – Tree schrumpft stattdessen
 */
public class BodyPanel extends JPanel {


  private final JTree tree;
  private final ColumnPanel columnPanel;
  private final JPanel treeContainer;


  public BodyPanel(JTree tree, ColumnPanel columnPanel) {

    this.tree        = tree;
    this.columnPanel = columnPanel;

    setLayout(new BorderLayout());
    //setBackground(new Color(248, 248, 252));

    // ── Tree-Container (CENTER – nimmt den Restplatz) ────────────────────
    treeContainer = new JPanel(new BorderLayout());
    //treeContainer.setBackground(new Color(248, 248, 252));
    treeContainer.add(tree, BorderLayout.CENTER);
    treeContainer.setMinimumSize(new Dimension(80, 0));
    add(treeContainer, BorderLayout.CENTER);

    // ColumnPanel kommt erst via showColumns() hinzu (BorderLayout.EAST)
  }



  // -------------------------------------------------------
  // Höhe synchronisieren – nötig für den JScrollPane
  // -------------------------------------------------------

  /**
   * Passt die preferredSize an die aktuelle Zeilenanzahl an.
   *
   * Der JScrollPane berechnet seine Scrollbar-Größe anhand der
   * preferredSize dieses Panels – ohne Update bleibt die Scrollbar stumm.
   *
   * Breite wird NICHT angefasst: BorderLayout regelt das selbst.
   *
   * @param rowCount   aktuell sichtbare Zeilen im Tree
   * @param rowHeight  Höhe einer einzelnen Zeile in Pixeln
   */
  public void updatePreferredHeight(int rowCount, int rowHeight) {

    int totalHeight = Math.max(rowCount * rowHeight, 100);

    // Nur die Höhe überschreiben, Breite bleibt 0 → BorderLayout bestimmt sie
    setPreferredSize(new Dimension(0, totalHeight));

    revalidate();
    repaint();
  }



  // -------------------------------------------------------
  // Spalten ein- / ausblenden
  // -------------------------------------------------------

  /**
   * Fügt das ColumnPanel als CENTER-Komponente ein.
   *
   * CENTER bekommt von BorderLayout genau den Platz,
   * der nach dem Tree (WEST) noch übrig bleibt.
   */
  public void showColumns() {
    if (columnPanel.getParent() == null) {
      add(columnPanel, BorderLayout.EAST);
      revalidate();
      repaint();
    }
  }


  /**
   * Entfernt das ColumnPanel.
   */
  public void hideColumns() {

    if (columnPanel.getParent() != null) {
      remove(columnPanel);
      revalidate();
      repaint();
    }
  }



  // -------------------------------------------------------
  // Getter
  // -------------------------------------------------------

  public JTree getTree() {
    return tree;
  }

  public ColumnPanel getColumnPanel() {
    return columnPanel;
  }

  /**
   * Wird von OutlinePanel.installTreeWidthListener() benötigt,
   * um die tatsächliche Breite des Tree-Bereichs abzulesen
   * und an den HeaderPanel weiterzugeben.
   */
  public JPanel getTreeContainer() {
    return treeContainer;
  }
}