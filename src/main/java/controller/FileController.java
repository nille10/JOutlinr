package controller;

import model.JSONParser;
import model.OutlineNode;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.tree.DefaultTreeModel;

import model.XMLParser;
import view.AppPreferences;
import view.MainWindow;

import java.awt.event.WindowEvent;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.attribute.FileTime;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;


public class FileController {

    private final MainWindow mainWindow;
    private final DefaultTreeModel treeModel;
    private final OutlineController outlineController;
    private File saveLocation;
    private FileTime lastSavedTime;

    public FileController(OutlineController outlineController, MainWindow mainWindow) {
        this.mainWindow = mainWindow;
        this.treeModel = outlineController.getTreeModel();
        this.outlineController = outlineController;
    }

    // Platzhalter
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

    public void openDocument() {
        if (checkSavedStatus() != SaveStatus.SAVE_FAILED) {
            int result = openDialog();
            if (result == 1) {

                try {
                    outlineController.deleteAllColumns();
                    importXML(saveLocation);
                } catch (Exception e) {
                    System.out.println(e.getMessage());
                }

            } else if (result == 2) {
                try {
                    outlineController.deleteAllColumns();
                    importJson(saveLocation);
                } catch (Exception e) {
                    System.out.println(e.getMessage());
                }
            }
            outlineController.saved();
        }
    }

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

    public void saveDocumentAs() throws Exception {
        saveDialog();
        updateLastSavedTime();
        outlineController.saved();
    }

    public void close() {
        mainWindow.dispatchEvent(new WindowEvent(mainWindow, WindowEvent.WINDOW_CLOSING));
    }

    public void exportAsLatex() throws IOException {
        File saveLocation = exportAsLatexDialog();
        if (saveLocation != null)
            Files.writeString(saveLocation.toPath(), toLatexString((OutlineNode) treeModel.getRoot()));
    }

    // Helper Methods

    private File exportAsLatexDialog() {
        CFileChooser fileChooser = new CFileChooser("Export als Latex",
                new FileNameExtensionFilter("Latex_Dateien (*.tex", "tex")
        );

        int result = fileChooser.getSaveResult(mainWindow);
        if (result == JFileChooser.APPROVE_OPTION) {
            return new File(fileChooser.getFile().getAbsolutePath() + ".tex");

        }
        return null;
    }

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
                } else if (!fileChooser.getFile().getName().toLowerCase().endsWith(".xml") && fileChooser.getFileFilter().getDescription().contains("XML")) {
                    saveLocation = new File(fileChooser.getFile().getAbsolutePath() + ".xml");
                    Files.writeString(saveLocation.toPath(), toXMLString(treeModel));
                    return SaveStatus.SAVED;
                } else{
                    JOptionPane.showMessageDialog(mainWindow, "Bitte einen gültigen Pfad angeben.", "Fehler", JOptionPane.ERROR_MESSAGE);
                    return SaveStatus.SAVE_FAILED;
                }
            }
        } else if (result != JFileChooser.CANCEL_OPTION) {
            JOptionPane.showMessageDialog(mainWindow, "Fehler beim Speichern der Datei", "Fehler", JOptionPane.ERROR_MESSAGE);
            return SaveStatus.SAVE_FAILED;
        }
        return SaveStatus.SAVE_FAILED;
    }

    private int openDialog() {
        CFileChooser fileChooser = new CFileChooser("Datei Öffnen",
                new FileNameExtensionFilter("XML-Dateien (*.xml)", "xml"),
                new FileNameExtensionFilter("JSON-Dateien (*.json)", "json")
                );

        int result = fileChooser.getOpenResult(mainWindow);

        if (result == JFileChooser.APPROVE_OPTION) {
            if (fileChooser.getFile().getName().toLowerCase().endsWith(".xml")) {
                saveLocation = new File(fileChooser.getFile().getAbsolutePath());
                return 1;
            } else if (fileChooser.getFile().getName().toLowerCase().endsWith(".json")) {
                saveLocation = new File(fileChooser.getFile().getAbsolutePath());
                return 2;
            }
        } else if (result != JFileChooser.CANCEL_OPTION) {
            JOptionPane.showMessageDialog(mainWindow, "Fehler beim Öffnen der Datei", "Fehler", JOptionPane.ERROR_MESSAGE);
            return -1;
        }
        return -1;
    }

    private void importJson(File file) throws Exception {
        JSONParser jsonParser = new JSONParser(outlineController);
        jsonParser.importJson(file);
    }

    private String toJsonString(DefaultTreeModel treeModel) {
        JSONParser jsonParser = new JSONParser(outlineController);
        return jsonParser.toJsonString(treeModel);
    }


    private void importXML(File file) throws Exception {
        XMLParser xmlParser = new XMLParser(outlineController);
        xmlParser.importXML(file);
    }

    private String toXMLString(DefaultTreeModel treeModel) throws Exception {
        XMLParser xmlParser = new XMLParser(outlineController);
        return xmlParser.toXMLString(treeModel);
    }

    private String toLatexString(OutlineNode node) {
        StringBuilder latexStr = new StringBuilder();
        for (int i = 0; i < node.getChildCount(); i++) {
            latexStr.append(getNodes((OutlineNode) node.getChildAt(i)));
        }
        return latexStr.toString();
    }

    private String getNodes(OutlineNode node) {
        StringBuilder sb = new StringBuilder();
        if (node.getLevel() == 1)
            sb.append("\\section{").append(node.getTitle()).append("}\n");
        else if (node.getLevel() == 2)
            sb.append("\\subsection{").append(node.getTitle()).append("}\n");
        else
            sb.append("\\subsubsection{").append(node.getTitle()).append("}\n");

        if (!node.getContent().isEmpty() && !node.getContent().isBlank())
            sb.append(node.getContent()).append("\n");
        sb.append("\n");

        for (int i = 0; i < node.getChildCount(); i++) {
            sb.append(getNodes((OutlineNode) node.getChildAt(i)));
        }
        return sb.toString();
    }

    private void updateLastSavedTime() throws IOException {
        lastSavedTime = Files.getLastModifiedTime(saveLocation.toPath());
        LocalDateTime localDateTime = LocalDateTime.ofInstant(lastSavedTime.toInstant(), ZoneId.systemDefault());
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss");

        mainWindow.getStatusBar().setText("Last Saved: " + localDateTime.format(dateTimeFormatter));
    }

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

    public void saveLastPath() {
        if (saveLocation != null) {
            AppPreferences.setLastFile(saveLocation.toString());
        }
    }

    public void tryImportLastFile() {
        String lastPath = AppPreferences.getLastFile();

        if (!lastPath.isEmpty()) {
            if (lastPath.endsWith("xml")) {
                try {
                    importXML(new File(lastPath));
                } catch (Exception ignored) {}
            } else if (lastPath.endsWith("json")) {
                try {
                    importJson(new File(lastPath));
                } catch (Exception ignored) {}
            }
        }
    }
}

class CFileChooser extends JFileChooser {

    CFileChooser(String dialogTitle, FileNameExtensionFilter... filters) {
        this.setDialogTitle(dialogTitle);
        for (FileNameExtensionFilter filter : filters)
            this.addChoosableFileFilter(filter);
        this.setAcceptAllFileFilterUsed(false);
    }

    public int getSaveResult(JFrame mainFrame) {
        return this.showSaveDialog(mainFrame);
    }

    public int getOpenResult(JFrame mainFrame) {return this.showOpenDialog(mainFrame); }

    public File getFile(){
        return this.getSelectedFile();
    }


}


