package controller;

import action.actions.commands.DeleteEntryCommand;
import action.actions.commands.PasteCommand;
import action.manager.UndoManager;
import model.OutlineNode;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.swing.tree.TreeNode;

/**
 * Der Controller ist für die Undo/Redo, sowie für die cut, copy, insert Operationen zuständig.
 *
 * <p>Ausschneiden und Kopieren legen den ausgewählten Knoten (inklusive
 * Teilbaum und den aktuellen Werten aller Zusatzspalten) als XML-Text in der
 * System-Zwischenablage ({@link Clipboard}) ab, statt ihn wie zuvor nur in
 * einem Feld dieser Instanz zu halten. Die Umwandlung übernimmt
 * {@link ClipboardXML}. Dadurch funktioniert Einfügen sowohl innerhalb
 * derselben Instanz als auch in eine andere, parallel laufende Instanz der
 * Anwendung — und die Spaltenwerte gehen dabei (anders als zuvor bei
 * {@code deepCopy()}) nicht mehr verloren, da sie Teil des XML-Texts sind.</p>
 *
 * @see UndoManager
 * @see ClipboardXML
 */
public class EditController {

    private final JFrame jFrame;
    private final UndoManager undoManager;
    private final OutlineController outlineController;

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
     * Schneidet die ausgewählten Knoten aus dem JTree aus.
     *
     * <p>Serialisiert die Knoten zuerst mit ihren aktuellen Spaltenwerten in
     * die Zwischenablage ({@link #copyToClipboard(List)}) und löscht
     * sie erst danach über die rückgängig machbare
     * {@link DeleteEntryCommand}. Die Reihenfolge ist wichtig: Die
     * Spaltenwerte sind in {@code Column} über den Original-Knoten als
     * Schlüssel gespeichert, die Serialisierung muss also erfolgen, solange
     * die Knoten noch im Baum sind.</p>
     */
    public void cut() {
        List<OutlineNode> topLevel = getTopLevelSelection();
        if (topLevel.isEmpty()) return;

        copyToClipboard(topLevel);

        undoManager.execute(new DeleteEntryCommand(outlineController));

        outlineController.getTree().clearSelection();
    }

    /**
     * Kopiert die ausgewählten Knoten (inklusive Teilbäumen und den Werten
     * aller Zusatzspalten) als XML-Text in die Zwischenablage.
     */
    public void copy() {
        List<OutlineNode> topLevel = getTopLevelSelection();

        if (!topLevel.isEmpty()) {
            copyToClipboard(topLevel);
        }
    }

    /**
     * Ermittelt die ausgewählten Knoten, die tatsächlich kopiert werden müssen.
     *
     * <p>Knoten, deren Vorfahre ebenfalls ausgewählt ist, werden weggelassen,
     * da der Vorfahre sie als Teil seines Teilbaums ohnehin enthält. Die Wurzel
     * wird ausgeschlossen. Die Reihenfolge entspricht der Reihenfolge im Baum
     * und nicht der Reihenfolge, in der geklickt wurde.</p>
     *
     * @return die obersten ausgewählten Knoten in Baum-Reihenfolge
     */
    private List<OutlineNode> getTopLevelSelection() {
        Set<OutlineNode> selected = new HashSet<>(outlineController.getSelectedNodes());
        selected.remove(outlineController.getRootNode());

        List<OutlineNode> result = new ArrayList<>();
        Enumeration<?> order = outlineController.getRootNode().preorderEnumeration();
        while (order.hasMoreElements()) {
            OutlineNode node = (OutlineNode) order.nextElement();
            if (!selected.contains(node)) continue;

            boolean ancestorSelected = false;
            for (TreeNode p = node.getParent(); p != null; p = p.getParent()) {
                if (selected.contains(p)) {
                    ancestorSelected = true;
                    break;
                }
            }
            if (!ancestorSelected) {
                result.add(node);
            }
        }
        return result;
    }

    /**
     * Serialisiert {@code nodes} per {@link ClipboardXML#serialize(List, List)}
     * und legt das Ergebnis als Text in der System-Zwischenablage ab.
     *
     * @param nodes die zu kopierenden Knoten (samt Teilbäumen)
     */
    private void copyToClipboard(List<OutlineNode> nodes) {
        String xml = ClipboardXML.serialize(nodes, outlineController.getSpalten());

        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        clipboard.setContents(new StringSelection(xml), null);
    }

    /**
     * Fügt die Knoten aus der Zwischenablage an der markierten Stelle ein.
     *
     * <p>Liest den Textinhalt der System-Zwischenablage aus und übergibt ihn
     * an {@link ClipboardXML#deserializeAll(String, OutlineController)}. Daraus
     * entstehen neue, unabhängige Teilbäume, da jeder Aufruf den Text frisch
     * parst. Passt auch nur einer der Teilbäume wegen der maximalen
     * Verschachtelungstiefe weder an die Auswahl noch in die Wurzel, wird
     * nichts eingefügt und eine Meldung angezeigt (alles oder nichts).</p>
     *
     * <p>Enthält die Zwischenablage keinen Text oder keinen von dieser
     * Anwendung stammenden XML-Text, geschieht nichts.</p>
     */
    public void insert() {
        String xml = readClipboardText();
        if (xml == null) return;

        List<OutlineNode> nodes = ClipboardXML.deserializeAll(xml, outlineController);
        if (nodes.isEmpty()) return;

        PasteCommand command = new PasteCommand(outlineController, nodes);
        if (!command.isPossible()) {
            outlineController.writeStatusBar("Zu viele Ebenen (maximal 3 zugelassen), es wurde nichts eingefügt");
            return;
        }

        undoManager.execute(command);
    }

    /**
     * Liest den aktuellen Textinhalt der System-Zwischenablage aus.
     *
     * @return der Text aus der Zwischenablage, oder {@code null}, wenn sie
     *         leer ist, keinen Text enthält, oder (z. B. weil ein anderes
     *         Programm sie exklusiv hält) nicht gelesen werden konnte
     */
    private String readClipboardText() {
        try {
            Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
            Transferable contents = clipboard.getContents(null);

            if (contents != null && contents.isDataFlavorSupported(DataFlavor.stringFlavor)) {
                return (String) contents.getTransferData(DataFlavor.stringFlavor);
            }
        } catch (Exception e) {
            // Zwischenablage nicht lesbar -> wie leere Zwischenablage behandeln
        }
        return null;
    }
}