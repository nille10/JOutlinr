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

    /*
    * Teils KI generiert
     */
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

        if (dropIndex < 0) {
            dropIndex = target.getChildCount();
        }

        if (getSubTreeHight() + target.getLevel() > 2) {
            mainFrame.getStatusBar().setText("Zu viele Ebenen (maximal 3 zugelassen)");
            return false;
        }

        for (OutlineNode node : draggedNodes) {

            if (target.isNodeAncestor(node)) {
                continue;
            }

            DefaultMutableTreeNode oldParent = (DefaultMutableTreeNode) node.getParent();
            if (oldParent != null) {
                int oldIndex = oldParent.getIndex(node);

                if (oldParent == target && oldIndex < dropIndex) {
                    dropIndex--;
                }

                model.removeNodeFromParent(node);
            }

            model.insertNodeInto(node, target, dropIndex);

            dropIndex++;
        }

        mainFrame.getOutlineController().markDirty();
        return true;
    }

    private int getSubTreeHight() {
        OutlineController outlineController = mainFrame.getOutlineController();

        int maxSubtreeHeight = 0;
        for (OutlineNode node : draggedNodes) {
            int height = outlineController.getRelativeDepth(node); // Höchster Pfad im Teilbaum
            if (height > maxSubtreeHeight) {
                maxSubtreeHeight = height;
            }
        }
        return maxSubtreeHeight;
    }
}