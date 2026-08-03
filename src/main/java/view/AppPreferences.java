package view;

import java.util.prefs.Preferences;

public class AppPreferences {

  private static final Preferences PREFS =
      Preferences.userNodeForPackage(AppPreferences.class);

  private static final String KEY_DARK_MODE = "darkMode";
  private static final String KEY_FULLSCREEN = "fullscreen";
  private static final String KEY_SELECT_DELETE = "selectAfterDelete";

  public static boolean isDarkMode() {
    return PREFS.getBoolean(KEY_DARK_MODE, false);
  }

  public static void setDarkMode(boolean enabled) {
    PREFS.putBoolean(KEY_DARK_MODE, enabled);
  }

  public static boolean isFullscreen(){
    return PREFS.getBoolean(KEY_FULLSCREEN, false);
  }

  public static void setFullScreen(boolean enabled) {
    PREFS.putBoolean(KEY_FULLSCREEN, enabled);
  }

  public static boolean isSelectAfterDeleteOn(){
    return PREFS.getBoolean(KEY_SELECT_DELETE, true);
  }

  public static void setSelectAfterDelete(boolean enabled) {
    PREFS.putBoolean(KEY_SELECT_DELETE, enabled);
  }
}