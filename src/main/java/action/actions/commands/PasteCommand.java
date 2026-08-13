/*
  KI generiert
*/
package action.actions.commands;

import action.manager.UndoableAction;
import controller.OutlineController;
import model.OutlineNode;

import javax.swing.tree.DefaultTreeModel;

public class PasteCommand implements UndoableAction {

    private final OutlineController controller;
    private final OutlineNode nodeToPaste;

    private OutlineNode pasteParent;
    private int pasteIndex;
    private boolean wasExecuted;

    public PasteCommand(OutlineController controller, OutlineNode nodeToPaste) {
        this.controller = controller;
        this.nodeToPaste = nodeToPaste;
    }

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

    @Override
    public void undo() {
        if (!wasExecuted) return;

        DefaultTreeModel model = controller.getTreeModel();
        model.removeNodeFromParent(nodeToPaste);
        controller.markDirty();
    }

    @Override
    public void redo() {
        if (!wasExecuted) return;
        doInsert();
    }

    private void doInsert() {
        DefaultTreeModel model = controller.getTreeModel();
        model.insertNodeInto(nodeToPaste, pasteParent, pasteIndex);
        controller.selectNode(nodeToPaste);
        controller.markDirty();
    }
}