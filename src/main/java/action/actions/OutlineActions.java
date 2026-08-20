package action.actions;

import action.manager.UndoManager;
import action.actions.outline.*;
import controller.OutlineController;

import javax.swing.*;

/**
 * Fasst alle Outline Actions (siehe {@link action.actions.outline}) zusammen.
 */
public class OutlineActions {
    public final Action newEntryAction;
    public final Action newChildEntryAction;
    public final Action newRootEntryAction;
    public final Action promoteEntryAction;
    public final Action demoteEntryAction;
    public final Action expandAllAction;
    public final Action collapseAllAction;
    public final Action expandEntryAction;
    public final Action collapseEntryAction;
    public final Action deleteEntryAction;
    public final Action addColumnAction;

    /**
     * Erstellt alle OutlineActions und initialisiert diese mit einem {@link OutlineController} und {@link UndoManager}.
     * @param outlineController OutlineController vom Outliner, darf nicht {@code null} sein
     * @param undoManager UndoManager vom Outliner, darf nicht {@code null} sein
     */
    public OutlineActions(OutlineController outlineController, UndoManager undoManager) {
        newEntryAction = new NewEntryAction(outlineController, undoManager);
        newChildEntryAction = new NewChildEntryAction(outlineController, undoManager);
        newRootEntryAction = new NewRootEntryAction(outlineController, undoManager);
        promoteEntryAction =  new PromoteEntryAction(outlineController, undoManager);
        demoteEntryAction = new DemoteEntryAction(outlineController, undoManager);
        expandAllAction = new ExpandAllAction(outlineController);
        collapseAllAction = new CollapseAllAction(outlineController);
        expandEntryAction = new ExpandEntryAction(outlineController);
        collapseEntryAction = new CollapseEntryAction(outlineController);
        deleteEntryAction = new DeleteEntryAction(outlineController, undoManager);
        addColumnAction = new AddColumnAction(outlineController, undoManager);

    }
}
