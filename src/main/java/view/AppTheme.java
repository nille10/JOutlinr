package view;

import com.formdev.flatlaf.FlatLightLaf;

import javax.swing.*;
import java.awt.*;

public class AppTheme {

  public static final Color ACCENT       = new Color(0xFC, 0x27, 0x39);
  public static final Color ACCENT_DARK  = new Color(0xD9, 0x1B, 0x2C);
  public static final Color ACCENT_LIGHT = new Color(0xFD, 0xE3, 0xE5);

  public static void apply() {

    FlatLightLaf.setup();

    UIManager.put("Component.focusColor", ACCENT_LIGHT);
    UIManager.put("Component.focusedBorderColor", ACCENT);

    UIManager.put("Button.default.background", ACCENT);
    UIManager.put("Button.default.foreground", Color.WHITE);
    UIManager.put("Button.default.focusedBackground", ACCENT_DARK);

    UIManager.put("TextField.selectionBackground", ACCENT);
    UIManager.put("TextField.selectionForeground", Color.WHITE);
    UIManager.put("TextArea.selectionBackground", ACCENT);
    UIManager.put("TextArea.selectionForeground", Color.WHITE);

    UIManager.put("Menu.selectionBackground", ACCENT);
    UIManager.put("MenuItem.selectionBackground", ACCENT);
    UIManager.put("MenuItem.selectionForeground", Color.WHITE);

    UIManager.put("List.selectionBackground", ACCENT);
    UIManager.put("List.selectionForeground", Color.WHITE);
    UIManager.put("ComboBox.selectionBackground", ACCENT);
    UIManager.put("ComboBox.selectionForeground", Color.WHITE);
    UIManager.put("Table.selectionBackground", ACCENT);
    UIManager.put("Table.selectionForeground", Color.WHITE);

    UIManager.put("Tree.selectionBackground", ACCENT);
    UIManager.put("Tree.selectionForeground", Color.WHITE);

    UIManager.put("ProgressBar.foreground", ACCENT);
    UIManager.put("ScrollBar.thumbArc", 999);
    UIManager.put("ScrollBar.thumbColor", new Color(210, 210, 216));
    UIManager.put("ScrollBar.trackArc", 999);
  }
}