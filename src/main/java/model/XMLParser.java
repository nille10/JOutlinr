package model;

import controller.OutlineController;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import view.columns.ColumnType;

import javax.swing.tree.DefaultTreeModel;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.io.StringWriter;

public class XMLParser {

    private final OutlineController outlineController;

    public XMLParser(OutlineController outlineController) {
        this.outlineController = outlineController;
    }

    public void importXML(File saveLocation) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();

        Document doc = builder.parse(saveLocation);

        Element rootElement = doc.getDocumentElement();

        Element headElement = (Element) rootElement.getElementsByTagName("head").item(0);
        Element rootNodeElement = (Element) rootElement.getElementsByTagName("node").item(0);

        if (rootNodeElement == null) {
            throw new IllegalArgumentException("Keine Root-Node gefunden");
        }

        outlineController.cancelActiveEditing();

        if (headElement != null) {
            rebuildColumns(headElement);
        }

        OutlineNode parsedRoot = parseNode(rootNodeElement);
        replaceRootContents(parsedRoot);
    }

    private void rebuildColumns(Element headElement) {

        NodeList columnHeaders = headElement.getElementsByTagName("ColumnHeader");

        for (int i = 0; i < columnHeaders.getLength(); i++) {
            Element headerElement = (Element) columnHeaders.item(i);

            String title = headerElement.getTextContent();
            String typeAttr = headerElement.getAttribute("type");

            ColumnType type = typeAttr.isEmpty()
                    ? ColumnType.TEXTFIELD
                    : ColumnType.valueOf(typeAttr);

            outlineController.addColumn(title, type);
        }
    }

    public String toXMLString(DefaultTreeModel treeModel) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.newDocument();

        Element rootElement = doc.createElement("outline");
        doc.appendChild(rootElement);

        appendHeader(doc, rootElement);

        OutlineNode rootNode = (OutlineNode) treeModel.getRoot();
        appendNode(doc, rootElement, rootNode);

        Transformer transformer = TransformerFactory.newInstance().newTransformer();
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");

        StringWriter writer = new StringWriter();
        transformer.transform(new DOMSource(doc), new StreamResult(writer));

        return writer.toString();
    }

    private void appendHeader(Document doc, Element rootElement) {
        Element header = doc.createElement("head");
        header.setAttribute("title", outlineController.getHeaderTitle());

        for (var column : outlineController.getSpalten()) {
            Element columnHeader = doc.createElement("ColumnHeader");
            columnHeader.setAttribute("type", column.getType().name());
            columnHeader.setTextContent(column.getTitle());

            header.appendChild(columnHeader);
        }
        rootElement.appendChild(header);
    }

    private void appendNode(Document doc, Element parentElement, OutlineNode node) {
        Element nodeElement = doc.createElement("node");
        nodeElement.setAttribute("title", node.getTitle());

        for (var column : outlineController.getSpalten()) {
            Element columnElement = doc.createElement("column");
            columnElement.setAttribute("columnTitle", column.getTitle());
            columnElement.setAttribute("columnType", column.getType().name());
            columnElement.setAttribute("columnValue", column.getValue(node).toString());
            nodeElement.appendChild(columnElement);
        }

        Element contentElement = doc.createElement("content");
        contentElement.appendChild(doc.createTextNode(node.getContent()));
        nodeElement.appendChild(contentElement);

        parentElement.appendChild(nodeElement);

        for (int i = 0; i < node.getChildCount(); i++) {
            OutlineNode child = (OutlineNode) node.getChildAt(i);
            appendNode(doc, nodeElement, child);
        }
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

        outlineController.getTreeModel().reload();
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

                case "column" -> {
                    String columnTitle = element.getAttribute("columnTitle");
                    String rawValue = element.getAttribute("columnValue");

                    for (var column : outlineController.getSpalten()) {
                        if (column.getTitle().equals(columnTitle)) {
                            Object value = column.getType() == ColumnType.CHECKBOX ? Boolean.parseBoolean(rawValue) : rawValue;
                            column.setValue(node, value);
                            break;
                        }
                    }
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
