package model.columns;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Verwaltet die Liste aller {@link Column}s eines Outliners.
 *
 * <p>Das Model enthält keinerlei Swing- oder Baum-Code: Es kennt weder den
 * {@code JTree} noch {@link model.OutlineNode}-Löschungen. Das Aufräumen von
 * Werten gelöschter Knoten (über {@link Column#removeValue}) ist bewusst nicht
 * Aufgabe dieser Klasse, sondern Sache des Controllers, der auf die
 * entsprechenden Baum-Events reagiert.</p>
 *
 * <p>Interessenten (typischerweise der Controller, der daraufhin die passenden
 * Views erzeugt oder entfernt) können sich über {@link #addChangeListener} für
 * Änderungen an der Spaltenliste benachrichtigen lassen.</p>
 */
public class ColumnModel {

    /** Alle aktuell vorhandenen Spalten in ihrer Anzeigereihenfolge (von links nach rechts). */
    private final List<Column> columns = new ArrayList<>();

    /** Die registrierten Listener, die bei jeder Änderung der Spaltenliste benachrichtigt werden. */
    private final List<ColumnModelListener> listeners = new ArrayList<>();

    /**
     * Erstellt eine neue, hinzugefügte Spalte und fügt sie am rechten Ende der
     * Liste hinzu.
     *
     * <p>Alle registrierten Listener werden anschließend über
     * {@link ColumnModelListener#columnAdded(Column)} benachrichtigt.</p>
     *
     * @param title der Titel der neuen Spalte
     * @param type  der Typ der neuen Spalte
     * @return die neu erstellte Spalte
     */
    public Column addColumn(String title, ColumnType type) {
        Column column = new Column(title, type);
        columns.add(column);
        fireColumnAdded(column);
        return column;
    }

    /**
     * Entfernt die angegebene Spalte aus der Liste.
     *
     * <p>Ist die Spalte enthalten, werden alle registrierten Listener über
     * {@link ColumnModelListener#columnRemoved(Column)} benachrichtigt. Ist sie
     * nicht enthalten, geschieht nichts.</p>
     *
     * @param column die zu entfernende Spalte
     */
    public void removeColumn(Column column) {
        if (columns.remove(column)) {
            fireColumnRemoved(column);
        }
    }

    /**
     * Gibt an, ob mindestens eine Spalte vorhanden ist.
     *
     * @return {@code true}, wenn das Model Spalten enthält, sonst {@code false}
     */
    public boolean hasColumns() {
        return !columns.isEmpty();
    }

    /**
     * Liefert die Anzahl der vorhandenen Spalten.
     *
     * @return die Anzahl der Spalten
     */
    public int getColumnCount() {
        return columns.size();
    }

    /**
     * Liefert eine schreibgeschützte Sicht auf die vorhandenen Spalten.
     *
     * <p>Die Liste spiegelt spätere Änderungen wider, kann aber vom Aufrufer nicht
     * verändert werden ({@link UnsupportedOperationException}). Um Spalten
     * hinzuzufügen oder zu entfernen, sind {@link #addColumn} und
     * {@link #removeColumn} zu verwenden.</p>
     *
     * @return unveränderbare Liste der Spalten in Anzeigereihenfolge
     */
    public List<Column> getColumns() {
        return Collections.unmodifiableList(columns);
    }

    /**
     * Registriert einen Listener, der über hinzugefügte und entfernte Spalten
     * benachrichtigt wird.
     *
     * @param listener der zu registrierende Listener
     */
    public void addChangeListener(ColumnModelListener listener) {
        listeners.add(listener);
    }

    /**
     * Entfernt einen zuvor registrierten Listener.
     *
     * @param listener der zu entfernende Listener
     */
    public void removeChangeListener(ColumnModelListener listener) {
        listeners.remove(listener);
    }

    /**
     * Benachrichtigt alle registrierten Listener über eine neu hinzugefügte Spalte.
     *
     * @param column die hinzugefügte Spalte
     */
    private void fireColumnAdded(Column column) {
        for (ColumnModelListener l : listeners) {
            l.columnAdded(column);
        }
    }

    /**
     * Benachrichtigt alle registrierten Listener über eine entfernte Spalte.
     *
     * @param column die entfernte Spalte
     */
    private void fireColumnRemoved(Column column) {
        for (ColumnModelListener l : listeners) {
            l.columnRemoved(column);
        }
    }

    /**
     * Wird über Änderungen an einem {@link ColumnModel} benachrichtigt.
     *
     * <p>Typischerweise vom Controller implementiert, um für jede hinzugefügte
     * Spalte die passenden View-Komponenten ({@code ColumnHeader},
     * {@code ColumnCellView}) zu erzeugen bzw. beim Entfernen wieder abzubauen.</p>
     */
    public interface ColumnModelListener {

        /**
         * Wird aufgerufen, nachdem eine neue Spalte hinzugefügt wurde.
         *
         * @param column die hinzugefügte Spalte
         */
        void columnAdded(Column column);

        /**
         * Wird aufgerufen, nachdem eine Spalte entfernt wurde.
         *
         * @param column die entfernte Spalte
         */
        void columnRemoved(Column column);
    }
}