package action.actions.file;

import controller.FileController;

import javax.swing.*;
import java.awt.event.ActionEvent;

public class CloseAction extends AbstractAction {

    private final FileController fileController;

    public CloseAction(FileController fileController) {
        super("Beenden");
        this.fileController = fileController;

        putValue(SHORT_DESCRIPTION, "Close File");
        putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke("ctrl Q"));
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        fileController.close();
    }
}
