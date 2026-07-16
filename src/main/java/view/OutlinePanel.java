package view;

import model.OutlineNode;
import view.columns.ColumnPanel;
import view.columns.ColumnType;
import view.columns.OutlineColumn;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.tree.DefaultTreeCellEditor;
import javax.swing.tree.DefaultTreeModel;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.util.EventObject;


/**
 * Hauptpanel des Outliners.
 *
 * Aufbau:
 *
 * HeaderPanel
 * -----------------
 *
 * JScrollPane
 *      |
 *      BodyPanel
 *          |
 *          JTree + ColumnPanel
 *
 */
public class OutlinePanel extends JPanel {


    private final JTree tree;

    private final DefaultTreeModel treeModel;

    private final OutlineNode rootNode;



    private final HeaderPanel headerPanel;

    private final ColumnPanel columnPanel;

    private final BodyPanel bodyPanel;



    private final JScrollPane scrollPane;




    public OutlinePanel() {


        setLayout(
            new BorderLayout());



        // --------------------------------
        // Tree erstellen
        // --------------------------------


        rootNode =
            new OutlineNode("Outliner");


        treeModel =
            new DefaultTreeModel(rootNode);



        tree =
            new JTree(treeModel);



        tree.setRootVisible(false);

        tree.setShowsRootHandles(true);

        tree.setEditable(true);



        tree.setBackground(
            new Color(
                248,
                248,
                252));


        tree.setFont(
            new Font(
                "Segoe UI",
                Font.PLAIN,
                13));


        tree.setRowHeight(24);


        tree.setBorder(
            new EmptyBorder(
                4,
                4,
                4,
                4));




        tree.setCellEditor(
            new DefaultTreeCellEditor(
                tree,
                (javax.swing.tree.DefaultTreeCellRenderer)
                    tree.getCellRenderer()) {



                @Override
                public boolean isCellEditable(
                    EventObject e) {


                    if(e instanceof MouseEvent mouseEvent) {

                        return mouseEvent.getClickCount() >= 2;
                    }


                    return false;
                }
            });




        // --------------------------------
        // Panels
        // --------------------------------


        headerPanel =
            new HeaderPanel();



        columnPanel =
            new ColumnPanel();



        bodyPanel =
            new BodyPanel(
                tree,
                columnPanel);



        scrollPane =
            new JScrollPane(bodyPanel);



        scrollPane.setBorder(
            BorderFactory.createEmptyBorder());



        scrollPane.getVerticalScrollBar()
            .setUnitIncrement(16);




        add(
            headerPanel,
            BorderLayout.NORTH);



        add(
            scrollPane,
            BorderLayout.CENTER);
    }





    /**
     * Wird vom Menü aufgerufen.
     *
     * Beispiel:
     *
     * addColumn("Status", ColumnType.CHECKBOX);
     */
    public OutlineColumn addColumn(
        String title,
        ColumnType type) {



        OutlineColumn column =
            columnPanel.addColumn(
                title,
                type);



        headerPanel.addColumnHeader(title);



        bodyPanel.showColumns();



        // aktuelle Tree-Zeilen übernehmen
        syncColumns();



        return column;
    }





    /**
     * Entfernt eine Spalte.
     */
    public void removeColumn(
        OutlineColumn column) {


        columnPanel.removeColumn(column);


        if(!columnPanel.hasColumns()) {

            bodyPanel.hideColumns();

            headerPanel.clearColumnHeaders();
        }
    }






    /**
     * Synchronisiert Zusatzspalten
     * mit den sichtbaren Tree-Zeilen.
     */
    public void syncColumns() {


        int count =
            tree.getRowCount();



        OutlineNode[] nodes =
            new OutlineNode[count];



        for(int i = 0; i < count; i++) {


            Object obj =
                tree.getPathForRow(i)
                    .getLastPathComponent();



            if(obj instanceof OutlineNode node) {

                nodes[i] = node;
            }
        }



        columnPanel.syncWithTree(
            nodes,
            tree.getRowHeight());
    }






    // -----------------------------
    // Getter
    // -----------------------------


    public JTree getTree() {

        return tree;
    }



    public DefaultTreeModel getTreeModel() {

        return treeModel;
    }



    public OutlineNode getRootNode() {

        return rootNode;
    }



    public HeaderPanel getHeaderPanel() {

        return headerPanel;
    }



    public ColumnPanel getColumnPanel() {

        return columnPanel;
    }

}