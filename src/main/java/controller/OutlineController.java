package controller;

import model.OutlineNode;
import view.TextWindow;
import view.OutlinePanel;

import javax.swing.*;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import java.util.ArrayList;
import java.util.List;


public class OutlineController {

    private final TextWindow textWindow;

    private final JTree tree;
    private final DefaultTreeModel treeModel;
    private final OutlineNode rootNode;
    private final JFrame mainWindow;
    private boolean dirty;

    public static final int MAX_LEVEL = 4; //Root = 0

    private final List<Runnable> updateListeners = new ArrayList<>();

    public OutlineController(OutlinePanel view, TextWindow textWindow, JFrame mainWindow) {

        this.textWindow = textWindow;

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

    private void selectNode(OutlineNode node) {

        TreePath path = new TreePath(treeModel.getPathToRoot(node));

        tree.setSelectionPath(path);
        tree.scrollPathToVisible(path);
    }

    private void startEditing(OutlineNode node) {

        TreePath path = new TreePath(treeModel.getPathToRoot(node));

        tree.setSelectionPath(path);

        SwingUtilities.invokeLater(() -> tree.startEditingAtPath(path));
    }

    public void deleteNode(OutlineNode node) {

        treeModel.removeNodeFromParent(node);

        textWindow.loadNode(null);

        markDirty();
    }

    public void insertNode(OutlineNode parent,
                           OutlineNode node,
                           int index) {

        treeModel.insertNodeInto(node, parent, index);
        selectNode(node);

        markDirty();
    }

    //Hilfsmethode: expanded ein Node
    public void expandNode(OutlineNode node) {

        if (node == null) {
            return;
        }

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

    private void markDirty() {
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
}
