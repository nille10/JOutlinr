/*
KI generiert
 */

package action.actions.commands;

import action.manager.UndoableAction;
import controller.OutlineController;
import model.OutlineNode;

import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Command zum Verschieben eines oder mehrerer Einträge an eine andere Stelle im Baum
 * (Drag and Drop, auch mit Mehrfachauswahl) mit Undo/Redo-Unterstützung.
 *
 * <p>Wie die übrigen strukturverändernden Commands sichert diese Klasse ihren Zustand
 * (Elternknoten und Index jedes verschobenen Knotens) schon im Konstruktor und
 * operiert direkt auf dem {@link DefaultTreeModel}, nicht über die höherwertigen
 * Methoden des {@link OutlineController}. Dadurch lösen Entfernen und Einfügen keine
 * Nebeneffekte aus (z. B. {@code textWindow.loadNode(null)}), die den Baumzustand
 * zwischen beiden Schritten verfälschen könnten.</p>
 *
 * <p>Es werden stets dieselben {@link OutlineNode}-Objekte verschoben. Die
 * Spaltenwerte, die in {@code Column} über den Knoten als Schlüssel abgelegt sind,
 * bleiben dadurch bei Verschieben, Undo und Redo erhalten.</p>
 *
 * <p>Folgende Knoten werden beim Verschieben nicht berücksichtigt.</p>
 * <ul>
 *   <li>Knoten ohne Elternknoten (die Wurzel).</li>
 *   <li>Knoten, in deren eigenen Teilbaum (oder auf sich selbst) verschoben würde.</li>
 *   <li>Knoten, deren Vorfahre ebenfalls verschoben wird, da dieser sie als Teil
 *       seines Teilbaums ohnehin mitnimmt.</li>
 * </ul>
 *
 * <p>Die Prüfung der maximalen Verschachtelungstiefe ist nicht Aufgabe dieser Klasse,
 * sondern muss vor dem Erzeugen erfolgen.</p>
 */
public class MoveEntryCommand implements UndoableAction {

    private final OutlineController controller;
    private final DefaultTreeModel model;

    /** Zielknoten, unter dem die Einträge eingefügt werden. */
    private final OutlineNode target;

    /** Die tatsächlich verschobenen Knoten in Baum-Reihenfolge. */
    private final List<OutlineNode> nodes = new ArrayList<>();

    /** Ursprünglicher Elternknoten je verschobenem Knoten (gleicher Index wie {@link #nodes}). */
    private final List<OutlineNode> oldParents = new ArrayList<>();

    /** Ursprünglicher Index je verschobenem Knoten im alten Elternknoten. */
    private final List<Integer> oldIndices = new ArrayList<>();

    /** Index im Zielknoten, ab dem eingefügt wird (nachdem alle Knoten entfernt wurden). */
    private final int insertIndex;

    /** {@code true}, wenn das Verschieben die Reihenfolge im Baum tatsächlich verändert. */
    private final boolean hasEffect;

    /**
     * Erstellt einen neuen {@code MoveEntryCommand}.
     *
     * <p>Der Baumzustand muss beim Erzeugen noch dem Zustand vor dem Verschieben
     * entsprechen, da hier die ursprünglichen Positionen gesichert werden.</p>
     *
     * @param controller  der {@link OutlineController} des Outliners
     * @param nodesToMove die zu verschiebenden Knoten, in beliebiger Reihenfolge
     * @param target      der Knoten, unter dem die Einträge eingefügt werden
     * @param dropIndex   gewünschter Index im Zielknoten, bezogen auf dessen Kinder
     *                    vor dem Verschieben (bei einem Wert außerhalb des gültigen
     *                    Bereichs wird er begrenzt)
     */
    public MoveEntryCommand(OutlineController controller,
                            Collection<OutlineNode> nodesToMove,
                            OutlineNode target,
                            int dropIndex) {
        this.controller = controller;
        this.model = controller.getTreeModel();
        this.target = target;

        collectTopLevelNodes(nodesToMove);

        for (OutlineNode node : nodes) {
            OutlineNode parent = (OutlineNode) node.getParent();
            oldParents.add(parent);
            oldIndices.add(parent.getIndex(node));
        }

        // Index im Zielknoten vor dem Verschieben auf gültigen Bereich begrenzen
        int index = Math.max(0, Math.min(dropIndex, target.getChildCount()));

        // Knoten, die aus dem Zielknoten selbst verschoben werden und vor der
        // Zielposition lagen, verschieben die Zielposition beim Entfernen nach vorn
        int shift = 0;
        for (int i = 0; i < nodes.size(); i++) {
            if (oldParents.get(i) == target && oldIndices.get(i) < index) {
                shift++;
            }
        }
        this.insertIndex = index - shift;

        boolean changes = false;
        for (int i = 0; i < nodes.size(); i++) {
            if (oldParents.get(i) != target || oldIndices.get(i) != insertIndex + i) {
                changes = true;
                break;
            }
        }
        this.hasEffect = !nodes.isEmpty() && changes;
    }

