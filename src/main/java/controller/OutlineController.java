package controller;

import model.OutlineNode;
import view.TextWindow;
import view.OutlinePanel;
import view.columns.ColumnType;

import javax.swing.*;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;


public class OutlineController {

    private final TextWindow textWindow;
    private final OutlinePanel outlinePanel;

    private final JTree tree;
    private final DefaultTreeModel treeModel;
    private final OutlineNode rootNode;
    private final JFrame mainWindow;
    private boolean dirty;

    private boolean isSelected = true;

    public static final int MAX_LEVEL = 3; //Root = 0

    private final List<Runnable> updateListeners = new ArrayList<>();

    public OutlineController(OutlinePanel view, TextWindow textWindow, JFrame mainWindow) {

        this.textWindow = textWindow;
        this.outlinePanel = view;

        this.tree = view.getTree();
        this.treeModel = view.getTreeModel();
        this.rootNode = view.getRootNode();
        this.mainWindow = mainWindow;

        //createExampleData();
        registerListeners();
        expandAll();
    }

    private void registerListeners() {

        tree.addTreeSelectionListener(e -> {
            OutlineNode selected = getSelectedNode();
            textWindow.loadNode(selected);
            fireUpdate();
        });

        treeModel.addTreeModelListener(
                new javax.swing.event.TreeModelListener() {

                    @Override
                    public void treeNodesChanged(javax.swing.event.TreeModelEvent e) {

                        Object[] children = e.getChildren();

                        if (children == null || children.length == 0) {
                            return;
                        }

                        OutlineNode child = (OutlineNode) children[0];

                        child.setTitle(child.getUserObject().toString());

                        markDirty();
                    }

                    @Override
                    public void treeNodesInserted(
                            javax.swing.event.TreeModelEvent e) {
                        markDirty();
                    }

                    @Override
                    public void treeNodesRemoved(javax.swing.event.TreeModelEvent e) {
                        markDirty();
                    }

                    @Override
                    public void treeStructureChanged(javax.swing.event.TreeModelEvent e) {
                        markDirty();
                    }
                });
    }

    private void createExampleData() {

        OutlineNode k1 = new OutlineNode("Kapitel 1");
        OutlineNode k2 = new OutlineNode("Kapitel 2");

        OutlineNode k1a = new OutlineNode("Abschnitt 1.1");
        OutlineNode k1b = new OutlineNode("Abschnitt 1.2");

        k1.add(k1a);
        k1.add(k1b);

        rootNode.add(k1);
        rootNode.add(k2);

        treeModel.reload();
    }

    public void addSibling() {

        OutlineNode selected = getSelectedNode();

        OutlineNode parent = selected != null ?(OutlineNode) selected.getParent() : rootNode;

        if (parent == null) {
            parent = rootNode;
        }

        OutlineNode node = new OutlineNode("Neuer Eintrag");

        int index = selected == null ? parent.getChildCount() : parent.getIndex(selected) + 1;

        treeModel.insertNodeInto(node, parent, index);

        startEditing(node);

        markDirty();
    }

    public void addRootEntry() {
        OutlineNode node = new OutlineNode("Neuer Eintrag");

        treeModel.insertNodeInto(node, rootNode, rootNode.getChildCount());

        startEditing(node);

        markDirty();
    }

    public void addChild() {

        OutlineNode selected = getSelectedNode();
        OutlineNode parent = selected != null ? selected : rootNode;

        if (!canAddChildTo(parent)) {
            return;
        }

        OutlineNode node = new OutlineNode("Neuer Unterknoten");

        treeModel.insertNodeInto(
            node,
            parent,
            parent.getChildCount());

        startEditing(node);

        markDirty();
    }

    public boolean canAddChildTo(OutlineNode parent) {
        if (parent == null) {
            return true;
        }
        return parent.getLevel() < MAX_LEVEL;
    }

    public boolean canAddChildToSelected() {
        OutlineNode selected = getSelectedNode();
        OutlineNode parent = selected != null ? selected : rootNode;
        return canAddChildTo(parent);
    }

    public void addUpdateListener(Runnable listener) {
        updateListeners.add(listener);
        listener.run(); // initialer Zustand direkt setzen
    }

    private void fireUpdate() {
        for (Runnable r : updateListeners) {
            r.run();
        }
    }

