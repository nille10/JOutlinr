package action.actions.file;

import controller.FileController;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.io.IOException;

public class ExportAsLatexAction extends AbstractAction {
    private final FileController fileController;

    public ExportAsLatexAction(FileController fileController) {
        super("Export als Latex");
        this.fileController = fileController;

        putValue(SHORT_DESCRIPTION, "Export As Latex");
        putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke("ctrl L"));
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        try {
            fileController.exportAsLatex();
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }
}
