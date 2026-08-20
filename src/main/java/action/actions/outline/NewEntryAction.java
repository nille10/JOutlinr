package action.actions.outline;

import action.actions.commands.NewEntryCommand;
import action.manager.UndoManager;
import controller.OutlineController;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

public class NewEntryAction extends AbstractAction {

    private final OutlineController outlineController;
    private final UndoManager undoManager;

    public NewEntryAction(OutlineController outlineController, UndoManager undoManager) {
        super("Neuer Eintrag");
        this.outlineController = outlineController;
        this.undoManager = undoManager;

        putValue(SHORT_DESCRIPTION, "Neuer Eintrag (Geschwister)");
        putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_W, KeyEvent.CTRL_DOWN_MASK));
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        undoManager.execute(
                new NewEntryCommand(outlineController));
    }
}
