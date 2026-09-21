/*
  KI generiert
*/
package view.columns;

import javax.swing.*;
import javax.swing.border.MatteBorder;
import java.awt.*;
import java.awt.event.*;

/**
 * Kopfzeile einer Spalte mit Titel, Inline-Umbenennung, Hover-Effekt,
 * Entfernen-Button (×) und Kontextmenü.
 *
 * <p>Der Titel wird in einem nicht editierbaren {@link JTextField} angezeigt und
 * per Klick oder über das Kontextmenü editierbar. Änderungen werden über
 * {@link #setOnRename(java.util.function.Consumer)} gemeldet, das Entfernen der
 * Spalte über {@link #setOnRemove(Runnable)}.</p>
 *
 * <p>Alle Farben werden zur Laufzeit aus dem {@link UIManager} gelesen, sodass die
 * Darstellung bei einem Wechsel zwischen Dark- und Light-Theme korrekt bleibt.</p>
 */
public class ColumnHeader extends JPanel {

    /** Feste Höhe der Kopfzeile in Pixeln. */
    public static final int HEADER_HEIGHT   = 28;

    /** Maximale Anzahl an Zeichen, die ein Spaltentitel haben darf. */
    public static final int MAX_TITLE_CHARS = 20;

    /** Zusätzlicher horizontaler Platz (in Pixeln) neben dem Text bei der Breitenberechnung. */
    private static final int HORIZONTAL_PADDING = 40;

    /**
     * Liefert die Standard-Hintergrundfarbe der Kopfzeile.
     * Die Farbe wird bei jedem Aufruf frisch aus dem {@link UIManager} gelesen.
     *
     * @return die aktuelle Farbe für {@code Panel.background}
     */
    private static Color bgDefault() { return UIManager.getColor("Panel.background"); }

    /**
     * Liefert die Hintergrundfarbe für den Hover-Zustand.
     * Sie entsteht, indem jeder RGB-Kanal der Standardfarbe um 12 abgedunkelt wird
     * (mindestens jedoch auf 0).
     *
     * @return die abgedunkelte Hintergrundfarbe
     */
    private static Color bgHover() {
        Color b = UIManager.getColor("Panel.background");
        return new Color(
                Math.max(b.getRed()   - 12, 0),
                Math.max(b.getGreen() - 12, 0),
                Math.max(b.getBlue()  - 12, 0));
    }

    /**
     * Liefert die Farbe für die Rahmenlinien der Kopfzeile.
     * Verwendet {@code Separator.foreground} aus dem {@link UIManager}; ist diese
     * nicht definiert, wird ein neutrales Grau (180, 180, 180) verwendet.
     *
     * @return die Rahmenfarbe, niemals {@code null}
     */
    private static Color borderCol() {
        Color c = UIManager.getColor("Separator.foreground");
        return c != null ? c : new Color(180, 180, 180);
    }

    private JTextField textField;
    private final JLabel removeButton;
    private Runnable onRemove;
    private java.util.function.Consumer<String> onRename;
    private boolean hovered = false;


