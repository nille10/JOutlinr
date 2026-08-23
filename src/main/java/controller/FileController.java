package controller;

import model.JSONParser;
import model.OutlineNode;
import model.XMLParser;
import model.LATEXParser;

import view.AppPreferences;
import view.MainWindow;
import view.CFileChooser;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.tree.DefaultTreeModel;

import java.awt.event.WindowEvent;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.attribute.FileTime;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

/**
 * Der Controller ist für Speichern und Laden von Dateien zuständig
 *<p>
 * Ist zuständig für den Import und Export des JTrees in den verschiedenen Formen (XML, JSON).
 * Koordiniert die Interaktion mit dem {@link CFileChooser}. Speichert zudem den zuletzt verwendeten Ort zum Speichern und die Uhrzeit der letzten Speicherung.
 *</p>
 * @see JSONParser
 * @see XMLParser
 * @see LATEXParser
 * @see OutlineController
 *
 */
public class FileController {

    private final MainWindow mainWindow;
    private final DefaultTreeModel treeModel;
    private final OutlineController outlineController;
    private File saveLocation;
    private FileTime lastSavedTime;

    /**
     * Erstellt einen neuen {@code FileController}
     *
     * @param outlineController Die Hauptsteuerung der Anwendung
     * @param mainWindow JFrame vom MainWindow
     */
    public FileController(OutlineController outlineController, MainWindow mainWindow) {
        this.mainWindow = mainWindow;
        this.treeModel = outlineController.getTreeModel();
        this.outlineController = outlineController;
    }

    /**
     * Erstellt ein neues Dokument
     * <p>
     * Überprüft, ob noch ein ungespeichertes Dokument geöffnet ist und bietet die möglichkeit dies zu Speichern.
     * Aktives bearbeiten des Outliners wird beendet und alle OutlineNodes werden gelöscht.
     * </p>
     */
    public void newDocument() {
        if (checkSavedStatus() != SaveStatus.SAVE_FAILED) {

            outlineController.cancelActiveEditing();
            outlineController.deleteAllColumns();

            OutlineNode root = outlineController.getRootNode();

            root.removeAllChildren();
            root.setTitle("Outliner");
            root.setContent("");

            treeModel.reload();

            saveLocation = null;

            outlineController.setHeaderTitle("Outliner");

            outlineController.saved();

            mainWindow.getStatusBar().setText(" Bereit");
        }
    }

    /**
     * Öffnet einen Dialog um eine Datei zu Importieren
     * <p>
     * Überprüft, ob noch ein ungespeichertes Dokument geöffnet ist und bietet die möglichkeit dies zu Speichern.
     * Wenn erfolgreich eine Datei ausgewählt wird, werden alle aktuellen OutlineNodes gelöscht und die neuen geladen.
     * Falls der Import nicht erfolgreich war, wird ein Fenster mit der entsprechenden Fehlermeldung angezeigt.
     * </p>
     */
    public void openDocument() {
        if (checkSavedStatus() != SaveStatus.SAVE_FAILED) {
            DataType result = openDialog();
            if (result == DataType.XML) {
                try {
                    outlineController.deleteAllColumns();
                    importXML(saveLocation);
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(mainWindow, "Fehler beim Öffnen der Datei: " + e, "Fehler", JOptionPane.ERROR_MESSAGE);
                }
            } else if (result == DataType.JSON) {
                try {
                    outlineController.deleteAllColumns(); // Error Fenster
                    importJson(saveLocation);
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(mainWindow, "Fehler beim Öffnen der Datei: " + e, "Fehler", JOptionPane.ERROR_MESSAGE);
                }
            }
            outlineController.saved();
        }
    }

