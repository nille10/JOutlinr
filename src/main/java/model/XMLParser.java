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

/**
 * Der {@code XMLParser} ist für das Importieren und Exportieren von Outlines im XML-Format zuständig.
 *
 * <p>
 *   Die Klasse nutzt die W3C-DOM-API, um XML-Dateien einzulesen oder eine Baumstruktur
 * ({@link DefaultTreeModel}) inklusive Metadaten und Spaltenkonfigurationen in ein XML-Dokument
 * zu serialisieren.
 * </p>
 */
public class XMLParser {

    private final OutlineController outlineController;

    /**
     * Erstellt einen neuen {@code XMLParser} mit dem angegebenen Controller.
     *
     * @param outlineController der Controller für den Zugriff auf die Outline-Daten
     */
    public XMLParser(OutlineController outlineController) {
        this.outlineController = outlineController;
    }

    /**
     * Liest die XML-Datei von dem angegebenen Dateipfad und stellt die Outline Struktur wider her.
     *
     * <p>
     * Laufende Editiervorgänge werden gestoppt und die struktur aus der XML-Datei wird im {@link OutlineController} wieder hergestellt.
     * </p>
     *
     * @param saveLocation die XML-Datei die importiert werden soll
     * @throws Exception wenn beim Parsen der Datei ein fehler auftritt
     */
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
        outlineController.replaceRootContents(parsedRoot);
    }

    /**
     * Wandelt den Inhalt des {@link OutlineController} in einen String in XML-Format um.
     * <p>
     * Erzeugt mit hilfe der W3C-DOM-API einen {@code String} im XML-Format,
     * aufgeteilt in Header (siehe {@link #appendHeader(Document, Element)}) und Nodes (siehe {@link #appendNode(Document, Element, OutlineNode)}.
     * </p>
     * @param treeModel TreeModel mit dem Inhalt des Outliners
     * @return {@code String} im XML-Format
     * @throws Exception wenn es beim Erstellen des {@code DocumentBuilder} oder {@code Transformer} einen fehler gab
     */
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

    /**
     * Baut die Spalten im {@link OutlineController} aus dem Importiertem header Element wieder auf.
     *
     * <p>
     * Stellt den Titel des Outliners wieder her und für die Spaltennamen mit dem richtigen Typen wieder her.
     * </p>
     *
     * @param headElement das Headelement aus der XML-Datei
     */
    private void rebuildColumns(Element headElement) {

        outlineController.setHeaderTitle(headElement.getAttribute("title"));

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

    /**
     * Erzeugt aus dem Inhalt des {@link OutlineController} den Header der XML-Datei mit den Informationen zur Überschrift und den Spaltennamen.
     * @param doc das {@code Document} das Ziel XML-Dokument
     * @param rootElement das RootElement des Outliners
     */
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

    /**
     * Fügt die {@link OutlineNode} sowie alle Kindknoten aus dem {@link OutlineController} zum XML-Dokument hinzu.
     *
     * @param doc das {@code Document} das Ziel XML-Dokument
     * @param parentElement der Elternknoten des zu erfassenden Knoten
     * @param node der zu erfassende Knoten
     */
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

    /**
     * Parst ein XML-Element rekursiv in eine {@link OutlineNode} um.
     *
     * @param nodeElement das zu parsende XML-Dokument
     * @return die vollständig geparste {@link OutlineNode}
     */
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
