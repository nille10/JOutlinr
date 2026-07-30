package action.actions.edit;

import controller.EditController;

import javax.swing.*;
import java.awt.event.ActionEvent;

public class RedoAction extends AbstractAction {

    private final EditController editController;

    public RedoAction(EditController editController) {
        super("Wiederholen");
        this.editController = editController;

        // TODO
        putValue(SHORT_DESCRIPTION, "");
        putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke("ctrl Y"));
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        editController.redo();
    }
}