    /**
     * Speichert das Dokument
     * <p>
     * Wenn bereits ein Dateipfad ({@link #saveLocation}) definiert ist, wird dieser genutzt.
     * Ansonsten öffnet sich ein Dialog, um einen Pfad anzugeben(siehe {@link #saveDialog()}).
     * Bei erfolgreichem Speichern wird der Zeitstempel in {@link #lastSavedTime} aktualisiert und der Status im Controller auf gespeichert gesetzt.
     * </p>
     * @return {@code SaveStatus.SAVED}, wenn erfolgreich gespeichert wurde.
     *         {@code SaveStatus.DONT_SAVE}, wenn der Speicherdialog geschlossen wurde.
     *         {@code SavedStatus.SAVE_FAILED}, wenn der Speicherprozess fehlgeschlagen ist.
     * @throws IOException Wenn beim Schreiben auf der Festplatte ein fehler auftritt.
     * @throws Exception Wenn ein fehler in {@link #saveDialog()} auftritt.
     */
    public SaveStatus saveDocument() throws Exception {
        if (saveLocation != null) {
            if (saveLocation.getName().toLowerCase().endsWith("xml")) {
                Files.writeString(saveLocation.toPath(), toXMLString(treeModel));
            } else if (saveLocation.getName().toLowerCase().endsWith(".json")) {
                Files.writeString(saveLocation.toPath(), toJsonString(treeModel));
            }
        } else {
            return saveDialog();
        }
        if(saveLocation != null)
            updateLastSavedTime();
        outlineController.saved();
        return SaveStatus.SAVED;
    }

    /**
     * Öffnet einen Dialog, um den Speicherort auszuwählen und speichert das Dokument.
     * <p>
     * Öffnet einen Dialog, um einen Pfad zu wählen (siehe {@link #saveDialog()}).
     * Aktualisiert die {@link #lastSavedTime} und den Status im {@link #outlineController} auf Saved.
     * </p>
     * @throws IOException Wenn beim Schreiben auf der Festplatte ein fehler auftritt.
     * @throws Exception Wenn ein fehler in {@link #saveDialog()} auftritt.
     */
    public void saveDocumentAs() throws Exception {
        SaveStatus result = saveDialog();
        if (result == SaveStatus.SAVED) {
            updateLastSavedTime();
            outlineController.saved();
        } else if (result == SaveStatus.SAVE_FAILED) {
            errorWindow("Fehler beim Speichern");
        }
    }

    /**
     * Schließt die Anwendung
     * <p>
     * Das dispatchEvent überprüft vor dem Schleißen, ob das aktuelle Dokument ungespeichert ist.
     * </p>
     */
    public void close() {
        mainWindow.dispatchEvent(new WindowEvent(mainWindow, WindowEvent.WINDOW_CLOSING));
    }

    /**
     * Öffnet einen Dialog und speichert das Dokument im gewählten Pfad (siehe {@code LATEXParser}).
     * <p>
     *     Eine Exception beim Parsen wird in einem {@link #errorWindow(String)} angezeigt.
     * </p>
     * @see LATEXParser
     */
    public void exportAsLatex() {

        LATEXParser latexParser = new LATEXParser(outlineController, mainWindow, treeModel);

        try {
            latexParser.exportAsLatex();
        } catch (IOException e) {
            errorWindow("Fehler beim export: " + e);
        }
    }