    /**
     * Erstellt eine neue Spaltenkopfzeile mit dem angegebenen Titel.
     *
     * <p>Ist der Titel länger als {@link #MAX_TITLE_CHARS}, wird er auf diese Länge
     * gekürzt. Der Konstruktor baut die Oberfläche auf (Titel-Textfeld, Entfernen-Button)
     * und installiert Hover-, Bearbeitungs- und Kontextmenü-Verhalten.</p>
     *
     * @param title der anzuzeigende Titel der Spalte, nicht {@code null}
     */
    public ColumnHeader(String title) {

        if (title.length() > MAX_TITLE_CHARS) {
            title = title.substring(0, MAX_TITLE_CHARS);
        }

        setLayout(new BorderLayout());
        setPreferredSize(new Dimension(0, HEADER_HEIGHT));
        setMinimumSize(new Dimension(0, HEADER_HEIGHT));
        setMaximumSize(new Dimension(Integer.MAX_VALUE, HEADER_HEIGHT));
        setOpaque(true);
        setBackground(bgDefault());
        setBorder(new MatteBorder(0, 1, 1, 0, borderCol()));

        textField = new JTextField(title);
        textField.setEditable(false);
        textField.setBorder(BorderFactory.createEmptyBorder(2, 8, 2, 4));
        textField.setOpaque(false);
        textField.setFont(new Font("Segoe UI", Font.BOLD, 12));
        add(textField, BorderLayout.CENTER);

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


    /**
     * Berechnet die bevorzugte Breite der Kopfzeile anhand des aktuellen Titeltextes.
     *
     * <p>Die Textbreite wird mit den {@link FontMetrics} der Titelschrift gemessen und
     * um {@link #HORIZONTAL_PADDING} erweitert. Liefert die Messung 0 (z. B. weil die
     * Komponente noch nicht dargestellt wurde), wird ersatzweise mit 8 Pixeln pro
     * Zeichen gerechnet.</p>
     *
     * @return die bevorzugte Breite in Pixeln
     */
    public int getPreferredContentWidth() {
        Font f = textField.getFont();
        FontMetrics fm = getFontMetrics(f);
        int textPx = fm.stringWidth(textField.getText());
        if (textPx == 0) textPx = textField.getText().length() * 8;
        return textPx + HORIZONTAL_PADDING;
    }


    /**
     * Installiert das Hover-Verhalten.
     *
     * <p>Betritt die Maus das Panel oder das Textfeld, wird der Hover-Zustand aktiviert.
     * Beim Verlassen wird er nur zurückgesetzt, wenn sich der Mauszeiger tatsächlich
     * außerhalb des Panels befindet – so flackert der Zustand nicht, wenn die Maus
     * zwischen Panel, Textfeld und Entfernen-Button wechselt.</p>
     */
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

    /**
     * Wendet den aktuellen Hover-Zustand auf die Darstellung an.
     *
     * <p>Hintergrund- und Rahmenfarbe werden dabei frisch aus dem {@link UIManager}
     * gelesen. Der Entfernen-Button ist nur sichtbar, wenn die Maus über der Kopfzeile
     * liegt und ein Entfernen-Callback gesetzt wurde.</p>
     */
    private void applyHover() {
        setBackground(hovered ? bgHover() : bgDefault());
        setBorder(new MatteBorder(0, 1, 1, 0, borderCol()));
        removeButton.setVisible(hovered && onRemove != null);
        repaint();
    }


    /**
     * Installiert das Verhalten für die Inline-Bearbeitung des Titels.
     *
     * <p>Ein Mausklick auf das (noch nicht editierbare) Textfeld startet die
     * Bearbeitung. Die Bearbeitung endet per Enter-Taste oder bei Verlust des
     * Fokus.</p>
     */
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

    /**
     * Versetzt das Titel-Textfeld in den Bearbeitungsmodus.
     *
     * <p>Das Feld wird editierbar und deckend dargestellt, erhält den Fokus und der
     * gesamte Text wird markiert.</p>
     */
    private void startEditing() {
        textField.setBackground(UIManager.getColor("Panel.background"));
        textField.setOpaque(true);
        textField.setEditable(true);
        textField.getCaret().setVisible(true);
        textField.requestFocusInWindow();
        textField.selectAll();
    }

    /**
     * Beendet den Bearbeitungsmodus des Titel-Textfelds.
     *
     * <p>Das Feld wird wieder schreibgeschützt und transparent dargestellt, die
     * Markierung wird aufgehoben und der Fokus wird durch kurzes Umschalten von
     * {@code focusable} entzogen. Anschließend wird – falls gesetzt – der
     * Umbenennen-Callback mit dem aktuellen Text aufgerufen.</p>
     */
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


    /**
     * Installiert die Erkennung des Rechtsklicks, der das Kontextmenü öffnet.
     *
     * <p>Der Popup-Trigger wird sowohl bei {@code mousePressed} als auch bei
     * {@code mouseReleased} geprüft, da er je nach Plattform bei einem der beiden
     * Ereignisse auftritt.</p>
     */
    private void installContextMenu() {
        MouseAdapter rc = new MouseAdapter() {
            @Override public void mousePressed(MouseEvent e)  { if (e.isPopupTrigger()) showContextMenu(e); }
            @Override public void mouseReleased(MouseEvent e) { if (e.isPopupTrigger()) showContextMenu(e); }
        };
        addMouseListener(rc);
        textField.addMouseListener(rc);
    }

    /**
     * Zeigt das Kontextmenü an der Position des Mausereignisses an.
     *
     * <p>Das Menü enthält immer den Eintrag „Umbenennen“. Ist ein Entfernen-Callback
     * gesetzt, folgt nach einem Trenner zusätzlich der Eintrag „Spalte entfernen“.</p>
     *
     * @param e das auslösende Mausereignis; dessen Quellkomponente und Koordinaten
     *          bestimmen die Position des Menüs
     */
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


    /**
     * Löst den Entfernen-Callback aus, sofern einer gesetzt ist.
     */
    private void fireRemove() { if (onRemove != null) onRemove.run(); }

    /**
     * Setzt den Callback, der beim Entfernen der Spalte aufgerufen wird.
     *
     * <p>Die Sichtbarkeit des Entfernen-Buttons wird sofort aktualisiert: Er ist nur
     * sichtbar, wenn die Kopfzeile gerade gehovert wird und ein Callback vorhanden ist.</p>
     *
     * @param onRemove der auszuführende Callback, oder {@code null}, um das Entfernen
     *                 zu deaktivieren
     */
    public void setOnRemove(Runnable onRemove) {
        this.onRemove = onRemove;
        removeButton.setVisible(hovered && onRemove != null);
    }

    /**
     * Setzt den Callback, der nach dem Umbenennen der Spalte aufgerufen wird.
     *
     * <p>Der Callback erhält den aktuellen Text des Titelfelds und wird bei jedem
     * Beenden der Bearbeitung aufgerufen (Enter oder Fokusverlust).</p>
     *
     * @param onRename der Callback, der den neuen Titel entgegennimmt, oder
     *                 {@code null}, um keine Benachrichtigung zu erhalten
     */
    public void setOnRename(java.util.function.Consumer<String> onRename) {
        this.onRename = onRename;
    }


    /**
     * Liefert den aktuell angezeigten Titel der Spalte.
     *
     * @return der Text des Titelfelds
     */
    public String     getTitle()         { return textField.getText(); }

    /**
     * Setzt den angezeigten Titel der Spalte.
     *
     * <p>Der Titel wird hier nicht auf {@link #MAX_TITLE_CHARS} gekürzt und löst
     * keinen Umbenennen-Callback aus.</p>
     *
     * @param t der neue Titel
     */
    public void       setTitle(String t) { textField.setText(t); }

    /**
     * Liefert das interne Textfeld, in dem der Titel angezeigt wird.
     *
     * @return das Titel-{@link JTextField}
     */
    public JTextField getTextField()     { return textField; }
}