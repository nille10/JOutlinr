package controller;

import action.actions.commands.DeleteEntryCommand;
import action.actions.commands.PasteCommand;
import action.manager.UndoManager;
import model.OutlineNode;

import javax.swing.*;

public class EditController {

    private final JFrame jFrame;
    private final UndoManager undoManager;
    private final OutlineController outlineController;

    public EditController(JFrame jFrame, UndoManager undoManager, OutlineController outlineController) {
        this.jFrame = jFrame;
        this.undoManager = undoManager;
        this.outlineController = outlineController;
    }

    public void undo() {
        undoManager.undo();
    }

    public void redo() {
        undoManager.redo();
    }

    public void cut() {
        copy();
        undoManager.execute(new DeleteEntryCommand(outlineController));
    }

    private OutlineNode clipboard;

    public void copy() {
        OutlineNode selected = outlineController.getSelectedNode();

        if (selected != null) {
            clipboard = selected.deepCopy();
        }
    }

    public void insert() {

        if (clipboard == null) {
            return;
        }

        undoManager.execute(
            new PasteCommand(outlineController,
                clipboard.deepCopy()));
    }
}