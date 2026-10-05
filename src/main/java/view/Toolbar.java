package view;

import action.actions.OutlineActions;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import javax.imageio.ImageIO;

/**
 * Die Werkzeugleiste des Hauptfensters.
 *
 * <p>Baut für jede Gliederungs-Aktion aus {@link OutlineActions} einen Button
 * mit Icon und Beschriftung auf (neuer Wurzel-/Normal-/Untereintrag, Löschen,
 * Ein-/Ausrücken, Auf-/Zuklappen, neue Spalte). Hält selbst keinen Zustand und
 * keine Geschäftslogik — jede Schaltfläche delegiert direkt an die übergebene
 * {@link Action}.</p>
 */
public class Toolbar extends JToolBar {

    /** Kantenlänge, auf die alle Icons skaliert werden, in Pixeln. */
    private static final int ICON_SIZE = 30;

    /**
     * Baut die Werkzeugleiste mit allen Buttons auf.
     *
     * @param outlineActions die vorgefertigten Gliederungs-Actions, aus denen die Buttons erzeugt werden
     */
    public Toolbar(OutlineActions outlineActions) {
        setFloatable(false);
        //setBackground(new Color(245, 245, 248));
        setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(200, 200, 210)));

        add(toolButton(outlineActions.newRootEntryAction, "root", "Root Eintrag"));
        add(toolButton(outlineActions.newEntryAction,      "neu", "Eintrag"));
        add(toolButton(outlineActions.newChildEntryAction, "unter", "Untereintrag"));
        add(toolButton(outlineActions.deleteEntryAction,   "del", "Löschen"));
        add(toolButton(outlineActions.promoteEntryAction,  "promote", "Promote"));
        add(toolButton(outlineActions.demoteEntryAction,   "demote", "Demote"));
        add(toolButton(outlineActions.expandAllAction,     "aufklappen", "Aufklappen"));
        add(toolButton(outlineActions.collapseAllAction,   "zuklappen", "Zuklappen"));
        add(toolButton(outlineActions.addColumnAction,   "column", "Neue Spalte"));
    }

    /** KI generiert
     * Erstellt einen Toolbar-Button mit Icon links und Text rechts.
     *
     * @param action     die zugehörige Action
     * @param iconName Dateiname ohne Extension
     * @param label      Beschriftung rechts neben dem Icon
     */
    private JButton toolButton(Action action, String iconName, String label) {

        JButton btn = new JButton(action);
        btn.setText(label);
        btn.setHorizontalTextPosition(SwingConstants.RIGHT);
        btn.setVerticalTextPosition(SwingConstants.CENTER);
        btn.setIconTextGap(6);
        btn.setFocusable(false);
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        Icon icon = loadIcon("/icons/" + iconName + ".png");
        if (icon != null) {
            btn.setIcon(icon);
        }

        return btn;
    }

    /**
     * Lädt ein Icon aus den Classpath-Ressourcen und skaliert es auf
     * {@link #ICON_SIZE}×{@link #ICON_SIZE} Pixel.
     *
     * @param resourcePath Pfad zur Ressource, relativ zum Classpath (z. B. {@code "/icons/del.png"})
     * @return das skalierte Icon, oder {@code null}, wenn die Ressource nicht
     *         gefunden wurde oder beim Laden ein Fehler auftrat
     */
    private Icon loadIcon(String resourcePath) {
        try {
            var stream = getClass().getResourceAsStream(resourcePath);
            if (stream == null) {
                System.err.println("Icon nicht gefunden: " + resourcePath);
                return null;
            }
            BufferedImage original = ImageIO.read(stream);
            Image scaled = original.getScaledInstance(ICON_SIZE, ICON_SIZE, Image.SCALE_SMOOTH);
            return new ImageIcon(scaled);
        } catch (IOException e) {
            System.err.println("Icon konnte nicht geladen werden: " + resourcePath);
            return null;
        }
    }
}