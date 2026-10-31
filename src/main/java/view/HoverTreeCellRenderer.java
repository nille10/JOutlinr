/*
  KI generiert
*/
package view;

import model.OutlineNode;
import model.preferences.AppPreferences;
import view.columns.RowHoverManager;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.tree.DefaultTreeCellRenderer;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;

/**
 * Renderer für die Zeilen des Outline-Baums.
 *
 * <p>Erweitert den {@link DefaultTreeCellRenderer} um zwei Eigenschaften. Zum einen wird
 * die Zeile hervorgehoben, über der sich die Maus befindet. Der aktuelle Zustand liefert
 * der {@link RowHoverManager}, den sich der Baum mit den Zusatzspalten teilt, sodass beide
 * Seiten gemeinsam hervorgehoben werden. Zum anderen zeigt der Renderer rechts neben dem
 * Titel ein kleines Symbol an, wenn für den Knoten Notizen im
 * {@link view.TextWindow} vorhanden sind (sofern
 * {@link AppPreferences#isShowIcon()} aktiv ist).</p>
 *
 * <p>Die Farben werden bei jedem Zeichnen neu bestimmt. Auswahl- und Standardfarben
 * stammen aus dem {@link UIManager}, die Hover-Farben aus {@link AppTheme}. Dadurch
 * übernimmt der Baum einen Wechsel zwischen Dark- und Light-Theme sowie geänderte
 * Akzentfarben ohne Neustart. Der Renderer enthält selbst keine festen Farbwerte, die
 * Fallbacks bei fehlenden {@code UIManager}-Einträgen sind lediglich neutrale
 * Standardwerte.</p>
 *
 * <p>Zurückgeliefert wird nicht der Renderer selbst, sondern ein {@link JPanel}, das den
 * Renderer (Mitte) und optional das Notiz-Symbol (rechts) enthält. Die Instanz wird für
 * alle Zeilen wiederverwendet, wie es bei Swing-Renderern üblich ist.</p>
 */
public class HoverTreeCellRenderer extends DefaultTreeCellRenderer {

    /**
     * Liefert die Hintergrundfarbe einer ausgewählten Zeile.
     *
     * @return {@code Tree.selectionBackground} aus dem {@link UIManager}, ersatzweise
     *         die Akzentfarbe von {@link AppTheme}
     */
    private static Color selectedBg() {
        Color c = UIManager.getColor("Tree.selectionBackground");
        return c != null ? c : AppTheme.ACCENT;
    }

    /**
     * Liefert die Textfarbe einer ausgewählten Zeile.
     *
     * @return {@code Tree.selectionForeground} aus dem {@link UIManager}, ersatzweise Weiß
     */
    private static Color selectedFg() {
        Color c = UIManager.getColor("Tree.selectionForeground");
        return c != null ? c : Color.WHITE;
    }

    /**
     * Liefert die Rahmenfarbe einer ausgewählten Zeile.
     *
     * @return {@code Tree.selectionBorderColor} aus dem {@link UIManager}, ersatzweise
     *         eine dunklere Variante der Auswahlfarbe
     */
    private static Color selectedBorder() {
        Color c = UIManager.getColor("Tree.selectionBorderColor");
        return c != null ? c : selectedBg().darker();
    }

    /**
     * Liefert die Hintergrundfarbe einer nicht ausgewählten Zeile.
     *
     * @return {@code Tree.background} aus dem {@link UIManager}, ersatzweise Weiß
     */
    private static Color defaultBg() {
        Color c = UIManager.getColor("Tree.background");
        return c != null ? c : Color.WHITE;
    }

    /**
     * Liefert die Textfarbe einer nicht ausgewählten Zeile.
     *
     * @return {@code Tree.foreground} aus dem {@link UIManager}, ersatzweise Schwarz
     */
    private static Color defaultFg() {
        Color c = UIManager.getColor("Tree.foreground");
        return c != null ? c : Color.BLACK;
    }

    /** Gemeinsamer Hover-Zustand von Baum und Spalten. */
    private final RowHoverManager hoverManager;

    /** Symbol, das anzeigt, dass für einen Knoten Notizen vorhanden sind. */
    private final Icon contentIndicatorIcon;

    /** Container, der pro Zeile zurückgeliefert wird und Renderer sowie Symbol aufnimmt. */
    private final JPanel wrapper;

    /** Label für das Notiz-Symbol am rechten Rand der Zeile. */
    private final JLabel indicatorLabel;


    /**
     * Erstellt einen neuen Renderer.
     *
     * <p>Lädt die Symbole für das Notiz-Symbol und für Blätter sowie auf- und zugeklappte
     * Knoten (alle drei zeigen denselben Punkt) und baut den Container auf.</p>
     *
     * @param hoverManager der {@link RowHoverManager}, den sich Baum und Spalten teilen,
     *                     darf nicht {@code null} sein
     */
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


    /**
     * Liefert die Komponente, mit der eine Baumzeile gezeichnet wird.
     *
     * <p>Zuerst werden die Farben des Basisklassen-Renderers aktualisiert. Danach wird das
     * Notiz-Symbol ein- oder ausgeblendet, je nachdem, ob der Knoten einen nicht leeren
     * Notizinhalt hat und die Anzeige in den Einstellungen aktiv ist. Zum Schluss wird
     * abhängig vom Zustand der Zeile gefärbt.</p>
     *
     * <p>Es gibt drei Zustände mit absteigender Priorität. Eine ausgewählte Zeile
     * erhält die Auswahlfarben, eine Zeile unter der Maus die Hover-Farben aus
     * {@link AppTheme}, alle übrigen werden ohne eigenen Hintergrund (nicht deckend)
     * gezeichnet.</p>
     *
     * @param tree     der {@link JTree}, der gezeichnet wird
     * @param value    der Knoten der Zeile, in der Regel ein {@link OutlineNode}
     * @param selected {@code true}, wenn die Zeile ausgewählt ist
     * @param expanded {@code true}, wenn der Knoten aufgeklappt ist
     * @param leaf     {@code true}, wenn der Knoten keine Kinder hat
     * @param row      Index der Zeile im Baum, wird für den Hover-Vergleich benötigt
     * @param hasFocus {@code true}, wenn die Zeile den Fokus hat
     * @return der Container mit Beschriftung und gegebenenfalls Notiz-Symbol
     */
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
            bg = AppTheme.hoverBackground(); fg = AppTheme.hoverForeground(); opaque = true;
        } else {
            bg = defaultBg(); fg = defaultFg(); opaque = false;
        }

        setBackground(bg);
        setForeground(fg);
        setOpaque(opaque);
        wrapper.setBackground(bg);
        wrapper.setOpaque(opaque);

        // Größe frei berechnen lassen, damit keine veraltete Größe der vorherigen Zeile bleibt
        wrapper.setPreferredSize(null);
        wrapper.revalidate();

        return wrapper;
    }


    /**
     * Lädt ein Icon aus den Classpath-Ressourcen und skaliert es auf die angegebene
     * Kantenlänge (quadratisch).
     *
     * @param path Pfad zur Ressource, relativ zum Classpath (z. B. {@code "/icons/dot.png"})
     * @param size Kantenlänge in Pixeln, auf die das Icon skaliert wird
     * @return das skalierte Icon, oder {@code null}, wenn die Ressource nicht gefunden
     *         wurde oder beim Laden ein Fehler auftrat (die Meldung wird auf
     *         {@code System.err} ausgegeben)
     */
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