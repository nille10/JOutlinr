package action.file;

import controller.FileController;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.io.IOException;

public class OpenFileAction extends AbstractAction {

    private final FileController fileController;

    public OpenFileAction(FileController fileController) {
        super( "Öffnen");
        this.fileController = fileController;

        putValue(SHORT_DESCRIPTION, "Open a File");
        putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke("ctrl O"));
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        try {
            fileController.openDocument();
        } catch (IOException ex) {
            throw new RuntimeException(ex); // Die Exception sollte hier nicht entstehen, da sie vorher schon behandelt wird

        }
    }
}
