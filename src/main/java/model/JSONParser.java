package model;

import controller.OutlineController;
import model.columns.Column;
import model.columns.ColumnType;

import javax.swing.tree.DefaultTreeModel;
import java.io.File;
import java.util.ArrayList;
import java.util.List;


/**
 * Parser der den JTree des Outliners in eine JSON Datei verarbeitet und diese abspeichern kann. Oder eine Datei in den Outliner importieren kann.
 */
public class JSONParser {

    private int pos;
    private final OutlineController outlineController;

    /** Spaltentitel aus dem Header der aktuell importierten Datei. */
    private final List<String> importedHeaderColumns = new ArrayList<>();

    /**
     * Erstellt einen neuen {@code JSONParser}.
     * @param outlineController der OutlineController des Outliners, wird benötigt, um den JTree auszulesen und diesen beim Import zu manipulieren.
     * Darf nicht {@code null} sein.
     */
    public JSONParser(OutlineController outlineController) {
        this.outlineController = outlineController;
    }

    /**
     * Wandelt den JTree in einen JSON String um.
     * @param treeModel DefaultTreeModel des Outliners
     * @return {@code String} mit dem Inhalt des JTrees.
     */
    public String toJsonString(DefaultTreeModel treeModel) {
        StringBuilder json = new StringBuilder("{\n");

        json.append(jsonHeader());

        OutlineNode root = (OutlineNode) treeModel.getRoot();

        json.append(jsonChild(0, root));
        json.append("\n}");

        return json.toString();
    }

    /**
     * Generiert einen JSON Header basierend auf den Daten im {@link OutlineController}.
     * <p>
     *   Der Header beinhaltet die Überschrift des Outliners und die Überschriften der Spalten.
     * </p>
     * @return ein {@code StringBuilder} mit dem Inhalt des Headers in JSON format
     */
    private StringBuilder jsonHeader() {
        StringBuilder jsonHeader = new StringBuilder();

        jsonHeader.append("  \"header\" : \"").append(escapeJson(outlineController.getHeaderTitle())).append("\",\n");
        jsonHeader.append("  \"headerColumns\" : [\n");
        var headers = outlineController.getHeaderList();
        for (int i = 0; i < headers.size(); i++) {
            jsonHeader.append("    \"").append(escapeJson(headers.get(i).getTitle())).append("\"");
            jsonHeader.append(i < headers.size() - 1 ? ",\n" : "\n");
        }
        jsonHeader.append("  ],\n");
        return jsonHeader;
    }

    /**
     * Generiert Rekursiv einen {@code StringBuilder}-Objekt mit dem Inhalt des JTrees des Outliners.
     * <p>
     *   Arbeitet rekursiv alle Knoten im Outliner ab und erstellt einen {@code StringBuilder}-Objekt im JSON format.
     *   Speichert den Titel, Content des Textfensters, die Spalten mit Spaltennamen, Spaltentyp und Spaltenwert.
     *   Alle Texte werden dabei mit {@link #escapeJson(String)} maskiert.
     * </p>
     * @param amountParents Anzahl der Elternknoten, bestimmt die Einrückung
     * @param node der Knoten, der (samt Kindern) ausgegeben wird
     * @return ein {@code StringBuilder} mit dem Knoten im JSON format
     */
    private StringBuilder jsonChild(int amountParents, OutlineNode node) {
        StringBuilder sb = new StringBuilder();

        // Spaces
        StringBuilder spaces = new StringBuilder("  ");
        spaces.repeat("  ", Math.max(0, amountParents));

        sb.append(spaces).append("\"title\" : \"").append(escapeJson(node.getTitle())).append("\",\n");
        sb.append(spaces).append("\"content\" : \"").append(escapeJson(node.getContent())).append("\",\n");
        sb.append(spaces).append("\"columns\" : [");
        var headers = outlineController.getColumns();
        for (int i = 0; i < headers.size(); i++) {
            sb.append(" {\n");
            sb.append(spaces).append("  ").append("\"columnTitle\" : \"").append(escapeJson(headers.get(i).getTitle())).append("\",\n");
            sb.append(spaces).append("  ").append("\"columnType\" : \"").append(escapeJson(String.valueOf(headers.get(i).getType()))).append("\",\n");
            sb.append(spaces).append("  ").append("\"columnValue\" : \"").append(escapeJson(String.valueOf(headers.get(i).getValue(node)))).append("\"\n");
            if (headers.size() == 1 || headers.size() -1 == i) {
                sb.append(spaces).append("} ");
            } else {
                sb.append(spaces).append("},");
            }
        }
        sb.append("],\n");

        sb.append(spaces).append("\"children\" : ").append("[ ");
        if (node.getChildCount() == 0) {
            sb.append("]");
            return sb;
        } else if (node.getChildCount() == 1) {
            sb.append("{\n");
            sb.append(jsonChild(++amountParents,(OutlineNode) node.getChildAt(0)));
            sb.append("\n").append(spaces).append("} ]");
        } else {
            amountParents++;
            for (int i = 0; i < node.getChildCount(); i++) {
                sb.append("{\n");
                if (node.getChildCount() -1 == i) {
                    sb.append(jsonChild(amountParents, (OutlineNode) node.getChildAt(i)));
                    sb.append("\n").append(spaces).append("} ]");
                } else {
                    sb.append(jsonChild(amountParents, (OutlineNode) node.getChildAt(i)));
                    sb.append("\n").append(spaces).append("}, ");
                }
            }
        }
        return sb;
    }

