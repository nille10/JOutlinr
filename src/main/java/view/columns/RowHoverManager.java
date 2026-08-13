/*
  KI generiert
*/
package view.columns;

import java.util.ArrayList;
import java.util.List;

/**
 * Zentraler Hover-State für alle Zeilen des Outliners.
 *
 * Problem ohne diesen Manager:
 *   JTree und jede OutlineColumn verwalten ihre Zellen separat.
 *   Wenn die Maus über Zeile 3 im Tree fährt, wissen die Columns
 *   davon nichts – und umgekehrt.
 *
 * Lösung:
 *   RowHoverManager hält eine einzige Zahl (hoveredRow).
 *   Jede Komponente die Hover-Feedback geben soll registriert
 *   einen Listener. Wenn sich hoveredRow ändert werden alle
 *   Listener benachrichtigt und zeichnen sich neu.
 *
 * Verwendung:
 *   - OutlinePanel erstellt eine Instanz und gibt sie an alle
 *     beteiligten Komponenten weiter (Tree-Renderer, ColumnPanel).
 *   - Mouse-Listener auf Tree und Zellen rufen setHoveredRow() auf.
 *   - Renderer / Zellen fragen isHovered(row) ab.
 */
public class RowHoverManager {

  public static final int NONE = -1;

  /** Aktuell gehoverte Zeile (0-basiert, wie JTree-Rows). */
  private int hoveredRow = NONE;

  private final List<Runnable> listeners = new ArrayList<>();


  // ── State ────────────────────────────────────────────────────────────────

  /**
   * Setzt die aktuell gehoverte Zeile und benachrichtigt alle Listener.
   * Kein Update wenn sich nichts geändert hat (kein unnötiges Repaint).
   */
  public void setHoveredRow(int row) {
    if (hoveredRow != row) {
      hoveredRow = row;
      fireChanged();
    }
  }

  public int getHoveredRow() {
    return hoveredRow;
  }

  public boolean isHovered(int row) {
    return hoveredRow == row;
  }

  public void clearHover() {
    setHoveredRow(NONE);
  }


  // ── Listener ─────────────────────────────────────────────────────────────

  public void addChangeListener(Runnable listener) {
    listeners.add(listener);
  }

  public void removeChangeListener(Runnable listener) {
    listeners.remove(listener);
  }

  private void fireChanged() {
    for (Runnable l : listeners) {
      l.run();
    }
  }
}