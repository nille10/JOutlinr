package view.columns;

import model.OutlineNode;

import javax.swing.*;
import javax.swing.border.MatteBorder;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;

/**
 * Eine einzelne Spalte im ColumnPanel.
 *
 * Aufbau:
 *   ┌─────────────┐
 *   │  Header     │  ← JPanel (feste Höhe = HEADER_HEIGHT)
 *   ├─────────────┤
 *   │  Zelle 0    │  ← je nach ColumnType ein JCheckBox / JTextField / JLabel
 *   │  Zelle 1    │
 *   │  …          │
 *   └─────────────┘
 *
 * Die Zeilenhöhe (ROW_HEIGHT) muss mit JTree.getRowHeight() übereinstimmen.
 */
public class OutlineColumn extends JPanel {

  public static final int HEADER_HEIGHT = 28;
  public static final int MIN_WIDTH     = 80;
  public static final int DEFAULT_WIDTH = 140;

  private final ColumnType type;
  private String title;

  /** Speichert den Komponentenwert pro Knoten-Referenz. */
  private final Map<OutlineNode, Object> dataMap = new HashMap<>();

  /** Die Box die alle Zeilen-Panels enthält (kein Header). */
  private final JPanel cellContainer;

  /** Aktuelle Zeilen in Reihenfolge der Tree-Rows. */
  private OutlineNode[] currentRows = new OutlineNode[0];

  private int rowHeight = 24;

  // ── Konstruktor ──────────────────────────────────────────────────────────

  public OutlineColumn(String title, ColumnType type) {
    this.title = title;
    this.type  = type;

    setLayout(new BorderLayout());
    setBackground(new Color(248, 248, 252));

    // Header
    JPanel header = buildHeader();
    add(header, BorderLayout.NORTH);

    // Zellen-Container (BoxLayout vertikal)
    cellContainer = new JPanel();
    cellContainer.setLayout(new BoxLayout(cellContainer, BoxLayout.Y_AXIS));
    cellContainer.setBackground(new Color(248, 248, 252));
    cellContainer.setOpaque(true);
    add(cellContainer, BorderLayout.CENTER);

    setPreferredSize(new Dimension(DEFAULT_WIDTH, 0));
    setMinimumSize(new Dimension(MIN_WIDTH, 0));
  }

  // ── Header ───────────────────────────────────────────────────────────────

  private JPanel buildHeader() {
    JPanel header = new JPanel(new BorderLayout(4, 0));
    header.setPreferredSize(new Dimension(0, HEADER_HEIGHT));
    header.setMinimumSize(new Dimension(0, HEADER_HEIGHT));
    header.setMaximumSize(new Dimension(Integer.MAX_VALUE, HEADER_HEIGHT));
    header.setBackground(new Color(235, 235, 240));
    header.setBorder(new MatteBorder(0, 1, 1, 0, new Color(200, 200, 210)));
    header.setOpaque(true);

    JLabel titleLabel = new JLabel(title);
    titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 11));
    titleLabel.setForeground(new Color(80, 80, 100));
    titleLabel.setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 4));
    header.add(titleLabel, BorderLayout.CENTER);

    // Löschen-Button (×)
    JButton removeBtn = new JButton("×");
    removeBtn.setFont(new Font("Segoe UI", Font.PLAIN, 13));
    removeBtn.setForeground(new Color(150, 80, 80));
    removeBtn.setBorderPainted(false);
    removeBtn.setContentAreaFilled(false);
    removeBtn.setFocusable(false);
    removeBtn.setToolTipText("Spalte entfernen");
    removeBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    removeBtn.addActionListener(e -> removeFromParent());
    header.add(removeBtn, BorderLayout.EAST);

    return header;
  }

  // ── Zeilen aktualisieren ─────────────────────────────────────────────────

  /**
   * Wird vom OutlinePanel aufgerufen, wenn sich der Baum ändert.
   * Baut alle Zellen-Panels neu auf, passend zu den sichtbaren Tree-Rows.
   *
   * @param nodes  geordnetes Array aller aktuell sichtbaren Knoten (Row 0..n)
   * @param rowH   Zeilenhöhe des JTree
   * @param treeY  Y-Offset des JTree im Scroll-Viewport (für späteren Einsatz)
   */
  public void rebuildRows(OutlineNode[] nodes, int rowH, int treeY) {
    this.rowHeight   = rowH;
    this.currentRows = nodes;

    cellContainer.removeAll();

    for (OutlineNode node : nodes) {
      JPanel row = buildCell(node, rowH);
      row.setMaximumSize(new Dimension(Integer.MAX_VALUE, rowH));
      row.setMinimumSize(new Dimension(0, rowH));
      row.setPreferredSize(new Dimension(getWidth(), rowH));
      cellContainer.add(row);
    }

    cellContainer.revalidate();
    cellContainer.repaint();
  }

  // ── Zelle bauen je nach Typ ───────────────────────────────────────────────

  private JPanel buildCell(OutlineNode node, int rowH) {
    JPanel cell = new JPanel(new BorderLayout());
    cell.setBackground(Color.WHITE);
    cell.setBorder(new MatteBorder(0, 1, 1, 0, new Color(220, 220, 228)));
    cell.setOpaque(true);

    switch (type) {
      case CHECKBOX -> {
        JCheckBox cb = new JCheckBox();
        cb.setHorizontalAlignment(SwingConstants.CENTER);
        cb.setOpaque(false);
        cb.setSelected(Boolean.TRUE.equals(dataMap.get(node)));
        cb.addItemListener(e -> dataMap.put(node, cb.isSelected()));
        cell.add(cb, BorderLayout.CENTER);
      }
      case TEXTFIELD -> {
        JTextField tf = new JTextField();
        tf.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        tf.setBorder(BorderFactory.createEmptyBorder(2, 6, 2, 6));
        tf.setText(dataMap.getOrDefault(node, "").toString());
        tf.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
          public void insertUpdate(javax.swing.event.DocumentEvent e)  { dataMap.put(node, tf.getText()); }
          public void removeUpdate(javax.swing.event.DocumentEvent e)  { dataMap.put(node, tf.getText()); }
          public void changedUpdate(javax.swing.event.DocumentEvent e) { dataMap.put(node, tf.getText()); }
        });
        cell.add(tf, BorderLayout.CENTER);
      }
      case LABEL -> {
        JLabel lbl = new JLabel(dataMap.getOrDefault(node, "").toString());
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lbl.setForeground(new Color(80, 80, 100));
        lbl.setBorder(BorderFactory.createEmptyBorder(2, 8, 2, 8));
        cell.add(lbl, BorderLayout.CENTER);
      }
    }

    return cell;
  }

  // ── Spalte aus dem Parent entfernen ──────────────────────────────────────

  private void removeFromParent() {
    Container parent = getParent();
    if (parent != null) {
      parent.remove(this);
      parent.revalidate();
      parent.repaint();
    }
  }

  // ── Getter ────────────────────────────────────────────────────────────────

  public ColumnType getType()  { return type; }
  public String     getTitle() { return title; }

  /** Gibt den gespeicherten Wert für einen Knoten zurück. */
  public Object getValue(OutlineNode node) {
    return dataMap.get(node);
  }
}