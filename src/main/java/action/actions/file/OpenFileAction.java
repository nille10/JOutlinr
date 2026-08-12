package action.actions.file;

import controller.FileController;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

public class OpenFileAction extends AbstractAction {

    private final FileController fileController;

    public OpenFileAction(FileController fileController) {
        super( "Öffnen");
        this.fileController = fileController;

        putValue(SHORT_DESCRIPTION, "Open a File");
        putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke("ctrl O"));
        putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_Q, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()));
    }

    @Override
    public void actionPerformed(ActionEvent e) {
            fileController.openDocument();
    }
}
