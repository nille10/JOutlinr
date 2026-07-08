package action.outline;

import controller.OutlineController;

import javax.swing.*;
import java.awt.event.ActionEvent;

public class ExpandAllAction extends AbstractAction {

    private final OutlineController outlineController;

    public ExpandAllAction(OutlineController outlineController) {
        super("Alles aufklappen");
        this.outlineController = outlineController;

        putValue(SHORT_DESCRIPTION, "Expand all Entry's in the Outliner");
        //putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke());
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        outlineController.expandAll();
    }
}
