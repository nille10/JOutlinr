package controller;

import view.MainWindow;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;

public class OutlineTransferHandler extends TransferHandler {

    private final MainWindow mainFrame;
    private DefaultMutableTreeNode draggedNode;

    public OutlineTransferHandler(MainWindow mainFrame) {
        this.mainFrame = mainFrame;
    }

    @Override
    protected Transferable createTransferable (JComponent c) {
        JTree tree = (JTree) c;

        draggedNode =(DefaultMutableTreeNode) tree.getLastSelectedPathComponent();

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

        JTree.DropLocation dl =(JTree.DropLocation) support.getDropLocation();

        return dl.getPath() != null;
    }

    @Override
    public boolean importData(TransferSupport support) {
        JTree tree = (JTree) support.getComponent();
        DefaultTreeModel model = (DefaultTreeModel) tree.getModel();

        JTree.DropLocation dl =(JTree.DropLocation) support.getDropLocation();

        TreePath path = dl.getPath();

        if (path == null) {
            return false;
        }

        int index = dl.getChildIndex();

        DefaultMutableTreeNode target =(DefaultMutableTreeNode) path.getLastPathComponent();

        if (draggedNode == null) {
            return false;
        }

        if (draggedNode == target) {
            return false;
        }

        DefaultMutableTreeNode oldParent =(DefaultMutableTreeNode) draggedNode.getParent();

        int oldIndex = oldParent.getIndex(draggedNode);

        if (index >= 0 && oldParent == target && oldIndex < index) {
            index--;
        }

        model.removeNodeFromParent(draggedNode);

        if (index >= 0) {
            model.insertNodeInto(draggedNode, target, index);

        } else {
            model.insertNodeInto(draggedNode, target, target.getChildCount());
        }

        mainFrame.getOutlineController().markDirty();

        return true;
    }



}