    public void addColumn() {

        JPanel panel = new JPanel(new GridLayout(2, 2, 5, 5));

        JTextField titleField = new JTextField("Neue Spalte");
        JComboBox<ColumnType> typeBox = new JComboBox<>(ColumnType.values());

        panel.add(new JLabel("Titel:"));
        panel.add(titleField);
        panel.add(new JLabel("Typ:"));
        panel.add(typeBox);

        int result = JOptionPane.showConfirmDialog(
            mainWindow,           // <-- statt "this"
            panel,
            "Neue Spalte",
            JOptionPane.OK_CANCEL_OPTION);

        if (result == JOptionPane.OK_OPTION) {

            String title = titleField.getText().trim();

            if (title.isEmpty()) {
                title = "Spalte";
            }

            ColumnType type = (ColumnType) typeBox.getSelectedItem();

            outlinePanel.addColumn(title, type); // <-- jetzt gültig, da Feld existiert
        }
    }

    public void deleteSelected() {

        OutlineNode selected = getSelectedNode();

        if (selected == null) {
            return;
        }

        treeModel.removeNodeFromParent(selected);

        textWindow.loadNode(null);

        markDirty();
    }

    public void promoteSelected() {

        OutlineNode selected = getSelectedNode();

        if (selected == null) {
            return;
        }

        OutlineNode parent = (OutlineNode) selected.getParent();

        if (parent == null || parent == rootNode) {
            return;
        }

        OutlineNode grandParent = (OutlineNode) parent.getParent();

        int index = grandParent.getIndex(parent) + 1;

        treeModel.removeNodeFromParent(selected);

        treeModel.insertNodeInto(
            selected,
            grandParent,
            index);

        selectNode(selected);

        markDirty();
    }

    public boolean canPromoteSelected() {

        OutlineNode selected = getSelectedNode();

        if (selected == null) {
            return false;
        }

        OutlineNode parent = (OutlineNode) selected.getParent();

        return parent != null && parent != rootNode;
    }

    public void demoteSelected() {

        OutlineNode selected = getSelectedNode();

        if (selected == null) {
            return;
        }

        OutlineNode parent =
            (OutlineNode) selected.getParent();

        int index = parent.getIndex(selected);

        if (index == 0) {
            return;
        }

        OutlineNode previous = (OutlineNode) parent.getChildAt(index - 1);

        treeModel.removeNodeFromParent(selected);

        treeModel.insertNodeInto(
            selected,
            previous,
            previous.getChildCount());

        selectNode(selected);

        markDirty();
    }

    public boolean canDemoteSelected() {

        OutlineNode selected = getSelectedNode();

        if (selected == null) {
            return false;
        }

        OutlineNode parent = (OutlineNode) selected.getParent();

        if (parent == null) {
            return false; // Root selbst
        }

        int index = parent.getIndex(selected);

        if (index == 0) {
            return false; // kein vorheriger Geschwisterknoten zum "Andocken"
        }

        // Tiefster Level im gesamten Teilbaum (inkl. aller Kinder/Enkel etc.)
        int deepestLevelInSubtree = getMaxLevelInSubtree(selected);

        // Nach dem Demote rückt der GESAMTE Teilbaum eine Ebene tiefer
        return deepestLevelInSubtree < MAX_LEVEL;
    }

    /**
     * KI generiert
     * Ermittelt die tiefste Verschachtelungsebene innerhalb eines Teilbaums
     * (inklusive des Knotens selbst).
     */
    private int getMaxLevelInSubtree(OutlineNode node) {

        int max = node.getLevel();

        for (int i = 0; i < node.getChildCount(); i++) {
            OutlineNode child = (OutlineNode) node.getChildAt(i);
            int childMax = getMaxLevelInSubtree(child);
            if (childMax > max) {
                max = childMax;
            }
        }

        return max;
    }

    //KI
    public boolean canPasteInto(OutlineNode parent, OutlineNode nodeToPaste) {

        if (parent == null || nodeToPaste == null) {
            return true;
        }

        // Tiefster Level, den der eingefügte Teilbaum relativ zu SICH SELBST hat
        // (0 = nodeToPaste selbst, +1 pro Verschachtelungsebene seiner Kinder)
        int subtreeDepth = getRelativeDepth(nodeToPaste);

        // Nach dem Einfügen läge der tiefste Nachkomme auf: parent.getLevel() + 1 + subtreeDepth
        int resultingMaxLevel = parent.getLevel() + 1 + subtreeDepth;

        return resultingMaxLevel <= MAX_LEVEL;
    }

    /**
     * KI generiert
     * Ermittelt die relative Tiefe eines (noch nicht eingehängten) Teilbaums,
     * unabhängig von seinem aktuellen Level im Baum (der bei einer Kopie z.B. 0 sein kann).
     */
    private int getRelativeDepth(OutlineNode node) {

        int max = 0;

        for (int i = 0; i < node.getChildCount(); i++) {
            OutlineNode child = (OutlineNode) node.getChildAt(i);
            int childDepth = 1 + getRelativeDepth(child);
            if (childDepth > max) {
                max = childDepth;
            }
        }

        return max;
    }

