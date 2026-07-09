package action.manager;

import java.util.Stack;

public class UndoManager {

  private final Stack<UndoableAction> undoStack = new Stack<>();
  private final Stack<UndoableAction> redoStack = new Stack<>();

  public void execute(UndoableAction action) {
    action.execute();
    undoStack.push(action);
    redoStack.clear();
  }

  public void undo() {
    if (!undoStack.isEmpty()) {
      UndoableAction action = undoStack.pop();
      action.undo();
      redoStack.push(action);
    }
  }

  public void redo() {
    if (!redoStack.isEmpty()) {
      UndoableAction action = redoStack.pop();
      action.redo();
      undoStack.push(action);
    }
  }

  public boolean canUndo() {
    return !undoStack.isEmpty();
  }

  public boolean canRedo() {
    return !redoStack.isEmpty();
  }
}