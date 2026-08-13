/*

KI generiert

 */

package view;

import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatLightLaf;

import javax.swing.*;
import java.awt.*;

public class AppTheme {

  // ── Accent-Palette ───────────────────────────────────────────────────────
  public static Color ACCENT       = new Color(250, 109, 123, 255);
  public static Color ACCENT_DARK  = new Color(0xFC, 0x27, 0x39);
  public static Color ACCENT_LIGHT = new Color(0xFD, 0xE3, 0xE5);

  private static boolean dark = false;


  // ── Einstiegspunkte ──────────────────────────────────────────────────────

  /** Einmalig beim App-Start aufrufen. */
  public static void apply() {
    FlatLightLaf.setup();
    applyAll();
  }

  /** Dark/Light umschalten und gesamtes Fenster neu zeichnen. */
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

  public static boolean isDark() { return dark; }


  // ── Alles auf einmal setzen ───────────────────────────────────────────────

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

  private static void applyAccent() {
    UIManager.put("Component.focusColor",                ACCENT_LIGHT);
    UIManager.put("Component.focusedBorderColor",        ACCENT);
    UIManager.put("Component.focusWidth",                1);
    UIManager.put("Component.innerFocusWidth",           0);
  }


  // ── Buttons ───────────────────────────────────────────────────────────────

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

  private static void applyTree() {
    UIManager.put("Tree.selectionBackground",            ACCENT);
    UIManager.put("Tree.selectionForeground",            Color.WHITE);
    UIManager.put("Tree.selectionInactiveBackground",    ACCENT_DARK);
    UIManager.put("Tree.selectionInactiveForeground",    Color.WHITE);
    UIManager.put("Tree.collapsedIcon",                  loadIcon("/icons/right.png", 12));
    UIManager.put("Tree.expandedIcon",                   loadIcon("/icons/down.png",  12));
  }


  // ── Scrollbar ─────────────────────────────────────────────────────────────

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

  private static void applySeparator() {
    Color sep = separatorColor();
    UIManager.put("Separator.foreground",                sep);
    UIManager.put("Separator.background",                sep);
    UIManager.put("PopupMenu.borderColor",               sep);
    UIManager.put("MenuBar.borderColor",                 sep);
  }


  // ── CheckBox & RadioButton ────────────────────────────────────────────────

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

  public static void setAccent(Color color, JFrame frame) {
    ACCENT = color;
    applyAll();
    SwingUtilities.updateComponentTreeUI(frame);
  }

  public static void setAccentDark(Color color, JFrame frame) {
    ACCENT_DARK = color;
    applyAll();
    SwingUtilities.updateComponentTreeUI(frame);
  }

  public static void setAccentLight(Color color, JFrame frame) {
    ACCENT_LIGHT = color;
    applyAll();
    SwingUtilities.updateComponentTreeUI(frame);
  }

  public static void resetToDefaults(JFrame frame) {
    ACCENT       = new Color(0xFC, 0x27, 0x39);
    ACCENT_DARK  = new Color(0xD9, 0x1B, 0x2C);
    ACCENT_LIGHT = new Color(0xFD, 0xE3, 0xE5);
    applyAll();
    SwingUtilities.updateComponentTreeUI(frame);
  }

  private static Color separatorColor() {
    return dark
        ? new Color(0x55, 0x55, 0x58)
        : new Color(0xD0, 0xD0, 0xD5);
  }

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