package view;

import action.actions.EditActions;
import action.actions.FileActions;
import action.actions.OutlineActions;
import view.columns.ColumnType;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

public class Menubar extends JMenuBar {

    public Menubar(FileActions fileActions, OutlineActions outlineActions, EditActions editActions, JFrame mainJFrame, OutlinePanel outlinePanel) {
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
            KeyStroke.getKeyStroke(KeyEvent.VK_N, KeyEvent.SHIFT_DOWN_MASK | KeyEvent.CTRL_DOWN_MASK)
        );


        addColumnItem.addActionListener(e -> {

            JPanel panel = new JPanel(new GridLayout(2,2,5,5));


            JTextField titleField =
                new JTextField("Neue Spalte");


            JComboBox<ColumnType> typeBox =
                new JComboBox<>(ColumnType.values());


            panel.add(new JLabel("Titel:"));
            panel.add(titleField);

            panel.add(new JLabel("Typ:"));
            panel.add(typeBox);



            int result = JOptionPane.showConfirmDialog(
                this,
                panel,
                "Neue Spalte",
                JOptionPane.OK_CANCEL_OPTION);


            if(result == JOptionPane.OK_OPTION) {


                String title =
                    titleField.getText().trim();


                if(title.isEmpty()) {
                    title = "Spalte";
                }


                ColumnType type =
                    (ColumnType) typeBox.getSelectedItem();



                outlinePanel.addColumn(
                    title,
                    type);
            }

        });


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
        JMenuItem expandAll = menuItem(outlineActions.expandAllAction);
        ansicht.add(expandAll);

        JMenuItem collapseAll = menuItem(outlineActions.collapseAllAction);
        ansicht.add(collapseAll);

        this.add(ansicht);

        // Hilfe-Menü TODO: muss noch gemacht werden
        JMenu hilfe = new JMenu("Hilfe");
        JMenuItem about = menuItem("Über…", null, e -> showAbout(mainJFrame));
        hilfe.add(about);
        this.add(hilfe);
    }

    private JMenuItem menuItem(String text, KeyStroke ks, ActionListener al) {
        JMenuItem item = new JMenuItem(text);
        if (ks != null) item.setAccelerator(ks);
        item.addActionListener(al);
        return item;
    }

    private JMenuItem menuItem(Action action) {
        return new JMenuItem(action);
    }

    // TODO:  die Methode muss hier noch raus
    private void showAbout(JFrame mainJFrame) {
        JOptionPane.showMessageDialog(mainJFrame,
                "Outliner v1.0\nErstellt mit Java Swing\n\nKlasse: MainWindow + OutlinePanel + TextWindow",
                "Über Outliner", JOptionPane.INFORMATION_MESSAGE);
    }
}
