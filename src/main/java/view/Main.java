package view;

import javax.swing.*;

/**
 * Einstiegspunkt der Outliner-Anwendung.
 */
public class Main {

  public static void main(String[] args) {
    // Natives Look & Feel des Betriebssystems verwenden

    AppTheme.apply();
/*
    try {
      UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
    } catch (Exception ignored) {}
*/
    SwingUtilities.invokeLater(() -> {
      MainWindow window = new MainWindow();
      window.setVisible(true);
    });
  }
}