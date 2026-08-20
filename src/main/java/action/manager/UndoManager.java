package action.manager;

import java.util.Stack;

/**
 * Ist für das Ausführen und Hinzufügen von Operationen auf einen Undo/Redo Stack zuständig.
 */
public class UndoManager {

  private final Stack<UndoableAction> undoStack = new Stack<>();
  private final Stack<UndoableAction> redoStack = new Stack<>();

  /**
   * Führt die Action aus und legt sie danach auf dem Undo Stack ab.
   * <p>
   *   Der Redo Stack wird bei jedem aufruf {@code execute(UndoableAction)} geleert.
   * </p>
   * @param action die auszuführende Action ({@link action.actions.commands},<br> {@link action.actions.edit},<br> {@link action.actions.file})
   */
  public void execute(UndoableAction action) {
    action.execute();
    undoStack.push(action);
    redoStack.clear();
  }

  /**
   * Macht die Action die auf dem Undo Stack liegt rückgängig und legt diese auf dem Redo Stack ab.
   */
  public void undo() {
    if (!undoStack.isEmpty()) {
      UndoableAction action = undoStack.pop();
      action.undo();
      redoStack.push(action);
    }
  }

  /**
   * Macht die Action die auf dem Redo Stack liegt rückgängig und legt diese auf dem Undo Stack ab.
   */
  public void redo() {
    if (!redoStack.isEmpty()) {
      UndoableAction action = redoStack.pop();
      action.redo();
      undoStack.push(action);
    }
  }

  // TODO wird nicht genutzt
  public boolean canUndo() {
    return !undoStack.isEmpty();
  }

  public boolean canRedo() {
    return !redoStack.isEmpty();
  }
}