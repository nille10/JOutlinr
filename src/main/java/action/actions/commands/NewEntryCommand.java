/*
  KI generiert
*/
package action.actions.commands;

import action.manager.UndoableAction;
import controller.OutlineController;
import model.OutlineNode;

public class NewEntryCommand implements UndoableAction {

    private final OutlineController controller;

    private OutlineNode parent;
    private OutlineNode createdNode;
    private int index;

    public NewEntryCommand(OutlineController controller) {
        this.controller = controller;
    }

    @Override
    public void execute() {

        OutlineNode selected = controller.getSelectedNode();

        parent = selected != null
                ? (OutlineNode) selected.getParent()
                : controller.getRootNode();

        if (parent == null) {
            parent = controller.getRootNode();
        }

        createdNode = new OutlineNode("Neuer Eintrag");

        index = selected == null
                ? parent.getChildCount()
                : parent.getIndex(selected) + 1;

        controller.insertNode(parent, createdNode, index);

        // Nach dem Einfügen wieder editierbar machen
        controller.startEditing(createdNode);
    }

    @Override
    public void undo() {

        if (createdNode == null) {
            return;
        }

        //todo
        //Vielleicht auch wieder letzten Eintrag dafür selecten einbauen?
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