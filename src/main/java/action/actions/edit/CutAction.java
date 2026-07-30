package action.actions.edit;

import controller.EditController;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

public class CutAction extends AbstractAction {

    private final EditController editController;

    public CutAction(EditController editController) {
        super("Ausschneiden");
        this.editController = editController;

        // TODO
        putValue(SHORT_DESCRIPTION, "");
        putValue(
            ACCELERATOR_KEY,
            KeyStroke.getKeyStroke(KeyEvent.VK_X, KeyEvent.CTRL_DOWN_MASK)
        );
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        editController.cut();
    }
}
