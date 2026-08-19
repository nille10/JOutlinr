package action.outline;

import action.actions.commands.NewRootEntryCommand;
import action.manager.UndoManager;
import controller.OutlineController;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

public class NewRootEntryAction extends AbstractAction {

  private final OutlineController outlineController;
  private final UndoManager undoManager;

  public NewRootEntryAction(OutlineController outlineController, UndoManager undoManager) {
    super("Neuer Root Eintrag");
    this.outlineController = outlineController;
    this.undoManager = undoManager;

    putValue(SHORT_DESCRIPTION, "Neuer Root Eintrag (Alleine)");
    putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_R, KeyEvent.CTRL_DOWN_MASK | KeyEvent.SHIFT_DOWN_MASK));
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    undoManager.execute(new NewRootEntryCommand(outlineController));
  }
}
