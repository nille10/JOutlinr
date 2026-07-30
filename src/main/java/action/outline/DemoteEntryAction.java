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

        outlineController.addUpdateListener(
            () -> setEnabled(outlineController.canDemoteSelected()));

        putValue(SHORT_DESCRIPTION, "Ebene tiefer (Demote)");
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        outlineController.demoteSelected();
    }
}
