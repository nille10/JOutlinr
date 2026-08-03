/*

KI generiert

 */

package action.actions.commands;

import action.manager.UndoableAction;
import controller.OutlineController;
import model.OutlineNode;

public class PasteCommand implements UndoableAction {

    private final OutlineController controller;
    private final OutlineNode nodeToPaste;

    private OutlineNode parent;
    private int index;
    private boolean wasExecuted;

    public PasteCommand(OutlineController controller,
                        OutlineNode nodeToPaste) {

        this.controller = controller;
        this.nodeToPaste = nodeToPaste;
    }

    @Override
    public void execute() {
        parent = controller.getSelectedNode();

        if (parent == null) {
            parent = controller.getRootNode();
        }

        if (nodeToPaste == null || !controller.canPasteInto(parent, nodeToPaste)) {
            wasExecuted = false;
            return;
        }

        index = parent.getChildCount();
        controller.insertNode(parent, nodeToPaste, index);
        wasExecuted = true;
    }

    @Override
    public void undo() {
        if (wasExecuted) {
            controller.deleteNode(nodeToPaste);
        }
    }

    @Override
    public void redo() {
        if (nodeToPaste != null && controller.canPasteInto(parent, nodeToPaste)) {
            controller.insertNode(parent, nodeToPaste, index);
            wasExecuted = true;
        }
    }
}