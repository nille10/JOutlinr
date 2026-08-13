package view;

import javax.swing.*;

public class Main {

  public static void main(String[] args) {

    //FlatLaf UI
    AppTheme.apply();

    //old
    // Natives Look & Feel des Betriebssystems verwenden
    /*
    try {
      UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
    } catch (Exception ignored) {}
    */
    SwingUtilities.invokeLater(() -> {
      MainWindow window = new MainWindow();
      window.setVisible(true);

      boolean isDarkModeActive = AppPreferences.isDarkMode();
      AppTheme.toggleDark(isDarkModeActive, window);

      if (AppPreferences.isFullscreen()) {
        window.setExtendedState(JFrame.MAXIMIZED_BOTH);
      }else {
        window.setExtendedState(JFrame.NORMAL);
      }

      window.setVisible(true);
    });
  }
}