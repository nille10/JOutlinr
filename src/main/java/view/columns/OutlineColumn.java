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
 * Eine einzelne Zusatzspalte.
 *
 * Breiten-Strategie je Typ:
 *
 *   CHECKBOX  → Breite = Titeltext-Pixel + Padding, fix (min = max = preferred).
 *               Titel wird auf MAX_TITLE_CHARS Zeichen begrenzt, also ist
 *               die Spalte maximal so breit wie der Header-Text.
 *
 *   TEXTFIELD → Fixe Breite = 40 Zeichen × 'M'-Breite + Padding.
 *               Unabhängig vom Titel.
 *
 * Der Tree-Header (CENTER im HeaderPanel / WEST im BodyPanel) bekommt
 * automatisch den restlichen Platz – keine explizite Breitensteuerung nötig.
 */
public class OutlineColumn extends JPanel {

  /** Anzahl Zeichen für die TEXTFIELD-Breite. */
  private static final int TEXTFIELD_CHARS   = 40;
  /** Horizontales Padding pro Spalte (links + rechts + ×-Button-Platz). */
  private static final int HEADER_PADDING    = 40;
  /** Padding innerhalb der TextField-Zelle. */
  private static final int TEXTFIELD_PADDING = 12;

  private static final Color CELL_DEFAULT = Color.WHITE;
  private static final Color CELL_HOVER   = new Color(232, 234, 246);

  private final ColumnType type;
  private final int columnWidth; // feste Breite dieser Spalte

  private final Map<OutlineNode, Object> dataMap    = new HashMap<>();
  private final JPanel                   cellContainer;
  private final List<JPanel>             cellPanels = new ArrayList<>();

  private OutlineNode[]  currentRows = new OutlineNode[0];
  private int            rowHeight   = 24;
  private RowHoverManager hoverManager;


  public OutlineColumn(String title, ColumnType type) {

    this.type = type;

    setLayout(new BorderLayout());
    setBackground(new Color(248, 248, 252));

    cellContainer = new JPanel();
    cellContainer.setLayout(new BoxLayout(cellContainer, BoxLayout.Y_AXIS));
    cellContainer.setBackground(new Color(248, 248, 252));
    // Kein AlignmentX-Problem: Zellen bekommen maximumWidth = MAX_VALUE
    // damit BoxLayout sie auf volle Containerbreite streckt
    add(cellContainer, BorderLayout.CENTER);

    // ── Breite beim Konstruieren berechnen ───────────────────────────────
    // Titel wurde in ColumnHeader bereits auf MAX_TITLE_CHARS gekürzt,
    // aber wir kürzen hier nochmal defensiv.
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
   * Berechnet die feste Spaltenbreite in Pixeln.
   *
   * CHECKBOX:  Titelbreite (Font "Segoe UI Bold 12") + HEADER_PADDING
   * TEXTFIELD: 40 × Breite von 'M' (Font "Segoe UI Plain 12") + TEXTFIELD_PADDING
   *
   * Wir messen mit einem temporären JLabel/JTextField um die Font-Metrik
   * zu bekommen – das funktioniert vor dem ersten paint().
   */
  private int computeWidth(String title) {
    switch (type) {

      case CHECKBOX -> {
        Font f = new Font("Segoe UI", Font.BOLD, 12);
        // Toolkit.getFontMetrics() funktioniert ohne sichtbares Component
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


  // ── HoverManager ─────────────────────────────────────────────────────────

  public void setHoverManager(RowHoverManager manager) {
    if (this.hoverManager != null) {
      this.hoverManager.removeChangeListener(this::repaintHover);
    }
    this.hoverManager = manager;
    if (manager != null) {
      manager.addChangeListener(this::repaintHover);
    }
  }


  // ── Zeilen aufbauen ───────────────────────────────────────────────────────

  public void rebuildRows(OutlineNode[] nodes, int rowHeight, int treeY) {

    this.currentRows = nodes;
    this.rowHeight   = rowHeight;

    cellContainer.removeAll();
    cellPanels.clear();

    for (int i = 0; i < nodes.length; i++) {

      JPanel cell = buildCell(nodes[i], i);
      // BoxLayout Y_AXIS streckt auf Containerbreite wenn:
      //   maximumSize.width = Integer.MAX_VALUE  (darf so breit werden wie Container)
      //   setAlignmentX(LEFT_ALIGNMENT)          (kein zentriertes Alignment-Problem)
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


  // ── Zelle bauen ──────────────────────────────────────────────────────────

  private JPanel buildCell(OutlineNode node, int rowIndex) {

    JPanel cell = new JPanel(new BorderLayout());
    cell.setBackground(CELL_DEFAULT);
    cell.setBorder(new MatteBorder(0, 1, 1, 0, new Color(220, 220, 228)));
    cell.setOpaque(true);

    installHoverListener(cell, rowIndex);

    switch (type) {

      case CHECKBOX -> {
        JCheckBox cb = new JCheckBox();
        cb.setHorizontalAlignment(SwingConstants.CENTER);
        cb.setOpaque(false);
        cb.setSelected(Boolean.TRUE.equals(dataMap.get(node)));
        cb.addActionListener(e -> dataMap.put(node, cb.isSelected()));
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
          public void insertUpdate(javax.swing.event.DocumentEvent e)  { dataMap.put(node, tf.getText()); }
          public void removeUpdate(javax.swing.event.DocumentEvent e)  { dataMap.put(node, tf.getText()); }
          public void changedUpdate(javax.swing.event.DocumentEvent e) { dataMap.put(node, tf.getText()); }
        });
        installHoverListener(tf, rowIndex);
        cell.add(tf, BorderLayout.CENTER);
      }
    }

    return cell;
  }


  // ── Hover ────────────────────────────────────────────────────────────────

  private void installHoverListener(JComponent c, int rowIndex) {
    c.addMouseListener(new MouseAdapter() {
      @Override public void mouseEntered(MouseEvent e) { if (hoverManager != null) hoverManager.setHoveredRow(rowIndex); }
      @Override public void mouseExited(MouseEvent e)  { if (hoverManager != null) hoverManager.clearHover(); }
    });
  }

  private void repaintHover() {
    int hovered = hoverManager != null ? hoverManager.getHoveredRow() : RowHoverManager.NONE;
    for (int i = 0; i < cellPanels.size(); i++) {
      JPanel cell = cellPanels.get(i);
      cell.setBackground(i == hovered ? CELL_HOVER : CELL_DEFAULT);
      cell.repaint();
    }
  }


  // ── Getter ────────────────────────────────────────────────────────────────

  public ColumnType getType()                    { return type; }
  public Object     getValue(OutlineNode node)   { return dataMap.get(node); }
  public int        getColumnWidth()             { return columnWidth; }
}