package view.columns;

import model.OutlineNode;

import javax.swing.*;
import javax.swing.border.MatteBorder;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;

/**
 * Eine einzelne Zusatzspalte.
 *
 * Enthält nur die Datenzellen.
 *
 * Der Header befindet sich separat im HeaderPanel.
 */
public class OutlineColumn extends JPanel {


  public static final int DEFAULT_WIDTH = 140;
  public static final int MIN_WIDTH = 80;


  private final ColumnType type;


  /**
   * Speichert Werte pro OutlineNode.
   */
  private final Map<OutlineNode, Object> dataMap =
      new HashMap<>();


  /**
   * Enthält alle Zellen dieser Spalte.
   */
  private final JPanel cellContainer;



  private OutlineNode[] currentRows =
      new OutlineNode[0];


  private int rowHeight = 24;



  public OutlineColumn(
      String title,
      ColumnType type) {


    this.type = type;


    setLayout(
        new BorderLayout());


    setBackground(
        new Color(248,248,252));


    cellContainer = new JPanel();

    cellContainer.setLayout(
        new BoxLayout(
            cellContainer,
            BoxLayout.Y_AXIS));


    cellContainer.setBackground(
        new Color(248,248,252));


    add(
        cellContainer,
        BorderLayout.CENTER);



    setPreferredSize(
        new Dimension(
            DEFAULT_WIDTH,
            0));


    setMinimumSize(
        new Dimension(
            MIN_WIDTH,
            0));
  }





  /**
   * Baut alle sichtbaren Zeilen neu auf.
   */
  public void rebuildRows(
      OutlineNode[] nodes,
      int rowHeight,
      int treeY) {


    this.currentRows = nodes;

    this.rowHeight = rowHeight;


    cellContainer.removeAll();



    for(OutlineNode node : nodes) {


      JPanel cell =
          buildCell(node);


      cell.setPreferredSize(
          new Dimension(
              DEFAULT_WIDTH,
              rowHeight));


      cell.setMaximumSize(
          new Dimension(
              Integer.MAX_VALUE,
              rowHeight));


      cellContainer.add(cell);
    }



    cellContainer.revalidate();

    cellContainer.repaint();
  }






  private JPanel buildCell(
      OutlineNode node) {


    JPanel cell =
        new JPanel(
            new BorderLayout());


    cell.setBackground(
        Color.WHITE);


    cell.setBorder(
        new MatteBorder(
            0,
            1,
            1,
            0,
            new Color(
                220,
                220,
                228)));



    switch(type) {


      case CHECKBOX -> {


        JCheckBox checkBox =
            new JCheckBox();


        checkBox.setHorizontalAlignment(
            SwingConstants.CENTER);


        checkBox.setOpaque(false);



        checkBox.setSelected(
            Boolean.TRUE.equals(
                dataMap.get(node)));



        checkBox.addActionListener(e -> {

          dataMap.put(
              node,
              checkBox.isSelected());
        });



        cell.add(
            checkBox,
            BorderLayout.CENTER);
      }





      case TEXTFIELD -> {


        JTextField textField =
            new JTextField();


        textField.setFont(
            new Font(
                "Segoe UI",
                Font.PLAIN,
                12));


        textField.setBorder(
            BorderFactory.createEmptyBorder(
                2,
                6,
                2,
                6));



        textField.setText(
            String.valueOf(
                dataMap.getOrDefault(
                    node,
                    "")));



        textField.getDocument()
            .addDocumentListener(
                new javax.swing.event.DocumentListener() {


                  public void insertUpdate(
                      javax.swing.event.DocumentEvent e) {

                    dataMap.put(
                        node,
                        textField.getText());
                  }


                  public void removeUpdate(
                      javax.swing.event.DocumentEvent e) {

                    dataMap.put(
                        node,
                        textField.getText());
                  }


                  public void changedUpdate(
                      javax.swing.event.DocumentEvent e) {

                    dataMap.put(
                        node,
                        textField.getText());
                  }

                });



        cell.add(
            textField,
            BorderLayout.CENTER);
      }
    }



    return cell;
  }






  public ColumnType getType() {

    return type;
  }



  public Object getValue(
      OutlineNode node) {

    return dataMap.get(node);
  }

}