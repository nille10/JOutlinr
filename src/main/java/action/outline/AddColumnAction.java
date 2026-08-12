package action.outline;

import action.manager.UndoManager;
import controller.OutlineController;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

public class AddColumnAction extends AbstractAction {

  private final OutlineController outlineController;

  public AddColumnAction(OutlineController outlineController, UndoManager undoManager) {
    super("Neue Spalte");
    this.outlineController = outlineController;

    putValue(SHORT_DESCRIPTION, "Add new Column");
    putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_N, KeyEvent.SHIFT_DOWN_MASK | KeyEvent.CTRL_DOWN_MASK));
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    outlineController.addColumn();
  }
}
