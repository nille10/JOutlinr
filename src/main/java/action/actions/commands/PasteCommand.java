/*
  KI generiert
*/
package action.actions.commands;

import action.manager.UndoableAction;
import controller.OutlineController;
import model.OutlineNode;

import javax.swing.tree.DefaultTreeModel;

/**
 * Der Command ist für das Einfügen ausgewählter Knoten zuständig.
 */
public class PasteCommand implements UndoableAction {

    private final OutlineController controller;
    private final OutlineNode nodeToPaste;

    private OutlineNode pasteParent;
    private int pasteIndex;
    private boolean wasExecuted;

    /**
     * Erstellt einen neuen PasteCommand mit UndoManager
     * @param controller der OutlineController vom Outliner
     */
    public PasteCommand(OutlineController controller, OutlineNode nodeToPaste) {
        this.controller = controller;
        this.nodeToPaste = nodeToPaste;
    }

    /**
     * Fügt die ausgewählten Knoten an dem ausgewählten Platz im Outliner wieder ein.
     */
    @Override
    public void execute() {
        wasExecuted = false;

        if (nodeToPaste == null) return;

        OutlineNode selected = controller.getSelectedNode();

        if (selected != null) {
            // Etwas selektiert → als letztes Kind des selektierten Knotens einfügen
            pasteParent = selected;
        } else {
            // Nichts selektiert → ans Ende der Root
            pasteParent = controller.getRootNode();
        }

        if (!controller.canPasteInto(pasteParent, nodeToPaste)) {
            // Tiefenlimit würde verletzt → zur Root zurückfallen
            pasteParent = controller.getRootNode();

            if (!controller.canPasteInto(pasteParent, nodeToPaste)) {
                // Auch in Root nicht möglich (Teilbaum selbst zu tief)
                return;
            }
        }

        pasteIndex = pasteParent.getChildCount();

        doInsert();
        wasExecuted = true;
    }

    /**
     * Macht das Einfügen von Knoten wieder rückgängig.
     */
    @Override
    public void undo() {
        if (!wasExecuted) return;

        DefaultTreeModel model = controller.getTreeModel();
        model.removeNodeFromParent(nodeToPaste);
        controller.markDirty();
    }

    /**
     * Macht das Ausführen con {@link #undo()} rückgängig.
     */
    @Override
    public void redo() {
        if (!wasExecuted) return;
        doInsert();
    }

    /**
     * Fügt die Knoten an der richtigen Stelle ein.
     */
    private void doInsert() {
        DefaultTreeModel model = controller.getTreeModel();
        model.insertNodeInto(nodeToPaste, pasteParent, pasteIndex);
        controller.selectNode(nodeToPaste);
        controller.markDirty();
    }
}