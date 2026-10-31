package view;

import action.actions.EditActions;
import action.actions.FileActions;
import action.actions.OutlineActions;
import controller.OutlineController;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.Desktop;
import java.net.URI;
/**
 * Die Menüleiste des Hauptfensters.
 *
 * <p>Baut die Menüs "Datei", "Bearbeiten", "Gliederung", "Ansicht",
 * "Einstellungen" und "Hilfe" auf und verdrahtet jeden Eintrag entweder mit
 * einer vorgefertigten {@link Action} aus {@link FileActions},
 * {@link OutlineActions} bzw. {@link EditActions}, oder — für Einträge ohne
 * eigene Action-Klasse ("Spalte hinzufügen", "Einstellungen…", "Über…") —
 * direkt mit einem Lambda, das den passenden Controller bzw. Dialog aufruft.
 * Hält selbst keinen Zustand.</p>
 */
public class Menubar extends JMenuBar {

    /**
     * Baut die komplette Menüleiste auf.
     *
     * @param fileActions       vorgefertigte Actions für das Datei-Menü
     * @param outlineActions    vorgefertigte Actions für das Gliederung- und Ansicht-Menü
     * @param editActions       vorgefertigte Actions für das Bearbeiten-Menü
     * @param mainJFrame        das Hauptfenster, über dem Dialoge (Einstellungen, Über) angezeigt werden
     * @param outlinePanel      das Outline-Panel (aktuell nur zur Signatur gehörend, im Rumpf nicht verwendet)
     * @param outlineController der Controller, der z. B. für "Spalte hinzufügen" und die Einstellungen angesprochen wird
     */
    public Menubar(FileActions fileActions, OutlineActions outlineActions, EditActions editActions, JFrame mainJFrame, OutlinePanel outlinePanel, OutlineController outlineController) {
        // Datei-Menü
        JMenu datei = new JMenu("Datei");
        datei.setMnemonic(KeyEvent.VK_D);

        JMenuItem newFile = menuItem(fileActions.newFileAction);
        datei.add(newFile);

        JMenuItem open = menuItem(fileActions.openFileAction);
        datei.add(open);

        JMenuItem save = menuItem(fileActions.saveFileAction);
        datei.add(save);

        JMenuItem saveAs = menuItem(fileActions.saveAsFileAction);
        datei.add(saveAs);

        JMenuItem exportAsLatex = menuItem(fileActions.exportAsLatexAction);
        datei.add(exportAsLatex);

        datei.addSeparator();
        JMenuItem close = menuItem(fileActions.closeAction);
        datei.add(close);

        this.add(datei);

        // Bearbeiten-Menü
        JMenu bearbeiten = new JMenu("Bearbeiten");
        bearbeiten.setMnemonic(KeyEvent.VK_B);

        JMenuItem undo = menuItem(editActions.undoAction);
        bearbeiten.add(undo);

        JMenuItem redo = menuItem(editActions.redoAction);
        bearbeiten.add(redo);

        bearbeiten.addSeparator();

        JMenuItem addColumnItem =
                new JMenuItem("Spalte hinzufügen");

        addColumnItem.setAccelerator(
                KeyStroke.getKeyStroke(KeyEvent.VK_N, KeyEvent.SHIFT_DOWN_MASK | Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx())
        );


        addColumnItem.addActionListener(e -> outlineController.addColumn());


        bearbeiten.add(addColumnItem);

        bearbeiten.addSeparator();

        JMenuItem cut = menuItem(editActions.cutAction);
        bearbeiten.add(cut);

        JMenuItem copy = menuItem(editActions.copyAction);
        bearbeiten.add(copy);

        JMenuItem insert = menuItem(editActions.insertAction);
        bearbeiten.add(insert);

        this.add(bearbeiten);

        // Gliederung-Menü
        JMenu gliederung = new JMenu("Gliederung");
        gliederung.setMnemonic(KeyEvent.VK_G);

        JMenuItem newEntry = menuItem(outlineActions.newEntryAction);
        gliederung.add(newEntry);

        JMenuItem newChildEntry = menuItem(outlineActions.newChildEntryAction);
        gliederung.add(newChildEntry);

        JMenuItem deleteEntry = menuItem(outlineActions.deleteEntryAction);
        gliederung.add(deleteEntry);

        gliederung.addSeparator();

        JMenuItem promote = menuItem(outlineActions.promoteEntryAction);
        gliederung.add(promote);

        JMenuItem demote = menuItem(outlineActions.demoteEntryAction);
        gliederung.add(demote);

        this.add(gliederung);

        // Ansicht-Menü
        JMenu ansicht = new JMenu("Ansicht");

        JMenuItem expandEntry = menuItem(outlineActions.expandEntryAction);
        ansicht.add(expandEntry);

        JMenuItem collapseEntry = menuItem(outlineActions.collapseEntryAction);
        //ansicht.add(collapseEntry);

        ansicht.addSeparator();

        JMenuItem expandAll = menuItem(outlineActions.expandAllAction);
        ansicht.add(expandAll);

        JMenuItem collapseAll = menuItem(outlineActions.collapseAllAction);
        //ansicht.add(collapseAll);

        this.add(ansicht);

        //Settings
        JMenuItem einstellungenItem = new JMenuItem("Einstellungen…");
        einstellungenItem.setAccelerator(
                KeyStroke.getKeyStroke(KeyEvent.VK_COMMA,
                        Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()));
        einstellungenItem.addActionListener(e ->
                new SettingsWindow(mainJFrame, outlineController).setVisible(true));

        JMenu einstellungen = new JMenu("Einstellungen");
        einstellungen.add(einstellungenItem);
        this.add(einstellungen);

        JMenu hilfe = new JMenu("Hilfe");
        JMenuItem about = menuItem("Benutzerhandbuch", null, e -> showAbout(mainJFrame));
        hilfe.add(about);
        this.add(hilfe);
    }

