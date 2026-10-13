package controller;

import model.OutlineNode;
import model.columns.Column;
import model.columns.ColumnType;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Wandelt einen einzelnen {@link OutlineNode}-Teilbaum (inklusive der Werte
 * aller Zusatzspalten) in einen XML-Text um und zurück.
 *
 * <p>Wird von {@link EditController} für Ausschneiden/Kopieren/Einfügen über
 * die System-Zwischenablage ({@link java.awt.datatransfer.Clipboard}) genutzt.
 * Das Format orientiert sich bewusst am bestehenden {@code model.XMLParser}
 * (gleiche Element-/Attributnamen {@code <node title="…">}, {@code <content>},
 * {@code <column columnTitle columnType columnValue>}, {@code <ColumnHeader
 * type="…">}), ist aber eigenständig implementiert, damit hier nur ein
 * einzelner Teilbaum (statt des gesamten Dokuments) serialisiert wird und das
 * Einfügen fehlende Spalten selbstständig anlegen kann, statt wie beim Import
 * den kompletten Baum zu ersetzen.</p>
 *
 * <p>Da der erzeugte Text über die System-Zwischenablage läuft, funktioniert
 * das Einfügen sowohl innerhalb derselben Instanz als auch in eine andere,
 * parallel laufende Instanz der Anwendung.</p>
 */
public final class ClipboardXML {

    /** Name des Wurzelelements. Dient als einfache Erkennung, ob ein Zwischenablage-Inhalt von dieser Klasse stammt. */
    private static final String ROOT_TAG = "clip";

    private ClipboardXML() {
        // Nur statische Methoden, keine Instanzen nötig.
    }

    /**
     * Serialisiert einen Knoten samt Teilbaum und den Werten aller
     * übergebenen Spalten als XML-Text.
     *
     * <p>Für jeden Knoten im Teilbaum wird pro Spalte ein {@code <column>}-
     * Element mit dem aktuellen Wert ({@link Column#getValue(OutlineNode)})
     * geschrieben, unabhängig davon, ob für den Knoten explizit ein Wert
     * gesetzt wurde oder der Typ-Standardwert verwendet wird.</p>
     *
     * @param node    der zu serialisierende Wurzelknoten des Teilbaums
     * @param columns alle aktuell im Outliner vorhandenen Spalten
     * @return der XML-Text
     * @throws RuntimeException wenn die Serialisierung fehlschlägt (z. B. durch eine interne XML-Konfigurationsausnahme)
     */
    public static String serialize(OutlineNode node, List<Column> columns) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.newDocument();

            Element root = doc.createElement(ROOT_TAG);
            doc.appendChild(root);

            appendHead(doc, root, columns);
            appendNode(doc, root, node, columns);

