package ui;

import javax.swing.*;

/**
 * Einstiegspunkt der Outliner-Anwendung.
 */
public class Main {

  public static void main(String[] args) {
    // Natives Look & Feel des Betriebssystems verwenden
    try {
      UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
    } catch (Exception ignored) {}

    SwingUtilities.invokeLater(() -> {
      MainWindow window = new MainWindow();
      window.setVisible(true);
    });
  }
}