    /**
     * Ersetzt bestimmte Zeichen wie Anführungszeichen, Backslashes und Zeilenumbrüche, damit diese in der JSON im richtigen Format gespeichert werden.
     * Wird für alle Texte (Titel, Inhalt des Textfelds, Spaltentitel, Spaltenwerte) benötigt.
     * @param input {@code String}
     * @return einen {@code String} der im JSON format ist
     */
    private String escapeJson(String input) {
        if (input == null) {
            return "";
        }
        StringBuilder sb = new StringBuilder(input.length() + 8);
        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);
            switch (c) {
                case '\\' -> sb.append("\\\\");
                case '"' -> sb.append("\\\"");
                case '\n' -> sb.append("\\n");
                case '\r' -> sb.append("\\r");
                case '\t' -> sb.append("\\t");
                default -> {
                    if (c < 0x20) {
                        sb.append(String.format("\\u%04x", (int) c));
                    } else {
                        sb.append(c);
                    }
                }
            }
        }
        return sb.toString();
    }

    // ---------------------------------------------------------------------------------------------------------

    /*
     * Rest der Klasse ist KI generiert
     */

    /**
     * Liest die JSON Datei den dem angegebenen
     * <p>
     *   Arbeitet sich durch die JSON Datei und fügt den Inhalt zu einer neuen {@link OutlineNode} hinzu, der Inhalt der Überschriften wird direkt eingefügt.
     *   Wenn die {@link OutlineNode} ungleich {@code null} ist wird der Inhalt des Outliners durch den Inhalt der neuen {@link OutlineNode} ersetzt.
     * </p>
     * @param saveLocation die JSON-Datei die importiert werden soll
     * @throws Exception wenn die {@code saveLocation} nicht gelesen werden kann.
     */
    public void importJson(File saveLocation) throws Exception {
        String json = java.nio.file.Files.readString(saveLocation.toPath());
        this.pos = 0;
        this.importedHeaderColumns.clear();

        outlineController.cancelActiveEditing();

        skipWhitespace(json);
        expectChar(json, '{');

        OutlineNode rootNode = null;

        label:
        while (pos < json.length()) {
            skipWhitespace(json);
            if (json.charAt(pos) == '}') {
                pos++;
                break;
            }

            String key = parseString(json);
            skipWhitespace(json);
            expectChar(json, ':');

            switch (key) {
                case "header":
                    outlineController.setHeaderTitle(parseString(json));
                    break;
                case "headerColumns":
                    parseHeaderColumns(json);
                    break;
                case "title":
                case "content":
                case "columns":
                case "children":
                    // Der Root-Knoten steht direkt im Hauptobjekt, der erste Schlüssel ist bereits gelesen
                    rootNode = parseNodeFields(json, key);
                    break label; // Nach dem Einlesen des Root-Knotens sind wir fertig
                default:
                    skipValue(json);
                    break;
            }

            skipWhitespace(json);
            if (pos < json.length() && json.charAt(pos) == ',') {
                pos++;
            }
        }

        if (rootNode == null) {
            throw new IllegalArgumentException("Keine Root-Node gefunden");
        }

        // Spalten, die im Header stehen, aber in keinem Knoten vorkamen, werden nachträglich angelegt
        for (String columnTitle : importedHeaderColumns) {
            if (findColumn(columnTitle) == null) {
                outlineController.addColumn(columnTitle, ColumnType.CHECKBOX);
            }
        }

        outlineController.replaceRootContents(rootNode);
    }

    // =========================================================================
    // Hilfsmethoden für das Parsen der JSON-Struktur
    // =========================================================================

    /**
     * Liest ein einzelnes JSON-Objekt ab der aktuellen Position und baut daraus ein {@link OutlineNode}-Objekt auf.
     *
     * @param json String mit Knoten im JSON Format
     * @return {@link OutlineNode} abgeleitet aus der JSON Datei
     */
    private OutlineNode parseNode(String json) {
        skipWhitespace(json);
        expectChar(json, '{');
        return parseNodeFields(json, null);
    }

    /**
     * Liest die Felder eines einzelnen JSON-Objekts ein und baut daraus ein {@link OutlineNode}-Objekt auf.
     *
     * <p>
     * Die Methode durchläuft den JSON-String ab der aktuellen Parser-Position ({@code pos}),
     * verarbeitet Schlüssel-Wert-Paare wie {@code title}, {@code content}, {@code columns} sowie
     * verschachtelte {@code children} und weist diese dem Knoten zu. Unbekannte Schlüssel
     * werden übersprungen.
     * </p>
     *
     * @param json String mit Knoten im JSON Format
     * @param firstKey der bereits gelesene erste Schlüssel des JSON-Objekts,
     * oder {@code null}, falls der erste Schlüssel noch geparst werden muss
     * @return {@link OutlineNode} abgeleitet aus der JSON Datei
     */
    private OutlineNode parseNodeFields(String json, String firstKey) {
        OutlineNode node = new OutlineNode("");
        boolean isFirst = true;

        while (pos < json.length()) {
            skipWhitespace(json);
            if (json.charAt(pos) == '}') {
                pos++;
                break;
            }

            String key;
            if (isFirst && firstKey != null) {
                key = firstKey;
                isFirst = false;
            } else {
                key = parseString(json);
                skipWhitespace(json);
                expectChar(json, ':');
            }

            switch (key) {
                case "title" -> node.setTitle(parseString(json));
                case "content" -> node.setContent(parseString(json));
                case "columns" -> parseColumns(json, node);
                case "children" -> parseChildren(json, node);
                default -> skipValue(json);
            }

            skipWhitespace(json);
            if (pos < json.length() && json.charAt(pos) == ',') {
                pos++;
            }
        }

        return node;
    }

    /**
     * Parst ein JSON-Array von Spaltenüberschriften und merkt sich die Titel.
     *
     * <p>
     * Der Header enthält nur Titel, keinen Typ. Die Spalten werden daher hier noch nicht angelegt,
     * sondern erst beim Einlesen der Spaltenobjekte der Knoten ({@link #parseColumns(String, OutlineNode)}),
     * da dort der Typ der Spalte gespeichert ist. Titel, für die in keinem Knoten eine Spalte
     * vorkommt, werden am Ende des Imports als {@link ColumnType#CHECKBOX} angelegt.
     * </p>
     *
     * @param json der zu parsende JSON-String
     */
    private void parseHeaderColumns(String json) {
        skipWhitespace(json);
        expectChar(json, '[');

        while (pos < json.length()) {
            skipWhitespace(json);
            if (json.charAt(pos) == ']') {
                pos++;
                break;
            }

            importedHeaderColumns.add(parseString(json));

            skipWhitespace(json);
            if (pos < json.length() && json.charAt(pos) == ',') {
                pos++;
            }
        }
    }

    /**
     * Parst ein JSON-Array von Spaltenobjekten und weist den geparsten Spaltenwerten dem übergebenen {@link OutlineNode} zu.
     *
     * <p>
     * Die Methode iteriert ab der aktuellen Zeigerposition {@code pos} durch das JSON-Array. Für jedes enthaltene
     * Objekt liest sie Titel, Typ und Wert der Spalte aus und sucht die passende Spalte im {@link OutlineController}.
     * Existiert noch keine Spalte mit diesem Titel, wird sie mit dem gespeicherten Typ neu angelegt.
     * Der Wert wird anschließend anhand des Spaltentyps umgewandelt (z. B. nach {@link Boolean} bei {@link ColumnType#CHECKBOX}).
     * </p>
     *
     * @param json der zu parsende JSON-String
     * @param node der Knoten, dem die geparsten Spaltenwerte zugewiesen werden sollen
     */
    private void parseColumns(String json, OutlineNode node) {
        skipWhitespace(json);
        expectChar(json, '[');

        while (pos < json.length()) {
            skipWhitespace(json);
            if (json.charAt(pos) == ']') {
                pos++;
                break;
            }

            skipWhitespace(json);
            expectChar(json, '{');

            String columnTitle = "";
            String columnType = "";
            String rawValue = "";

            while (pos < json.length()) {
                skipWhitespace(json);
                if (json.charAt(pos) == '}') {
                    pos++;
                    break;
                }

                String key = parseString(json);
                skipWhitespace(json);
                expectChar(json, ':');

                switch (key) {
                    case "columnTitle" -> columnTitle = parseString(json);
                    case "columnType" -> columnType = parseString(json);
                    case "columnValue" -> rawValue = parseString(json);
                    default -> skipValue(json);
                }

                skipWhitespace(json);
                if (pos < json.length() && json.charAt(pos) == ',') {
                    pos++;
                }
            }

            var column = findColumn(columnTitle);
            if (column == null) {
                // Spalte existiert im Zieldokument noch nicht: mit dem gespeicherten Typ anlegen
                outlineController.addColumn(columnTitle, parseColumnType(columnType));
                column = findColumn(columnTitle);
            }
            if (column != null) {
                Object value = column.getType() == ColumnType.CHECKBOX
                        ? Boolean.parseBoolean(rawValue)
                        : rawValue;
                column.setValue(node, value);
            }

            skipWhitespace(json);
            if (pos < json.length() && json.charAt(pos) == ',') {
                pos++;
            }
        }
    }

    /**
     * Sucht im {@link OutlineController} die Spalte mit dem angegebenen Titel.
     *
     * @param title Titel der gesuchten Spalte
     * @return die erste Spalte mit diesem Titel oder {@code null}, falls keine existiert
     */
    private Column findColumn(String title) {
        for (var column : outlineController.getSpalten()) {
            if (column.getTitle().equals(title)) {
                return column;
            }
        }
        return null;
    }

    /**
     * Wandelt den gespeicherten Typnamen in einen {@link ColumnType} um.
     *
     * @param name Name des Typs aus der JSON-Datei
     * @return der passende {@link ColumnType}, bei unbekanntem Namen {@link ColumnType#CHECKBOX}
     */
    private ColumnType parseColumnType(String name) {
        try {
            return ColumnType.valueOf(name.trim());
        } catch (IllegalArgumentException e) {
            return ColumnType.CHECKBOX;
        }
    }

    /**
     * Parst ein JSON-Array von Kindknoten und fügt diese rekursiv dem übergebenen Elternknoten hinzu.
     *
     * <p>
     * Liest den JSON-String ab der aktuellen Zeigerposition {@code pos} ein, verarbeitet jedes
     * enthaltene Knoten-Objekt über {@link #parseNode(String)} und hängt den resultierenden
     * {@link OutlineNode} an den {@code parentNode} an.
     * </p>
     *
     * @param json       der zu parsende JSON-String
     * @param parentNode der Elternknoten, dem die geparsten Kindknoten hinzugefügt werden
     */
    private void parseChildren(String json, OutlineNode parentNode) {
        skipWhitespace(json);
        expectChar(json, '[');

        while (pos < json.length()) {
            skipWhitespace(json);
            if (json.charAt(pos) == ']') {
                pos++;
                break;
            }

            OutlineNode childNode = parseNode(json);
            parentNode.add(childNode);

            skipWhitespace(json);
            if (pos < json.length() && json.charAt(pos) == ',') {
                pos++;
            }
        }
    }

    /**
     * Liest eine in Anführungszeichen eingefasste Zeichenkette aus dem JSON-String ein.
     *
     * <p>
     *   Die Methode überspringt vorab führende Leerzeichen, erwartet ein öffnendes Anführungszeichen
     * ({@code "}) und liest die Zeichenfolge bis zum schließenden Anführungszeichen ein.
     * Dabei werden gängige Escape-Sequenzen (z.&nbsp;B. {@code \"}, {@code \\}, {@code \n},
     * {@code \r}, {@code \t}, {@code \}{@code uXXXX}) aufgelöst und in die entsprechenden Zeichen umgewandelt.
     * </p>

     * @param json der zu parsende JSON-String
     * @return die extrahierte und dekodierte Zeichenkette
     * @throws IllegalArgumentException falls der String nicht ordnungsgemäß mit einem
     *                                  Anführungszeichen geschlossen wird
     */
    private String parseString(String json) {
        skipWhitespace(json);
        expectChar(json, '"');

        StringBuilder sb = new StringBuilder();
        while (pos < json.length()) {
            char c = json.charAt(pos++);
            if (c == '"') {
                return sb.toString();
            } else if (c == '\\' && pos < json.length()) {
                char escaped = json.charAt(pos++);
                switch (escaped) {
                    case '"' -> sb.append('"');
                    case '\\' -> sb.append('\\');
                    case 'n' -> sb.append('\n');
                    case 'r' -> sb.append('\r');
                    case 't' -> sb.append('\t');
                    case 'u' -> {
                        if (pos + 4 > json.length()) {
                            throw new IllegalArgumentException("Ungültige Unicode-Sequenz an Position " + pos);
                        }
                        try {
                            sb.append((char) Integer.parseInt(json.substring(pos, pos + 4), 16));
                        } catch (NumberFormatException e) {
                            throw new IllegalArgumentException("Ungültige Unicode-Sequenz an Position " + pos);
                        }
                        pos += 4;
                    }
                    default -> sb.append(escaped);
                }
            } else {
                sb.append(c);
            }
        }
        throw new IllegalArgumentException("Unvollständiger String an Position " + pos);
    }

    /**
     * Überspringt den aktuellen JSON-Wert an der Position {@code pos}, unabhängig von dessen Datentyp.
     *
     * <p>Die Methode erkennt den Typ des Werts anhand des ersten Zeichens und überspringt ihn entsprechend:</p>
     * <ul>
     *   <li><b>Strings ({@code "}):</b> Werden über {@link #parseString(String)} eingelesen und verworfen.</li>
     *   <li><b>Objekte ({@code {}) & Arrays ({@code []}):</b> Werden durch Zählen der Klammerungstiefe
     *       ({@code depth}) inklusive aller geschachtelten Strukturen übersprungen. Zeichenketten innerhalb
     *       werden als Ganzes übersprungen, damit Klammern in Texten die Zählung nicht verfälschen.</li>
     *   <li><b>Primitive Werte (Zahlen, Booleans, Null):</b> Werden bis zum nächsten Trennzeichen
     *       (Komma, Schließende Klammer oder Whitespace) durchlaufen.</li>
     * </ul>
     *
     * @param json der zu parsende JSON-String
     */
    private void skipValue(String json) {
        skipWhitespace(json);
        if (pos >= json.length()) return;

        char c = json.charAt(pos);
        if (c == '"') {
            parseString(json);
        } else if (c == '{' || c == '[') {
            char close = (c == '{') ? '}' : ']';
            int depth = 1;
            pos++;
            while (pos < json.length() && depth > 0) {
                char ch = json.charAt(pos);
                if (ch == '"') {
                    parseString(json);
                    continue;
                }
                if (ch == c) depth++;
                else if (ch == close) depth--;
                pos++;
            }
        } else {
            while (pos < json.length() && ",}] \t\r\n".indexOf(json.charAt(pos)) == -1) {
                pos++;
            }
        }
    }

    /**
     * Überspringt alle nachfolgenden Whitespace-Zeichen im JSON-String ab der aktuellen Position.
     *
     * <p>Erhöht den Zeiger {@code pos} so lange, bis ein Nicht-Whitespace-Zeichen
     * erreicht wird oder das Ende des Strings erreicht ist (gemäß {@link Character#isWhitespace(char)}).</p>
     *
     * @param json der zu parsende JSON-String
     */
    private void skipWhitespace(String json) {
        while (pos < json.length() && Character.isWhitespace(json.charAt(pos))) {
            pos++;
        }
    }

    /**
     * Prüft, ob an der aktuellen Position das erwartete Zeichen steht, und rückt den Zeiger weiter.
     *
     * <p>Überspringt zunächst etwaige führende Whitespaces. Stimmt das Zeichen an der Position {@code pos}
     * mit {@code expected} überein, wird {@code pos} um eins erhöht. Andernfalls wird eine Exception geworfen.</p>
     *
     * @param json     der zu parsende JSON-String
     * @param expected das an der aktuellen Position erwartete Zeichen
     * @throws IllegalArgumentException falls das Ende des Strings erreicht ist oder das Zeichen
     *                                  nicht mit {@code expected} übereinstimmt
     */
    private void expectChar(String json, char expected) {
        skipWhitespace(json);
        if (pos >= json.length() || json.charAt(pos) != expected) {
            throw new IllegalArgumentException("Erwartet: '" + expected + "' an Position " + pos);
        }
        pos++;
    }
}