            return toXmlString(doc);
        } catch (Exception e) {
            throw new RuntimeException("Konnte Knoten nicht als XML serialisieren", e);
        }
    }

    /**
     * Schreibt ein {@code <head>}-Element mit einem {@code <ColumnHeader>}
     * pro übergebener Spalte (Titel als Textinhalt, Typ als Attribut).
     */
    private static void appendHead(Document doc, Element root, List<Column> columns) {
        Element head = doc.createElement("head");
        root.appendChild(head);

        for (Column column : columns) {
            Element header = doc.createElement("ColumnHeader");
            header.setAttribute("type", column.getType().name());
            header.setTextContent(column.getTitle());
            head.appendChild(header);
        }
    }

    /**
     * Schreibt rekursiv ein {@code <node>}-Element für {@code node} (mit
     * Titel, Inhalt und einem {@code <column>}-Wert pro Spalte) sowie für
     * alle seine Nachkommen.
     */
    private static void appendNode(Document doc, Element parentElement, OutlineNode node, List<Column> columns) {
        Element nodeElement = doc.createElement("node");
        nodeElement.setAttribute("title", node.getTitle());
        parentElement.appendChild(nodeElement);

        Element content = doc.createElement("content");
        content.setTextContent(node.getContent() != null ? node.getContent() : "");
        nodeElement.appendChild(content);

        for (Column column : columns) {
            Element columnElement = doc.createElement("column");
            columnElement.setAttribute("columnTitle", column.getTitle());
            columnElement.setAttribute("columnType", column.getType().name());
            Object value = column.getValue(node);
            columnElement.setAttribute("columnValue", value != null ? value.toString() : "");
            nodeElement.appendChild(columnElement);
        }

        for (int i = 0; i < node.getChildCount(); i++) {
            OutlineNode child = (OutlineNode) node.getChildAt(i);
            appendNode(doc, nodeElement, child, columns);
        }
    }

    /** Serialisiert ein {@link Document} in einen XML-Text ohne XML-Deklaration. */
    private static String toXmlString(Document doc) throws Exception {
        TransformerFactory tf = TransformerFactory.newInstance();
        Transformer transformer = tf.newTransformer();
        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");

        StringWriter writer = new StringWriter();
        transformer.transform(new DOMSource(doc), new StreamResult(writer));
        return writer.toString();
    }

    /**
     * Baut aus einem zuvor mit {@link #serialize(OutlineNode, List)} erzeugten
     * XML-Text wieder einen {@link OutlineNode}-Teilbaum auf.
     *
     * <p>Für jede im Text enthaltene {@code <ColumnHeader>}-Angabe wird im
     * Ziel-Outliner ({@code outlineController}) eine passende Spalte ermittelt:
     * zuerst wird nach einer Spalte mit gleichem Typ <em>und</em> gleichem
     * Titel gesucht, sonst nach einer Spalte mit nur gleichem Typ (die erste
     * gefundene wird wiederverwendet), und erst wenn auch das fehlschlägt,
     * wird eine neue Spalte mit dem Typ und Titel aus dem XML angelegt. Die
     * im XML gespeicherten Werte werden anschließend für jeden Knoten auf die
     * so ermittelte Spalte übertragen.</p>
     *
     * <p>Ist der Text leer, kein gültiges XML oder stammt er erkennbar nicht
     * von dieser Klasse (kein {@code <clip>}-Wurzelelement), wird {@code null}
     * zurückgegeben, statt eine Ausnahme zu werfen — so kann
     * {@link EditController#insert()} einen Zwischenablage-Inhalt aus einer
     * anderen Anwendung einfach ignorieren.</p>
     *
     * @param xml               der zu parsende XML-Text
     * @param outlineController der Ziel-Outliner, in den eingefügt werden soll
     * @return der aufgebaute Teilbaum, oder {@code null} bei ungültigem Text
     */
    public static OutlineNode deserialize(String xml, OutlineController outlineController) {
        if (xml == null || !xml.contains("<" + ROOT_TAG)) {
            return null;
        }

        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(new InputSource(new StringReader(xml)));

            Element root = doc.getDocumentElement();
            if (root == null || !ROOT_TAG.equals(root.getTagName())) {
                return null;
            }

            Element headElement = firstChildElement(root, "head");
            Map<String, Column> columnByTitle = headElement != null
                    ? resolveColumns(headElement, outlineController)
                    : Collections.emptyMap();

            Element nodeElement = firstChildElement(root, "node");
            if (nodeElement == null) {
                return null;
            }

            return parseNode(nodeElement, columnByTitle);

        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Ermittelt bzw. legt für jede {@code <ColumnHeader>}-Angabe im
     * {@code <head>}-Element die passende Ziel-Spalte an (siehe
     * {@link #deserialize(String, OutlineController)} für die Zuordnungsregeln).
     *
     * @return Abbildung vom Spaltentitel aus dem XML auf die Ziel-{@link Column}
     */
    private static Map<String, Column> resolveColumns(Element headElement, OutlineController outlineController) {
        Map<String, Column> result = new LinkedHashMap<>();

        NodeList headers = headElement.getElementsByTagName("ColumnHeader");
        for (int i = 0; i < headers.getLength(); i++) {
            Element header = (Element) headers.item(i);
            String title = header.getTextContent();

            ColumnType type;
            try {
                type = ColumnType.valueOf(header.getAttribute("type"));
            } catch (IllegalArgumentException ex) {
                continue; // unbekannter/ungültiger Typ -> Spalte überspringen
            }

            Column target = findColumnByTypeAndTitle(outlineController, type, title);
            if (target == null) {
                target = findColumnByType(outlineController, type);
            }
            if (target == null) {
                target = createColumn(outlineController, title, type);
            }

            result.put(title, target);
        }
        return result;
    }

    private static Column findColumnByTypeAndTitle(OutlineController outlineController, ColumnType type, String title) {
        for (Column column : outlineController.getSpalten()) {
            if (column.getType() == type && column.getTitle().equals(title)) {
                return column;
            }
        }
        return null;
    }

    private static Column findColumnByType(OutlineController outlineController, ColumnType type) {
        for (Column column : outlineController.getSpalten()) {
            if (column.getType() == type) {
                return column;
            }
        }
        return null;
    }

    /**
     * Legt im Ziel-Outliner eine neue Spalte an und liefert die neu erzeugte
     * {@link Column} zurück.
     *
     * <p>{@code OutlineController#addColumn(String, ColumnType)} gibt die
     * erzeugte Spalte nicht zurück (anders als {@code OutlinePanel#addColumn}),
     * daher wird hier nach dem Anlegen die zuletzt in der Spaltenliste
     * enthaltene Spalte entnommen (neue Spalten werden stets am Ende
     * angefügt).</p>
     *
     * <p><strong>Achtung:</strong> {@code OutlineController#addColumn(String,
     * ColumnType)} ruft intern {@code saved()} auf (gedacht für den
     * Dokumenten-Import, wo das korrekt ist). Beim Einfügen aus der
     * Zwischenablage wäre das hier ein Seiteneffekt, der einen eigentlich
     * ungespeicherten Zustand fälschlich als gespeichert markieren würde,
     * daher wird direkt danach wieder {@link OutlineController#markDirty()}
     * aufgerufen.</p>
     */
    private static Column createColumn(OutlineController outlineController, String title, ColumnType type) {
        outlineController.addColumn(title, type);
        outlineController.markDirty(); // siehe Hinweis oben: addColumn(..) markiert sonst fälschlich "gespeichert"

        List<Column> all = outlineController.getSpalten();
        return all.get(all.size() - 1);
    }

    /**
     * Baut rekursiv einen {@link OutlineNode} aus einem {@code <node>}-Element
     * auf: Titel aus dem {@code title}-Attribut, Inhalt aus {@code <content>},
     * Spaltenwerte aus den {@code <column>}-Kindelementen (siehe
     * {@link #applyColumnValue(Element, OutlineNode, Map)}), Kinder rekursiv
     * aus verschachtelten {@code <node>}-Elementen.
     */
    private static OutlineNode parseNode(Element nodeElement, Map<String, Column> columnByTitle) {
        OutlineNode node = new OutlineNode(nodeElement.getAttribute("title"));

        for (org.w3c.dom.Node child = nodeElement.getFirstChild(); child != null; child = child.getNextSibling()) {
            if (!(child instanceof Element childElement)) {
                continue;
            }

            switch (childElement.getTagName()) {
                case "content" -> node.setContent(childElement.getTextContent());
                case "column" -> applyColumnValue(childElement, node, columnByTitle);
                case "node" -> node.add(parseNode(childElement, columnByTitle));
                default -> { /* unbekanntes Element ignorieren */ }
            }
        }
        return node;
    }

    /**
     * Liest Titel und Wert aus einem {@code <column>}-Element, ermittelt die
     * zugehörige Ziel-Spalte über {@code columnByTitle} und setzt dort den
     * Wert für {@code node}. Gibt es für den Titel keine Ziel-Spalte (z. B.
     * weil der {@code <head>}-Block fehlte), geschieht nichts.
     */
    private static void applyColumnValue(Element columnElement, OutlineNode node, Map<String, Column> columnByTitle) {
        String title = columnElement.getAttribute("columnTitle");
        String rawValue = columnElement.getAttribute("columnValue");

        Column column = columnByTitle.get(title);
        if (column == null) {
            return;
        }

        Object value = column.getType() == ColumnType.CHECKBOX
                ? Boolean.parseBoolean(rawValue)
                : rawValue;

        column.setValue(node, value);
    }

    /** Liefert das erste direkte Kindelement von {@code parent} mit dem angegebenen Tag-Namen, oder {@code null}. */
    private static Element firstChildElement(Element parent, String tagName) {
        for (org.w3c.dom.Node child = parent.getFirstChild(); child != null; child = child.getNextSibling()) {
            if (child instanceof Element element && tagName.equals(element.getTagName())) {
                return element;
            }
        }
        return null;
    }
}