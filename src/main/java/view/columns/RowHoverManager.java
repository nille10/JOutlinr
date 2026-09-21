/*
  KI generiert
*/
package view.columns;

import java.util.ArrayList;
import java.util.List;

/**
 * Zentraler Hover-State für alle Zeilen des Outliners.
 *
 * <p><b>Problem ohne diesen Manager:</b><br>
 * JTree und jede {@code OutlineColumn} verwalten ihre Zellen separat. Wenn die Maus
 * über Zeile 3 im Tree fährt, wissen die Columns davon nichts – und umgekehrt.</p>
 *
 * <p><b>Lösung:</b><br>
 * Der {@code RowHoverManager} hält eine einzige Zahl ({@code hoveredRow}). Jede
 * Komponente, die Hover-Feedback geben soll, registriert einen Listener. Wenn sich
 * {@code hoveredRow} ändert, werden alle Listener benachrichtigt und zeichnen sich
 * neu.</p>
 *
 * <p><b>Verwendung:</b></p>
 * <ul>
 *   <li>OutlinePanel erstellt eine Instanz und gibt sie an alle beteiligten
 *       Komponenten weiter (Tree-Renderer, ColumnPanel).</li>
 *   <li>Mouse-Listener auf Tree und Zellen rufen {@link #setHoveredRow(int)} auf.</li>
 *   <li>Renderer und Zellen fragen {@link #isHovered(int)} ab.</li>
 * </ul>
 */
public class RowHoverManager {

  /** Wert für „keine Zeile wird gehovert“. */
  public static final int NONE = -1;

  /** Aktuell gehoverte Zeile (0-basiert, wie JTree-Rows), oder {@link #NONE}. */
  private int hoveredRow = NONE;

  /** Die registrierten Listener, die bei jeder Änderung von {@link #hoveredRow} aufgerufen werden. */
  private final List<Runnable> listeners = new ArrayList<>();


  /**
   * Setzt die aktuell gehoverte Zeile und benachrichtigt alle Listener.
   *
   * <p>Hat sich der Wert nicht geändert, geschieht nichts – so wird kein unnötiges
   * Repaint ausgelöst.</p>
   *
   * @param row der 0-basierte Zeilenindex, oder {@link #NONE}, wenn keine Zeile
   *            gehovert wird
   */
  public void setHoveredRow(int row) {
    if (hoveredRow != row) {
      hoveredRow = row;
      fireChanged();
    }
  }

  /**
   * Liefert die aktuell gehoverte Zeile.
   *
   * @return der 0-basierte Zeilenindex, oder {@link #NONE}, wenn keine Zeile
   *         gehovert wird
   */
  public int getHoveredRow() {
    return hoveredRow;
  }

  /**
   * Prüft, ob die angegebene Zeile aktuell gehovert wird.
   *
   * @param row der 0-basierte Zeilenindex
   * @return {@code true}, wenn diese Zeile die gehoverte Zeile ist, sonst {@code false}
   */
  public boolean isHovered(int row) {
    return hoveredRow == row;
  }

  /**
   * Hebt die Hover-Hervorhebung auf, indem die gehoverte Zeile auf {@link #NONE}
   * gesetzt wird. Listener werden nur benachrichtigt, wenn zuvor eine Zeile gehovert
   * wurde.
   */
  public void clearHover() {
    setHoveredRow(NONE);
  }


  /**
   * Registriert einen Listener, der bei jeder Änderung der gehoverten Zeile
   * aufgerufen wird.
   *
   * @param listener der aufzurufende Listener
   */
  public void addChangeListener(Runnable listener) {
    listeners.add(listener);
  }

  /**
   * Entfernt einen zuvor registrierten Listener.
   *
   * <p>Das Entfernen funktioniert nur mit derselben {@link Runnable}-Instanz, die
   * auch registriert wurde. Wird der Listener beim Entfernen neu erzeugt (etwa über
   * eine erneut geschriebene Methodenreferenz wie {@code this::method}), wird er
   * nicht gefunden und bleibt registriert.</p>
   *
   * @param listener der zu entfernende Listener
   */
  public void removeChangeListener(Runnable listener) {
    listeners.remove(listener);
  }

  /**
   * Benachrichtigt alle registrierten Listener über eine Änderung der gehoverten Zeile.
   */
  private void fireChanged() {
    for (Runnable l : listeners) {
      l.run();
    }
  }
}