package view.columns;

import model.OutlineNode;

import javax.swing.*;
import javax.swing.border.MatteBorder;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * ColumnPanel sitzt rechts neben dem JTree im selben Scroll-Viewport.
 *
 * Layout innerhalb des Panels:
 *   ┌──────────┬──────────┬──────────┐
 *   │ Header A │ Header B │ + Spalte │
 *   ├──────────┼──────────┤          │
 *   │ Zelle A0 │ Zelle B0 │          │
 *   │ Zelle A1 │ Zelle B1 │          │
 *   └──────────┴──────────┴──────────┘
 *
 * Der Header-Bereich hat dieselbe Höhe wie der TreeColumnHeader eines JTable,
 * damit er optisch mit dem JTree-Bereich fluchtet.
 *
 * Wichtig: ColumnPanel muss in denselben JScrollPane wie der JTree eingebettet
 * werden – dann scrollt beides gemeinsam.
 */
public class ColumnPanel extends JPanel {

  // Muss mit OutlineColumn.HEADER_HEIGHT übereinstimmen
  private static final int HEADER_HEIGHT = OutlineColumn.HEADER_HEIGHT;

  private final List<OutlineColumn> columns = new ArrayList<>();

  /** Fügt-Spalte-Button am rechten Rand */
  private final JButton addColumnButton;

  /** Aktuell sichtbare Knoten (von OutlinePanel gesetzt) */
  private OutlineNode[] currentNodes = new OutlineNode[0];
  private int currentRowHeight = 24;

  public ColumnPanel() {
    setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
    setBackground(new Color(248, 248, 252));
    setOpaque(true);

    // "Spalte hinzufügen"-Button (immer am rechten Rand)
    addColumnButton = buildAddButton();
    add(addColumnButton);
  }

  // ── Spalten hinzufügen / entfernen ───────────────────────────────────────

  /**
   * Fügt eine neue Spalte mit dem gegebenen Typ und Titel ein.
   * Die Spalte wird links vom Add-Button eingefügt.
   */
  public OutlineColumn addColumn(String title, ColumnType type) {
    OutlineColumn col = new OutlineColumn(title, type);
    // Vor dem Add-Button einfügen
    remove(addColumnButton);
    columns.add(col);
    add(col);
    add(addColumnButton);

    // Sofort mit aktuellen Daten befüllen
    col.rebuildRows(currentNodes, currentRowHeight, 0);

    revalidate();
    repaint();
    return col;
  }

  /** Entfernt eine bestimmte Spalte. */
  public void removeColumn(OutlineColumn col) {
    columns.remove(col);
    remove(col);
    revalidate();
    repaint();
  }

  // ── Zeilen synchronisieren ───────────────────────────────────────────────

  /**
   * Wird vom OutlinePanel aufgerufen, wenn sich die sichtbaren Knoten ändern.
   * Aktualisiert alle Spalten.
   *
   * @param nodes     geordnetes Array aller aktuell sichtbaren Knoten (Row 0, 1, 2…)
   * @param rowHeight Zeilenhöhe des JTree
   */
  public void syncWithTree(OutlineNode[] nodes, int rowHeight) {
    this.currentNodes    = nodes;
    this.currentRowHeight = rowHeight;

    for (OutlineColumn col : columns) {
      col.rebuildRows(nodes, rowHeight, 0);
    }

    // Gesamthöhe anpassen: Header + alle Zeilen
    int totalH = HEADER_HEIGHT + (nodes.length * rowHeight);
    setPreferredSize(new Dimension(getPreferredSize().width, totalH));
    revalidate();
    repaint();
  }

  // ── Add-Button ───────────────────────────────────────────────────────────

  private JButton buildAddButton() {
    JButton btn = new JButton("+ Spalte");
    btn.setFont(new Font("Segoe UI", Font.PLAIN, 11));
    btn.setForeground(new Color(80, 100, 180));
    btn.setBackground(new Color(238, 240, 255));
    btn.setBorder(BorderFactory.createCompoundBorder(
        new MatteBorder(0, 1, 1, 0, new Color(200, 200, 210)),
        BorderFactory.createEmptyBorder(4, 10, 4, 10)
    ));
    btn.setFocusable(false);
    btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    btn.setPreferredSize(new Dimension(90, HEADER_HEIGHT));

    btn.addActionListener(e -> showAddColumnDialog());
    return btn;
  }

  private void showAddColumnDialog() {
    JPanel panel = new JPanel(new GridBagLayout());
    GridBagConstraints c = new GridBagConstraints();
    c.insets  = new Insets(4, 4, 4, 4);
    c.fill    = GridBagConstraints.HORIZONTAL;
    c.anchor  = GridBagConstraints.WEST;

    // Titel
    c.gridx = 0; c.gridy = 0;
    panel.add(new JLabel("Titel:"), c);
    c.gridx = 1;
    JTextField titleField = new JTextField("Neue Spalte", 16);
    panel.add(titleField, c);

    // Typ-Auswahl
    c.gridx = 0; c.gridy = 1;
    panel.add(new JLabel("Typ:"), c);
    c.gridx = 1;
    JComboBox<ColumnType> typeBox = new JComboBox<>(ColumnType.values());
    panel.add(typeBox, c);

    // Beschreibung
    c.gridx = 0; c.gridy = 2; c.gridwidth = 2;
    JLabel desc = new JLabel("<html><i>" + ColumnType.CHECKBOX.getDescription() + "</i></html>");
    desc.setForeground(new Color(100, 100, 120));
    desc.setFont(new Font("Segoe UI", Font.ITALIC, 11));
    panel.add(desc, c);

    typeBox.addItemListener(e -> {
      ColumnType sel = (ColumnType) typeBox.getSelectedItem();
      if (sel != null) desc.setText("<html><i>" + sel.getDescription() + "</i></html>");
    });

    int result = JOptionPane.showConfirmDialog(
        this, panel, "Neue Spalte hinzufügen",
        JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

    if (result == JOptionPane.OK_OPTION) {
      String title = titleField.getText().trim();
      if (title.isEmpty()) title = "Spalte";
      addColumn(title, (ColumnType) typeBox.getSelectedItem());
    }
  }

  // ── Getter ────────────────────────────────────────────────────────────────

  public List<OutlineColumn> getColumns() {
    return List.copyOf(columns);
  }
}