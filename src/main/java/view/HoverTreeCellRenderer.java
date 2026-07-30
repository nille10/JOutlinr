package view;

import view.columns.RowHoverManager;

import javax.swing.*;
import javax.swing.tree.DefaultTreeCellRenderer;
import java.awt.*;

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

  private static final Color SELECTED_BG     = new Color(91, 132, 219);
  private static final Color SELECTED_FG     = Color.WHITE;
  private static final Color SELECTED_BORDER = new Color(70, 110, 200);

  private static final Color HOVER_BG  = new Color(225, 228, 245);
  private static final Color HOVER_FG  = new Color(30, 30, 50);

  private static final Color DEFAULT_BG = new Color(248, 248, 252);
  private static final Color DEFAULT_FG = new Color(30, 30, 30);

  private final RowHoverManager hoverManager;


  public HoverTreeCellRenderer(RowHoverManager hoverManager) {
    this.hoverManager = hoverManager;

    setBackgroundNonSelectionColor(DEFAULT_BG);
    setBackgroundSelectionColor(SELECTED_BG);
    setTextSelectionColor(SELECTED_FG);
    setTextNonSelectionColor(DEFAULT_FG);
    setBorderSelectionColor(SELECTED_BORDER);
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
}