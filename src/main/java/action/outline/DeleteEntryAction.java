package action.outline;

import controller.OutlineController;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

public class DeleteEntryAction extends AbstractAction {

    private final OutlineController outlineController;

    public DeleteEntryAction(OutlineController outlineController) {
        super("Eintrag löschen");
        this.outlineController = outlineController;

        putValue(SHORT_DESCRIPTION, "Lösche einen Eintrag aus dem Outliner");
        putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_DELETE , 0));
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        outlineController.deleteSelected();
    }
}
