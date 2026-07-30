package action.actions.edit;

import controller.EditController;

import javax.swing.*;
import java.awt.event.ActionEvent;

public class InsertAction extends AbstractAction {

    private final EditController editController;

    public InsertAction(EditController editController) {
        super("Einfügen");
        this.editController = editController;

        // TODO
        putValue(SHORT_DESCRIPTION, "");
        putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke("ctrl V"));
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        editController.insert();
    }
}
