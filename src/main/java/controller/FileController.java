package controller;

import model.JSONParser;
import model.OutlineNode;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.tree.DefaultTreeModel;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import model.XMLParser;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import view.MainWindow;
import view.columns.ColumnHeader;
import view.columns.OutlineColumn;

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

            OutlineNode root = outlineController.getRootNode();

            root.removeAllChildren();
            root.setTitle("Outliner");
            root.setContent("");

            treeModel.reload();

            saveLocation = null;

            outlineController.saved();

            mainWindow.getStatusBar().setText(" Bereit");
        }
    }

    public void openDocument() {
        if (checkSavedStatus() != SaveStatus.SAVE_FAILED) {
            int result = openDialog();
            if (result == 1) {

                try {
                    XMLParser xmlParser = new XMLParser(outlineController);
                    xmlParser.importXML(saveLocation);
                } catch (Exception e) {
                    System.out.println(e.getMessage());
                }

            } else if (result == 2) {
                try {
                    fromJson();
                } catch (IOException e) {
                    System.out.println(e.getMessage());
                }
            }
            outlineController.saved();
        }
    }

    public void saveDocument() throws Exception {
        /*System.out.println(outlineController.getHeaderTitle());
        for (ColumnHeader c :outlineController.getSpaltenNamen()) {
            System.out.println(c.getTitle());
        }
        for (OutlineColumn c : outlineController.getSpalten()) {
            System.out.println(c.getType());
        }*/


        if (saveLocation != null) {
            if (saveLocation.getName().toLowerCase().endsWith("xml")) {
                XMLParser xmlParser = new XMLParser(outlineController);
                Files.writeString(saveLocation.toPath(), xmlParser.toXMLString(treeModel));
            } else if (saveLocation.getName().toLowerCase().endsWith(".json")) {
                Files.writeString(saveLocation.toPath(), toJsonString(treeModel));
            }
        } else {
            saveDialog();
        }
        if(saveLocation != null)
            updateLastSavedTime();
        outlineController.saved();
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

    private void saveDialog() throws Exception {
        CFileChooser fileChooser = new CFileChooser("Datei Speichern",
                new FileNameExtensionFilter("XML-Dateien (*.xml)", "xml"),
                new FileNameExtensionFilter("JSON-Dateien (*.json)", "json")
        );

        int result = fileChooser.getSaveResult(mainWindow);
        if (result == JFileChooser.APPROVE_OPTION) {
            if (fileChooser.getFile() != null) {
                if (!fileChooser.getFile().getName().toLowerCase().endsWith(".json") && fileChooser.getFileFilter().getDescription().contains("JSON")) {
                    saveLocation = new File(fileChooser.getFile().getAbsolutePath() + ".json");
                    Files.writeString(saveLocation.toPath(), toJsonString(treeModel));
                } else if (!fileChooser.getFile().getName().toLowerCase().endsWith(".xml") && fileChooser.getFileFilter().getDescription().contains("XML")) {
                    saveLocation = new File(fileChooser.getFile().getAbsolutePath() + ".xml");
                    XMLParser xmlParser = new XMLParser(outlineController);
                    Files.writeString(saveLocation.toPath(), xmlParser.toXMLString(treeModel));
                } else{
                    JOptionPane.showMessageDialog(mainWindow, "Bitte einen gültigen Pfad angeben.", "Fehler", JOptionPane.ERROR_MESSAGE);
                }
            }
        } else if (result != JFileChooser.CANCEL_OPTION) {
            JOptionPane.showMessageDialog(mainWindow, "Fehler beim Speichern der Datei", "Fehler", JOptionPane.ERROR_MESSAGE);
        }
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

    private static String toJsonString(DefaultTreeModel treeModel) {
        StringBuilder json = new StringBuilder("{\n");
        OutlineNode root = (OutlineNode) treeModel.getRoot();

        json.append(JsonChild(0, root));
        json.append("\n}");

        return json.toString();
    }

    private static StringBuilder JsonChild(int amountParents, OutlineNode node) {
        StringBuilder sb = new StringBuilder();

        // Spaces
        StringBuilder spaces = new StringBuilder("  ");
        spaces.repeat("  ", Math.max(0, amountParents));

        sb.append(spaces).append("\"title\" : \"").append(node.getTitle()).append("\",\n");
        sb.append(spaces).append("\"content\" : \"").append(node.getContent()).append("\",\n");
        sb.append(spaces).append("\"children\" : ").append("[ ");
        if (node.getChildCount() == 0) {
            sb.append("]");
            return sb;
        } else if (node.getChildCount() == 1) {
            sb.append("{\n");
            sb.append(JsonChild(++amountParents,(OutlineNode) node.getChildAt(0)));
            sb.append("\n").append(spaces).append("} ]");
        } else {
            amountParents++;
            for (int i = 0; i < node.getChildCount(); i++) {
                sb.append("{\n");
                if (node.getChildCount() -1 == i) {
                    sb.append(JsonChild(amountParents, (OutlineNode) node.getChildAt(i)));
                    sb.append("\n").append(spaces).append("} ]");
                } else {
                    sb.append(JsonChild(amountParents, (OutlineNode) node.getChildAt(i)));
                    sb.append("\n").append(spaces).append("}, ");
                }
            }
        }
        return sb;
    }

    private void fromJson() throws IOException {
        JSONParser jsonParser = new JSONParser(getJson());

        outlineController.cancelActiveEditing();

        OutlineNode parsedRoot = jsonParser.parse();
        replaceRootContents(parsedRoot);
    }

    private String getJson() throws IOException {
        StringBuilder json = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new FileReader(saveLocation))) {
            String line = br.readLine();

            while (line != null) {
                json.append(line);
                json.append(System.lineSeparator());
                line = br.readLine();
            }
        }
        return json.toString();
    }

    private void replaceRootContents(OutlineNode parsedRoot) {
        OutlineNode actualRoot = outlineController.getRootNode();

        actualRoot.removeAllChildren();
        actualRoot.setTitle(parsedRoot.getTitle());
        actualRoot.setContent(parsedRoot.getContent());

        while (parsedRoot.getChildCount() > 0) {
            OutlineNode child = (OutlineNode) parsedRoot.getChildAt(0);
            actualRoot.add(child);
        }

        treeModel.reload();
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
                    saveDocument();
                    return SaveStatus.SAVED;
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


