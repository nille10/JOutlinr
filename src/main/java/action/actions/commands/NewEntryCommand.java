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

    /**
     * Erstellt einen neuen NewEntryCommand mit UndoManager
     * <p>
     *   NewEntry erstellt einen neuen Knoten im Outliner
     * </p>
     * @param controller der OutlineController vom Outliner
     */
    public NewEntryCommand(OutlineController controller) {
        this.controller = controller;
    }

    /**
     * Erstellt einen neuen Knoten im Outliner auf der Ebene vom ausgewählten Knoten, ist kein Knoten ausgewählt wird der neue Knoten auf der obersten Ebene eingefügt.
     */
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

    /**
     * Macht das Erstellen eines neuen Knotens rückgängig
     */
    @Override
    public void undo() {

        if (createdNode == null) {
            return;
        }

        //todo
        //Vielleicht auch wieder letzten Eintrag dafür selecten einbauen?
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