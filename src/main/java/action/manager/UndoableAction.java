package action.manager;

public interface UndoableAction {
  void execute();
  void undo();
  default void redo() {
    execute();
  }
}