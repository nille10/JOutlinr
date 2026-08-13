/*
  KI generiert
*/
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

  // Hover: sehr helles, gedämpftes Rosé — deutlich sanfter als der Akzent
  private static final Color HOVER_BG_LIGHT = new Color(0xFA, 0xEA, 0xEB); // fast weiß, leicht rosa
  private static final Color HOVER_FG_LIGHT = new Color(0x3A, 0x1A, 0x1C); // warmes Dunkelgrau
  private static final Color HOVER_BG_DARK  = new Color(0x4A, 0x22, 0x25); // dunkles gedämpftes Rot
  private static final Color HOVER_FG_DARK  = new Color(0xF5, 0xD5, 0xD7); // helles Rosa

  private static Color selectedBg() {
    Color c = UIManager.getColor("Tree.selectionBackground");
    return c != null ? c : new Color(255, 156, 164, 255);
  }

  private static Color selectedFg() {
    Color c = UIManager.getColor("Tree.selectionForeground");
    return c != null ? c : Color.WHITE;
  }

  private static Color selectedBorder() {
    Color c = UIManager.getColor("Tree.selectionBorderColor");
    return c != null ? c : selectedBg().darker();
  }

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

  private final JPanel wrapper;
  private final JLabel indicatorLabel;


  public HoverTreeCellRenderer(RowHoverManager hoverManager) {
    this.hoverManager         = hoverManager;
    this.contentIndicatorIcon = loadIcon("/icons/document.png", 12);

    setLeafIcon(loadIcon("/icons/dot.png", 8));
    setClosedIcon(loadIcon("/icons/dot.png", 8));
    setOpenIcon(loadIcon("/icons/dot.png", 8));

    wrapper = new JPanel(new BorderLayout(0, 0));
    wrapper.setOpaque(false);
    wrapper.add(this, BorderLayout.CENTER);

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

    setBackgroundSelectionColor(selectedBg());
    setTextSelectionColor(selectedFg());
    setBorderSelectionColor(selectedBorder());
    setBackgroundNonSelectionColor(defaultBg());
    setTextNonSelectionColor(defaultFg());

    super.getTreeCellRendererComponent(
            tree, value, selected, expanded, leaf, row, hasFocus);

    // ── Content-Indicator ──────────────────────────────────────────
    boolean hasContent = false;
    if (value instanceof OutlineNode node) {
      hasContent = node.getContent() != null && !node.getContent().isBlank();
    }

    if (hasContent && contentIndicatorIcon != null && AppPreferences.isShowIcon()) {
      indicatorLabel.setIcon(contentIndicatorIcon);
      indicatorLabel.setPreferredSize(
              new Dimension(contentIndicatorIcon.getIconWidth() + 6, 1));
      if (indicatorLabel.getParent() == null) {
        wrapper.add(indicatorLabel, BorderLayout.EAST);
      }
    } else {
      if (indicatorLabel.getParent() != null) {
        wrapper.remove(indicatorLabel);
      }
    }

    // ── Farben ────────────────────────────────────────────────────
    Color bg; Color fg; boolean opaque;

    if (selected) {
      bg = selectedBg(); fg = selectedFg(); opaque = true;
    } else if (hoverManager.isHovered(row)) {
      bg = hoverBg(); fg = hoverFg(); opaque = true;
    } else {
      bg = defaultBg(); fg = defaultFg(); opaque = false;
    }

    setBackground(bg);
    setForeground(fg);
    setOpaque(opaque);
    wrapper.setBackground(bg);
    wrapper.setOpaque(opaque);

    return wrapper;
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