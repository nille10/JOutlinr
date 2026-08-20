package action.actions.outline;

import controller.OutlineController;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

public class ExpandEntryAction extends AbstractAction {

  private final OutlineController outlineController;

  public ExpandEntryAction(OutlineController outlineController) {
    super("Eintrag auf/zuklappen");
    this.outlineController = outlineController;

    putValue(SHORT_DESCRIPTION, "Expand/collapse selected Entry in the Outliner");
    putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0));
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    outlineController.expandNode();
  }
}
