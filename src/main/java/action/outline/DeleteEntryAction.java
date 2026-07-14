package action.outline;

import action.commands.DeleteEntryCommand;
import action.manager.UndoManager;
import controller.OutlineController;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

public class DeleteEntryAction extends AbstractAction {

    private final OutlineController outlineController;
    private final UndoManager undoManager;

    public DeleteEntryAction(OutlineController outlineController,
                             UndoManager undoManager) {
        super("Eintrag löschen");
        this.outlineController = outlineController;
        this.undoManager = undoManager;
        putValue(SHORT_DESCRIPTION, "Lösche einen Eintrag aus dem Outliner");
        putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_DELETE , 0));
    }

    @Override
    public void actionPerformed(ActionEvent e) {

        undoManager.execute(
            new DeleteEntryCommand(outlineController));
    }
}