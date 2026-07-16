package view;

import model.OutlineNode;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.tree.DefaultTreeCellEditor;
import javax.swing.tree.DefaultTreeModel;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.util.EventObject;

public class OutlinePanel extends JPanel {

    private final JTree tree;
    private final DefaultTreeModel treeModel;
    private final OutlineNode rootNode;

    //new column panel

    public OutlinePanel() {

        setLayout(new BorderLayout());

        rootNode = new OutlineNode("Outliner");
        treeModel = new DefaultTreeModel(rootNode);

        tree = new JTree(treeModel);

        tree.setRootVisible(false);
        tree.setShowsRootHandles(true);
        tree.setEditable(true);

        tree.setBackground(new Color(248,248,252));
        tree.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        tree.setRowHeight(24);
        tree.setBorder(new EmptyBorder(4,4,4,4));

        tree.setCellEditor(
                new DefaultTreeCellEditor(
                        tree,
                        (javax.swing.tree.DefaultTreeCellRenderer) tree.getCellRenderer()) {

                    @Override
                    public boolean isCellEditable(EventObject e) {
                        if (e instanceof MouseEvent mouseEvent) {
                            return mouseEvent.getClickCount() >= 2;
                        }
                        return false;
                    }
                });

        JScrollPane scrollPane = new JScrollPane(tree);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());

        add(scrollPane, BorderLayout.CENTER);
    }

    public JTree getTree() {
        return tree;
    }

    public DefaultTreeModel getTreeModel() {
        return treeModel;
    }

    public OutlineNode getRootNode() {
        return rootNode;
    }
}
