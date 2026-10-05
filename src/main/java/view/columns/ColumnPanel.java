/*
  KI generiert
*/
package view.columns;

import controller.columns.ColumnController;
import model.columns.Column;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Container für alle Zusatzspalten rechts neben dem JTree.
 *
 * <p>Die Spalten werden horizontal (X-Achse) nebeneinander angeordnet. Im
 * Unterschied zum früheren {@code ColumnPanel} führt diese Klasse keine eigene
 * Liste von Spalten und kennt weder {@link model.OutlineNode} noch Zeilenhöhen:
 * Sie ist ein reiner {@link ColumnController.ColumnViewListener}, der auf
 * {@code columnViewAdded}/{@code columnViewRemoved} reagiert, indem er die
 * jeweilige {@link ColumnCellView} ins Layout einfügt bzw. daraus entfernt und
 * die Gesamtbreite neu berechnet. Das Anlegen, Entfernen und Befüllen der
 * Spalten mit Zeilen übernimmt ausschließlich der {@link ColumnController}.</p>
 *
 * <p>Damit neu hinzugefügte Spalten ihre Zeilen zeigen, muss nach
 * {@link ColumnController#addColumn} wie bisher ein
 * {@link ColumnController#syncWithTree} erfolgen; dieses Panel baut die Zeilen
 * selbst nicht auf.</p>
 */
public class ColumnPanel extends JPanel implements ColumnController.ColumnViewListener {

  /** Die aktuell im Layout enthaltenen Views in ihrer Anzeigereihenfolge (von links nach rechts). */
  private final List<ColumnCellView> views = new ArrayList<>();


  /**
   * Erstellt ein neues, zunächst leeres Spaltenpanel.
   *
   * <p>Verwendet ein {@link BoxLayout} entlang der X-Achse und ist transparent,
   * sodass der Hintergrund des umgebenden Containers durchscheint.</p>
   */
  public ColumnPanel() {
    setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
    setOpaque(false);
  }


  /**
   * Fügt die View einer neu hinzugefügten Spalte am rechten Ende des Panels ein.
   *
   * <p>Wird vom {@link ColumnController} aufgerufen, nachdem er die
   * {@link ColumnCellView} für die übergebene {@link Column} erzeugt hat. Die View
   * zeigt zu diesem Zeitpunkt noch keine Zeilen; dafür ist ein anschließender
   * Aufruf von {@link ColumnController#syncWithTree} nötig.</p>
   *
   * @param column die hinzugefügte Spalte (hier nicht weiter benötigt, da das
   *               Panel keine spaltenbezogenen Daten hält)
   * @param view   die für die Spalte erzeugte View
   */
  @Override
  public void columnViewAdded(Column column, ColumnCellView view) {
    views.add(view);
    add(view);
    updatePreferredWidth();
    revalidate();
    repaint();
  }

  /**
   * Entfernt die View einer gelöschten Spalte aus dem Panel.
   *
   * <p>Wird vom {@link ColumnController} aufgerufen, nachdem er die Spalte aus
   * seinem Model entfernt hat. Ist die View nicht (mehr) im Panel enthalten,
   * geschieht nichts.</p>
   *
   * @param column die entfernte Spalte (hier nicht weiter benötigt)
   * @param view   die abzubauende View
   */
  @Override
  public void columnViewRemoved(Column column, ColumnCellView view) {
    if (views.remove(view)) {
      remove(view);
      updatePreferredWidth();
      revalidate();
      repaint();
    }
  }


  /**
   * Setzt {@code preferredSize}, {@code minimumSize} und {@code maximumSize} auf die
   * Summe der Breiten aller aktuell enthaltenen Views.
   *
   * <p>Die Höhe bleibt dabei flexibel: Sie ist 0 für die bevorzugte und minimale Größe
   * und {@link Integer#MAX_VALUE} für die maximale Größe.</p>
   */
  private void updatePreferredWidth() {
    int totalW = views.stream().mapToInt(ColumnCellView::getColumnWidth).sum();
    setPreferredSize(new Dimension(totalW, 0));
    setMaximumSize(new Dimension(totalW, Integer.MAX_VALUE));
    setMinimumSize(new Dimension(totalW, 0));
  }
}