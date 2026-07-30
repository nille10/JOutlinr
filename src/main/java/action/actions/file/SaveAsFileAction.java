package action.actions.file;

import controller.FileController;

import javax.swing.*;
import java.awt.event.ActionEvent;

public class SaveAsFileAction extends AbstractAction {

    private final FileController fileController;

    public SaveAsFileAction(FileController fileController) {
        super("Speichern unter…");
        this.fileController = fileController;

        putValue(SHORT_DESCRIPTION, "Close File");
        //putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke());
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        try {
            fileController.saveDocumentAs();
        } catch (Exception ex) {
            throw new RuntimeException(ex); // Die Exception sollte hier nicht entstehen, da sie vorher schon behandelt wird
        }
    }
}
