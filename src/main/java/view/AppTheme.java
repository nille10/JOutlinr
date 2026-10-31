/*

KI generiert

 */

package view;

import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatLightLaf;

import javax.swing.*;
import java.awt.*;

/**
 * Zentrale Stelle für das Erscheinungsbild (Theme) der Anwendung.
 *
 * <p>Baut auf FlatLaf auf ({@link FlatLightLaf}/{@link FlatDarkLaf}) und überschreibt
 * darüber hinaus gezielt einzelne {@link UIManager}-Properties mit der eigenen
 * Akzentfarb-Palette ({@link #ACCENT}, {@link #ACCENT_DARK}, {@link #ACCENT_LIGHT}),
 * damit Buttons, Menüs, Listen, der Baum und weitere Swing-Komponenten einheitlich
 * eingefärbt sind. Enthält ausschließlich Darstellungslogik (Look &amp; Feel); es
 * werden keinerlei Anwendungsdaten gehalten oder verändert.</p>
 *
 * <p>{@link #apply()} wird einmalig beim Start der Anwendung aufgerufen,
 * {@link #toggleDark(boolean, JFrame)} schaltet danach zwischen Dark- und
 * Light-Theme um.</p>
 */
public class AppTheme {

  // ── Accent-Palette ───────────────────────────────────────────────────────

  /** Die aktuelle Akzentfarbe, z. B. für Buttons, Auswahl-Hintergründe und Fokus-Rahmen. */
  public static Color ACCENT       = new Color(250, 109, 123, 255);

  /** Dunklere Variante der Akzentfarbe, u. a. für gedrückte/fokussierte Zustände. */
  public static Color ACCENT_DARK  = new Color(0xFC, 0x27, 0x39);

  /** Hellere Variante der Akzentfarbe, u. a. für Fokus-Glow und ausgewählte Tabs. */
  public static Color ACCENT_LIGHT = new Color(0xFD, 0xE3, 0xE5);

  /** Hover-Hintergrund einer Baumzeile im Light-Theme: sehr helles, gedämpftes Rosé. */
  private static final Color HOVER_BG_LIGHT = new Color(0xFA, 0xEA, 0xEB);

  /** Hover-Textfarbe einer Baumzeile im Light-Theme: warmes Dunkelgrau. */
  private static final Color HOVER_FG_LIGHT = new Color(0x3A, 0x1A, 0x1C);

  /** Hover-Hintergrund einer Baumzeile im Dark-Theme: dunkles, gedämpftes Rot. */
  private static final Color HOVER_BG_DARK  = new Color(0x4A, 0x22, 0x25);

  /** Hover-Textfarbe einer Baumzeile im Dark-Theme: helles Rosa. */
  private static final Color HOVER_FG_DARK  = new Color(0xF5, 0xD5, 0xD7);

  /** Gibt an, ob aktuell das Dark-Theme aktiv ist. */
  private static boolean dark = false;


  // ── Einstiegspunkte ──────────────────────────────────────────────────────

  /**
   * Initialisiert das Light-Theme und wendet alle Theme-Anpassungen an.
   *
   * <p>Einmalig beim App-Start aufzurufen, bevor das erste Fenster erzeugt wird.</p>
   */
  public static void apply() {
    FlatLightLaf.setup();
    applyAll();
  }

