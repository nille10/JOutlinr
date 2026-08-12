package action.actions.commands;

import action.manager.UndoableAction;
import controller.OutlineController;
import model.OutlineNode;

import javax.swing.tree.DefaultTreeModel;

public class DemoteEntryCommand implements UndoableAction {

    private final OutlineController controller;

    private OutlineNode node;
    private OutlineNode originalParent;
    private OutlineNode newParent; // der vorherige Geschwisterknoten
    private int originalIndex;

    private boolean wasExecuted;

    public DemoteEntryCommand(OutlineController controller) {
        this.controller = controller;
    }

    @Override
    public void execute() {

        node = controller.getSelectedNode();

        if (node == null) {
            wasExecuted = false;
            return;
        }

        originalParent = (OutlineNode) node.getParent();

        if (originalParent == null) {
            wasExecuted = false;
            return;
        }

        originalIndex = originalParent.getIndex(node);

        if (originalIndex == 0) {
            // kein vorheriger Geschwisterknoten vorhanden
            wasExecuted = false;
            return;
        }

        newParent = (OutlineNode) originalParent.getChildAt(originalIndex - 1);

        doDemote();

        wasExecuted = true;
    }

    @Override
    public void undo() {
        if (!wasExecuted) return;

        DefaultTreeModel model = controller.getTreeModel();

        // Aus newParent raus, zurück in originalParent an originalIndex
        model.removeNodeFromParent(node);
        model.insertNodeInto(node, originalParent, originalIndex);

        controller.selectNode(node);
        controller.markDirty();
    }

    @Override
    public void redo() {
        if (!wasExecuted) return;
        doDemote();
    }

    private void doDemote() {
        DefaultTreeModel model = controller.getTreeModel();

        // Ans Ende von newParent anhängen
        model.removeNodeFromParent(node);
        model.insertNodeInto(node, newParent, newParent.getChildCount());

        controller.selectNode(node);
        controller.markDirty();
    }
}