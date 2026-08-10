package view;

import model.OutlineNode;
import view.columns.RowHoverManager;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.tree.DefaultTreeCellRenderer;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;

public class HoverTreeCellRenderer extends DefaultTreeCellRenderer {

  private static final Color SELECTED_BG     = new Color(0xFC, 0x27, 0x39);
  private static final Color SELECTED_FG     = Color.WHITE;
  private static final Color SELECTED_BORDER = new Color(0xD9, 0x1B, 0x2C);

  private static final Color HOVER_BG_LIGHT = new Color(0xFD, 0xE3, 0xE5);
  private static final Color HOVER_FG_LIGHT = new Color(0x40, 0x20, 0x22);
  private static final Color HOVER_BG_DARK  = new Color(0x5A, 0x1A, 0x20);
  private static final Color HOVER_FG_DARK  = new Color(0xFF, 0xCC, 0xCC);

  private static Color defaultBg() {
    Color c = UIManager.getColor("Tree.background");
    return c != null ? c : Color.WHITE;
  }
  private static Color defaultFg() {
    Color c = UIManager.getColor("Tree.foreground");
    return c != null ? c : Color.BLACK;
  }
  private static boolean isDark() { return defaultBg().getRed() < 128; }
  private static Color hoverBg()  { return isDark() ? HOVER_BG_DARK  : HOVER_BG_LIGHT; }
  private static Color hoverFg()  { return isDark() ? HOVER_FG_DARK  : HOVER_FG_LIGHT; }

  private final RowHoverManager hoverManager;
  private final Icon contentIndicatorIcon;

  // Wrapper: label links, indicator rechts
  private final JPanel wrapper;
  private final JLabel indicatorLabel;


  public HoverTreeCellRenderer(RowHoverManager hoverManager) {
    this.hoverManager         = hoverManager;
    this.contentIndicatorIcon = loadIcon("/icons/document.png", 12);

    setBackgroundSelectionColor(SELECTED_BG);
    setTextSelectionColor(SELECTED_FG);
    setBorderSelectionColor(SELECTED_BORDER);

    setLeafIcon(loadIcon("/icons/dot.png",   8));
    setClosedIcon(loadIcon("/icons/empty.png", 8));
    setOpenIcon(loadIcon("/icons/empty.png", 8));

    wrapper = new JPanel(new BorderLayout(0, 0));
    wrapper.setOpaque(false);
    // 'this' (das Label) sitzt im CENTER
    wrapper.add(this, BorderLayout.CENTER);

    // Indicator: feste Breite nur wenn sichtbar, sonst width=0
    indicatorLabel = new JLabel();
    indicatorLabel.setOpaque(false);
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

    setBackgroundNonSelectionColor(defaultBg());
    setTextNonSelectionColor(defaultFg());

    super.getTreeCellRendererComponent(
            tree, value, selected, expanded, leaf, row, hasFocus);

    // ── Content-Indicator ───────────────────────────────────────────────
    boolean hasContent = false;
    if (value instanceof OutlineNode node) {
      hasContent = node.getContent() != null && !node.getContent().isBlank();
    }

    if (hasContent && contentIndicatorIcon != null) {
      indicatorLabel.setIcon(contentIndicatorIcon);
      indicatorLabel.setPreferredSize(new Dimension(contentIndicatorIcon.getIconWidth() + 6, 1));
      if (indicatorLabel.getParent() == null) {
        wrapper.add(indicatorLabel, BorderLayout.EAST);
      }
    } else {
      // Indicator aus dem Wrapper entfernen → kein Platz verbraucht
      if (indicatorLabel.getParent() != null) {
        wrapper.remove(indicatorLabel);
      }
    }

    // ── Farben ──────────────────────────────────────────────────────────
    Color bg; Color fg; boolean opaque;
    if (selected) {
      bg = SELECTED_BG; fg = SELECTED_FG; opaque = true;
    } else if (hoverManager.isHovered(row)) {
      bg = hoverBg(); fg = hoverFg(); opaque = true;
    } else {
      bg = defaultBg(); fg = defaultFg(); opaque = false;
    }

    setBackground(bg); setForeground(fg); setOpaque(opaque);
    wrapper.setBackground(bg); wrapper.setOpaque(opaque);

    return wrapper;
  }


  private Icon loadIcon(String path, int size) {
    try {
      var stream = getClass().getResourceAsStream(path);
      if (stream == null) { System.err.println("Icon nicht gefunden: " + path); return null; }
      BufferedImage img = ImageIO.read(stream);
      Image scaled = img.getScaledInstance(size, size, Image.SCALE_SMOOTH);
      return new ImageIcon(scaled);
    } catch (IOException e) {
      System.err.println("Icon konnte nicht geladen werden: " + path);
      return null;
    }
  }
}