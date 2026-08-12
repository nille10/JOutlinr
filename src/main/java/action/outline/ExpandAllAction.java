package action.outline;

import controller.OutlineController;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

public class ExpandAllAction extends AbstractAction {

    private final OutlineController outlineController;

    public ExpandAllAction(OutlineController outlineController) {
        super("Alles auf/zuklappen");
        this.outlineController = outlineController;

        putValue(SHORT_DESCRIPTION, "Expand/Collapse all Entry's in the Outliner");
        putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, KeyEvent.CTRL_DOWN_MASK | KeyEvent.SHIFT_DOWN_MASK));
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        outlineController.expandAll();
    }
}