    /**
     * Öffnet einen speicher Dialog, um einen Pfad und das Dateiformat zu wählen.
     * <p>
     * Öffnet einen {@code CFileChooser} mit den optionen XML und JSON.
     * Falls das Dokument einen nicht leeren Titel hat, wird dieser als Titel der exportierten Datei vorgeschlagen.
     * Bei einem ungültigen Pfad wird ein {@link #errorWindow(String)} angezeigt.
     * </p>
     * @return {@code SaveStatus.SAVED}, wenn erfolgreich gespeichert wurde.
     *         {@code SaveStatus.DONT_SAVE}, wenn der Speicherdialog geschlossen wurde.
     *         {@code SavedStatus.SAVE_FAILED}, wenn der Speicherprozess fehlgeschlagen ist.
     *
     * @throws Exception wenn beim Schreiben auf der Festplatte ein fehler auftritt.
     */
    private SaveStatus saveDialog() throws Exception {
        CFileChooser fileChooser = new CFileChooser("Datei Speichern",
                new FileNameExtensionFilter("XML-Dateien (*.xml)", "xml"),
                new FileNameExtensionFilter("JSON-Dateien (*.json)", "json")
        );

        if (!outlineController.getHeaderTitle().isEmpty()) {
            fileChooser.setSelectedFile(new File(outlineController.getHeaderTitle()));
        }

        int result = fileChooser.getSaveResult(mainWindow);
        if (result == JFileChooser.APPROVE_OPTION) {
            if (fileChooser.getFile() != null) {
                if (!fileChooser.getFile().getName().toLowerCase().endsWith(".json") && fileChooser.getFileFilter().getDescription().contains("JSON")) {
                    saveLocation = new File(fileChooser.getFile().getAbsolutePath() + ".json");
                    Files.writeString(saveLocation.toPath(), toJsonString(treeModel));
                    return SaveStatus.SAVED;
                } else if (fileChooser.getFile().getName().toLowerCase().endsWith(".json") && fileChooser.getFileFilter().getDescription().contains("JSON")) {
                    saveLocation = new File(fileChooser.getFile().getAbsolutePath());
                    Files.writeString(saveLocation.toPath(), toJsonString(treeModel));
                    return SaveStatus.SAVED;
                }else if (!fileChooser.getFile().getName().toLowerCase().endsWith(".xml") && fileChooser.getFileFilter().getDescription().contains("XML")) {
                    saveLocation = new File(fileChooser.getFile().getAbsolutePath() + ".xml");
                    Files.writeString(saveLocation.toPath(), toXMLString(treeModel));
                    return SaveStatus.SAVED;
                } else if (fileChooser.getFile().getName().toLowerCase().endsWith(".xml") && fileChooser.getFileFilter().getDescription().contains("XML")) {
                    saveLocation = new File(fileChooser.getFile().getAbsolutePath());
                    Files.writeString(saveLocation.toPath(), toXMLString(treeModel));
                    return SaveStatus.SAVED;
                }else{
                    errorWindow("Bitte einen gültigen Pfad angeben.");
                    return SaveStatus.SAVE_FAILED;
                }
            }
        } else if (result != JFileChooser.CANCEL_OPTION) {
            errorWindow("Fehler beim Speichern der Datei");
            return SaveStatus.SAVE_FAILED;
        }
        return SaveStatus.DONT_SAVE;
    }

    /**
     * Öffnet einen Dialog, um eine Datei zu wählen und speichert die gewählte Datei in {@link #saveLocation}.
     * @return {@code DataType.XML}
     *         {@code DataType.JSON}
     *         {@code DataType.ERROR}
     */
    private DataType openDialog() {
        CFileChooser fileChooser = new CFileChooser("Datei Öffnen",
                new FileNameExtensionFilter("XML-Dateien (*.xml)", "xml"),
                new FileNameExtensionFilter("JSON-Dateien (*.json)", "json")
                );

        int result = fileChooser.getOpenResult(mainWindow);

        if (result == JFileChooser.APPROVE_OPTION) {
            if (fileChooser.getFile().getName().toLowerCase().endsWith(".xml")) {
                saveLocation = new File(fileChooser.getFile().getAbsolutePath());
                return DataType.XML;
            } else if (fileChooser.getFile().getName().toLowerCase().endsWith(".json")) {
                saveLocation = new File(fileChooser.getFile().getAbsolutePath());
                return DataType.JSON;
            }
        } else if (result != JFileChooser.CANCEL_OPTION) {
            JOptionPane.showMessageDialog(mainWindow, "Fehler beim Öffnen der Datei", "Fehler", JOptionPane.ERROR_MESSAGE);
            return DataType.ERROR;
        }
        return DataType.ERROR;
    }

    /**
     * Importiert eine JSON Datei
     * @param file die zu importierende JSON-Datei
     * @throws Exception wenn die JSON-Struktur nicht gültig ist
     * @see JSONParser
     */
    private void importJson(File file) throws Exception {
        JSONParser jsonParser = new JSONParser(outlineController);
        jsonParser.importJson(file);
    }

    /**
     * Erzeugt aus dem treeModel einen String im JSON-Format
     * @param treeModel das TreeModel mit der Datenstruktur des Outliners
     * @return String im JSON-Format
     * @see JSONParser
     */
    private String toJsonString(DefaultTreeModel treeModel) {
        JSONParser jsonParser = new JSONParser(outlineController);
        return jsonParser.toJsonString(treeModel);
    }

