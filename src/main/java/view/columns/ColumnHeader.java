package view.columns;

import javax.swing.*;
import javax.swing.border.MatteBorder;
import java.awt.*;
import java.awt.event.*;

/**
 * Einheitlicher Header für Tree- und Datenspalten.
 *
 * Eigenschaften:
 * - Titel wird auf MAX_TITLE_CHARS Zeichen begrenzt
 * - Hover-Effekt: leichte Aufhellung + Entfernen-Button erscheint
 * - Doppelklick: Titel inline bearbeiten
 * - Rechtsklick: Kontextmenü mit "Spalte entfernen"
 * - getPreferredContentWidth(): misst die Pixelbreite des Titels
 *   → wird von OutlineColumn genutzt um ihre Breite zu berechnen
 */
public class ColumnHeader extends JPanel {

  public static final int HEADER_HEIGHT   = 28;
  public static final int MAX_TITLE_CHARS = 20;

  // Padding links (8) + rechts für ×-Button (28) + Puffer (4)
  private static final int HORIZONTAL_PADDING = 40;

  private static final Color BG_DEFAULT = new Color(235, 235, 240);
  private static final Color BG_HOVER   = new Color(220, 222, 232);
  private static final Color BORDER_COL = new Color(205, 205, 215);

  private final JTextField textField;
  private final JLabel removeButton;
  private Runnable onRemove;
  private boolean hovered = false;


  public ColumnHeader(String title) {

    // ── Titel kürzen ────────────────────────────────────────────────────
    if (title.length() > MAX_TITLE_CHARS) {
      title = title.substring(0, MAX_TITLE_CHARS);
    }

    setLayout(new BorderLayout());
    setPreferredSize(new Dimension(0, HEADER_HEIGHT));
    setMinimumSize(new Dimension(0, HEADER_HEIGHT));
    setMaximumSize(new Dimension(Integer.MAX_VALUE, HEADER_HEIGHT));
    setBackground(BG_DEFAULT);
    setOpaque(true);
    setBorder(new MatteBorder(0, 1, 1, 0, BORDER_COL));

    // ── Titel-Textfeld ───────────────────────────────────────────────────
    textField = new JTextField(title);
    textField.setEditable(false);
    textField.setBorder(BorderFactory.createEmptyBorder(2, 8, 2, 4));
    textField.setOpaque(false);
    textField.setFont(new Font("Segoe UI", Font.BOLD, 12));
    textField.setForeground(new Color(70, 70, 90));
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

  /**
   * Gibt die Pixelbreite zurück die dieser Header-Text benötigt.
   *
   * Benutzt getFontMetrics() direkt auf diesem Panel – liefert
   * korrekte Werte sobald das Panel zum Layout hinzugefügt wurde.
   * Vor dem ersten Paint: Fallback auf 8px × Zeichenanzahl.
   */
  public int getPreferredContentWidth() {
    Font f = textField.getFont();
    FontMetrics fm = getFontMetrics(f);
    int textPx = fm.stringWidth(textField.getText());
    if (textPx == 0) {
      // Noch nicht auf dem Screen – grobe Schätzung
      textPx = textField.getText().length() * 8;
    }
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
    setBackground(hovered ? BG_HOVER : BG_DEFAULT);
    removeButton.setVisible(hovered && onRemove != null);
    repaint();
  }


  // ── Inline-Bearbeitung ───────────────────────────────────────────────────

  private void installEditingBehaviour() {

    // Caret standardmäßig unsichtbar – wird nur beim Editieren aktiviert
    textField.getCaret().setVisible(false);
    textField.setCaretColor(new Color(70, 70, 90));

    // Einfacher Klick startet Editierung
    textField.addMouseListener(new MouseAdapter() {
      @Override public void mouseClicked(MouseEvent e) {
        if (!textField.isEditable()) startEditing();
      }
    });

    // Enter bestätigt
    textField.addActionListener(e -> stopEditing());

    // Klick irgendwo außerhalb → Fokus geht verloren → stopEditing
    textField.addFocusListener(new FocusAdapter() {
      @Override public void focusLost(FocusEvent e) {
        stopEditing();
      }
    });
  }

  private void startEditing() {
    textField.setEditable(true);
    textField.getCaret().setVisible(true);
    textField.requestFocusInWindow();
    textField.selectAll();
  }

  private void stopEditing() {
    textField.setEditable(false);
    // Caret explizit verstecken – setEditable(false) allein reicht nicht
    textField.getCaret().setVisible(false);
    textField.select(0, 0);      // Selektion aufheben
    textField.setFocusable(false);
    textField.setFocusable(true); // Fokus-Kette resetten ohne Sichtbarkeitseffekte
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


  // ── Getter / Setter ───────────────────────────────────────────────────────

  public String     getTitle()     { return textField.getText(); }
  public void       setTitle(String t) { textField.setText(t); }
  public JTextField getTextField() { return textField; }
}