/*
  KI generiert
*/
package action.actions.commands;

import action.manager.UndoableAction;
import controller.OutlineController;
import model.OutlineNode;

import javax.swing.tree.DefaultTreeModel;
import java.util.ArrayList;
import java.util.List;

/**
 * Der Command ist für das Einfügen eines oder mehrerer Knoten (inklusive Teilbäumen)
 * aus der Zwischenablage zuständig.
 *
 * <p>Die Knoten werden in der übergebenen Reihenfolge, die der Reihenfolge im
 * ursprünglichen Baum entspricht, als letzte Kinder des Zielknotens angehängt.
 * Zielknoten ist der ausgewählte Knoten, ohne Auswahl oder wenn die maximale
 * Verschachtelungstiefe verletzt würde die Wurzel. Passt auch dort nicht jeder
 * Teilbaum hin, wird nichts eingefügt ({@link #isPossible()}).</p>
 *
 * <p>Undo entfernt alle eingefügten Knoten, Redo fügt dieselben Knotenobjekte
 * an denselben Indizes wieder ein. Die Spaltenwerte, die in {@code Column} über
 * den Knoten als Schlüssel abgelegt sind, bleiben dadurch erhalten.</p>
 */
public class PasteCommand implements UndoableAction {

    private final OutlineController controller;
    private final List<OutlineNode> nodesToPaste;

    private OutlineNode pasteParent;
    private int pasteIndex;
    private boolean wasExecuted;

    /**
     * Erstellt einen neuen PasteCommand für mehrere Knoten.
     * @param controller der OutlineController vom Outliner
     * @param nodesToPaste die einzufügenden Teilbäume in der gewünschten Reihenfolge
     */
    public PasteCommand(OutlineController controller, List<OutlineNode> nodesToPaste) {
        this.controller = controller;
        this.nodesToPaste = new ArrayList<>(nodesToPaste);
    }

    /**
     * Erstellt einen neuen PasteCommand für einen einzelnen Knoten.
     * @param controller der OutlineController vom Outliner
     * @param nodeToPaste der einzufügende Teilbaum
     */
    public PasteCommand(OutlineController controller, OutlineNode nodeToPaste) {
        this(controller, List.of(nodeToPaste));
    }

    /**
     * Prüft, ob die Knoten an der aktuellen Auswahl oder ersatzweise in der Wurzel
     * eingefügt werden können (maximale Verschachtelungstiefe), und legt dabei den
     * Zielknoten fest.
     *
     * @return {@code true}, wenn alle Teilbäume eingefügt werden können
     */
    public boolean isPossible() {
        if (nodesToPaste.isEmpty()) return false;

        OutlineNode selected = controller.getSelectedNode();
        OutlineNode parent = selected != null ? selected : controller.getRootNode();

        if (!fitsInto(parent)) {
            // Tiefenlimit würde verletzt → zur Root zurückfallen
            parent = controller.getRootNode();
            if (!fitsInto(parent)) {
                return false;
            }
        }

        pasteParent = parent;
        return true;
    }

    private boolean fitsInto(OutlineNode parent) {
        for (OutlineNode node : nodesToPaste) {
            if (!controller.canPasteInto(parent, node)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Fügt die Knoten am Ende des Zielknotens ein.
     */
    @Override
    public void execute() {
        wasExecuted = false;

        if (!isPossible()) return;

        pasteIndex = pasteParent.getChildCount();

        doInsert();
        wasExecuted = true;
    }

    /**
     * Macht das Einfügen der Knoten wieder rückgängig.
     */
    @Override
    public void undo() {
        if (!wasExecuted) return;

        DefaultTreeModel model = controller.getTreeModel();
        for (OutlineNode node : nodesToPaste) {
            model.removeNodeFromParent(node);
        }
        controller.markDirty();
    }

    /**
     * Macht das Ausführen von {@link #undo()} rückgängig.
     */
    @Override
    public void redo() {
        if (!wasExecuted) return;
        doInsert();
    }

    /**
     * Fügt die Knoten hintereinander ab {@code pasteIndex} ein und wählt sie aus.
     */
    private void doInsert() {
        DefaultTreeModel model = controller.getTreeModel();

        int index = pasteIndex;
        for (OutlineNode node : nodesToPaste) {
            model.insertNodeInto(node, pasteParent, index++);
        }

        for (OutlineNode node : nodesToPaste) {
            controller.expandSubtree(node);
        }
        controller.selectNodes(nodesToPaste);
        controller.markDirty();
    }
}