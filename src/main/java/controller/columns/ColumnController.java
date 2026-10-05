package controller.columns;

import model.OutlineNode;
import model.columns.Column;
import model.columns.ColumnModel;
import model.columns.ColumnType;
import view.columns.ColumnCellView;
import view.columns.RowHoverManager;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Verbindet das {@link ColumnModel} mit der Darstellung der Spalten.
 *
 * <p>Der Controller ist der einzige Ort, der sowohl Model (Spaltendaten) als auch
 * View (Zell-Darstellung) kennt. Er besitzt das {@link ColumnModel}, erzeugt bei
 * jeder neu hinzugefügten {@link Column} eine passende {@link ColumnCellView},
 * verdrahtet deren Callbacks mit dem Model und entfernt die View wieder, wenn die
 * Spalte gelöscht wird.</p>
 *
 * <p>Der Controller hält selbst keine Referenz auf ein konkretes Container-Panel wie
 * {@code view.columns.ColumnPanel} oder auf {@code view.HeaderPanel}. Stattdessen
 * benachrichtigt er registrierte {@link ColumnViewListener}, sobald eine
 * {@link ColumnCellView} erzeugt oder entfernt wurde. So kann sowohl das
 * Spalten-Panel (fügt die View in sein Layout ein) als auch das Header-Panel (baut
 * die passende Kopfzeile) unabhängig voneinander auf dieselben Ereignisse reagieren,
 * ohne dass dieser Controller eine der beiden Klassen kennen muss.</p>
 */
public class ColumnController implements ColumnModel.ColumnModelListener {

    /** Das vom Controller verwaltete Model mit allen Spalten. */
    private final ColumnModel columnModel = new ColumnModel();

    /** Der gemeinsame Hover-Manager, der an jede neu erzeugte {@link ColumnCellView} weitergegeben wird. */
    private final RowHoverManager hoverManager;

    /** Ordnet jeder Spalte die für sie erzeugte View zu. */
    private final Map<Column, ColumnCellView> cellViews = new LinkedHashMap<>();

    /** Die registrierten Listener, die über erzeugte bzw. entfernte Views benachrichtigt werden. */
    private final List<ColumnViewListener> viewListeners = new ArrayList<>();

    /**
     * Callback für „irgendetwas an den Spalten hat sich geändert“ (Spalte hinzugefügt,
     * entfernt, umbenannt oder ein Zellenwert geändert). Typischerweise an
     * {@code markDirty()} des Hauptfensters angeschlossen.
     */
    private Runnable onChange;


    /**
     * Erstellt einen neuen Controller mit leerem Model.
     *
     * @param hoverManager der Hover-Manager, der an alle erzeugten {@link ColumnCellView}
     *                      weitergegeben wird, damit Zeilen-Hover spaltenübergreifend
     *                      synchron bleibt
     */
    public ColumnController(RowHoverManager hoverManager) {
        this.hoverManager = hoverManager;
        columnModel.addChangeListener(this);
    }


    /**
     * Fügt eine neue Spalte hinzu.
     *
     * <p>Legt die Spalte im Model an, was über {@link #columnAdded(Column)} automatisch
     * die passende {@link ColumnCellView} erzeugt und alle {@link ColumnViewListener}
     * benachrichtigt.</p>
     *
     * @param title der Titel der neuen Spalte
     * @param type  der Typ der neuen Spalte
     * @return die neu erstellte Spalte
     */
    public Column addColumn(String title, ColumnType type) {
        return columnModel.addColumn(title, type);
    }

    /**
     * Entfernt eine Spalte.
     *
     * <p>Entfernt die Spalte aus dem Model, was über {@link #columnRemoved(Column)}
     * automatisch die zugehörige {@link ColumnCellView} abbaut und alle
     * {@link ColumnViewListener} benachrichtigt. Ist die Spalte nicht vorhanden,
     * geschieht nichts.</p>
     *
     * @param column die zu entfernende Spalte
     */
    public void removeColumn(Column column) {
        columnModel.removeColumn(column);
    }