    /**
     * Wählt aus den übergebenen Knoten diejenigen aus, die tatsächlich verschoben
     * werden, und legt sie in Baum-Reihenfolge in {@link #nodes} ab.
     *
     * @param candidates die vom Aufrufer übergebenen Knoten
     */
    private void collectTopLevelNodes(Collection<OutlineNode> candidates) {
        Set<OutlineNode> selected = new HashSet<>();
        for (OutlineNode node : candidates) {
            if (node.getParent() != null && !target.isNodeAncestor(node)) {
                selected.add(node);
            }
        }

        // Knoten mit ebenfalls ausgewähltem Vorfahren entfernen
        Set<OutlineNode> topLevel = new HashSet<>();
        for (OutlineNode node : selected) {
            boolean ancestorSelected = false;
            for (OutlineNode other : selected) {
                if (other != node && node.isNodeAncestor(other)) {
                    ancestorSelected = true;
                    break;
                }
            }
            if (!ancestorSelected) {
                topLevel.add(node);
            }
        }

        // In Baum-Reihenfolge bringen
        Enumeration<?> order = controller.getRootNode().preorderEnumeration();
        while (order.hasMoreElements()) {
            OutlineNode node = (OutlineNode) order.nextElement();
            if (topLevel.contains(node)) {
                nodes.add(node);
            }
        }
    }

    /**
     * Gibt an, ob dieser Command etwas verändern würde.
     *
     * <p>Das ist nicht der Fall, wenn keine gültigen Knoten übrig bleiben oder die
     * Knoten ohnehin schon an der Zielposition stehen. Ein solcher Command sollte
     * nicht ausgeführt werden, damit kein wirkungsloser Eintrag im Undo-Stack
     * entsteht.</p>
     *
     * @return {@code true}, wenn das Verschieben die Baumstruktur verändert
     */
    public boolean hasEffect() {
        return hasEffect;
    }

    /**
     * Verschiebt die Knoten an die Zielposition und wählt sie anschließend aus.
     */
    @Override
    public void execute() {
        controller.commitEditing();

        for (OutlineNode node : nodes) {
            model.removeNodeFromParent(node);
        }

        int index = insertIndex;
        for (OutlineNode node : nodes) {
            model.insertNodeInto(node, target, index++);
        }

        controller.getTree().expandPath(new TreePath(model.getPathToRoot(target)));
        controller.selectNodes(nodes);
        controller.markDirty();
    }

    /**
     * Setzt jeden Knoten an seinen ursprünglichen Elternknoten und Index zurück
     * und wählt die Knoten wieder aus.
     *
     * <p>Zuerst werden alle Knoten entfernt und danach in Baum-Reihenfolge wieder
     * eingefügt. So ist jeder gesicherte Index beim Einfügen gültig, weil alle Knoten
     * mit kleinerem ursprünglichem Index im selben Elternknoten bereits wieder an ihrer
     * Stelle stehen.</p>
     */
    @Override
    public void undo() {
        controller.commitEditing();

        for (OutlineNode node : nodes) {
            model.removeNodeFromParent(node);
        }

        for (int i = 0; i < nodes.size(); i++) {
            model.insertNodeInto(nodes.get(i), oldParents.get(i), oldIndices.get(i));
        }

        controller.selectNodes(nodes);
        controller.markDirty();
        controller.writeStatusBar("Rückgängig gemacht");
    }
}