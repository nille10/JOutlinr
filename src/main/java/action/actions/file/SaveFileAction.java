package action.actions.file;

import controller.FileController;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

public class SaveFileAction extends AbstractAction {
    private final FileController fileController;

    public SaveFileAction(FileController fileController) {
        super("Speichern");
        this.fileController = fileController;

        putValue(SHORT_DESCRIPTION, "Save File");
        putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_S, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()));
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        try {
            fileController.saveDocument();
        } catch (Exception ex) {
            throw new RuntimeException(ex); // Die Exception sollte hier nicht entstehen, da sie vorher schon behandelt wird
        }
    }
}
