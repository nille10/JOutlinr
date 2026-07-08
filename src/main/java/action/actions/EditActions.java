package action.actions;

import action.edit.*;
import controller.EditController;

import javax.swing.*;

public class EditActions {
    public final Action copyAction;
    public final Action cutAction;
    public final Action insertAction;
    public final Action redoAction;
    public final Action undoAction;

    public EditActions(EditController editController) {
        copyAction = new CopyAction(editController);
        cutAction = new CutAction(editController);
        insertAction = new InsertAction(editController);
        redoAction = new RedoAction(editController);
        undoAction = new UndoAction(editController);
    }
}
