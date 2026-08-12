package action.outline;

import controller.OutlineController;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

public class CollapseAllAction extends AbstractAction {

    private final OutlineController outlineController;

    public CollapseAllAction(OutlineController outlineController) {
        super("Alles zuklappen");
        this.outlineController = outlineController;

        putValue(SHORT_DESCRIPTION, "Collapse all Entrys in the Outliner");
        //alt
        //putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_BACK_SPACE, KeyEvent.CTRL_DOWN_MASK));

    }

    @Override
    public void actionPerformed(ActionEvent e) {
        outlineController.collapseAll();
    }
}
