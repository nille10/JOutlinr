package action.actions.commands;

import action.manager.UndoableAction;
import controller.OutlineController;
import model.OutlineNode;

public class NewChildEntryCommand implements UndoableAction {

    private final OutlineController controller;

    private OutlineNode parent;
    private OutlineNode createdNode;
    private int index;

    public NewChildEntryCommand(OutlineController controller) {
        this.controller = controller;
    }

    @Override
    public void execute() {

        OutlineNode selected = controller.getSelectedNode();

        parent = selected != null ? selected : controller.getRootNode();

        if (!controller.canAddChildTo(parent)) {
            createdNode = null;
            return;
        }

        createdNode = new OutlineNode("Neuer Unterknoten");

        index = parent.getChildCount();

        controller.insertNode(parent, createdNode, index);

        controller.startEditing(createdNode);
    }

    @Override
    public void undo() {

        if (createdNode == null) {
            return;
        }

        controller.deleteNode(createdNode);
    }

    @Override
    public void redo() {

        if (createdNode == null) {
            return;
        }

        controller.insertNode(parent, createdNode, index);
        controller.startEditing(createdNode);
    }
}