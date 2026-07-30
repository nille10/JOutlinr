package action.outline;

import controller.OutlineController;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

public class PromoteEntryAction extends AbstractAction{

    private final OutlineController outlineController;

    public PromoteEntryAction(OutlineController outlineController) {
        super("Ebene höher");
        this.outlineController = outlineController;

        outlineController.addUpdateListener(
            () -> setEnabled(outlineController.canPromoteSelected()));

        putValue(SHORT_DESCRIPTION, "Ebene höher (Promote)");
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        outlineController.promoteSelected();
    }
}
