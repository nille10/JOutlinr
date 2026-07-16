package view;

import view.columns.ColumnPanel;

import javax.swing.*;
import java.awt.*;

/**
 * Scrollbarer Inhaltsbereich des Outliners.
 *
 * Aufbau:
 *
 * +------------------+----------------+
 * | JTree            | ColumnPanel    |
 * |                  |                |
 * |                  |                |
 * +------------------+----------------+
 *
 * Dieses Panel liegt innerhalb des JScrollPane.
 */
public class BodyPanel extends JPanel {


  private final JTree tree;

  private final ColumnPanel columnPanel;


  private final JPanel treeContainer;



  public BodyPanel(
      JTree tree,
      ColumnPanel columnPanel) {


    this.tree = tree;

    this.columnPanel = columnPanel;



    setLayout(
        new BoxLayout(
            this,
            BoxLayout.X_AXIS));


    setBackground(
        new Color(
            248,
            248,
            252));



    // --------------------------------
    // Tree Bereich
    // --------------------------------

    treeContainer =
        new JPanel(
            new BorderLayout());


    treeContainer.setBackground(
        new Color(
            248,
            248,
            252));



    treeContainer.add(
        tree,
        BorderLayout.CENTER);



    treeContainer.setPreferredSize(
        new Dimension(
            320,
            0));



    treeContainer.setMinimumSize(
        new Dimension(
            220,
            0));



    treeContainer.setMaximumSize(
        new Dimension(
            320,
            Integer.MAX_VALUE));



    add(treeContainer);



    // --------------------------------
    // ColumnPanel kommt erst später
    // --------------------------------

  }





  /**
   * Fügt die Zusatzspalten rechts hinzu.
   */
  public void showColumns() {


    if(columnPanel.getParent() == null) {


      add(columnPanel);


      revalidate();

      repaint();
    }
  }





  /**
   * Entfernt die Zusatzspalten wieder.
   */
  public void hideColumns() {


    if(columnPanel.getParent() != null) {


      remove(columnPanel);


      revalidate();

      repaint();
    }
  }





  public JTree getTree() {

    return tree;
  }



  public ColumnPanel getColumnPanel() {

    return columnPanel;
  }

}