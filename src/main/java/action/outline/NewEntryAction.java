package action.outline;

import controller.OutlineController;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

public class NewEntryAction extends AbstractAction {

    private final OutlineController outlineController;

    public NewEntryAction(OutlineController outlineController) {
        super("Neuer Eintrag");
        this.outlineController = outlineController;

        putValue(SHORT_DESCRIPTION, "Neuer Eintrag (Geschwister)");
        putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_W, KeyEvent.CTRL_DOWN_MASK));
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        outlineController.addSibling();
    }
}
