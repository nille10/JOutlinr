/*
  KI generiert
*/
package view.columns;

import model.OutlineNode;

import javax.swing.*;
import javax.swing.border.MatteBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Eine einzelne Zusatzspalte neben dem Baum.
 *
 * <p>Die Spalte zeigt für jeden {@link OutlineNode} eine Zelle an, deren Aussehen vom
 * {@link ColumnType} abhängt (z. B. Checkbox oder Textfeld). Die eingegebenen Werte
 * werden pro Knoten in einer Map gehalten und bleiben auch dann erhalten, wenn die
 * Zellen mit {@link #rebuildRows(OutlineNode[], int, int)} neu aufgebaut werden.</p>
 *
 * <p>Alle Farben werden dynamisch aus dem {@link UIManager} gelesen und passen sich
 * daher einem Wechsel zwischen Dark- und Light-Theme an. Über einen gemeinsamen
 * {@link RowHoverManager} wird das Hervorheben der Zeile unter dem Mauszeiger mit
 * den anderen Spalten und dem Baum synchronisiert.</p>
 */
public class OutlineColumn extends JPanel {

  /** Anzahl der Zeichen (Breite des Buchstabens „M“), die ein Textfeld in dieser Spalte breit sein soll. */
  private static final int TEXTFIELD_CHARS   = 40;

  /** Zusätzlicher horizontaler Platz (in Pixeln) neben dem Titeltext bei Checkbox-Spalten. */
  private static final int HEADER_PADDING    = 40;

  /** Zusätzlicher horizontaler Platz (in Pixeln) für Rand und Innenabstand bei Textfeld-Spalten. */
  private static final int TEXTFIELD_PADDING = 12;

  /**
   * Liefert die Standard-Hintergrundfarbe einer Zelle.
   * Wird bei jedem Aufruf aus {@code List.background} des {@link UIManager} gelesen;
   * ist diese nicht definiert, wird Weiß verwendet.
   *
   * @return die Standardfarbe einer Zelle, niemals {@code null}
   */
  private static Color cellDefault() {
    Color c = UIManager.getColor("List.background");
    return c != null ? c : Color.WHITE;
  }

  /**
   * Liefert die Hintergrundfarbe einer Zelle im Hover-Zustand.
   *
   * <p>Ist die Standardfarbe dunkel (Rotanteil unter 128, Dark Mode), wird sie
   * aufgehellt; andernfalls (Light Mode) wird sie leicht abgedunkelt und im
   * Blauanteil minimal angehoben.</p>
   *
   * @return die Hover-Farbe einer Zelle
   */
  private static Color cellHover() {
    Color base = cellDefault();
    boolean isDark = base.getRed() < 128;
    int delta = 20;
    return isDark
            ? new Color(
            Math.min(base.getRed()   + delta, 255),
            Math.min(base.getGreen() + delta, 255),
            Math.min(base.getBlue()  + delta + 10, 255))
            : new Color(
            Math.max(base.getRed()   - 10, 0),
            Math.max(base.getGreen() - 10, 0),
            Math.min(base.getBlue()  + 15, 255));
  }

  /**
   * Liefert die Farbe für die Rahmenlinien der Zellen.
   * Verwendet {@code Separator.foreground} aus dem {@link UIManager}; ist diese nicht
   * definiert, wird ein helles Blaugrau (200, 200, 210) verwendet.
   *
   * @return die Rahmenfarbe, niemals {@code null}
   */
  private static Color borderCol() {
    Color c = UIManager.getColor("Separator.foreground");
    return c != null ? c : new Color(200, 200, 210);
  }

  /** Der Typ dieser Spalte; bestimmt, welche Zellen erzeugt werden und wie breit die Spalte ist. */
  private final ColumnType type;

  /** Die feste Breite der Spalte in Pixeln, einmalig im Konstruktor berechnet. */
  private final int columnWidth;

  /** Der Titel der Spalte; kann sich ändern (Umbenennen). */
  private String title;

  /** Die eingegebenen Werte pro Knoten (z. B. {@link Boolean} bei Checkboxen, {@link String} bei Textfeldern). */
  private final Map<OutlineNode, Object> dataMap    = new HashMap<>();

  /** Container, der die Zellen der Spalte vertikal untereinander anordnet. */
  private final JPanel                   cellContainer;

  /** Die aktuell aufgebauten Zellen in Zeilenreihenfolge; der Index entspricht dem Zeilenindex. */
  private final List<JPanel>             cellPanels = new ArrayList<>();

  /** Die Knoten, mit denen die Zeilen zuletzt aufgebaut wurden. */
  private OutlineNode[]   currentRows  = new OutlineNode[0];

  /** Die zuletzt verwendete Zeilenhöhe in Pixeln (Standardwert: 24). */
  private int             rowHeight    = 24;

  /** Der gemeinsame Hover-Manager zur Synchronisierung der hervorgehobenen Zeile, oder {@code null}. */
  private RowHoverManager hoverManager;

  /**
   * Der beim {@link RowHoverManager} registrierte Listener, der {@link #repaintHover()} aufruft.
   *
   * <p>Die Methodenreferenz wird bewusst nur einmal erzeugt und in diesem Feld gehalten.
   * Jeder Ausdruck {@code this::repaintHover} erzeugt ein neues Objekt; würde man ihn beim
   * Anmelden und beim Abmelden getrennt schreiben, könnte der Manager den Listener beim
   * Entfernen nicht wiederfinden. Mit diesem Feld wird für An- und Abmeldung stets dieselbe
   * Instanz verwendet.</p>
   */
  private final Runnable hoverListener = this::repaintHover;


  /**
   * Erstellt eine neue Spalte mit Titel und Typ.
   *
   * <p>Die Breite wird anhand des Typs und des (auf {@link ColumnHeader#MAX_TITLE_CHARS}
   * gekürzten) Titels berechnet und für Preferred-, Minimum- und Maximum-Größe fest
   * gesetzt. Das Panel selbst ist transparent – die Zellen zeichnen ihren Hintergrund
   * selbst.</p>
   *
   * @param title der Titel der Spalte
   * @param type  der Typ der Spalte
   */
  public OutlineColumn(String title, ColumnType type) {

    this.title = title;
    this.type = type;

    setLayout(new BorderLayout());
    setOpaque(false);

    cellContainer = new JPanel();
    cellContainer.setLayout(new BoxLayout(cellContainer, BoxLayout.Y_AXIS));
    cellContainer.setOpaque(false);
    add(cellContainer, BorderLayout.CENTER);

    String capped = title.length() > ColumnHeader.MAX_TITLE_CHARS
            ? title.substring(0, ColumnHeader.MAX_TITLE_CHARS)
            : title;

    columnWidth = computeWidth(capped);

    Dimension d = new Dimension(columnWidth, 0);
    setPreferredSize(d);
    setMinimumSize(d);
    setMaximumSize(new Dimension(columnWidth, Integer.MAX_VALUE));
  }


  /**
   * Berechnet die Breite der Spalte in Pixeln abhängig vom Spaltentyp.
   *
   * <ul>
   *   <li>{@link ColumnType#CHECKBOX}: Breite des Titels in fetter Schrift plus
   *       {@link #HEADER_PADDING}.</li>
   *   <li>{@link ColumnType#TEXTFIELD}: {@link #TEXTFIELD_CHARS} mal die Breite des
   *       Buchstabens „M“ (mindestens 8 Pixel pro Zeichen) plus
   *       {@link #TEXTFIELD_PADDING}.</li>
   *   <li>Alle anderen Typen: fester Wert von 140 Pixeln.</li>
   * </ul>
   *
   * @param title der (bereits gekürzte) Titel der Spalte
   * @return die Breite der Spalte in Pixeln
   */
  private int computeWidth(String title) {
    switch (type) {
      case CHECKBOX -> {
        Font f = new Font("Segoe UI", Font.BOLD, 12);
        @SuppressWarnings("deprecation")
        FontMetrics fm = Toolkit.getDefaultToolkit().getFontMetrics(f);
        return fm.stringWidth(title) + HEADER_PADDING;
      }
      case TEXTFIELD -> {
        Font f = new Font("Segoe UI", Font.PLAIN, 12);
        @SuppressWarnings("deprecation")
        FontMetrics fm = Toolkit.getDefaultToolkit().getFontMetrics(f);
        int charW = fm.charWidth('M');
        if (charW == 0) charW = 8;
        return charW * TEXTFIELD_CHARS + TEXTFIELD_PADDING;
      }
      default -> { return 140; }
    }
  }


  /**
   * Setzt den gemeinsamen {@link RowHoverManager} dieser Spalte.
   *
   * <p>Ein zuvor gesetzter Manager wird abgemeldet, der neue Manager (sofern nicht
   * {@code null}) meldet diese Spalte als Change-Listener an, damit die Hervorhebung
   * der Zeile bei jeder Änderung neu gezeichnet wird.</p>
   *
   * <p>Für An- und Abmeldung wird immer dieselbe Listener-Instanz
   * ({@link #hoverListener}) verwendet. Dadurch wird die Spalte beim Wechsel oder
   * Entfernen des Managers tatsächlich abgemeldet und bleibt nicht als „toter“
   * Listener registriert.</p>
   *
   * @param manager der neue Hover-Manager, oder {@code null}, um die Spalte abzumelden
   */
  public void setHoverManager(RowHoverManager manager) {
    if (this.hoverManager != null) {
      this.hoverManager.removeChangeListener(hoverListener);
    }
    this.hoverManager = manager;
    if (manager != null) {
      manager.addChangeListener(hoverListener);
    }
  }


  /**
   * Baut alle Zellen der Spalte für die übergebenen Knoten neu auf.
   *
   * <p>Bestehende Zellen werden entfernt und pro Knoten eine neue Zelle erzeugt. Die
   * bereits eingegebenen Werte bleiben erhalten, da sie in einer separaten Map pro
   * Knoten gespeichert sind.</p>
   *
   * @param nodes     die Knoten des Baums in Zeilenreihenfolge
   * @param rowHeight die Höhe jeder Zeile in Pixeln
   * @param treeY     derzeit nicht verwendet
   */
  public void rebuildRows(OutlineNode[] nodes, int rowHeight, int treeY) {

    this.currentRows = nodes;
    this.rowHeight   = rowHeight;

    cellContainer.removeAll();
    cellPanels.clear();

    for (int i = 0; i < nodes.length; i++) {
      JPanel cell = buildCell(nodes[i], i);
      cell.setAlignmentX(Component.LEFT_ALIGNMENT);
      cell.setPreferredSize(new Dimension(columnWidth, rowHeight));
      cell.setMaximumSize(new Dimension(Integer.MAX_VALUE, rowHeight));
      cell.setMinimumSize(new Dimension(0, rowHeight));
      cellContainer.add(cell);
      cellPanels.add(cell);
    }

    cellContainer.revalidate();
    cellContainer.repaint();
  }


  /**
   * Erzeugt die Zelle für einen Knoten passend zum Spaltentyp.
   *
   * <p>Die Zelle erhält Hintergrund und Rahmen aus den aktuellen UIManager-Farben und
   * einen Hover-Listener. Je nach Typ enthält sie eine {@link JCheckBox} oder ein
   * {@link JTextField}, das mit dem bisher gespeicherten Wert des Knotens
   * vorbelegt wird. Änderungen werden in die Daten-Map übernommen und über den
   * Daten-Callback gemeldet. Hier können bei Bedarf weitere Spaltentypen ergänzt
   * werden.</p>
   *
   * @param node     der Knoten, für den die Zelle erzeugt wird
   * @param rowIndex der Zeilenindex der Zelle, wird für die Hover-Synchronisierung verwendet
   * @return die fertig aufgebaute Zelle
   */
  private JPanel buildCell(OutlineNode node, int rowIndex) {

    JPanel cell = new JPanel(new BorderLayout());
    cell.setBackground(cellDefault());
    cell.setBorder(new MatteBorder(0, 1, 1, 0, borderCol()));
    cell.setOpaque(true);

    installHoverListener(cell, rowIndex);

    switch (type) {

      case CHECKBOX -> {
        JCheckBox cb = new JCheckBox();
        cb.setHorizontalAlignment(SwingConstants.CENTER);
        cb.setOpaque(false);
        cb.setSelected(Boolean.TRUE.equals(dataMap.get(node)));
        cb.addActionListener(e -> {
          dataMap.put(node, cb.isSelected());
          if (onDataChanged != null) onDataChanged.run();
        });
        installHoverListener(cb, rowIndex);
        cell.add(cb, BorderLayout.CENTER);
      }

      case TEXTFIELD -> {
        JTextField tf = new JTextField();
        tf.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        tf.setBorder(BorderFactory.createEmptyBorder(2, 6, 2, 6));
        tf.setText(String.valueOf(dataMap.getOrDefault(node, "")));
        tf.setOpaque(false);
        tf.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
          public void insertUpdate(javax.swing.event.DocumentEvent e)  { dataMap.put(node, tf.getText()); if (onDataChanged != null) onDataChanged.run(); }
          public void removeUpdate(javax.swing.event.DocumentEvent e)  { dataMap.put(node, tf.getText()); if (onDataChanged != null) onDataChanged.run(); }
          public void changedUpdate(javax.swing.event.DocumentEvent e) { dataMap.put(node, tf.getText()); if (onDataChanged != null) onDataChanged.run(); }
        });
        installHoverListener(tf, rowIndex);
        cell.add(tf, BorderLayout.CENTER);
      }
    }

    return cell;
  }


  /**
   * Installiert einen Maus-Listener, der die Hover-Zeile an den {@link RowHoverManager} meldet.
   *
   * <p>Beim Betreten der Komponente wird {@code rowIndex} als hervorgehobene Zeile
   * gesetzt, beim Verlassen wird die Hervorhebung aufgehoben. Ist kein Manager
   * gesetzt, geschieht nichts.</p>
   *
   * @param c        die Komponente, an der der Listener registriert wird
   * @param rowIndex der Zeilenindex, zu dem die Komponente gehört
   */
  private void installHoverListener(JComponent c, int rowIndex) {
    c.addMouseListener(new MouseAdapter() {
      @Override public void mouseEntered(MouseEvent e) { if (hoverManager != null) hoverManager.setHoveredRow(rowIndex); }
      @Override public void mouseExited(MouseEvent e)  { if (hoverManager != null) hoverManager.clearHover(); }
    });
  }

  /**
   * Aktualisiert die Darstellung aller Zellen entsprechend der aktuell gehoverten Zeile.
   *
   * <p>Die gehoverte Zeile erhält die Hover-Farbe, alle übrigen die Standardfarbe. Die
   * Farben werden bei jedem Aufruf frisch aus dem {@link UIManager} gelesen. Ohne
   * Hover-Manager gilt keine Zeile als gehovert.</p>
   */
  private void repaintHover() {
    int hovered = hoverManager != null ? hoverManager.getHoveredRow() : RowHoverManager.NONE;
    for (int i = 0; i < cellPanels.size(); i++) {
      JPanel cell = cellPanels.get(i);
      cell.setBackground(i == hovered ? cellHover() : cellDefault());
      cell.setBorder(new MatteBorder(0, 1, 1, 0, borderCol()));
      cell.repaint();
    }
  }

  /**
   * Callback, der aufgerufen wird, wenn sich ein Wert in dieser Spalte ändert
   * (Checkbox geklickt oder Text im Textfeld geändert).
   */
  private Runnable onDataChanged;

  /**
   * Setzt den Callback, der bei Datenänderungen in dieser Spalte aufgerufen wird.
   *
   * @param callback der auszuführende Callback, oder {@code null}
   */
  public void setOnDataChanged(Runnable callback) {
    this.onDataChanged = callback;
  }


  /**
   * Liefert den Typ dieser Spalte.
   *
   * @return der {@link ColumnType} der Spalte
   */
  public ColumnType getType()                  { return type; }

  /**
   * Liefert den gespeicherten Wert für einen Knoten.
   *
   * <p>Wurde für den Knoten noch kein Wert gesetzt, wird ein typabhängiger
   * Standardwert zurückgegeben: {@link Boolean#FALSE} bei Checkbox-Spalten, sonst
   * ein leerer String.</p>
   *
   * @param node der Knoten, dessen Wert abgefragt wird
   * @return der gespeicherte Wert oder der Standardwert, niemals {@code null}
   */
  public Object getValue(OutlineNode node) {
    Object value = dataMap.get(node);

    if (value == null) {
      return switch (type) {
        case CHECKBOX -> Boolean.FALSE;
        case TEXTFIELD -> "";
        default -> "";
      };
    }

    return value;
  }

  /**
   * Liefert die feste Breite der Spalte.
   *
   * @return die Breite in Pixeln
   */
  public int        getColumnWidth()           { return columnWidth; }

  /**
   * Liefert den aktuellen Titel der Spalte.
   *
   * @return der Titel
   */
  public String getTitle() {
    return title;
  }


  /**
   * Setzt den Wert für einen Knoten.
   *
   * <p>Der Wert wird nur in der Daten-Map gespeichert. Bereits angezeigte Zellen
   * werden dabei nicht aktualisiert; die neue Anzeige erscheint erst nach dem
   * nächsten {@link #rebuildRows(OutlineNode[], int, int)}. Der Daten-Callback wird
   * nicht ausgelöst.</p>
   *
   * @param node  der Knoten, dem der Wert zugeordnet wird
   * @param value der neue Wert (z. B. {@link Boolean} oder {@link String})
   */
  public void setValue(OutlineNode node, Object value) {
    dataMap.put(node, value);
  }

  /**
   * Setzt den Titel der Spalte.
   *
   * <p>Die Breite der Spalte wird dadurch nicht neu berechnet.</p>
   *
   * @param title der neue Titel
   */
  public void setTitle(String title) {
    this.title = title;
  }
}