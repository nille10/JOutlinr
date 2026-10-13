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
     * Schneidet einen Knoten aus dem JTree aus.
     *
     * <p>Serialisiert den Knoten zuerst mit seinen aktuellen Spaltenwerten in
     * die Zwischenablage ({@link #copyToClipboard(OutlineNode)}) und löscht
     * ihn erst danach über die rückgängig machbare
     * {@link DeleteEntryCommand}. Die Reihenfolge ist wichtig: Die
     * Spaltenwerte sind in {@code Column} über den Original-Knoten als
     * Schlüssel gespeichert, die Serialisierung muss also erfolgen, solange
     * der Knoten noch im Baum ist.</p>
     */
    public void cut() {
        OutlineNode selected = outlineController.getSelectedNode();
        if (selected == null) return;

        copyToClipboard(selected);

        undoManager.execute(new DeleteEntryCommand(outlineController));

        outlineController.getTree().clearSelection();
    }

    /**
     * Kopiert den ausgewählten Knoten (inklusive Teilbaum und den Werten
     * aller Zusatzspalten) als XML-Text in die Zwischenablage.
     */
    public void copy() {
        OutlineNode selected = outlineController.getSelectedNode();

        if (selected != null) {
            copyToClipboard(selected);
        }
    }

    /**
     * Serialisiert {@code node} per {@link ClipboardXML#serialize(OutlineNode, java.util.List)}
     * und legt das Ergebnis als Text in der System-Zwischenablage ab.
     *
     * @param node der zu kopierende Knoten (samt Teilbaum)
     */
    private void copyToClipboard(OutlineNode node) {
        String xml = ClipboardXML.serialize(node, outlineController.getSpalten());

        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        clipboard.setContents(new StringSelection(xml), null);
    }

    /**
     * Fügt den Knoten aus der Zwischenablage an der markierten Stelle ein.
     *
     * <p>Liest den Textinhalt der System-Zwischenablage aus und übergibt ihn
     * an {@link ClipboardXML#deserialize(String, OutlineController)}. Daraus
     * entsteht ein neuer, unabhängiger {@link OutlineNode}-Teilbaum — ein
     * zusätzliches {@code deepCopy()} wie zuvor ist nicht mehr nötig, da jeder
     * Aufruf von {@link #insert()} den Text erneut frisch parst und damit
     * ohnehin neue Knoten-Objekte erzeugt, auch bei mehrfachem Einfügen
     * desselben Zwischenablage-Inhalts.</p>
     *
     * <p>Enthält die Zwischenablage keinen Text, keinen von dieser Anwendung
     * stammenden XML-Text (z. B. weil zuvor in einem anderen Programm etwas
     * kopiert wurde), oder konnte er nicht gelesen werden, geschieht nichts.</p>
     */
    public void insert() {
        String xml = readClipboardText();
        if (xml == null) return;

        OutlineNode node = ClipboardXML.deserialize(xml, outlineController);
        if (node == null) return;

        undoManager.execute(
                new PasteCommand(outlineController, node));
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