    /**
     * Benennt eine Spalte um.
     *
     * <p>Ändert nur den Titel im Model ({@link Column#rename(String)}). Die bereits
     * berechnete Breite der zugehörigen {@link ColumnCellView} wird dadurch
     * <strong>nicht</strong> neu berechnet, ebenso wenig wie beim bisherigen
     * {@code OutlineColumn.setTitle}. Löst {@link #onChange} aus, damit z. B.
     * {@code markDirty()} aufgerufen wird.</p>
     *
     * @param column   die umzubenennende Spalte
     * @param newTitle der neue Titel
     */
    public void renameColumn(Column column, String newTitle) {
        column.rename(newTitle);
        fireChange();
    }


    /**
     * Erzeugt für eine neu hinzugefügte Spalte die passende {@link ColumnCellView}.
     *
     * <p>Die View erhält den gemeinsamen Hover-Manager und meldet Datenänderungen über
     * {@link #fireChange()}. Anschließend werden alle {@link ColumnViewListener} über
     * {@link ColumnViewListener#columnViewAdded(Column, ColumnCellView)} benachrichtigt,
     * damit sie die View z. B. in ihr Layout einfügen können.</p>
     *
     * @param column die hinzugefügte Spalte
     */
    @Override
    public void columnAdded(Column column) {
        ColumnCellView view = new ColumnCellView(column);
        view.setHoverManager(hoverManager);
        view.setOnDataChanged(this::fireChange);
        cellViews.put(column, view);

        for (ColumnViewListener l : viewListeners) {
            l.columnViewAdded(column, view);
        }
        fireChange();
    }

    /**
     * Baut die {@link ColumnCellView} einer entfernten Spalte ab.
     *
     * <p>Meldet die View zunächst beim Hover-Manager ab (siehe
     * {@link ColumnCellView#setHoverManager(RowHoverManager)}), damit sie nicht als
     * „toter“ Listener registriert bleibt, und benachrichtigt danach alle
     * {@link ColumnViewListener} über
     * {@link ColumnViewListener#columnViewRemoved(Column, ColumnCellView)}, damit sie
     * die View z. B. aus ihrem Layout entfernen können.</p>
     *
     * @param column die entfernte Spalte
     */
    @Override
    public void columnRemoved(Column column) {
        ColumnCellView view = cellViews.remove(column);
        if (view != null) {
            view.setHoverManager(null);
            for (ColumnViewListener l : viewListeners) {
                l.columnViewRemoved(column, view);
            }
        }
        fireChange();
    }


    /**
     * Baut die Zeilen aller Spalten anhand der aktuell sichtbaren Knoten des Baums neu auf.
     *
     * <p>Entspricht dem, was bisher {@code ColumnPanel.syncWithTree} für alle
     * {@code OutlineColumn}-Instanzen erledigt hat, arbeitet hier aber auf den vom
     * Controller verwalteten {@link ColumnCellView}-Instanzen.</p>
     *
     * @param nodes     die aktuell sichtbaren Knoten des Baums in Zeilenreihenfolge
     * @param rowHeight die Zeilenhöhe des Baums in Pixeln
     */
    public void syncWithTree(OutlineNode[] nodes, int rowHeight) {
        for (ColumnCellView view : cellViews.values()) {
            view.rebuildRows(nodes, rowHeight, 0);
        }
    }