    /**
     * Erstellt einen Menüeintrag mit eigenem Text, optionalem Tastaturkürzel und
     * einem direkt übergebenen {@link ActionListener}.
     *
     * <p>Für Einträge gedacht, die keine eigene {@link Action}-Klasse haben
     * (z. B. "Über…").</p>
     *
     * @param text Beschriftung des Menüeintrags
     * @param ks   Tastaturkürzel, oder {@code null} für keines
     * @param al   der auszuführende Listener
     * @return der fertig konfigurierte Menüeintrag
     */
    private JMenuItem menuItem(String text, KeyStroke ks, ActionListener al) {
        JMenuItem item = new JMenuItem(text);
        if (ks != null) item.setAccelerator(ks);
        item.addActionListener(al);
        return item;
    }

    /**
     * Erstellt einen Menüeintrag aus einer vorgefertigten {@link Action}.
     *
     * <p>Übernimmt Beschriftung, Tastaturkürzel und Enabled-Status automatisch
     * von der {@link Action}.</p>
     *
     * @param action die Action, aus der der Menüeintrag erzeugt wird
     * @return der fertig konfigurierte Menüeintrag
     */
    private JMenuItem menuItem(Action action) {
        return new JMenuItem(action);
    }

    /**
     * Öffnet das User Manual im Browser.
     *
     * @param mainJFrame das Elternfenster, über dem der Dialog angezeigt wird
     */
    private void showAbout(JFrame mainJFrame) {
        try {
            URI manualUri = new URI(
                    "https://github.com/nille10/JOutlinr/blob/main/Ideen.md"
            );

            if (Desktop.isDesktopSupported()
                    && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                Desktop.getDesktop().browse(manualUri);
            } else {
                JOptionPane.showMessageDialog(
                        mainJFrame,
                        "Der Browser kann nicht automatisch geöffnet werden.\n"
                                + "Du findest das Handbuch hier:\n"
                                + manualUri,
                        "Über JOutlinr",
                        JOptionPane.INFORMATION_MESSAGE
                );
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(
                    mainJFrame,
                    "Das Handbuch konnte nicht im Browser geöffnet werden.\n"
                            + "Öffne diesen Link manuell:\n"
                            + "https://github.com/nille10/JOutlinr/blob/main/Ideen.md",
                    "Über JOutlinr",
                    JOptionPane.INFORMATION_MESSAGE
            );
        }
    }
}