    public void expandAll() {

        for (int i = 0; i < tree.getRowCount(); i++) {
            tree.expandRow(i);
        }
    }

    public void collapseAll() {

        for (int i = 0; i < tree.getRowCount(); i++) {
            tree.collapseRow(i);
        }
    }

    public OutlineNode getSelectedNode() {

        TreePath path = tree.getSelectionPath();

        if (path == null) {
            return null;
        }

        return (OutlineNode) path.getLastPathComponent();
    }

    public void selectNode(OutlineNode node) {

        TreePath path = new TreePath(treeModel.getPathToRoot(node));

        tree.setSelectionPath(path);
        tree.scrollPathToVisible(path);
    }

    private void startEditing(OutlineNode node) {

        if (tree.isEditing()) {
            tree.getCellEditor().cancelCellEditing();
        }

        TreePath path = new TreePath(treeModel.getPathToRoot(node));

        tree.setSelectionPath(path);

        SwingUtilities.invokeLater(() -> tree.startEditingAtPath(path));
    }

    public void deleteNode(OutlineNode node) {

        treeModel.removeNodeFromParent(node);

        textWindow.loadNode(null);

        markDirty();
    }

    public java.util.List<OutlineNode> getSelectedNodes() {

        TreePath[] paths = tree.getSelectionPaths();

        if (paths == null) {
            return java.util.Collections.emptyList();
        }

        java.util.List<OutlineNode> nodes = new ArrayList<>();

        for (TreePath path : paths) {
            nodes.add((OutlineNode) path.getLastPathComponent());
        }

        return nodes;
    }

    public void deleteNodes(java.util.List<OutlineNode> nodesToDelete) {

        if (nodesToDelete.isEmpty()) {
            return;
        }

        for (OutlineNode node : nodesToDelete) {
            treeModel.removeNodeFromParent(node);
        }

        textWindow.loadNode(null);

        markDirty();
    }
    public void selectNodes(List<OutlineNode> nodes) {

        if (nodes.isEmpty()) {
            return;
        }

        TreePath[] paths = new TreePath[nodes.size()];

        for (int i = 0; i < nodes.size(); i++) {
            paths[i] = new TreePath(treeModel.getPathToRoot(nodes.get(i)));
        }

        tree.setSelectionPaths(paths);

        // Auf den letzten wiederhergestellten Knoten scrollen, damit er sichtbar ist
        tree.scrollPathToVisible(paths[paths.length - 1]);
    }


    public void insertNode(OutlineNode parent,
                           OutlineNode node,
                           int index) {

        treeModel.insertNodeInto(node, parent, index);

        selectNode(node);

        markDirty();
    }

    public void collapseNode() {
        OutlineNode selected = getSelectedNode();
        if (selected == null) return;

        TreePath path = new TreePath(treeModel.getPathToRoot(selected));
        tree.collapsePath(path);
    }

    public void expandNode() {
        OutlineNode node = getSelectedNode();
        if (node == null) return;

        TreePath path = new TreePath(treeModel.getPathToRoot(node));
        tree.expandPath(path);
    }

    //Hilfsmethode: expanded einen Knoten und nur deren Kinder
    public void expandSubtree(OutlineNode node) {

        if (node == null) {
            return;
        }

        TreePath path = new TreePath(treeModel.getPathToRoot(node));
        tree.expandPath(path);

        for (int i = 0; i < node.getChildCount(); i++) {
            expandSubtree((OutlineNode) node.getChildAt(i));
        }
    }

    public OutlineNode getRootNode() {
        return rootNode;
    }


    public DefaultTreeModel getTreeModel() {
        return treeModel;
    }

    public void markDirty() {
        dirty = true;
        updateTitle();
        fireUpdate();
    }

    public boolean isDirty() {
        return dirty;
    }

    public void saved() {
        dirty = false;
        updateTitle();
    }

    private void updateTitle() {
        mainWindow.setTitle((isDirty()? "*" : "") + mainWindow.getTitle());
    }

    public void cancelActiveEditing() {
        if (tree.isEditing()) {
            tree.getCellEditor().cancelCellEditing();
        }
    }

    public void setBoolean(boolean isSelected){
        this.isSelected = isSelected;
    }

    public boolean getBoolean(){
        return this.isSelected;
    }
}
