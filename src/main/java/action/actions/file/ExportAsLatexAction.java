package action.actions.file;

import controller.FileController;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.IOException;

public class ExportAsLatexAction extends AbstractAction {

    private final FileController fileController;

    public ExportAsLatexAction(FileController fileController) {
        super("Export als Latex");
        this.fileController = fileController;

        putValue(SHORT_DESCRIPTION, "Export As Latex");
        putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_L, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()));
    }

    @Override
    public void actionPerformed(ActionEvent e) {
            fileController.exportAsLatex();
    }
}
