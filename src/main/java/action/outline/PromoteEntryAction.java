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

        putValue(SHORT_DESCRIPTION, "Ebene höher (Promote)");
        putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, KeyEvent.SHIFT_DOWN_MASK | KeyEvent.ALT_DOWN_MASK));
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        outlineController.promoteSelected();
    }
}
