/*

KI generiert

 */

package view;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class LatexExportDialog {

    public enum Mode { STANDARD, EXPERT, CANCELLED }

    public record LatexMetadata(
            String fachbereich,
            String abteilung,
            String titel,
            String artDerArbeit,   // z.B. "Bericht", "Projektarbeit"
            List<String> autoren,
            String ort,
            List<String> betreuer
    ) {}

    public static Mode askMode(JFrame parent) {

        Object[] options = {"Standard", "Benutzerdefiniert", "Abbrechen"};

        int choice = JOptionPane.showOptionDialog(
                parent,
                "Wie soll der LaTeX-Export erstellt werden?",
                "LaTeX-Export",
                JOptionPane.YES_NO_CANCEL_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,
                options,
                options[0]
        );

        return switch (choice) {
            case 0 -> Mode.STANDARD;
            case 1 -> Mode.EXPERT;
            default -> Mode.CANCELLED;
        };
    }

    public static LatexMetadata askMetadata(JFrame parent) {

        JTextField fachbereichField = new JTextField("Technik");
        JTextField abteilungField = new JTextField("Elektrotechnik und Informatik");
        JTextField titelField = new JTextField();
        JTextField artField = new JTextField("Bericht");
        JTextArea autorenArea = new JTextArea(4, 20);
        JTextField ortField = new JTextField("Emden");
        JTextArea betreuerArea = new JTextArea(3, 20);

        JPanel formPanel = new JPanel(new GridLayout(0, 2, 6, 6));

        formPanel.add(new JLabel("Fachbereich:"));
        formPanel.add(fachbereichField);
        formPanel.add(new JLabel("Abteilung:"));
        formPanel.add(abteilungField);
        formPanel.add(new JLabel("Titel der Arbeit:"));
        formPanel.add(titelField);
        formPanel.add(new JLabel("Art der Arbeit:"));
        formPanel.add(artField);
        formPanel.add(new JLabel("Ort:"));
        formPanel.add(ortField);

        JPanel autorenPanel = new JPanel(new BorderLayout());
        autorenPanel.add(new JLabel("Autoren (einer pro Zeile):"), BorderLayout.NORTH);
        autorenPanel.add(new JScrollPane(autorenArea), BorderLayout.CENTER);

        JPanel betreuerPanel = new JPanel(new BorderLayout());
        betreuerPanel.add(new JLabel("Betreuer (einer pro Zeile):"), BorderLayout.NORTH);
        betreuerPanel.add(new JScrollPane(betreuerArea), BorderLayout.CENTER);

        JPanel listsPanel = new JPanel(new GridLayout(1, 2, 10, 0));
        listsPanel.add(autorenPanel);
        listsPanel.add(betreuerPanel);

        JPanel wrapper = new JPanel(new BorderLayout(0, 10));
        wrapper.add(formPanel, BorderLayout.NORTH);
        wrapper.add(listsPanel, BorderLayout.CENTER);

        int result = JOptionPane.showConfirmDialog(
                parent,
                wrapper,
                "Angaben für Titelseite",
                JOptionPane.OK_CANCEL_OPTION
        );

        if (result != JOptionPane.OK_OPTION) {
            return null;
        }

        return new LatexMetadata(
                fachbereichField.getText().trim(),
                abteilungField.getText().trim(),
                titelField.getText().trim(),
                artField.getText().trim(),
                parseLines(autorenArea.getText()),
                ortField.getText().trim(),
                parseLines(betreuerArea.getText())
        );
    }

    private static List<String> parseLines(String text) {
        List<String> lines = new ArrayList<>();
        for (String line : text.split("\n")) {
            String trimmed = line.trim();
            if (!trimmed.isEmpty()) {
                lines.add(trimmed);
            }
        }
        return lines;
    }
}