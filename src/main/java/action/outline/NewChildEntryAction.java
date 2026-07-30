package action.outline;

import controller.OutlineController;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

public class NewChildEntryAction extends AbstractAction {

    private final OutlineController outlineController;

    public NewChildEntryAction(OutlineController outlineController) {
        super("Neuer Untereintrag");
        this.outlineController = outlineController;

        putValue(SHORT_DESCRIPTION, "Neuer Untereintrag (Kind)");
        putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, KeyEvent.SHIFT_DOWN_MASK | KeyEvent.CTRL_DOWN_MASK));

        outlineController.addUpdateListener(
            () -> setEnabled(outlineController.canAddChildToSelected()));
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        outlineController.addChild();
    }
}