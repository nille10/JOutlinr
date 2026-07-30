package view;

import javax.swing.*;
import java.awt.*;

public class AppTheme {

  public static final Color ACCENT        = new Color(0xFC, 0x27, 0x39); // #FC2739
  public static final Color ACCENT_DARK   = new Color(0xD9, 0x1B, 0x2C);
  public static final Color ACCENT_LIGHT  = new Color(0xFD, 0xE3, 0xE5);

  public static void apply() {

    try {
      // Nimbus aktivieren – respektiert UIManager-Farben zuverlässig
      for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
        if ("Nimbus".equals(info.getName())) {
          UIManager.setLookAndFeel(info.getClassName());
          break;
        }
      }
    } catch (Exception e) {
      System.err.println("Nimbus konnte nicht geladen werden, Fallback auf Default L&F");
    }

    // Globale Nimbus-Akzentfarben (deckt Buttons, Menüs, ComboBox, CheckBox etc. ab)
    UIManager.put("nimbusSelectionBackground", ACCENT);
    UIManager.put("nimbusSelection", ACCENT);
    UIManager.put("nimbusFocus", ACCENT);
    UIManager.put("nimbusBase", ACCENT_DARK);
    UIManager.put("nimbusBlueGrey", new Color(230, 230, 235));
    UIManager.put("control", new Color(248, 248, 250));

    // Textfeld-Selektion (z.B. beim Umbenennen im Tree)
    UIManager.put("TextField.selectionBackground", ACCENT);
    UIManager.put("TextField.selectionForeground", Color.WHITE);
    UIManager.put("TextArea.selectionBackground", ACCENT);
    UIManager.put("TextArea.selectionForeground", Color.WHITE);

    // Menüs
    UIManager.put("Menu.selectionBackground", ACCENT);
    UIManager.put("MenuItem.selectionBackground", ACCENT);
    UIManager.put("MenuItem.selectionForeground", Color.WHITE);

    // Listen / ComboBox / Table (falls du sowas nutzt)
    UIManager.put("List.selectionBackground", ACCENT);
    UIManager.put("List.selectionForeground", Color.WHITE);
    UIManager.put("ComboBox.selectionBackground", ACCENT);
    UIManager.put("ComboBox.selectionForeground", Color.WHITE);
    UIManager.put("Table.selectionBackground", ACCENT);
    UIManager.put("Table.selectionForeground", Color.WHITE);

    // Tree (globaler Fallback, falls irgendwo doch der Standard-Renderer greift)
    UIManager.put("Tree.selectionBackground", ACCENT);
    UIManager.put("Tree.selectionForeground", Color.WHITE);
    UIManager.put("Tree.selectionBorderColor", ACCENT_DARK);

    // ProgressBar, Scrollbar-Thumb etc. optional
    UIManager.put("ProgressBar.foreground", ACCENT);
  }
}