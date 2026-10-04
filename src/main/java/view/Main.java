package view;

import model.preferences.AppPreferences;

import javax.swing.*;

/**
 * Einstiegspunkt der Anwendung.
 *
 * <p>Richtet das Look &amp; Feel ein ({@link AppTheme}), erzeugt das Hauptfenster
 * und stellt es gemäß den gespeicherten Einstellungen dar ({@link AppPreferences}:
 * Dark Mode, Vollbild).</p>
 */
public class Main {

  /**
   * Startet die Anwendung.
   *
   * <p>Verhindert unter Linux, dass das native GTK-Look-&amp;-Feel vor FlatLaf
   * greift, wendet anschließend {@link AppTheme#apply()} an und baut das
   * Hauptfenster auf dem Event Dispatch Thread auf. Nach dem Erzeugen werden
   * Dark Mode und Vollbildstatus aus {@link AppPreferences} übernommen.</p>
   *
   * @param args Kommandozeilenargumente, werden nicht ausgewertet
   */
  public static void main(String[] args) {

    // Auf Linux GTK-L&F verhindern, latLaf soll komplett übernehmen
    if (System.getProperty("os.name").toLowerCase().contains("linux")) {
      System.setProperty("awt.useSystemAAFontSettings", "on");
      System.setProperty("swing.defaultlaf", "javax.swing.plaf.metal.MetalLookAndFeel");
    }

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