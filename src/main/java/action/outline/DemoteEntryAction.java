package action.outline;

import controller.OutlineController;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

public class DemoteEntryAction extends AbstractAction {

    private final OutlineController outlineController;

    public DemoteEntryAction(OutlineController outlineController) {
        super("Demote");
        this.outlineController = outlineController;

        outlineController.addUpdateListener(
            () -> setEnabled(outlineController.canDemoteSelected()));

        putValue(SHORT_DESCRIPTION, "Ebene tiefer (Demote)");
        putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, KeyEvent.CTRL_DOWN_MASK));
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        outlineController.demoteSelected();
    }
}