    /**
     * Entfernt die gespeicherten Werte eines gelöschten Knotens aus allen Spalten.
     *
     * <p>Ist für Knoten gedacht, die aus dem {@code DefaultTreeModel} entfernt wurden
     * (z. B. aus {@code TreeModelListener#treeNodesRemoved}), damit die Werte-Map
     * jeder {@link Column} nicht unbegrenzt mit Einträgen für nicht mehr existierende
     * Knoten wächst. Objekte, die keine {@link OutlineNode}-Instanzen sind, werden
     * ignoriert.</p>
     *
     * @param removedNodes die aus dem Baum entfernten Objekte
     */
    public void handleNodesRemoved(Object[] removedNodes) {
        for (Object obj : removedNodes) {
            if (obj instanceof OutlineNode node) {
                for (Column column : columnModel.getColumns()) {
                    column.removeValue(node);
                }
            }
        }
    }


    /**
     * Gibt an, ob mindestens eine Spalte vorhanden ist.
     *
     * @return {@code true}, wenn Spalten vorhanden sind, sonst {@code false}
     */
    public boolean hasColumns() {
        return columnModel.hasColumns();
    }

    /**
     * Liefert die Anzahl der vorhandenen Spalten.
     *
     * @return die Anzahl der Spalten
     */
    public int getColumnCount() {
        return columnModel.getColumnCount();
    }

    /**
     * Liefert eine schreibgeschützte Sicht auf die vorhandenen Spalten.
     *
     * @return unveränderbare Liste der Spalten in Anzeigereihenfolge
     */
    public List<Column> getColumns() {
        return columnModel.getColumns();
    }

    /**
     * Liefert die zu einer Spalte gehörende View.
     *
     * @param column die Spalte, zu der die View gesucht wird
     * @return die zugehörige {@link ColumnCellView}, oder {@code null}, wenn die
     *         Spalte nicht (mehr) verwaltet wird
     */
    public ColumnCellView getCellView(Column column) {
        return cellViews.get(column);
    }


    /**
     * Registriert einen Listener, der über erzeugte und entfernte
     * {@link ColumnCellView}-Instanzen benachrichtigt wird.
     *
     * @param listener der zu registrierende Listener
     */
    public void addViewListener(ColumnViewListener listener) {
        viewListeners.add(listener);
    }

    /**
     * Entfernt einen zuvor registrierten {@link ColumnViewListener}.
     *
     * @param listener der zu entfernende Listener
     */
    public void removeViewListener(ColumnViewListener listener) {
        viewListeners.remove(listener);
    }


    /**
     * Setzt den Callback, der bei jeder Änderung an den Spalten aufgerufen wird:
     * Spalte hinzugefügt, entfernt, umbenannt, oder ein Zellenwert geändert.
     *
     * @param onChange der auszuführende Callback, oder {@code null}
     */
    public void setOnChange(Runnable onChange) {
        this.onChange = onChange;
    }

    /**
     * Ruft den über {@link #setOnChange(Runnable)} gesetzten Callback auf, sofern
     * einer gesetzt ist.
     */
    private void fireChange() {
        if (onChange != null) {
            onChange.run();
        }
    }


    /**
     * Wird über neu erzeugte bzw. entfernte {@link ColumnCellView}-Instanzen
     * benachrichtigt.
     *
     * <p>Typischerweise von {@code view.columns.ColumnPanel} implementiert, um die
     * View in sein Layout einzufügen bzw. daraus zu entfernen, und von
     * {@code view.HeaderPanel}, um die passende Kopfzeile aufzubauen bzw. abzubauen.</p>
     */
    public interface ColumnViewListener {

        /**
         * Wird aufgerufen, nachdem für eine neu hinzugefügte Spalte eine
         * {@link ColumnCellView} erzeugt wurde.
         *
         * @param column die hinzugefügte Spalte
         * @param view   die für sie erzeugte View
         */
        void columnViewAdded(Column column, ColumnCellView view);

        /**
         * Wird aufgerufen, nachdem die {@link ColumnCellView} einer entfernten Spalte
         * abgebaut wurde.
         *
         * @param column die entfernte Spalte
         * @param view   die für sie abgebaute View
         */
        void columnViewRemoved(Column column, ColumnCellView view);
    }
}