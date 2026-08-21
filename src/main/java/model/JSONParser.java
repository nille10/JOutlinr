package model;

import controller.OutlineController;
import view.columns.ColumnType;

import javax.swing.tree.DefaultTreeModel;
import java.io.File;


/**
 * Parser der den JTree des Outliners in eine JSON Datei verarbeitet und diese abspeichern kann. Oder eine Datei in den Outliner importieren kann.
 */
public class JSONParser {

    private int pos;
    private final OutlineController outlineController;

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

        jsonHeader.append("  \"header\" : \"").append(outlineController.getHeaderTitle()).append("\",\n");
        jsonHeader.append("  \"headerColumns\" : [\n");
        var headers = outlineController.getHeaderList();
        for (int i = 0; i < headers.size(); i++) {
            if (headers.size() == 1 || headers.size() -1 == i)
                jsonHeader.append("    \"").append(headers.get(i).getTitle()).append("\"\n");
            else
                jsonHeader.append("    \"").append(headers.get(i).getTitle()).append("\",\n");
        }
        jsonHeader.append("  ],\n");
        return jsonHeader;
    }

    /**
     * Generiert Rekursiv einen {@code StringBuilder}-Objekt mit dem Inhalt des JTrees des Outliners.
     * <p>
     *   Arbeitet rekursiv alle Knoten im Outliner ab und erstellt einen {@code StringBuilder}-Objekt im JSON format.
     *   Speichert den Titel, Content des Textfensters, die Spalten mit Spaltennamen, Spaltentyp und Spaltenwert.
     * </p>
     * @param amountParents
     * @param node
     * @return
     */
    private StringBuilder jsonChild(int amountParents, OutlineNode node) {
        StringBuilder sb = new StringBuilder();

        // Spaces
        StringBuilder spaces = new StringBuilder("  ");
        spaces.repeat("  ", Math.max(0, amountParents));

        sb.append(spaces).append("\"title\" : \"").append(node.getTitle()).append("\",\n");
        sb.append(spaces).append("\"content\" : \"").append(escapeJson(node.getContent())).append("\",\n");
        sb.append(spaces).append("\"columns\" : [");
        var headers = outlineController.getColumns();
        for (int i = 0; i < headers.size(); i++) {
            sb.append(" {\n");
            sb.append(spaces).append("  ").append("\"columnTitle\" : \"").append(headers.get(i).getTitle()).append("\",\n");
            sb.append(spaces).append("  ").append("\"columnType\" : \"").append(headers.get(i).getType()).append("\",\n");
            sb.append(spaces).append("  ").append("\"columnValue\" : \"").append(headers.get(i).getValue(node)).append("\"\n");
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
     * Ersetzt bestimmte Zeichen wie Zeilenumbrüche, damit diese in der JSON im richtigen Format gespeichert werden. Wird für das Textfeld der Knoten benötigt.
     * @param input {@code String}
     * @return einen {@code String} der im JSON format ist
     */
    private String escapeJson(String input) {
        if (input == null) {
            return "";
        }
        return input.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
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
                    // KORREKTUR: Wir bauen den Root-Knoten direkt im aktuellen Kontext des Hauptobjekts
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

        outlineController.replaceRootContents(rootNode);
    }

    // =========================================================================
    // Hilfsmethoden für das Parsen der JSON-Struktur
    // =========================================================================

    /**
     *
     * @param json
     * @return
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
     * Parst ein JSON-Array von Spaltenüberschriften und fügt neue Spalten dem Controller hinzu.
     *
     * <p>
     * Liest den JSON-String ab der aktuellen Position {@code pos} ein, erwartet ein Array
     * mit Zeichenketten und prüft für jeden Eintrag, ob eine entsprechende Spalte im
     * {@link OutlineController} bereits existiert. Fehlt die Spalte, wird sie automatisch als
     * {@link ColumnType#CHECKBOX} neu angelegt.
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

            String columnTitle = parseString(json);

            // Prüfen, ob die Spalte bereits existiert, bevor sie hinzugefügt wird
            boolean exists = outlineController.getSpalten().stream()
                    .anyMatch(col -> col.getTitle().equals(columnTitle));
            if (!exists) {
                outlineController.addColumn(columnTitle, ColumnType.CHECKBOX);
            }

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
     * Objekt liest sie Titel, Typ und Wert der Spalte aus, sucht die passende Spalte im {@link OutlineController}
     * und wandelt den Wert basierend auf dem Spaltentyp (z. B. Konvertierung nach {@link Boolean} bei {@link ColumnType#CHECKBOX}) um.
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

            for (var column : outlineController.getSpalten()) {
                if (column.getTitle().equals(columnTitle)) {
                    Object value = column.getType() == ColumnType.CHECKBOX
                            ? Boolean.parseBoolean(rawValue)
                            : rawValue;
                    column.setValue(node, value);
                    break;
                }
            }

            skipWhitespace(json);
            if (pos < json.length() && json.charAt(pos) == ',') {
                pos++;
            }
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
     * {@code \r}, {@code \t}) aufgelöst und in die entsprechenden Steuerzeichen umgewandelt.
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
     *       ({@code depth}) inklusive aller geschachtelten Strukturen übersprungen.</li>
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
                if (json.charAt(pos) == c) depth++;
                else if (json.charAt(pos) == close) depth--;
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
