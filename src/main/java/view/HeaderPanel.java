package view;

import view.columns.ColumnHeader;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Nicht scrollender Header-Bereich des Outliners.
 *
 * Aufbau:
 *
 * +----------------+----------------+----------------+
 * | Outliner       | Status         | Notiz          |
 * +----------------+----------------+----------------+
 *
 */
public class HeaderPanel extends JPanel {

  private static final int TREE_WIDTH = 320;

  private final ColumnHeader treeHeader;

  private final JPanel columnHeaders;

  private final List<ColumnHeader> headers = new ArrayList<>();


  public HeaderPanel() {

    setLayout(new BorderLayout());

    setBackground(new Color(235,235,240));


    // ---------------------------------
    // Tree Header
    // ---------------------------------

    treeHeader = new ColumnHeader("Outliner");

    treeHeader.setPreferredSize(
        new Dimension(
            TREE_WIDTH,
            ColumnHeader.HEADER_HEIGHT));

    add(treeHeader, BorderLayout.WEST);


    // ---------------------------------
    // Zusatzspalten Header
    // ---------------------------------

    columnHeaders = new JPanel();

    columnHeaders.setLayout(
        new BoxLayout(
            columnHeaders,
            BoxLayout.X_AXIS));

    columnHeaders.setOpaque(false);


    add(columnHeaders, BorderLayout.CENTER);
  }



  /**
   * Fügt einen neuen Spaltenheader hinzu.
   */
  public ColumnHeader addColumnHeader(String title) {

    ColumnHeader header =
        new ColumnHeader(title);


    headers.add(header);

    columnHeaders.add(header);


    revalidate();
    repaint();


    return header;
  }



  /**
   * Entfernt einen Spaltenheader.
   */
  public void removeColumnHeader(ColumnHeader header) {

    headers.remove(header);

    columnHeaders.remove(header);


    revalidate();
    repaint();
  }



  /**
   * Entfernt alle Zusatzheader.
   */
  public void clearColumnHeaders() {

    headers.clear();

    columnHeaders.removeAll();


    revalidate();
    repaint();
  }



  public ColumnHeader getTreeHeader() {
    return treeHeader;
  }


  public List<ColumnHeader> getHeaders() {
    return List.copyOf(headers);
  }

}