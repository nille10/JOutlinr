/*
  KI generiert
*/
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

    /**
     * Erstellt einen neuen DemoteEntryCommand mit UndoManager
     * <p>
     *   DemoteEntry schiebt einen Knoten im Baum eine Ebene nach unten (Rechts).
     * </p>
     * @param controller der OutlineController vom Outliner
     */
    public DemoteEntryCommand(OutlineController controller) {
        this.controller = controller;
    }

    /**
     * Schiebt einen Knoten eine Ebene nach unten (Rechts).
     */
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

    /**
     * Macht das Einrücken des Knotens rückgängig.
     */
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

    /**
     * Macht das Ausführen con {@link #undo()} rückgängig.
     */
    @Override
    public void redo() {
        if (!wasExecuted) return;
        doDemote();
    }

    /**
     * Entfernt den ausgewählten Knoten aus dem Tree und fügt ihn eine Ebene tiefer wieder ein.
     */
    private void doDemote() {
        DefaultTreeModel model = controller.getTreeModel();

        // Ans Ende von newParent anhängen
        model.removeNodeFromParent(node);
        model.insertNodeInto(node, newParent, newParent.getChildCount());

        controller.selectNode(node);
        controller.markDirty();
    }
}