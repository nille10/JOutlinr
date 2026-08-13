package action.outline;

import action.actions.commands.PromoteEntryCommand;
import action.manager.UndoManager;
import controller.OutlineController;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

public class PromoteEntryAction extends AbstractAction{

    private final OutlineController outlineController;
    private final UndoManager undoManager;

    public PromoteEntryAction(OutlineController outlineController, UndoManager undoManager) {
        super("Promote");
        this.outlineController = outlineController;
        this.undoManager = undoManager;

        outlineController.addUpdateListener(
            () -> setEnabled(outlineController.canPromoteSelected()));

        putValue(SHORT_DESCRIPTION, "Ebene höher (Promote)");
        putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, KeyEvent.CTRL_DOWN_MASK));
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        undoManager.execute(
                new PromoteEntryCommand(outlineController));
    }
}
