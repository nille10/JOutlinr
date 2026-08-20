/*

erstellt mit KI

 */
package action.actions.commands;

import action.manager.UndoableAction;
import controller.OutlineController;
import model.OutlineNode;

import java.util.ArrayList;
import java.util.List;

/**
 * Der Command ist für das Löschen von Knoten aus dem Outliner zuständig.
 */
public class DeleteEntryCommand implements UndoableAction {

  private final OutlineController controller;

  private record DeletedEntry(OutlineNode node, OutlineNode parent, int index) {}

  private final List<DeletedEntry> deleted = new ArrayList<>();
  private final List<OutlineNode> originallySelected = new ArrayList<>();

  /**
   * Erstellt einen neuen DeleteEntryCommand mit UndoManager.
   * @param controller der OutlineController von Outliner
   */
  public DeleteEntryCommand(OutlineController controller) {
    this.controller = controller;
  }

  /**
   * Löscht die ausgewählten Knoten im Outliner.
   * <p>
   *   Nach dem Löschen wird der nächste Knoten ausgewählt, falls {@code outliner.getSelectLastEntryOption()} true ist.
   *   Das löschen wird auf einem Undo Stack abgelegt.
   * </p>
   */
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

    if(controller.getSelectLastEntryOption()){
      // Nach dem Löschen den Knoten vor dem ersten gelöschten auswählen
      DeletedEntry first = deleteOrder.getLast(); // kleinster Index
      OutlineNode parent = first.parent();
      int prevIndex = first.index() - 1;

      if (prevIndex >= 0 && prevIndex < parent.getChildCount()) {
        // Knoten direkt darüber
        controller.selectNode((OutlineNode) parent.getChildAt(prevIndex));
      } else if (parent.getChildCount() > 0) {
        // Kein Vorgänger aber noch Kinder → ersten nehmen
        controller.selectNode((OutlineNode) parent.getChildAt(0));
      } else if (parent != controller.getRootNode()) {
        // Parent selbst wenn er kein Root ist
        controller.selectNode(parent);
      }
    }
  }

  /**
   * Das Löschen eines Knotens wird wieder rückgängig gemacht.
   */
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

  /**
   * Überprüft ob der Knoten ausgewählte Elternknoten hat.
   * @param node der zu prüfende Knoten
   * @param selected die ausgewählten Knoten
   * @return {@code true} wenn die Node einen ausgewählten Elternknoten hat, ansonsten {@code false}
   */
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