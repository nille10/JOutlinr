/*

erstellt mit KI

 */
package action.actions.commands;

import action.manager.UndoableAction;
import controller.OutlineController;
import model.OutlineNode;

import java.util.ArrayList;
import java.util.List;

public class DeleteEntryCommand implements UndoableAction {

  private final OutlineController controller;

  private record DeletedEntry(OutlineNode node, OutlineNode parent, int index) {}

  private final List<DeletedEntry> deleted = new ArrayList<>();
  private final List<OutlineNode> originallySelected = new ArrayList<>();

  public DeleteEntryCommand(OutlineController controller) {
    this.controller = controller;
  }

  @Override
  public void execute() {

    // Wichtig: execute() wird bei Redo erneut aufgerufen –
    // alte Zustände aus vorherigem Durchlauf müssen verworfen werden
    deleted.clear();
    originallySelected.clear();

    List<OutlineNode> selected = controller.getSelectedNodes();

    if (selected.isEmpty())
      return;

    originallySelected.addAll(selected);

    List<OutlineNode> topLevel = new ArrayList<>();

    for (OutlineNode node : selected) {
      if (!hasSelectedAncestor(node, selected)) {
        topLevel.add(node);
      }
    }

    for (OutlineNode node : topLevel) {
      OutlineNode parent = (OutlineNode) node.getParent();
      int index = parent.getIndex(node);
      deleted.add(new DeletedEntry(node, parent, index));
    }

    List<DeletedEntry> deleteOrder = new ArrayList<>(deleted);
    deleteOrder.sort((a, b) -> Integer.compare(b.index(), a.index()));

    List<OutlineNode> nodesToRemove = new ArrayList<>();
    for (DeletedEntry entry : deleteOrder) {
      nodesToRemove.add(entry.node());
    }

    controller.deleteNodes(nodesToRemove);
  }

  @Override
  public void undo() {

    List<DeletedEntry> undoOrder = new ArrayList<>(deleted);
    undoOrder.sort((a, b) -> Integer.compare(a.index(), b.index()));

    for (DeletedEntry entry : undoOrder) {
      controller.insertNode(entry.parent(), entry.node(), entry.index());
      controller.expandSubtree(entry.node());
    }

    // Die GESAMTE ursprüngliche Auswahl wiederherstellen (inkl. Kinder,
    // die nur implizit mit ihrem Parent zurückkamen)
    controller.selectNodes(originallySelected);
  }

  private boolean hasSelectedAncestor(OutlineNode node, List<OutlineNode> selected) {

    OutlineNode parent = (OutlineNode) node.getParent();

    while (parent != null) {
      if (selected.contains(parent)) {
        return true;
      }
      parent = (OutlineNode) parent.getParent();
    }

    return false;
  }
}