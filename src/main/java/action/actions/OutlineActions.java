package action.actions;

import action.manager.UndoManager;
import action.outline.*;
import controller.OutlineController;

import javax.swing.*;

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

    public OutlineActions(OutlineController outlineController, UndoManager undoManager) {
        newEntryAction = new NewEntryAction(outlineController);
        newChildEntryAction = new NewChildEntryAction(outlineController);
        newRootEntryAction = new NewRootEntryAction(outlineController);
        promoteEntryAction =  new PromoteEntryAction(outlineController);
        demoteEntryAction = new DemoteEntryAction(outlineController);
        expandAllAction = new ExpandAllAction(outlineController);
        collapseAllAction = new CollapseAllAction(outlineController);
        expandEntryAction = new ExpandEntryAction(outlineController);
        collapseEntryAction = new CollapseEntryAction(outlineController);
        deleteEntryAction = new DeleteEntryAction(outlineController, undoManager);

    }
}
