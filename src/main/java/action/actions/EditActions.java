package action.actions;

import action.actions.edit.*;
import controller.EditController;

import javax.swing.*;

/**
 * Fasst alle Edit Actions (siehe {@link action.actions.edit}) zusammen. (kopieren, ausschneiden, einfügen, redo, undo)
 */
public class EditActions {
    public final Action copyAction;
    public final Action cutAction;
    public final Action insertAction;
    public final Action redoAction;
    public final Action undoAction;

    /**
     * Erstellt alle EditActions und initialisiert diese mit einem {@link EditController}.
     * @param editController EditController vom Outliner, darf nicht {@code null} sein.
     */
    public EditActions(EditController editController) {
        copyAction = new CopyAction(editController);
        cutAction = new CutAction(editController);
        insertAction = new InsertAction(editController);
        redoAction = new RedoAction(editController);
        undoAction = new UndoAction(editController);
    }
}
