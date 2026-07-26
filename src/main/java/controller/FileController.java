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

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.awt.event.WindowEvent;
import java.io.*;
import java.nio.file.Files;


public class FileController {

    private final JFrame jFrame;
    private final DefaultTreeModel treeModel;
    private File saveLocation;

    public FileController(DefaultTreeModel treeModel, JFrame jFrame) {
        this.jFrame = jFrame;
        this.treeModel = treeModel;
    }

    // Platzhalter
    public void newDocument()    { JOptionPane.showMessageDialog(jFrame, "Neu – noch nicht implementiert.");            }

    public void openDocument() throws Exception {
        int result = openDialog();
        if (result == 1) {
            fromJson();
        } else if (result == 2) {
            importXML();
        }
    }

    public void saveDocument() throws Exception {
        if (saveLocation != null) {
            if (saveLocation.toPath().endsWith(".json")) {
                Files.writeString(saveLocation.toPath(), toJsonString(treeModel));
            } else if (saveLocation.toPath().endsWith(".xml")) {
                Files.writeString(saveLocation.toPath(), toXMLString(treeModel));
            }
        } else {
            saveDialog();
        }
    }

    public void saveDocumentAs() throws Exception {
        saveDialog();
    }

    public void close() {
        jFrame.dispatchEvent(new WindowEvent(jFrame, WindowEvent.WINDOW_CLOSING));
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

        int result = fileChooser.getResult(jFrame);
        if (result == JFileChooser.APPROVE_OPTION) {
            return new File(fileChooser.getFile().getAbsolutePath() + ".tex");

        }
        return null;
    }

    private void saveDialog() throws Exception {
        CFileChooser fileChooser = new CFileChooser("Datei Speichern",
                new FileNameExtensionFilter("JSON-Dateien (*.json)", "json"),
                new FileNameExtensionFilter("XML-Dateien (*.xml)", "xml")
        );

        int result = fileChooser.getResult(jFrame);
        if (result == JFileChooser.APPROVE_OPTION) {
            if (fileChooser.getFile() != null) {
                if (!fileChooser.getFile().getName().toLowerCase().endsWith(".json") && fileChooser.getFileFilter().getDescription().contains("JSON")) {
                    saveLocation = new File(fileChooser.getFile().getAbsolutePath() + ".json");
                    Files.writeString(saveLocation.toPath(), toJsonString(treeModel));
                } else if (!fileChooser.getFile().getName().toLowerCase().endsWith(".xml") && fileChooser.getFileFilter().getDescription().contains("XML")) {
                    saveLocation = new File(fileChooser.getFile().getAbsolutePath() + ".xml");
                    Files.writeString(saveLocation.toPath(), toXMLString(treeModel));
                } else{
                    JOptionPane.showMessageDialog(jFrame, "Bitte einen gültigen Pfad angeben.", "Fehler", JOptionPane.ERROR_MESSAGE);
                }
            }
        } else if (result != JFileChooser.CANCEL_OPTION) {
            JOptionPane.showMessageDialog(jFrame, "Fehler beim Speichern der Datei", "Fehler", JOptionPane.ERROR_MESSAGE);
        }
    }

    private int openDialog() {
        CFileChooser fileChooser = new CFileChooser("Datei Öffnen",
                new FileNameExtensionFilter("JSON-Dateien (*.json)", "json"),
                new FileNameExtensionFilter("XML-Dateien (*.xml)", "xml")
                );

        int result = fileChooser.getResult(jFrame);

        if (result == JFileChooser.APPROVE_OPTION) {
            if (fileChooser.getFile().getName().toLowerCase().endsWith(".json")) {
                saveLocation = new File(fileChooser.getFile().getAbsolutePath());
                return 1;
            } else if (fileChooser.getFile().getName().toLowerCase().endsWith(".xml")) {
                saveLocation = new File(fileChooser.getFile().getAbsolutePath());
                return 2;
            }
        } else if (result != JFileChooser.CANCEL_OPTION) {
            JOptionPane.showMessageDialog(jFrame, "Fehler beim Öffnen der Datei", "Fehler", JOptionPane.ERROR_MESSAGE);
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

        treeModel.setRoot(jsonParser.parse());
        treeModel.reload();
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

    public String toXMLString(DefaultTreeModel treeModel) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.newDocument();

        Element rootElement = doc.createElement("outline");
        doc.appendChild(rootElement);

        OutlineNode rootNode = (OutlineNode) treeModel.getRoot();
        appendNode(doc, rootElement, rootNode);

        Transformer transformer = TransformerFactory.newInstance().newTransformer();
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        //transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");

        StringWriter writer = new StringWriter();
        transformer.transform(new DOMSource(doc), new StreamResult(writer));

        return writer.toString();
    }

    private void appendNode(Document doc, Element parentElement, OutlineNode node) {
        Element nodeElement = doc.createElement("node");
        nodeElement.setAttribute("title", node.getTitle());

        Element contentElement = doc.createElement("content");
        contentElement.appendChild(doc.createTextNode(node.getContent()));
        nodeElement.appendChild(contentElement);

        parentElement.appendChild(nodeElement);

        for (int i = 0; i < node.getChildCount(); i++) {
            OutlineNode child = (OutlineNode) node.getChildAt(i);
            appendNode(doc, nodeElement, child);
        }
    }

    public void importXML() throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();

        Document doc = builder.parse(saveLocation);

        Element rootElement = doc.getDocumentElement();
        Element rootNodeElement = (Element) rootElement.getElementsByTagName("node").item(0);

        if (rootNodeElement == null) {
            throw new IllegalArgumentException("Keine Root-Node gefunden");
        }

        treeModel.setRoot(parseNode(rootNodeElement));
        treeModel.reload();
    }

    private OutlineNode parseNode(Element nodeElement) {
        String title = nodeElement.getAttribute("title");

        OutlineNode node = new OutlineNode(title);

        NodeList children = nodeElement.getChildNodes();

        for (int i = 0; i < children.getLength(); i++) {
            Node child = children.item(i);

            if (!(child instanceof Element element)) {
                continue;
            }

            switch (element.getTagName()) {
                case "content" -> node.setContent(element.getTextContent());

                case "node" -> {
                    OutlineNode childNode = parseNode(element);
                    node.add(childNode);
                }
            }
        }

        return node;
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
}

class CFileChooser extends JFileChooser {

    CFileChooser(String dialogTitle, FileNameExtensionFilter... filters) {
        this.setDialogTitle(dialogTitle);
        for (FileNameExtensionFilter filter : filters)
            this.addChoosableFileFilter(filter);
        this.setAcceptAllFileFilterUsed(false);
    }

    public int getResult(JFrame mainFrame) {
        return this.showSaveDialog(mainFrame);
    }

    public File getFile(){
        return this.getSelectedFile();
    }
}


