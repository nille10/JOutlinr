package controller;

import com.fasterxml.jackson.databind.JsonNode;
import model.OutlineNode;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.tree.DefaultTreeModel;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
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
            importJson();
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

    private void saveDialog() throws Exception {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Datei Speichern");
        fileChooser.addChoosableFileFilter(new FileNameExtensionFilter("JSON-Dateien (*.json)", "json"));
        fileChooser.addChoosableFileFilter(new FileNameExtensionFilter("XML-Dateien (*.xml)", "xml"));
        fileChooser.setAcceptAllFileFilterUsed(false);

        int result = fileChooser.showSaveDialog(jFrame);
        if (result == JFileChooser.APPROVE_OPTION) {
            File temp = fileChooser.getSelectedFile();
            if (temp != null) {
                if (!temp.getName().toLowerCase().endsWith(".json") && fileChooser.getFileFilter().getDescription().contains("JSON")) {
                    saveLocation = new File(temp.getAbsolutePath() + ".json");
                    Files.writeString(saveLocation.toPath(), toJsonString(treeModel));
                } else if (!temp.getName().toLowerCase().endsWith(".xml") && fileChooser.getFileFilter().getDescription().contains("XML")) {
                    saveLocation = new File(temp.getAbsolutePath() + ".xml");
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
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Datei Öffnen");
        fileChooser.addChoosableFileFilter(new FileNameExtensionFilter("JSON-Dateien (*.json)", "json"));
        fileChooser.addChoosableFileFilter(new FileNameExtensionFilter("XML-Dateien (*.xml)", "xml"));
        fileChooser.setAcceptAllFileFilterUsed(false);

        int result = fileChooser.showOpenDialog(jFrame);

        if (result == JFileChooser.APPROVE_OPTION) {
            File temp = fileChooser.getSelectedFile();
            if (temp.getName().toLowerCase().endsWith(".json")) {
                saveLocation = new File(temp.getAbsolutePath());
                return 1;
            } else if (temp.getName().toLowerCase().endsWith(".xml")) {
                saveLocation = new File(temp.getAbsolutePath());
                return 2;
            }
        } else if (result != JFileChooser.CANCEL_OPTION) {
            JOptionPane.showMessageDialog(jFrame, "Fehler beim Öffnen der Datei", "Fehler", JOptionPane.ERROR_MESSAGE);
            return -1;
        }
        return -1;
    }

    private String export(OutlineNode root) throws Exception {
        ObjectMapper mapper = new ObjectMapper();

        ObjectNode jsonRoot = toJsonNode(root, mapper);

        return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(jsonRoot);
    }

    private void importJson() {
        try {
            JsonNode rootJson = new ObjectMapper().readTree(saveLocation);
            OutlineNode rootNode = fromJson(rootJson);

            treeModel.setRoot(rootNode);
            treeModel.reload();
        }catch (Exception ex) {
            JOptionPane.showMessageDialog(jFrame, "Fehler beim Laden:\n" + ex.getMessage(), "Fehler", JOptionPane.ERROR_MESSAGE);
        }
    }

    private static ObjectNode toJsonNode(OutlineNode treeNode, ObjectMapper mapper) {

        ObjectNode node = mapper.createObjectNode();

        node.put("title", treeNode.getTitle());
        node.put("content", treeNode.getContent());

        ArrayNode children = mapper.createArrayNode();

        for (int i = 0; i < treeNode.getChildCount(); i++) {
            OutlineNode child = (OutlineNode) treeNode.getChildAt(i);

            children.add(toJsonNode(child, mapper));
        }

        node.set("children", children);

        return node;
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

    private static OutlineNode fromJson(JsonNode json) {
        OutlineNode node = new OutlineNode(json.get("title").asText());

        node.setContent(json.get("content").asText());

        JsonNode children = json.get("children");

        if (children != null) {
            for (JsonNode child : children) {
                node.add(fromJson(child));
            }
        }

        return node;
    }

    private OutlineNode fromJson(File saveLocation) throws IOException {
        StringBuilder json = new StringBuilder(getJson(saveLocation));
        return new OutlineNode("");
    }

    private StringBuilder getJson(File saveLocation) throws IOException {
        StringBuilder json = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new FileReader(saveLocation))) {
            String line = br.readLine();

            while (line != null) {
                json.append(line);
                json.append(System.lineSeparator());
                line = br.readLine();
            }
        }
        System.out.println(json.toString());
        return json;
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
                case "content" -> {
                    node.setContent(element.getTextContent());
                }

                case "node" -> {
                    OutlineNode childNode = parseNode(element);
                    node.add(childNode);
                }
            }
        }

        return node;
    }

}