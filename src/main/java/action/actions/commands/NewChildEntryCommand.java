/*
  KI generiert
*/
package action.actions.commands;

import action.manager.UndoableAction;
import controller.OutlineController;
import model.OutlineNode;

/**
 * Der Command ist für das Erstellen neuer Unterknoten im Outliner zuständig.
 */
public class NewChildEntryCommand implements UndoableAction {

    private final OutlineController controller;

    private OutlineNode parent;
    private OutlineNode createdNode;
    private int index;

    /**
     * Erstellt einen neuen NewChildEntryCommand mit UndoManager
     * @param controller der OutlineController vom Outliner
     */
    public NewChildEntryCommand(OutlineController controller) {
        this.controller = controller;
    }

    /**
     * Erstellt einen neuen Unterknoten unter dem ausgewähltem Knoten
     */
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

    /**
     * Macht die Erstellung eines neuen Unterknotens rückgängig.
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

        controller.insertNode(parent, createdNode, index);
        controller.startEditing(createdNode);
    }
}