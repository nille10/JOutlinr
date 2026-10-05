package view;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.io.File;

/**
 * Vorkonfigurierter {@link JFileChooser} für Öffnen- und Speichern-Dialoge.
 *
 * <p>Erspart das wiederholte Zusammenstellen von Titel und Dateityp-Filtern an
 * jeder Aufrufstelle: Titel und Filter werden einmalig im Konstruktor gesetzt,
 * die vordefinierte "Alle Dateien"-Option wird dabei deaktiviert, sodass nur die
 * übergebenen Filter zur Auswahl stehen.</p>
 */
public class CFileChooser extends JFileChooser {

    /**
     * Erstellt einen neuen Dateiauswahl-Dialog mit Titel und Dateityp-Filtern.
     *
     * @param dialogTitle der im Dialog angezeigte Titel
     * @param filters     die wählbaren Dateityp-Filter; die Option "Alle Dateien"
     *                    wird deaktiviert, sodass ausschließlich diese Filter zur
     *                    Verfügung stehen
     */
    public CFileChooser(String dialogTitle, FileNameExtensionFilter... filters) {
        this.setDialogTitle(dialogTitle);
        for (FileNameExtensionFilter filter : filters)
            this.addChoosableFileFilter(filter);
        this.setAcceptAllFileFilterUsed(false);
    }

    /**
     * Zeigt den Speichern-Dialog modal über dem übergebenen Fenster an.
     *
     * @param mainFrame das Elternfenster, über dem der Dialog angezeigt wird
     * @return eines der {@link JFileChooser}-Ergebnisse, z. B.
     *         {@link JFileChooser#APPROVE_OPTION} oder {@link JFileChooser#CANCEL_OPTION}
     */
    public int getSaveResult(JFrame mainFrame) {
        return this.showSaveDialog(mainFrame);
    }

    /**
     * Zeigt den Öffnen-Dialog modal über dem übergebenen Fenster an.
     *
     * @param mainFrame das Elternfenster, über dem der Dialog angezeigt wird
     * @return eines der {@link JFileChooser}-Ergebnisse, z. B.
     *         {@link JFileChooser#APPROVE_OPTION} oder {@link JFileChooser#CANCEL_OPTION}
     */
    public int getOpenResult(JFrame mainFrame) {return this.showOpenDialog(mainFrame); }

    /**
     * Liefert die vom Benutzer im Dialog ausgewählte Datei.
     *
     * @return die ausgewählte Datei, oder {@code null}, wenn keine ausgewählt wurde
     */
    public File getFile(){
        return this.getSelectedFile();
    }
}