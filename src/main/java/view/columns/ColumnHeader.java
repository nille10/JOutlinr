package view.columns;

import javax.swing.*;
import javax.swing.border.MatteBorder;
import java.awt.*;
import java.awt.event.*;

public class ColumnHeader extends JPanel {

  public static final int HEADER_HEIGHT   = 28;
  public static final int MAX_TITLE_CHARS = 20;
  private static final int HORIZONTAL_PADDING = 40;

  // Farben immer zur Laufzeit aus UIManager lesen → korrekt bei Dark/Light-Wechsel
  private static Color bgDefault() { return UIManager.getColor("Panel.background"); }
  private static Color bgHover() {
    Color b = UIManager.getColor("Panel.background");
    return new Color(
        Math.max(b.getRed()   - 12, 0),
        Math.max(b.getGreen() - 12, 0),
        Math.max(b.getBlue()  - 12, 0));
  }
  private static Color borderCol() {
    Color c = UIManager.getColor("Separator.foreground");
    return c != null ? c : new Color(180, 180, 180);
  }

  private final JTextField textField;
  private final JLabel removeButton;
  private Runnable onRemove;
  private java.util.function.Consumer<String> onRename;
  private boolean hovered = false;


  public ColumnHeader(String title) {

    if (title.length() > MAX_TITLE_CHARS) {
      title = title.substring(0, MAX_TITLE_CHARS);
    }

    setLayout(new BorderLayout());
    setPreferredSize(new Dimension(0, HEADER_HEIGHT));
    setMinimumSize(new Dimension(0, HEADER_HEIGHT));
    setMaximumSize(new Dimension(Integer.MAX_VALUE, HEADER_HEIGHT));
    setOpaque(true);
    // Hintergrund beim Erstellen setzen – wird bei applyHover() aktualisiert
    setBackground(bgDefault());
    setBorder(new MatteBorder(0, 1, 1, 0, borderCol()));

    // ── Titel-Textfeld ───────────────────────────────────────────────────
    textField = new JTextField(title);
    textField.setEditable(false);
    textField.setBorder(BorderFactory.createEmptyBorder(2, 8, 2, 4));
    textField.setOpaque(false);
    textField.setFont(new Font("Segoe UI", Font.BOLD, 12));
    // kein setForeground – FlatLaf übernimmt TextField.foreground
    add(textField, BorderLayout.CENTER);

    // ── Entfernen-Button (×) ─────────────────────────────────────────────
    removeButton = new JLabel("  ×  ");
    removeButton.setFont(new Font("Segoe UI", Font.PLAIN, 13));
    removeButton.setForeground(new Color(160, 80, 80));
    removeButton.setOpaque(false);
    removeButton.setVisible(false);
    removeButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    removeButton.setToolTipText("Spalte entfernen");
    removeButton.addMouseListener(new MouseAdapter() {
      @Override public void mouseClicked(MouseEvent e) { fireRemove(); }
      @Override public void mouseEntered(MouseEvent e) { removeButton.setForeground(new Color(200, 40, 40)); }
      @Override public void mouseExited(MouseEvent e)  { removeButton.setForeground(new Color(160, 80, 80)); }
    });
    add(removeButton, BorderLayout.EAST);

    installHoverBehaviour();
    installEditingBehaviour();
    installContextMenu();
  }


  // ── Breite messen ────────────────────────────────────────────────────────

  public int getPreferredContentWidth() {
    Font f = textField.getFont();
    FontMetrics fm = getFontMetrics(f);
    int textPx = fm.stringWidth(textField.getText());
    if (textPx == 0) textPx = textField.getText().length() * 8;
    return textPx + HORIZONTAL_PADDING;
  }


  // ── Hover ────────────────────────────────────────────────────────────────

  private void installHoverBehaviour() {
    MouseAdapter hover = new MouseAdapter() {
      @Override public void mouseEntered(MouseEvent e) { hovered = true;  applyHover(); }
      @Override public void mouseExited(MouseEvent e)  {
        if (!contains(e.getPoint())) { hovered = false; applyHover(); }
      }
    };
    addMouseListener(hover);
    textField.addMouseListener(hover);
    removeButton.addMouseListener(new MouseAdapter() {
      @Override public void mouseExited(MouseEvent e) {
        Point p = SwingUtilities.convertPoint(removeButton, e.getPoint(), ColumnHeader.this);
        if (!ColumnHeader.this.contains(p)) { hovered = false; applyHover(); }
      }
    });
  }

  private void applyHover() {
    // Farben bei jedem Aufruf frisch aus UIManager lesen
    setBackground(hovered ? bgHover() : bgDefault());
    setBorder(new MatteBorder(0, 1, 1, 0, borderCol()));
    removeButton.setVisible(hovered && onRemove != null);
    repaint();
  }


  // ── Inline-Bearbeitung ───────────────────────────────────────────────────

  private void installEditingBehaviour() {
    textField.getCaret().setVisible(false);

    textField.addMouseListener(new MouseAdapter() {
      @Override public void mouseClicked(MouseEvent e) {
        if (!textField.isEditable()) startEditing();
      }
    });

    textField.addActionListener(e -> stopEditing());

    textField.addFocusListener(new FocusAdapter() {
      @Override public void focusLost(FocusEvent e) { stopEditing(); }
    });
  }

  private void startEditing() {
    textField.setBackground(UIManager.getColor("Panel.background"));
    textField.setOpaque(true);
    textField.setEditable(true);
    textField.getCaret().setVisible(true);
    textField.requestFocusInWindow();
    textField.selectAll();
  }

  private void stopEditing() {
    textField.setEditable(false);
    textField.setOpaque(false);
    textField.getCaret().setVisible(false);
    textField.select(0, 0);
    textField.setFocusable(false);
    textField.setFocusable(true);

    if (onRename != null) {
      onRename.accept(textField.getText());
    }
  }


  // ── Kontextmenü ──────────────────────────────────────────────────────────

  private void installContextMenu() {
    MouseAdapter rc = new MouseAdapter() {
      @Override public void mousePressed(MouseEvent e)  { if (e.isPopupTrigger()) showContextMenu(e); }
      @Override public void mouseReleased(MouseEvent e) { if (e.isPopupTrigger()) showContextMenu(e); }
    };
    addMouseListener(rc);
    textField.addMouseListener(rc);
  }

  private void showContextMenu(MouseEvent e) {
    JPopupMenu menu = new JPopupMenu();
    JMenuItem rename = new JMenuItem("Umbenennen");
    rename.addActionListener(ev -> startEditing());
    menu.add(rename);
    if (onRemove != null) {
      menu.addSeparator();
      JMenuItem remove = new JMenuItem("Spalte entfernen");
      remove.setForeground(new Color(180, 40, 40));
      remove.addActionListener(ev -> fireRemove());
      menu.add(remove);
    }
    menu.show(e.getComponent(), e.getX(), e.getY());
  }


  // ── Callback ─────────────────────────────────────────────────────────────

  private void fireRemove() { if (onRemove != null) onRemove.run(); }

  public void setOnRemove(Runnable onRemove) {
    this.onRemove = onRemove;
    removeButton.setVisible(hovered && onRemove != null);
  }

  public void setOnRename(java.util.function.Consumer<String> onRename) {
    this.onRename = onRename;
  }


  // ── Getter / Setter ───────────────────────────────────────────────────────

  public String     getTitle()         { return textField.getText(); }
  public void       setTitle(String t) { textField.setText(t); }
  public JTextField getTextField()     { return textField; }
}