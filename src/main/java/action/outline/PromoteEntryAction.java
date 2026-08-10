package action.outline;

import controller.OutlineController;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

public class PromoteEntryAction extends AbstractAction{

    private final OutlineController outlineController;

    public PromoteEntryAction(OutlineController outlineController) {
        super("Promote");
        this.outlineController = outlineController;

        outlineController.addUpdateListener(
            () -> setEnabled(outlineController.canPromoteSelected()));

        putValue(SHORT_DESCRIPTION, "Ebene höher (Promote)");
        putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, KeyEvent.CTRL_DOWN_MASK));
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        outlineController.promoteSelected();
    }
}