  /**
   * Schaltet zwischen Dark- und Light-Theme um und zeichnet das übergebene Fenster
   * komplett neu.
   *
   * @param enableDark {@code true}, um das Dark-Theme zu aktivieren, {@code false}
   *                   für das Light-Theme
   * @param frame      das Fenster, dessen Komponentenbaum nach dem Umschalten mit
   *                   {@link SwingUtilities#updateComponentTreeUI(Component)}
   *                   aktualisiert wird
   */
  public static void toggleDark(boolean enableDark, JFrame frame) {
    dark = enableDark;
    try {
      if (dark) FlatDarkLaf.setup();
      else      FlatLightLaf.setup();
      applyAll();
      SwingUtilities.updateComponentTreeUI(frame);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  /**
   * Gibt an, ob aktuell das Dark-Theme aktiv ist.
   *
   * @return {@code true}, wenn das Dark-Theme aktiv ist, sonst {@code false}
   */
  public static boolean isDark() { return dark; }

  /**
   * Liefert die Hintergrundfarbe einer Baumzeile im Hover-Zustand passend zum
   * aktuellen Theme.
   *
   * @return die Hover-Hintergrundfarbe
   */
  public static Color hoverBackground() { return dark ? HOVER_BG_DARK : HOVER_BG_LIGHT; }

  /**
   * Liefert die Textfarbe einer Baumzeile im Hover-Zustand passend zum
   * aktuellen Theme.
   *
   * @return die Hover-Textfarbe
   */
  public static Color hoverForeground() { return dark ? HOVER_FG_DARK : HOVER_FG_LIGHT; }


  // ── Alles auf einmal setzen ───────────────────────────────────────────────

  /**
   * Wendet alle Theme-Anpassungen in der richtigen Reihenfolge auf den
   * {@link UIManager} an.
   *
   * <p>Wird nach jedem Wechsel des Look &amp; Feel sowie nach jeder Änderung der
   * Akzentfarben erneut aufgerufen, damit alle betroffenen {@code UIManager}-Keys
   * konsistent bleiben.</p>
   */
  private static void applyAll() {
    applyAccent();
    applyButtons();
    applyInputs();
    applyMenus();
    applyLists();
    applyTree();
    applyScrollBar();
    applySeparator();
    applyCheckBox();
    applyMisc();
  }


  // ── Accent (Fokus, Rahmen) ────────────────────────────────────────────────

  /**
   * Setzt die {@code UIManager}-Properties für Fokus-Rahmen und -Glow auf die
   * aktuelle Akzentfarb-Palette.
   */
  private static void applyAccent() {
    UIManager.put("Component.focusColor",                ACCENT_LIGHT);
    UIManager.put("Component.focusedBorderColor",        ACCENT);
    UIManager.put("Component.focusWidth",                1);
    UIManager.put("Component.innerFocusWidth",           0);
  }


  // ── Buttons ───────────────────────────────────────────────────────────────

  /**
   * Setzt die {@code UIManager}-Properties für Standard-Buttons (Hintergrund,
   * Vordergrund, Zustände) sowie die Eckenrundung von Buttons und Toggle-Buttons.
   */
  private static void applyButtons() {
    UIManager.put("Button.default.background",           ACCENT);
    UIManager.put("Button.default.foreground",           Color.WHITE);
    UIManager.put("Button.default.focusedBackground",    ACCENT_DARK);
    UIManager.put("Button.default.pressedBackground",    ACCENT_DARK);
    UIManager.put("Button.default.boldText",             false);
    UIManager.put("Button.arc",                          6);
    UIManager.put("ToggleButton.arc",                    6);
  }


  // ── Texteingaben ─────────────────────────────────────────────────────────

  /**
   * Setzt die {@code UIManager}-Properties für die Textmarkierung (Auswahlfarbe)
   * in Textfeldern, Textbereichen und Passwortfeldern sowie deren Eckenrundung.
   */
  private static void applyInputs() {
    UIManager.put("TextField.selectionBackground",       ACCENT);
    UIManager.put("TextField.selectionForeground",       Color.WHITE);
    UIManager.put("TextArea.selectionBackground",        ACCENT);
    UIManager.put("TextArea.selectionForeground",        Color.WHITE);
    UIManager.put("TextPane.selectionBackground",        ACCENT);
    UIManager.put("TextPane.selectionForeground",        Color.WHITE);
    UIManager.put("PasswordField.selectionBackground",   ACCENT);
    UIManager.put("PasswordField.selectionForeground",   Color.WHITE);
    UIManager.put("TextField.arc",                       6);
    UIManager.put("Component.arc",                       6);
  }


  // ── Menüs ─────────────────────────────────────────────────────────────────

  /**
   * Setzt die {@code UIManager}-Properties für die Auswahlfarbe in Menüs,
   * Menüeinträgen, Checkbox- und Radiobutton-Menüeinträgen sowie die Rahmenfarbe
   * der Menüleiste.
   */
  private static void applyMenus() {
    UIManager.put("Menu.selectionBackground",            ACCENT);
    UIManager.put("Menu.selectionForeground",            Color.WHITE);
    UIManager.put("MenuItem.selectionBackground",        ACCENT);
    UIManager.put("MenuItem.selectionForeground",        Color.WHITE);
    UIManager.put("CheckBoxMenuItem.selectionBackground",ACCENT);
    UIManager.put("CheckBoxMenuItem.selectionForeground",Color.WHITE);
    UIManager.put("RadioButtonMenuItem.selectionBackground", ACCENT);
    UIManager.put("RadioButtonMenuItem.selectionForeground", Color.WHITE);
    UIManager.put("MenuBar.borderColor",                 separatorColor());
  }


  // ── Listen, Tabellen, ComboBox ────────────────────────────────────────────

  /**
   * Setzt die {@code UIManager}-Properties für die Auswahlfarbe in Listen,
   * ComboBoxen und Tabellen und entfernt den Fokus-Rahmen einzelner Tabellenzellen.
   */
  private static void applyLists() {
    UIManager.put("List.selectionBackground",            ACCENT);
    UIManager.put("List.selectionForeground",            Color.WHITE);
    UIManager.put("ComboBox.selectionBackground",        ACCENT);
    UIManager.put("ComboBox.selectionForeground",        Color.WHITE);
    UIManager.put("Table.selectionBackground",           ACCENT);
    UIManager.put("Table.selectionForeground",           Color.WHITE);
    UIManager.put("Table.focusCellHighlightBorder",      BorderFactory.createEmptyBorder());
  }


  // ── Tree ──────────────────────────────────────────────────────────────────

  /**
   * Setzt die {@code UIManager}-Properties für die Auswahlfarbe des Baums (aktiv
   * und inaktiv) sowie die Auf-/Zuklapp-Icons.
   */
  private static void applyTree() {
    UIManager.put("Tree.selectionBackground",            ACCENT);
    UIManager.put("Tree.selectionForeground",            Color.WHITE);
    UIManager.put("Tree.selectionInactiveBackground",    ACCENT_DARK);
    UIManager.put("Tree.selectionInactiveForeground",    Color.WHITE);
    UIManager.put("Tree.collapsedIcon",                  loadIcon("/icons/right.png", 12));
    UIManager.put("Tree.expandedIcon",                   loadIcon("/icons/down.png",  12));
  }


  // ── Scrollbar ─────────────────────────────────────────────────────────────

  /**
   * Setzt die {@code UIManager}-Properties für die Scrollbar-Darstellung
   * (Rundung, Breite, Thumb-Farben je nach Theme, Buttons ausgeblendet).
   */
  private static void applyScrollBar() {
    UIManager.put("ScrollBar.thumbArc",                  999);
    UIManager.put("ScrollBar.trackArc",                  999);
    UIManager.put("ScrollBar.width",                     8);
    UIManager.put("ScrollBar.thumbColor",
            dark ? new Color(0x60, 0x60, 0x64) : new Color(0xC0, 0xC0, 0xC6));
    UIManager.put("ScrollBar.hoverThumbColor",
            dark ? new Color(0x80, 0x80, 0x86) : new Color(0xA0, 0xA0, 0xA8));
    UIManager.put("ScrollBar.pressedThumbColor",         ACCENT);
    UIManager.put("ScrollBar.showButtons",               false);
  }


  // ── Separator ─────────────────────────────────────────────────────────────

  /**
   * Setzt die {@code UIManager}-Properties für Trennlinien-Farben (Separator,
   * Popup-Menü-Rahmen, Menüleisten-Rahmen) anhand von {@link #separatorColor()}.
   */
  private static void applySeparator() {
    Color sep = separatorColor();
    UIManager.put("Separator.foreground",                sep);
    UIManager.put("Separator.background",                sep);
    UIManager.put("PopupMenu.borderColor",               sep);
    UIManager.put("MenuBar.borderColor",                 sep);
  }


  // ── CheckBox & RadioButton ────────────────────────────────────────────────

  /**
   * Setzt die {@code UIManager}-Properties für ausgewählte Checkboxen und
   * Radiobuttons (Hintergrund, Rahmen, Häkchen-Farbe, Eckenrundung sowie die
   * Zustände fokussiert/gehovert/gedrückt).
   */
  private static void applyCheckBox() {
    UIManager.put("CheckBox.icon.selectedBackground",         ACCENT);
    UIManager.put("CheckBox.icon.selectedBorderColor",        ACCENT_DARK);
    UIManager.put("CheckBox.icon.checkmarkColor",             Color.WHITE);
    UIManager.put("CheckBox.icon.focusedSelectedBackground",  ACCENT_DARK);
    UIManager.put("CheckBox.icon.hoverSelectedBackground",    ACCENT_DARK);
    UIManager.put("CheckBox.icon.pressedSelectedBackground",  ACCENT_DARK);
    UIManager.put("CheckBox.icon.arc",                        4);

    UIManager.put("RadioButton.icon.selectedColor",           ACCENT);
    UIManager.put("RadioButton.icon.focusedSelectedColor",    ACCENT_DARK);
    UIManager.put("RadioButton.icon.hoverSelectedColor",      ACCENT_DARK);
  }


  // ── Sonstiges ─────────────────────────────────────────────────────────────

  /**
   * Setzt die {@code UIManager}-Properties für Fortschrittsbalken, Schieberegler,
   * Tab-Leisten sowie die Fenster-Titelleiste (Farbe je nach Theme).
   */
  private static void applyMisc() {
    UIManager.put("ProgressBar.foreground",              ACCENT);
    UIManager.put("ProgressBar.arc",                     999);
    UIManager.put("Slider.thumbColor",                   ACCENT);
    UIManager.put("TabbedPane.selectedBackground",       ACCENT_LIGHT);
    UIManager.put("TabbedPane.underlineColor",           ACCENT);
    UIManager.put("TabbedPane.focusColor",               ACCENT_LIGHT);
    // Fensterdekoration
    UIManager.put("TitlePane.background",
            dark ? new Color(0x2B, 0x2B, 0x2E) : new Color(0xF5, 0xF5, 0xF8));
    UIManager.put("TitlePane.foreground",
            dark ? Color.WHITE : new Color(0x1E, 0x1E, 0x1E));
  }


  // ── Hilfsmethoden ────────────────────────────────────────────────────────

  /**
   * Setzt {@link #ACCENT} auf die angegebene Farbe, wendet alle Theme-Anpassungen
   * erneut an und zeichnet das übergebene Fenster neu.
   *
   * @param color die neue Akzentfarbe
   * @param frame das neu zu zeichnende Fenster
   */
  public static void setAccent(Color color, JFrame frame) {
    ACCENT = color;
    applyAll();
    SwingUtilities.updateComponentTreeUI(frame);
  }

  /**
   * Setzt {@link #ACCENT_DARK} auf die angegebene Farbe, wendet alle
   * Theme-Anpassungen erneut an und zeichnet das übergebene Fenster neu.
   *
   * @param color die neue dunkle Akzentfarbe
   * @param frame das neu zu zeichnende Fenster
   */
  public static void setAccentDark(Color color, JFrame frame) {
    ACCENT_DARK = color;
    applyAll();
    SwingUtilities.updateComponentTreeUI(frame);
  }

  /**
   * Setzt {@link #ACCENT_LIGHT} auf die angegebene Farbe, wendet alle
   * Theme-Anpassungen erneut an und zeichnet das übergebene Fenster neu.
   *
   * @param color die neue helle Akzentfarbe
   * @param frame das neu zu zeichnende Fenster
   */
  public static void setAccentLight(Color color, JFrame frame) {
    ACCENT_LIGHT = color;
    applyAll();
    SwingUtilities.updateComponentTreeUI(frame);
  }

  /**
   * Setzt die Akzentfarb-Palette auf die Standardwerte zurück, wendet alle
   * Theme-Anpassungen erneut an und zeichnet das übergebene Fenster neu.
   *
   * @param frame das neu zu zeichnende Fenster
   */
  public static void resetToDefaults(JFrame frame) {

    /** Die aktuelle Akzentfarbe, z. B. für Buttons, Auswahl-Hintergründe und Fokus-Rahmen. */
    ACCENT       = new Color(250, 109, 123, 255);

    /** Dunklere Variante der Akzentfarbe, u. a. für gedrückte/fokussierte Zustände. */
    ACCENT_DARK  = new Color(0xFC, 0x27, 0x39);

    /** Hellere Variante der Akzentfarbe, u. a. für Fokus-Glow und ausgewählte Tabs. */
    ACCENT_LIGHT = new Color(0xFD, 0xE3, 0xE5);
    applyAll();
    SwingUtilities.updateComponentTreeUI(frame);
  }

  /**
   * Liefert die Trennlinienfarbe passend zum aktuellen Theme.
   *
   * @return ein dunkles Grau im Dark-Theme, ein helles Grau im Light-Theme
   */
  private static Color separatorColor() {
    return dark
            ? new Color(0x55, 0x55, 0x58)
            : new Color(0xD0, 0xD0, 0xD5);
  }

  /**
   * Lädt ein Icon aus den Classpath-Ressourcen und skaliert es auf die
   * angegebene Kantenlänge (quadratisch).
   *
   * @param path Pfad zur Ressource, relativ zum Classpath (z. B. {@code "/icons/right.png"})
   * @param size Kantenlänge in Pixeln, auf die das Icon skaliert wird
   * @return das skalierte Icon, oder {@code null}, wenn die Ressource nicht
   *         gefunden wurde oder beim Laden ein Fehler auftrat
   */
  private static Icon loadIcon(String path, int size) {
    try {
      var stream = AppTheme.class.getResourceAsStream(path);
      if (stream == null) return null;
      java.awt.image.BufferedImage img = javax.imageio.ImageIO.read(stream);
      java.awt.Image scaled = img.getScaledInstance(size, size, java.awt.Image.SCALE_SMOOTH);
      return new ImageIcon(scaled);
    } catch (java.io.IOException e) {
      return null;
    }
  }

}