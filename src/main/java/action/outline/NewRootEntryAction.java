package action.outline;

import controller.OutlineController;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

public class NewRootEntryAction extends AbstractAction {

  private final OutlineController outlineController;

  public NewRootEntryAction(OutlineController outlineController) {
    super("Neuer Root Eintrag");
    this.outlineController = outlineController;

    putValue(SHORT_DESCRIPTION, "Neuer Root Eintrag (Alleine)");
    putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, KeyEvent.CTRL_DOWN_MASK | KeyEvent.SHIFT_DOWN_MASK));
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    outlineController.addRootEntry();
  }
}
