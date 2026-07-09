package controller;

import action.manager.UndoManager;

import javax.swing.*;

public class EditController {

    private final JFrame jFrame;
    private final UndoManager undoManager;

    public EditController(JFrame jFrame, UndoManager undoManager) {
        this.jFrame = jFrame;
        this.undoManager = undoManager;
    }

    public void undo() {
        undoManager.undo();
    }

    public void redo() {
        undoManager.redo();
    }

    public void cut() { }

    public void copy() { }

    public void insert() { }
}