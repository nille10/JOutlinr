/*

KI generiert

 */

package view;

import model.LatexExportMode;
import model.LatexMetadata;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Dialoge für den LaTeX-Export des Outliners.
 *
 * <p>Bietet zwei aufeinanderfolgende Dialogschritte: zuerst die Auswahl des
 * Export-Modus ({@link #askMode(JFrame)}), danach — falls benutzerdefiniert
 * gewünscht — die Abfrage der Titelseiten-Angaben ({@link #askMetadata(JFrame)}).
 * Beide Methoden geben bei Abbruch {@code null} bzw. {@link LatexExportMode#CANCELLED}
 * zurück, statt eine Exception zu werfen.</p>
 *
 * <p>MVC-Umbau: {@code Mode} und {@code LatexMetadata} waren ursprünglich
 * verschachtelte Typen dieser Klasse. Da beide reine Daten ohne Swing-Bezug sind,
 * stehen sie jetzt als eigenständige Typen in {@code model}
 * ({@link LatexExportMode}, {@link LatexMetadata}); diese Klasse enthält nur noch
 * die Swing-Dialog-Logik, die sie befüllt.</p>
 */
public class LatexExportDialog {

    /**
     * Fragt den gewünschten Export-Modus ab.
     *
     * <p>Zeigt einen Dialog mit den drei Optionen "Standard", "Benutzerdefiniert"
     * und "Abbrechen". Wird der Dialog anderweitig geschlossen (z. B. über das
     * Fenster-Schließen-Symbol), liefert {@link JOptionPane#showOptionDialog} einen
     * Wert außerhalb von {@code 0}/{@code 1}, was hier ebenfalls als
     * {@link LatexExportMode#CANCELLED} behandelt wird.</p>
     *
     * @param parent das Elternfenster, über dem der Dialog angezeigt wird
     * @return der gewählte {@link LatexExportMode}
     */
    public static LatexExportMode askMode(JFrame parent) {

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
            case 0 -> LatexExportMode.STANDARD;
            case 1 -> LatexExportMode.EXPERT;
            default -> LatexExportMode.CANCELLED;
        };
    }

    /**
     * Fragt die Titelseiten-Angaben für den benutzerdefinierten Export ab.
     *
     * <p>Zeigt ein Formular mit Textfeldern für Fachbereich, Abteilung, Titel, Art
     * der Arbeit und Ort sowie zwei mehrzeiligen Textbereichen für Autoren und
     * Betreuer (eine Person pro Zeile, leere Zeilen werden über
     * {@link #parseLines(String)} herausgefiltert). Die Felder sind mit
     * sinnvollen Vorgaben vorbelegt</p>
     *
     * @param parent das Elternfenster, über dem der Dialog angezeigt wird
     * @return die eingegebenen {@link LatexMetadata}, oder {@code null}, wenn der
     *         Dialog nicht mit "OK" bestätigt wurde
     */
    public static LatexMetadata askMetadata(JFrame parent) {

        JTextField fachbereichField = new JTextField("Technik");
        JTextField abteilungField = new JTextField("Elektrotechnik und Informatik");
        JTextField titelField = new JTextField();
        JTextField artField = new JTextField("Bericht");
        JTextArea autorenArea = new JTextArea(4, 20);
        JTextField ortField = new JTextField("Deine Stadt");
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

    /**
     * Zerlegt mehrzeiligen Text in eine Liste nicht-leerer, getrimmter Zeilen.
     *
     * <p>Jede Zeile wird getrimmt; leere Zeilen (auch solche, die nur aus
     * Leerzeichen bestehen) werden verworfen und tauchen nicht in der
     * zurückgegebenen Liste auf.</p>
     *
     * @param text der zu zerlegende Text, Zeilen getrennt durch {@code \n}
     * @return die Liste der nicht-leeren, getrimmten Zeilen in ihrer ursprünglichen Reihenfolge
     */
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