/*
  KI generiert
*/
package action.actions.commands;

import action.manager.UndoableAction;
import controller.OutlineController;
import model.OutlineNode;

public class NewRootEntryCommand implements UndoableAction {

    private final OutlineController controller;

    private OutlineNode createdNode;
    private int index;

    public NewRootEntryCommand(OutlineController controller) {
        this.controller = controller;
    }

    @Override
    public void execute() {

        createdNode = new OutlineNode("Neuer Eintrag");

        index = controller.getRootNode().getChildCount();

        controller.insertNode(controller.getRootNode(), createdNode, index);

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

        controller.insertNode(controller.getRootNode(), createdNode, index);
        controller.startEditing(createdNode);
    }
}