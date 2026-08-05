package model;

import model.OutlineNode;


public class JSONParser {


    private final String json;
    private int pos;

    public JSONParser(String json) {
        this.json = json;
    }

    public OutlineNode parse() {
        skipWhitespace();
        return parseNode();
    }

    private OutlineNode parseNode() {
        expect('{');

        String title = "";
        String content = "";
        OutlineNode node = null;

        while (true) {
            skipWhitespace();

            if (peek() == '}') {
                pos++;
                break;
            }

            String key = parseString();

            skipWhitespace();
            expect(':');
            skipWhitespace();

            switch (key) {
                case "title":
                    title = parseString();
                    if (node == null) {
                        node = new OutlineNode(title);
                    } else {
                        node.setTitle(title);
                    }
                    break;

                case "content":
                    content = parseString();
                    if (node == null) {
                        node = new OutlineNode(title);
                    }
                    node.setContent(content);
                    break;

                case "children":
                    if (node == null) {
                        node = new OutlineNode(title);
                        node.setContent(content);
                    }

                    expect('[');
                    skipWhitespace();

                    while (peek() != ']') {
                        OutlineNode child = parseNode();
                        node.add(child);

                        skipWhitespace();
                        if (peek() == ',') {
                            pos++;
                            skipWhitespace();
                        }
                    }

                    expect(']');
                    break;

                default:
                    throw new RuntimeException("Unbekanntes Feld: " + key);
            }

            skipWhitespace();

            if (peek() == ',') {
                pos++;
                continue;
            }

            if (peek() == '}') {
                pos++;
                break;
            }
        }

        if (node == null) {
            node = new OutlineNode(title);
            node.setContent(content);
        }

        return node;
    }

    private String parseString() {
        expect('"');

        StringBuilder sb = new StringBuilder();

        while (pos < json.length()) {
            char c = json.charAt(pos++);

            if (c == '\\') {
                if (pos >= json.length()) {
                    throw new RuntimeException("Ungültige Escape-Sequenz");
                }

                char escaped = json.charAt(pos++);

                switch (escaped) {
                    case 'n':
                        sb.append('\n');
                        break;
                    case 'r':
                        sb.append('\r');
                        break;
                    case 't':
                        sb.append('\t');
                        break;
                    case '"':
                        sb.append('"');
                        break;
                    case '\\':
                        sb.append('\\');
                        break;
                    default:
                        sb.append(escaped);
                }
            } else if (c == '"') {
                break;
            } else {
                sb.append(c);
            }
        }

        return sb.toString();
    }

    private void skipWhitespace() {
        while (pos < json.length()
                && Character.isWhitespace(json.charAt(pos))) {
            pos++;
        }
    }

    private void expect(char c) {
        skipWhitespace();

        if (pos >= json.length() || json.charAt(pos) != c) {
            throw new RuntimeException(
                    "Erwartet '" + c + "' an Position " + pos);
        }

        pos++;
    }

    private char peek() {
        skipWhitespace();

        if (pos >= json.length()) {
            return '\0';
        }

        return json.charAt(pos);
    }
}
