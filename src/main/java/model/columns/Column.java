package model.columns;

import model.OutlineNode;

import java.util.HashMap;
import java.util.Map;

/**
 * Die Daten einer einzelnen Zusatzspalte neben dem Baum.
 *
 * <p>Eine {@code Column} besteht aus einem Titel, einem festen {@link ColumnType}
 * und den eingegebenen Werten pro {@link OutlineNode}. Sie enthält keinerlei
 * Swing-Code: Die Darstellung der Zellen übernimmt die View (z. B. eine
 * {@code ColumnCellView}), die über {@link #getValue(OutlineNode)} und
 * {@link #setValue(OutlineNode, Object)} mit dieser Klasse kommuniziert.</p>
 *
 * <p>Fehlt für einen Knoten ein eigener Wert, liefert {@link #getValue(OutlineNode)}
 * den typabhängigen Standardwert aus {@link ColumnType#getDefaultValue()}.</p>
 */
public class Column {

    /** Maximale Anzahl an Zeichen, die ein Spaltentitel haben darf. */
    public static final int MAX_TITLE_CHARS = 20;

    /** Der Typ dieser Spalte; bestimmt den Standardwert und wie die View die Zellen aufbaut. */
    private final ColumnType type;

    /** Der Titel der Spalte; kann sich durch {@link #rename(String)} ändern. */
    private String title;

    /** Die eingegebenen Werte pro Knoten (z. B. {@link Boolean} bei Checkboxen, {@link String} bei Textfeldern). */
    private final Map<OutlineNode, Object> values = new HashMap<>();

    /**
     * Erstellt eine neue Spalte mit Titel und Typ.
     *
     * <p>Ist der Titel länger als {@link #MAX_TITLE_CHARS}, wird er auf diese Länge
     * gekürzt (siehe {@link #rename(String)}).</p>
     *
     * @param title der Titel der Spalte
     * @param type  der Typ der Spalte, nicht {@code null}
     */
    public Column(String title, ColumnType type) {
        this.type = type;
        this.title = cap(title);
    }

    /**
     * Kürzt einen Titel auf {@link #MAX_TITLE_CHARS} Zeichen, falls er länger ist.
     *
     * @param title der zu kürzende Titel
     * @return der gekürzte Titel, oder der unveränderte Titel, wenn er nicht zu lang ist
     */
    private static String cap(String title) {
        return title.length() > MAX_TITLE_CHARS ? title.substring(0, MAX_TITLE_CHARS) : title;
    }

    /**
     * Liefert den Typ dieser Spalte.
     *
     * @return der {@link ColumnType} der Spalte
     */
    public ColumnType getType() {
        return type;
    }

    /**
     * Liefert den aktuellen Titel der Spalte.
     *
     * @return der Titel
     */
    public String getTitle() {
        return title;
    }

    /**
     * Benennt die Spalte um.
     *
     * <p>Der neue Titel wird, wie beim Erstellen, auf {@link #MAX_TITLE_CHARS} Zeichen
     * gekürzt, falls er länger ist.</p>
     *
     * @param title der neue Titel
     */
    public void rename(String title) {
        this.title = cap(title);
    }

    /**
     * Liefert den gespeicherten Wert für einen Knoten.
     *
     * <p>Wurde für den Knoten noch kein Wert gesetzt, wird der Standardwert dieses
     * Spaltentyps zurückgegeben ({@link ColumnType#getDefaultValue()}).</p>
     *
     * @param node der Knoten, dessen Wert abgefragt wird
     * @return der gespeicherte Wert oder der Standardwert, niemals {@code null}
     */
    public Object getValue(OutlineNode node) {
        Object value = values.get(node);
        return value != null ? value : type.getDefaultValue();
    }

    /**
     * Setzt den Wert für einen Knoten.
     *
     * @param node  der Knoten, dem der Wert zugeordnet wird
     * @param value der neue Wert (z. B. {@link Boolean} oder {@link String})
     */
    public void setValue(OutlineNode node, Object value) {
        values.put(node, value);
    }

    /**
     * Entfernt den gespeicherten Wert für einen Knoten.
     *
     * <p>Wird aufgerufen, wenn ein Knoten aus dem Baum gelöscht wird, damit die
     * Werte-Map nicht unbegrenzt mit Einträgen für nicht mehr existierende Knoten
     * wächst. Ist für den Knoten kein Wert gespeichert, geschieht nichts.</p>
     *
     * @param node der zu entfernende Knoten
     */
    public void removeValue(OutlineNode node) {
        values.remove(node);
    }
}