    /**
     * Importiert eine XML Datei
     * @param file die zu importierende XML-Datei
     * @throws Exception wenn die XML-Struktur nicht gültig ist
     * @see XMLParser
     */
    private void importXML(File file) throws Exception {
        XMLParser xmlParser = new XMLParser(outlineController);
        xmlParser.importXML(file);
    }

    /**
     * Erzeugt aus dem treeModel einen String im XML-Format
     * @param treeModel das TreeModel mit der Datenstruktur des Outliners
     * @return String im XML-Format
     * @throws Exception wenn der DokumentBuilder im XMLParser nicht erstellt werden konnte (siehe {@link XMLParser#toXMLString(DefaultTreeModel)}).
     */
    private String toXMLString(DefaultTreeModel treeModel) throws Exception {
        XMLParser xmlParser = new XMLParser(outlineController);
        return xmlParser.toXMLString(treeModel);
    }

    /**
     * Aktualisiert die {@link #lastSavedTime} und zeigt diese in der StatusBar des Outliners an.
     * @throws IOException wenn beim Lesen der META-Daten der Datei ein fehler auftritt.
     */
    private void updateLastSavedTime() throws IOException {
        lastSavedTime = Files.getLastModifiedTime(saveLocation.toPath());
        LocalDateTime localDateTime = LocalDateTime.ofInstant(lastSavedTime.toInstant(), ZoneId.systemDefault());
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss");

        mainWindow.getStatusBar().setText("Last Saved: " + localDateTime.format(dateTimeFormatter));
    }

    /**
     * Überprüft, ob es ungespeicherte Änderungen gibt. Fragt ab, ob diese gespeichert werden sollen, wenn vorhanden.
     * @return {@code SaveStatus.NO_CHANGES} wenn es keine ungespeicherten Änderungen gibt
     *         {@code SaveStatus.DONT_SAVE} wenn das Dokument nicht gespeichert werden soll
     *         {@code SaveStatus.SAVED} wenn das Dokument erfolgreich gespeichert wurde
     *         {@code SaveStatus.SAVE_FAILED} wenn ein fehler beim Speichern aufgetreten ist
     * @see #saveDocument()
     */
    public SaveStatus checkSavedStatus() {
        if (outlineController.isDirty()) {
            int result = JOptionPane.showConfirmDialog(
                    mainWindow,
                    "Änderungen speichern?",
                    "Unsaved changes",
                    JOptionPane.YES_NO_OPTION
            );
            if (result == JOptionPane.YES_OPTION) {
                try {
                    return saveDocument();
                } catch (Exception ignored) {
                    return SaveStatus.SAVE_FAILED;
                }
            } else if (result == JOptionPane.NO_OPTION) {
                return SaveStatus.DONT_SAVE;
            } else {
                return SaveStatus.SAVE_FAILED;
            }
        }
        return SaveStatus.NO_CHANGES;
    }

    /**
     * Speichert die {@link #saveLocation} in den {@link AppPreferences}.
     */
    public void saveLastPath() {
        if (saveLocation != null) {
            AppPreferences.setLastFile(saveLocation.toString());
        }
    }

    /**
     * Importiert eine Datei, die unter {@link AppPreferences} gespeichert wurde.
     * <p>
     *     Falls beim Schließen des Outliners ein Pfad gespeichert wurde, wird die Datei geöffnet.
     *     Falls die Datei nicht mehr existiert oder keine gespeichert wurde passiert nichts.
     * </p>
     * @see AppPreferences
     */
    public void tryImportLastEditedFile() {
        String lastPath = AppPreferences.getLastFile();

        if (!lastPath.isEmpty()) {
            if (lastPath.endsWith("xml")) {
                try {
                    importXML(new File(lastPath));
                    saveLocation = new File(lastPath);
                } catch (Exception ignored) {}
            } else if (lastPath.endsWith("json")) {
                try {
                    importJson(new File(lastPath));
                    new File(lastPath);
                } catch (Exception ignored) {}
            }
        }
    }

    /**
     * Zeigt einen fehler in einem Dialog an
     * @param error error Nachricht
     */
    private void errorWindow(String error) {
        JOptionPane.showMessageDialog(mainWindow, error, "Fehler", JOptionPane.ERROR_MESSAGE);
    }
}