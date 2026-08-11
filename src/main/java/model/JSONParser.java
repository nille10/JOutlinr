package model;

import controller.OutlineController;
import view.columns.ColumnType;

import javax.swing.tree.DefaultTreeModel;
import java.io.File;



public class JSONParser {

    private int pos;
    private final OutlineController outlineController;

    public JSONParser(OutlineController outlineController) {
        this.outlineController = outlineController;
    }

    public String toJsonString(DefaultTreeModel treeModel) {
        StringBuilder json = new StringBuilder("{\n");

        json.append(jsonHeader());

        OutlineNode root = (OutlineNode) treeModel.getRoot();

        json.append(jsonChild(0, root));
        json.append("\n}");

        return json.toString();
    }

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

        replaceRootContents(rootNode);
    }

    // =========================================================================
    // Hilfsmethoden für das Parsen der JSON-Struktur
    // =========================================================================

    private OutlineNode parseNode(String json) {
        skipWhitespace(json);
        expectChar(json, '{');
        return parseNodeFields(json, null);
    }
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

    // =========================================================================
    // Low-Level Tokenizer-Methoden
    // =========================================================================

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

    private void skipWhitespace(String json) {
        while (pos < json.length() && Character.isWhitespace(json.charAt(pos))) {
            pos++;
        }
    }

    private void expectChar(String json, char expected) {
        skipWhitespace(json);
        if (pos >= json.length() || json.charAt(pos) != expected) {
            throw new IllegalArgumentException("Erwartet: '" + expected + "' an Position " + pos);
        }
        pos++;
    }
}
