/*
  KI generiert
*/
package action.actions.commands;

import action.manager.UndoableAction;
import controller.OutlineController;
import model.OutlineNode;

import javax.swing.tree.DefaultTreeModel;

public class PromoteEntryCommand implements UndoableAction {

    private final OutlineController controller;

    private OutlineNode node;
    private OutlineNode originalParent;
    private OutlineNode grandParent;
    private int originalIndex;
    private int promotedIndex;

    private boolean wasExecuted;

    /**
     * Erstellt einen neuen PromoteEntryCommand mit UndoManager
     * <p>
     *   PromoteEntry schiebt einen Knoten im Baum eine Ebene nach oben (Links).
     * </p>
     * @param controller der OutlineController vom Outliner
     */
    public PromoteEntryCommand(OutlineController controller) {
        this.controller = controller;
    }

    /**
     * Schiebt einen Knoten eine Ebene nach oben (Links).
     */
    @Override
    public void execute() {

        node = controller.getSelectedNode();

        if (node == null) {
            wasExecuted = false;
            return;
        }

        originalParent = (OutlineNode) node.getParent();

        if (originalParent == null || originalParent == controller.getRootNode()) {
            wasExecuted = false;
            return;
        }

        grandParent = (OutlineNode) originalParent.getParent();

        if (grandParent == null) {
            wasExecuted = false;
            return;
        }

        originalIndex = originalParent.getIndex(node);
        promotedIndex = grandParent.getIndex(originalParent) + 1;

        doPromote();

        wasExecuted = true;
    }

    /**
     * Macht das Ausrücken des Knotens rückgängig.
     */
    @Override
    public void undo() {
        if (!wasExecuted) return;

        // Zurück in originalParent an originalIndex
        DefaultTreeModel model = controller.getTreeModel();
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
        doPromote();
    }

    /**
     * Entfernt den ausgewählten Knoten aus dem Tree und fügt ihn eine Ebene höher wieder ein.
     */
    private void doPromote() {
        DefaultTreeModel model = controller.getTreeModel();

        // Index VOR dem Entfernen berechnen, damit er nach dem Entfernen noch stimmt
        int insertIndex = grandParent.getIndex(originalParent) + 1;

        model.removeNodeFromParent(node);
        model.insertNodeInto(node, grandParent, insertIndex);

        controller.selectNode(node);
        controller.markDirty();
    }
}