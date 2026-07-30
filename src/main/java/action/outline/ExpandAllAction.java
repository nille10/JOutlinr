package action.outline;

import controller.OutlineController;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

public class ExpandAllAction extends AbstractAction {

    private final OutlineController outlineController;

    public ExpandAllAction(OutlineController outlineController) {
        super("Alles aufklappen");
        this.outlineController = outlineController;

        putValue(SHORT_DESCRIPTION, "Expand all Entry's in the Outliner");
        putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0));
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        outlineController.expandAll();
    }
}
