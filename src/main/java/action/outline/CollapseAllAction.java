package action.outline;

import controller.OutlineController;

import javax.swing.*;
import java.awt.event.ActionEvent;

public class CollapseAllAction extends AbstractAction {

    private final OutlineController outlineController;

    public CollapseAllAction(OutlineController outlineController) {
        super("Alles zuklappen");
        this.outlineController = outlineController;

        putValue(SHORT_DESCRIPTION, "Collapse all Entrys in the Outliner");
        //putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke());
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        outlineController.collapseAll();
    }
}
