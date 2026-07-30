package action.actions.file;

import controller.FileController;

import javax.swing.*;
import java.awt.event.ActionEvent;

public class NewFileAction extends AbstractAction {

    private final FileController fileController;

    public NewFileAction(FileController fileController) {
        super("Neu");
        this.fileController = fileController;

        putValue(SHORT_DESCRIPTION, "Create a new File");
        putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke("ctrl N"));
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        fileController.newDocument();
    }
}
