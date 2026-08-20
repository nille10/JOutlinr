package action.actions.outline;

import action.actions.commands.DemoteEntryCommand;
import action.manager.UndoManager;
import controller.OutlineController;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

public class DemoteEntryAction extends AbstractAction {

    private final OutlineController outlineController;
    private final UndoManager undoManager;

    public DemoteEntryAction(OutlineController outlineController, UndoManager undoManager) {
        super("Demote");
        this.outlineController = outlineController;
        this.undoManager = undoManager;

        outlineController.addUpdateListener(
            () -> setEnabled(outlineController.canDemoteSelected()));

        putValue(SHORT_DESCRIPTION, "Ebene tiefer (Demote)");
        putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, KeyEvent.CTRL_DOWN_MASK));
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        undoManager.execute(
                new DemoteEntryCommand(outlineController));
    }
}
