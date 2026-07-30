package action.actions.commands;

import action.manager.UndoableAction;
import controller.OutlineController;
import model.OutlineNode;

public class DeleteEntryCommand implements UndoableAction {

  private final OutlineController controller;

  private OutlineNode node;
  private OutlineNode parent;
  private int index;

  public DeleteEntryCommand(OutlineController controller) {
    this.controller = controller;
  }

  @Override
  public void execute() {

    node = controller.getSelectedNode();

    if (node == null)
      return;

    parent = (OutlineNode) node.getParent();
    index = parent.getIndex(node);

    controller.deleteNode(node);
  }

  @Override
  public void undo() {

    controller.insertNode(parent, node, index);
    controller.expandSubtree(node);
  }
}