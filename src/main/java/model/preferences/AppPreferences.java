package model.preferences;

import java.util.prefs.Preferences;

/**
 * Persistiert anwendungsweite Einstellungen über die {@link Preferences}-API des
 * Betriebssystems (z. B. Registry unter Windows), sodass sie über
 * Programmstarts hinweg erhalten bleiben.
 *
 * <p>Enthält ausschließlich statische Getter/Setter für einzelne Einstellungen;
 * hält selbst keinen Zustand im Arbeitsspeicher, sondern liest und schreibt bei
 * jedem Aufruf direkt über die zugrundeliegende {@link Preferences}-Instanz.</p>
 */
public class AppPreferences {

  /** Die {@link Preferences}-Instanz, unter der alle Einstellungen dieser Klasse abgelegt werden. */
  private static final Preferences PREFS =
          Preferences.userNodeForPackage(AppPreferences.class);

  /** Schlüssel für die Einstellung „Dark Mode aktiv“. */
  private static final String KEY_DARK_MODE = "darkMode";

  /** Schlüssel für die Einstellung „Vollbild aktiv“. */
  private static final String KEY_FULLSCREEN = "fullscreen";

  /** Schlüssel für die Einstellung „nach dem Löschen automatisch den nächsten Knoten auswählen“. */
  private static final String KEY_SELECT_DELETE = "selectAfterDelete";

  /** Schlüssel für die Einstellung „Icon anzeigen“. */
  private static final String KEY_SHOW_ICON = "showIcon";

  /** Schlüssel für den Pfad der zuletzt geöffneten Datei. */
  private static final String KEY_LAST_FILE = "";

  /**
   * Liefert, ob der Dark Mode aktiv ist.
   *
   * @return {@code true}, wenn der Dark Mode aktiv ist; ohne gespeicherten Wert
   *         standardmäßig {@code false}
   */
  public static boolean isDarkMode() {
    return PREFS.getBoolean(KEY_DARK_MODE, false);
  }

  /**
   * Setzt, ob der Dark Mode aktiv ist.
   *
   * @param enabled {@code true}, um den Dark Mode zu aktivieren
   */
  public static void setDarkMode(boolean enabled) {
    PREFS.putBoolean(KEY_DARK_MODE, enabled);
  }

  /**
   * Liefert, ob der Vollbildmodus aktiv ist.
   *
   * @return {@code true}, wenn der Vollbildmodus aktiv ist; ohne gespeicherten
   *         Wert standardmäßig {@code false}
   */
  public static boolean isFullscreen(){
    return PREFS.getBoolean(KEY_FULLSCREEN, false);
  }

  /**
   * Setzt, ob der Vollbildmodus aktiv ist.
   *
   * @param enabled {@code true}, um den Vollbildmodus zu aktivieren
   */
  public static void setFullScreen(boolean enabled) {
    PREFS.putBoolean(KEY_FULLSCREEN, enabled);
  }

  /**
   * Liefert, ob nach dem Löschen eines Knotens automatisch ein anderer Knoten
   * ausgewählt werden soll.
   *
   * @return {@code true}, wenn die automatische Auswahl aktiv ist; ohne
   *         gespeicherten Wert standardmäßig {@code true}
   */
  public static boolean isSelectAfterDeleteOn(){
    return PREFS.getBoolean(KEY_SELECT_DELETE, true);
  }

  /**
   * Setzt, ob nach dem Löschen eines Knotens automatisch ein anderer Knoten
   * ausgewählt werden soll.
   *
   * @param enabled {@code true}, um die automatische Auswahl zu aktivieren
   */
  public static void setSelectAfterDelete(boolean enabled) {
    PREFS.putBoolean(KEY_SELECT_DELETE, enabled);
  }

  /**
   * Liefert, ob das Anwendungs-Icon angezeigt werden soll.
   *
   * @return {@code true}, wenn das Icon angezeigt werden soll; ohne gespeicherten
   *         Wert standardmäßig {@code true}
   */
  public static boolean isShowIcon(){
    return PREFS.getBoolean(KEY_SHOW_ICON, true);
  }

  /**
   * Setzt, ob das Anwendungs-Icon angezeigt werden soll.
   *
   * @param enabled {@code true}, um das Icon anzuzeigen
   */
  public static void setShowIcon(boolean enabled){
    PREFS.putBoolean(KEY_SHOW_ICON, enabled);
  }

  /**
   * Speichert den Pfad der zuletzt geöffneten bzw. gespeicherten Datei.
   *
   * @param path der zu speichernde Dateipfad
   */
  public static void setLastFile(String path) {PREFS.put(KEY_LAST_FILE, path); }

  /**
   * Liefert den Pfad der zuletzt geöffneten bzw. gespeicherten Datei.
   *
   * @return der gespeicherte Dateipfad, oder ein leerer String, wenn noch keiner
   *         gespeichert wurde
   */
  public static String getLastFile() {return  PREFS.get(KEY_LAST_FILE, ""); }
}