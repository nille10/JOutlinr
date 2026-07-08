package action.edit;

import controller.EditController;

import javax.swing.*;
import java.awt.event.ActionEvent;

public class UndoAction extends AbstractAction {

    private final EditController editController;

    public UndoAction(EditController editController) {
        super("Rückgängig");
        this.editController = editController;

        // TODO
        putValue(SHORT_DESCRIPTION, "");
        putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke("ctrl Z"));
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        editController.undo();
    }
}
