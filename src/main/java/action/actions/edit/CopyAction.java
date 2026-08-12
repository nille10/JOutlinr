package action.actions.edit;

import controller.EditController;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

public class CopyAction extends AbstractAction {

    private final EditController editController;

    public CopyAction(EditController editController) {
        super("Kopieren");
        this.editController = editController;

        // TODO
        putValue(SHORT_DESCRIPTION, "");
        putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_C, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()));
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        System.out.println("copy action");
        editController.copy();
    }
}
