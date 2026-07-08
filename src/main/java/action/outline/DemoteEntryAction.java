package action.outline;

import controller.OutlineController;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

public class DemoteEntryAction extends AbstractAction {

    private final OutlineController outlineController;

    public DemoteEntryAction(OutlineController outlineController) {
        super("Ebene tiefer");
        this.outlineController = outlineController;

        putValue(SHORT_DESCRIPTION, "Ebene tiefer (Demote)");
        putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, KeyEvent.SHIFT_DOWN_MASK | KeyEvent.ALT_DOWN_MASK));
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        outlineController.demoteSelected();
    }
}
