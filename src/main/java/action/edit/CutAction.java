package action.edit;

import controller.EditController;

import javax.swing.*;
import java.awt.event.ActionEvent;

public class CutAction extends AbstractAction {

    private final EditController editController;

    public CutAction(EditController editController) {
        super("Ausschneiden");
        this.editController = editController;

        // TODO
        putValue(SHORT_DESCRIPTION, "");
        putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke("ctrl X"));
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        editController.cut();
    }
}
