package action.actions.file;

import controller.FileController;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

public class NewFileAction extends AbstractAction {

    private final FileController fileController;

    public NewFileAction(FileController fileController) {
        super("Neu");
        this.fileController = fileController;

        putValue(SHORT_DESCRIPTION, "Create a new File");
        putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_N, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()));
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        fileController.newDocument();
    }
}
