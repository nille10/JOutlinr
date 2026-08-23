package controller;

import action.actions.commands.DeleteEntryCommand;
import action.actions.commands.PasteCommand;
import action.manager.UndoManager;
import model.OutlineNode;

import javax.swing.*;

/**
 * Der Controller ist für die Undo/Redo, sowie für die cut, copy, insert Operationen zuständig.
 * @see UndoManager
 */
public class EditController {

    private final JFrame jFrame;
    private final UndoManager undoManager;
    private final OutlineController outlineController;
    private OutlineNode clipboard;

    /**
     * Erstellt einen neuen {@code OutlineController}.
     * @param jFrame Hauptfenster der Anwendung
     * @param undoManager der UndoManager für den Outliner
     * @param outlineController die Hauptsteuerung der Anwendung
     * @see UndoManager
     */
    public EditController(JFrame jFrame, UndoManager undoManager, OutlineController outlineController) {
        this.jFrame = jFrame;
        this.undoManager = undoManager;
        this.outlineController = outlineController;
    }

    /**
     * Macht die zuletzt ausgeführte Operation rückgängig.
     */
    public void undo() {
        undoManager.undo();
    }

    /**
     * Macht das Ausführen von {@link #undo()} rückgängig.
     */
    public void redo() {
        undoManager.redo();
    }

    /**
     * Schneidet einen Knoten aus dem JTree aus.
     */
    public void cut() {
        OutlineNode selected = outlineController.getSelectedNode();
        if (selected == null) return;

        clipboard = selected.deepCopy();

        undoManager.execute(new DeleteEntryCommand(outlineController));

        outlineController.getTree().clearSelection();
    }

    /**
     * Kopiert den ausgewählten Knoten
     */
    public void copy() {
        OutlineNode selected = outlineController.getSelectedNode();

        if (selected != null) {
            clipboard = selected.deepCopy();
        }
    }

    /**
     * Fügt einen Knoten der vorher ausgeschnitten({@link #cut()}) oder kopiert({@link #copy()}) wurde an der markierten Stelle wieder ein.
     */
    public void insert() {

        if (clipboard == null) {
            return;
        }

        undoManager.execute(
            new PasteCommand(outlineController,
                clipboard.deepCopy()));
    }
}