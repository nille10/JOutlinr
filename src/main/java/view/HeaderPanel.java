/*
  KI generiert
*/
package view;

import controller.columns.ColumnController;
import model.columns.Column;
import view.columns.ColumnCellView;
import view.columns.ColumnHeader;

import javax.swing.*;
import java.awt.*;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Nicht scrollender Header-Bereich des Outliners.
 *
 * Layout: BorderLayout
 *
 *   CENTER = treeHeader   → nimmt den gesamten restlichen Platz
 *   EAST   = columnHeaders (JPanel, BoxLayout X_AXIS) → fix-breite Spalten
 *
 * Damit verhält sich der Tree-Header exakt wie der Tree selbst:
 * er füllt den Platz der übrig bleibt nachdem die Columns ihren
 * festen Platz eingenommen haben.
 *
 * updateTreeHeaderWidth() ist nicht mehr nötig und entfällt –
 * BorderLayout CENTER erledigt das automatisch.
 *
 * MVC-Umbau: HeaderPanel wird nicht mehr von außen gefüttert
 * (addColumnHeader/clearColumnHeaders), sondern ist selbst ein
 * {@link ColumnController.ColumnViewListener}. Es baut und entfernt seine
 * Spalten-Header direkt als Reaktion auf die Ereignisse des
 * {@link ColumnController} und ruft bei Entfernen/Umbenennen auch direkt
 * dessen Methoden auf – ein Umweg über einen externen Callback
 * (setOnColumnRemove) ist dafür nicht mehr nötig.
 */
public class HeaderPanel extends JPanel implements ColumnController.ColumnViewListener {


    private final ColumnController controller;

    private final ColumnHeader treeHeader;

    /** Rechts-angedockter Container für alle Spalten-Header (fix-breit). */
    private final JPanel columnHeaders;

    /** Ordnet jeder Spalte ihren zugehörigen Header zu. */
    private final Map<Column, ColumnHeader> headers = new LinkedHashMap<>();


    public HeaderPanel(ColumnController controller) {
        this.controller = controller;

        setLayout(new BorderLayout());
        //setBackground(new Color(235, 235, 240));


        // ── Tree-Header (CENTER – nimmt restlichen Platz) ────────────────────
        treeHeader = new ColumnHeader("Outliner");
        // preferredSize Breite irrelevant – BorderLayout CENTER dehnt automatisch
        treeHeader.setPreferredSize(new Dimension(0, ColumnHeader.HEADER_HEIGHT));
        add(treeHeader, BorderLayout.CENTER);


        // ── Spalten-Header-Container (EAST – fix-breite Spalten) ─────────────
        columnHeaders = new JPanel();
        columnHeaders.setLayout(new BoxLayout(columnHeaders, BoxLayout.X_AXIS));
        columnHeaders.setOpaque(false);
        add(columnHeaders, BorderLayout.EAST);
    }



    // ── ColumnController.ColumnViewListener ─────────────────────────────────

    /**
     * Baut den Header für eine neu hinzugefügte Spalte auf.
     *
     * Der Header bekommt dieselbe feste Breite wie die zugehörige
     * {@link ColumnCellView} (view.getColumnWidth()) – dadurch fluchten Header
     * und Zellen exakt, genau wie bisher.
     */
    @Override
    public void columnViewAdded(Column column, ColumnCellView view) {

        ColumnHeader header = new ColumnHeader(column.getTitle());

        int w = view.getColumnWidth();
        header.setPreferredSize(new Dimension(w, ColumnHeader.HEADER_HEIGHT));
        header.setMinimumSize(new Dimension(w, ColumnHeader.HEADER_HEIGHT));
        header.setMaximumSize(new Dimension(w, ColumnHeader.HEADER_HEIGHT));

        header.setOnRemove(() -> controller.removeColumn(column));
        header.setOnRename(newTitle -> controller.renameColumn(column, newTitle));

        headers.put(column, header);
        columnHeaders.add(header);

        revalidate();
        repaint();
    }

    /**
     * Entfernt den Header einer gelöschten Spalte wieder.
     */
    @Override
    public void columnViewRemoved(Column column, ColumnCellView view) {

        ColumnHeader header = headers.remove(column);
        if (header != null) {
            columnHeaders.remove(header);
            revalidate();
            repaint();
        }
    }


    // updateTreeHeaderWidth() entfällt – BorderLayout CENTER erledigt das



    // ── Getter ────────────────────────────────────────────────────────────────

    public ColumnHeader getTreeHeader() { return treeHeader; }

    public List<ColumnHeader> getHeaders() {
        return List.copyOf(headers.values());
    }
}