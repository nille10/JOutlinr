package action.actions.outline;

import controller.OutlineController;

import javax.swing.*;
import java.awt.event.ActionEvent;

public class CollapseEntryAction extends AbstractAction {

  private final OutlineController outlineController;

  public CollapseEntryAction(OutlineController outlineController) {
    super("Eintrag zuklappen");
    this.outlineController = outlineController;

    putValue(SHORT_DESCRIPTION, "Collapse selected Entry in the Outliner");
    //putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_BACK_SPACE, 0));
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    outlineController.collapseNode();
  }
}
