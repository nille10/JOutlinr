package action.actions.file;

import controller.FileController;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

public class CloseAction extends AbstractAction {

    private final FileController fileController;

    public CloseAction(FileController fileController) {
        super("Beenden");
        this.fileController = fileController;

        putValue(SHORT_DESCRIPTION, "Close File");
        putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_Q, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()));
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        fileController.close();
    }
}
