package action.actions.outline;

import action.actions.commands.NewChildEntryCommand;
import action.manager.UndoManager;
import controller.OutlineController;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

public class NewChildEntryAction extends AbstractAction {

    private final OutlineController outlineController;
    private final UndoManager undoManager;

    public NewChildEntryAction(OutlineController outlineController, UndoManager undoManager) {
        super("Neuer Untereintrag");
        this.outlineController = outlineController;
        this.undoManager = undoManager;

        putValue(SHORT_DESCRIPTION, "Neuer Untereintrag (Kind)");
        putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, KeyEvent.SHIFT_DOWN_MASK | KeyEvent.CTRL_DOWN_MASK));

        outlineController.addUpdateListener(
            () -> setEnabled(outlineController.canAddChildToSelected()));
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        undoManager.execute(
                new NewChildEntryCommand(outlineController));
    }
}