package view;

import view.columns.RowHoverManager;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.tree.DefaultTreeCellRenderer;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;

public class HoverTreeCellRenderer extends DefaultTreeCellRenderer {

  // Accent-Farben bleiben fix – gehören zum App-Design
  private static final Color SELECTED_BG     = new Color(0xFC, 0x27, 0x39);
  private static final Color SELECTED_FG     = Color.WHITE;
  private static final Color SELECTED_BORDER = new Color(0xD9, 0x1B, 0x2C);

  // Hover hell (Light Mode)
  private static final Color HOVER_BG_LIGHT = new Color(0xFD, 0xE3, 0xE5);
  private static final Color HOVER_FG_LIGHT = new Color(0x40, 0x20, 0x22);

  // Hover dunkel (Dark Mode)
  private static final Color HOVER_BG_DARK  = new Color(0x5A, 0x1A, 0x20);
  private static final Color HOVER_FG_DARK  = new Color(0xFF, 0xCC, 0xCC);

  // Default: immer aus UIManager lesen
  private static Color defaultBg() {
    Color c = UIManager.getColor("Tree.background");
    return c != null ? c : Color.WHITE;
  }
  private static Color defaultFg() {
    Color c = UIManager.getColor("Tree.foreground");
    return c != null ? c : Color.BLACK;
  }
  private static boolean isDark() {
    return defaultBg().getRed() < 128;
  }
  private static Color hoverBg() { return isDark() ? HOVER_BG_DARK : HOVER_BG_LIGHT; }
  private static Color hoverFg() { return isDark() ? HOVER_FG_DARK : HOVER_FG_LIGHT; }

  private final RowHoverManager hoverManager;


  public HoverTreeCellRenderer(RowHoverManager hoverManager) {
    this.hoverManager = hoverManager;

    setBackgroundSelectionColor(SELECTED_BG);
    setTextSelectionColor(SELECTED_FG);
    setBorderSelectionColor(SELECTED_BORDER);

    Icon leaf   = loadIcon("/icons/dot.png",   8);
    Icon closed = loadIcon("/icons/empty.png", 8);
    Icon open   = loadIcon("/icons/empty.png", 8);

    setLeafIcon(leaf);
    setClosedIcon(closed);
    setOpenIcon(open);
  }


  @Override
  public Component getTreeCellRendererComponent(
      JTree tree,
      Object value,
      boolean selected,
      boolean expanded,
      boolean leaf,
      int row,
      boolean hasFocus) {

    // Default-Farben vor super-Aufruf aktuell setzen
    setBackgroundNonSelectionColor(defaultBg());
    setTextNonSelectionColor(defaultFg());

    Component c = super.getTreeCellRendererComponent(
        tree, value, selected, expanded, leaf, row, hasFocus);

    JLabel label = (JLabel) c;

    if (selected) {
      label.setBackground(SELECTED_BG);
      label.setForeground(SELECTED_FG);
      label.setOpaque(true);

    } else if (hoverManager.isHovered(row)) {
      label.setBackground(hoverBg());
      label.setForeground(hoverFg());
      label.setOpaque(true);

    } else {
      label.setBackground(defaultBg());
      label.setForeground(defaultFg());
      label.setOpaque(false);
    }

    return c;
  }

  private Icon loadIcon(String path, int size) {
    try {
      var stream = getClass().getResourceAsStream(path);
      if (stream == null) {
        System.err.println("Icon nicht gefunden: " + path);
        return null;
      }
      BufferedImage img = ImageIO.read(stream);
      Image scaled = img.getScaledInstance(size, size, Image.SCALE_SMOOTH);
      return new ImageIcon(scaled);
    } catch (IOException e) {
      System.err.println("Icon konnte nicht geladen werden: " + path);
      return null;
    }
  }
}