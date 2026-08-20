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

    /**
     * Erstellt einen neuen NewRootEntryCommand mit UndoManager
     * <p>
     *   NewRootEntry erstellt einen neuen Wurzelknoten im Outliner
     * </p>
     * @param controller der OutlineController vom Outliner
     */
    public NewRootEntryCommand(OutlineController controller) {
        this.controller = controller;
    }

    /**
     * Erstellt einen neuen Wurzelknoten im Outliner.
     */
    @Override
    public void execute() {

        createdNode = new OutlineNode("Neuer Eintrag");

        index = controller.getRootNode().getChildCount();

        controller.insertNode(controller.getRootNode(), createdNode, index);

        controller.startEditing(createdNode);
    }

    /**
     * Macht das Erstellen eines neuen Wurzelknotens rückgängig.
     */
    @Override
    public void undo() {

        if (createdNode == null) {
            return;
        }

        controller.deleteNode(createdNode);
    }

    /**
     * Macht das Ausführen con {@link #undo()} rückgängig.
     */
    @Override
    public void redo() {

        if (createdNode == null) {
            return;
        }

        controller.insertNode(controller.getRootNode(), createdNode, index);
        controller.startEditing(createdNode);
    }
}