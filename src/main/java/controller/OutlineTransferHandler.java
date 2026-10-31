package controller;

import action.actions.commands.MoveEntryCommand;
import model.OutlineNode;
import view.MainWindow;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.util.List;

public class OutlineTransferHandler extends TransferHandler {
    private final MainWindow mainFrame;
    private List<OutlineNode> draggedNodes;

    /**
     * Transfer Handler für die Drag-and-Drop funktion
     * @param mainFrame das Main window
     */
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


    /**
     * Verschiebt die ausgewählten Knoten an die Stelle, an der sie abgelegt wurden.
     * <p>
     *   Prüft, ob die Position an der die Knoten eingefügt werden sollen zulässig ist
     *   (maximale Verschachtelungstiefe). Das eigentliche Verschieben übernimmt ein
     *   {@link MoveEntryCommand}, der über den {@code UndoManager} ausgeführt wird,
     *   sodass das Verschieben (auch mehrerer Knoten) per Undo/Redo rückgängig gemacht
     *   werden kann.
     * </p>
     * @param support the object containing the details of
     *        the transfer, not <code>null</code>.
     * @return {@code true} wenn die Knoten an der gewählten Stelle eingefügt werden können, ansonsten {@code false}
     */
    @Override
    public boolean importData(TransferSupport support) {
        if (!canImport(support)) {
            return false;
        }

        if (draggedNodes == null || draggedNodes.isEmpty()) {
            return false;
        }

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

        if (getSubTreeHeight() + target.getLevel() > 2) {
            mainFrame.getStatusBar().setText("Zu viele Ebenen (maximal 3 zugelassen)");
            return false;
        }

        MoveEntryCommand command = new MoveEntryCommand(
                mainFrame.getOutlineController(), draggedNodes, (OutlineNode) target, dropIndex);

        if (!command.hasEffect()) {
            return false;
        }

        mainFrame.getUndoManager().execute(command);
        return true;
    }

    /**
     * Ermittelt die tiefe des SubBaums, die per Drag-and-Drop bewegt werden sollen.
     * @return die maximale tiefe des SubBaums
     */
    private int getSubTreeHeight() {
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