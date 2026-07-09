package action.commands;

import action.manager.UndoableAction;
import controller.OutlineController;
import model.OutlineNode;

public class PasteCommand implements UndoableAction {

  private final OutlineController controller;
  private final OutlineNode nodeToPaste;

  private OutlineNode parent;
  private int index;

  public PasteCommand(OutlineController controller,
                      OutlineNode nodeToPaste) {

    this.controller = controller;
    this.nodeToPaste = nodeToPaste;
  }

  @Override
  public void execute() {
    parent = controller.getSelectedNode();

    if (parent == null) {
      parent = controller.getRootNode();
    }

    index = parent.getChildCount();

    if(nodeToPaste != null){
      controller.insertNode(parent, nodeToPaste, index);
    }

  }

  @Override
  public void undo() {
    controller.deleteNode(nodeToPaste);
  }

  @Override
  public void redo() {
    controller.insertNode(parent, nodeToPaste, index);
  }
}