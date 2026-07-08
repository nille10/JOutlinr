package action.file;

import controller.FileController;

import javax.swing.*;
import java.awt.event.ActionEvent;

public class SaveFileAction extends AbstractAction {
    private final FileController fileController;

    public SaveFileAction(FileController fileController) {
        super("Speichern");
        this.fileController = fileController;

        putValue(SHORT_DESCRIPTION, "Save File");
        putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke("ctrl S"));
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        fileController.saveDocument();
    }
}
