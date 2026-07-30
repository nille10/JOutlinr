package view;

import view.columns.RowHoverManager;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.tree.DefaultTreeCellRenderer;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;

/**
 * Tree-Renderer mit Hover- und Selektions-Highlighting.
 *
 * Prioritäten:
 *   1. selected → Blau
 *   2. hovered  → Hellgrau
 *   3. default  → transparent
 *
 * Wichtig: DefaultTreeCellRenderer ist ein einzelnes wiederverwendetes
 * JLabel-Objekt. Swing ruft getTreeCellRendererComponent() für jede
 * sichtbare Zeile nacheinander auf – Zustand vom vorherigen Aufruf
 * bleibt hängen wenn man ihn nicht explizit überschreibt.
 *
 * Deshalb: in JEDEM der drei Fälle werden background, foreground
 * und opaque vollständig gesetzt – kein Fall lässt etwas offen.
 */
public class HoverTreeCellRenderer extends DefaultTreeCellRenderer {

  private static final Color SELECTED_BG     = new Color(0xFC, 0x27, 0x39); // #FC2739
  private static final Color SELECTED_FG     = Color.WHITE;
  private static final Color SELECTED_BORDER = new Color(0xD9, 0x1B, 0x2C); // etwas dunkler, für dezente Kontur

  private static final Color HOVER_BG  = new Color(0xFD, 0xE3, 0xE5); // sehr helles Rosé
  private static final Color HOVER_FG  = new Color(0x40, 0x20, 0x22); // leicht warmes Dunkelgrau/Braun statt reines Schwarz

  private static final Color DEFAULT_BG = new Color(0xF8, 0xF8, 0xFA);
  private static final Color DEFAULT_FG = new Color(0x1E, 0x1E, 0x1E);

  private final RowHoverManager hoverManager;


  public HoverTreeCellRenderer(RowHoverManager hoverManager) {
    this.hoverManager = hoverManager;

    setBackgroundNonSelectionColor(DEFAULT_BG);
    setBackgroundSelectionColor(SELECTED_BG);
    setTextSelectionColor(SELECTED_FG);
    setTextNonSelectionColor(DEFAULT_FG);
    setBorderSelectionColor(SELECTED_BORDER);

    Icon leaf = loadIcon("/icons/dot.png", 8);
    Icon closed = loadIcon("/icons/empty.png", 8);
    Icon open = loadIcon("/icons/empty.png", 8);


    setLeafIcon(leaf);
    setClosedIcon(open);
    setOpenIcon(closed);
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

    Component c = super.getTreeCellRendererComponent(
        tree, value, selected, expanded, leaf, row, hasFocus);

    JLabel label = (JLabel) c;

    if (selected) {
      // Explizit setzen – nicht dem super-Zustand vertrauen
      label.setBackground(SELECTED_BG);
      label.setForeground(SELECTED_FG);
      label.setOpaque(true);

    } else if (hoverManager.isHovered(row)) {
      label.setBackground(HOVER_BG);
      label.setForeground(HOVER_FG);
      label.setOpaque(true);

    } else {
      label.setBackground(DEFAULT_BG);
      label.setForeground(DEFAULT_FG);
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