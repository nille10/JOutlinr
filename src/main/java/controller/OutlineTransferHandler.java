package controller;

import model.OutlineNode;
import view.MainWindow;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.util.List;

public class OutlineTransferHandler extends TransferHandler {
    private final MainWindow mainFrame;
    private DefaultMutableTreeNode draggedNode;
    private List<OutlineNode> draggedNodes;

    public OutlineTransferHandler(MainWindow mainFrame) {
        this.mainFrame = mainFrame;
    }

    @Override
    protected Transferable createTransferable(JComponent c) {
        draggedNodes = mainFrame.getOutlineController().getSelectedNodes();
        return new StringSelection("node");
    }

    @Override
    public int getSourceActions(JComponent c) {
        return MOVE;
    }

    @Override
    public boolean canImport(TransferSupport support) {
        if (!support.isDrop()) {
            return false;
        }
        JTree.DropLocation dl = (JTree.DropLocation) support.getDropLocation();
        return dl.getPath() != null;
    }

    @Override
    public boolean importData(TransferSupport support) {
        if (!canImport(support)) {
            return false;
        }

        if (draggedNodes == null || draggedNodes.isEmpty()) {
            return false;
        }

        JTree tree = (JTree) support.getComponent();
        DefaultTreeModel model = (DefaultTreeModel) tree.getModel();
        JTree.DropLocation dl = (JTree.DropLocation) support.getDropLocation();

        TreePath path = dl.getPath();
        if (path == null) {
            return false;
        }

        DefaultMutableTreeNode target = (DefaultMutableTreeNode) path.getLastPathComponent();
        int dropIndex = dl.getChildIndex();

        // Falls auf den Knoten selbst statt zwischen Knoten gedroppt wird, an das Ende anfügen
        if (dropIndex < 0) {
            dropIndex = target.getChildCount();
        }

        for (OutlineNode node : draggedNodes) {
            // OutlineNode muss ein DefaultMutableTreeNode sein (oder davon erben)
            DefaultMutableTreeNode draggedNode = (DefaultMutableTreeNode) (Object) node;

            // Verschieben in sich selbst oder eigene Kinder verhindern
            if (target.isNodeAncestor(draggedNode)) {
                continue;
            }

            DefaultMutableTreeNode oldParent = (DefaultMutableTreeNode) draggedNode.getParent();
            if (oldParent != null) {
                int oldIndex = oldParent.getIndex(draggedNode);

                // Index-Korrektur, wenn innerhalb desselben Elternknotens nach unten verschoben wird
                if (oldParent == target && oldIndex < dropIndex) {
                    dropIndex--;
                }

                model.removeNodeFromParent(draggedNode);
            }

            // Knoten an der berechneten Position einfügen
            model.insertNodeInto(draggedNode, target, dropIndex);

            // Nachfolgende gedroppte Knoten direkt dahinter einfügen
            dropIndex++;
        }

        mainFrame.getOutlineController().markDirty();
        return true;
    }
}