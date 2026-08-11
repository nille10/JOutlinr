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

public class OutlineColumn extends JPanel {

  private static final int TEXTFIELD_CHARS   = 40;
  private static final int HEADER_PADDING    = 40;
  private static final int TEXTFIELD_PADDING = 12;

  // Farben dynamisch aus UIManager – passen sich Dark/Light an
  private static Color cellDefault() {
    Color c = UIManager.getColor("List.background");
    return c != null ? c : Color.WHITE;
  }

  private static Color cellHover() {
    Color base = cellDefault();
    // Im Dark Mode ist base dunkel → aufhellen; im Light Mode → abdunkeln
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

  private static Color borderCol() {
    Color c = UIManager.getColor("Separator.foreground");
    return c != null ? c : new Color(200, 200, 210);
  }

  private final ColumnType type;
  private final int columnWidth;
  private String title; //kann sich ändern (umbenennen)

  private final Map<OutlineNode, Object> dataMap    = new HashMap<>();
  private final JPanel                   cellContainer;
  private final List<JPanel>             cellPanels = new ArrayList<>();

  private OutlineNode[]   currentRows  = new OutlineNode[0];
  private int             rowHeight    = 24;
  private RowHoverManager hoverManager;


  public OutlineColumn(String title, ColumnType type) {

    this.title = title;
    this.type = type;

    setLayout(new BorderLayout());
    setOpaque(false); // kein eigener Hintergrund – Zellen kümmern sich selbst

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
    cell.setBackground(cellDefault()); // dynamisch
    cell.setBorder(new MatteBorder(0, 1, 1, 0, borderCol())); // dynamisch
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
        // kein setForeground – FlatLaf übernimmt das
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
      // Farben bei jedem Hover-Update frisch aus UIManager lesen
      cell.setBackground(i == hovered ? cellHover() : cellDefault());
      cell.setBorder(new MatteBorder(0, 1, 1, 0, borderCol()));
      cell.repaint();
    }
  }

  //column data Change
  private Runnable onDataChanged;

  public void setOnDataChanged(Runnable callback) {
    this.onDataChanged = callback;
  }

  // ── Getter ──

  public ColumnType getType()                  { return type; }
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
  public int        getColumnWidth()           { return columnWidth; }
  public String getTitle() {
    return title;
  }


  // ── Setter ──
  public void setValue(OutlineNode node, Object value) {
    dataMap.put(node, value);
  }

  public void setTitle(String title) {
    this.title = title;
  }
}