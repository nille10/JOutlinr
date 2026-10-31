package view;

import controller.OutlineController;
import model.preferences.AppPreferences;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * Modaler Einstellungs-Dialog der Anwendung.
 *
 * <p>Zeigt zwei Tabs: "Allgemein" (Dark Mode, Vollbild, Verhaltens-Optionen) und
 * "Farben" (Akzentfarb-Palette von {@link AppTheme}). Änderungen werden sofort
 * angewendet (kein "Übernehmen"-Button) und über {@link AppPreferences}
 * persistiert, sodass sie über Programmstarts hinweg erhalten bleiben.</p>
 */
public class SettingsWindow extends JDialog {

  /**
   * Baut den Einstellungs-Dialog auf.
   *
   * @param parent            das Elternfenster, über dem der Dialog modal angezeigt wird
   * @param outlineController der Controller, über den verhaltensbezogene Einstellungen
   *                          (Auswahl nach Löschen, Icon-Anzeige) direkt auf den Baum wirken
   */
  public SettingsWindow(JFrame parent, OutlineController outlineController) {
    super(parent, "Einstellungen", true); // modal
    setSize(480, 520);
    setLocationRelativeTo(parent);
    setResizable(false);

    JPanel root = new JPanel(new BorderLayout(0, 12));
    root.setBorder(new EmptyBorder(16, 16, 16, 16));

    // ── Tabs ─────────────────────────────────────────────────────────
    JTabbedPane tabs = new JTabbedPane();
    tabs.addTab("Allgemein", buildGeneralPanel(outlineController, parent));
    tabs.addTab("Farben",    buildColorPanel(parent));

    root.add(tabs, BorderLayout.CENTER);

    // ── Schließen-Button ─────────────────────────────────────────────
    JButton closeBtn = new JButton("Schließen");
    closeBtn.addActionListener(e -> dispose());

    JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
    btnPanel.add(closeBtn);
    root.add(btnPanel, BorderLayout.SOUTH);

    setContentPane(root);
  }


  // ── Tab 1: Allgemein ─────────────────────────────────────────────────

  /**
   * Baut den "Allgemein"-Tab auf: Darstellungs-Optionen (Dark Mode, Vollbild)
   * und Verhaltens-Optionen (Auswahl nach Löschen, Icon-Anzeige).
   *
   * <p>Jede Checkbox wird mit dem aktuell in {@link AppPreferences} gespeicherten
   * Wert vorbelegt; jede Änderung wendet die Einstellung sofort an (auf
   * {@link AppTheme}, {@code parent} bzw. {@code outlineController}) und
   * persistiert sie anschließend über {@link AppPreferences}.</p>
   *
   * @param outlineController der Controller für verhaltensbezogene Einstellungen
   * @param parent            das Hauptfenster, auf das Dark Mode und Vollbild angewendet werden
   * @return das fertig aufgebaute Tab-Panel
   */
  private JPanel buildGeneralPanel(OutlineController outlineController, JFrame parent) {
    JPanel panel = new JPanel();
    panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
    panel.setBorder(new EmptyBorder(12, 8, 8, 8));

    // Dark Mode
    JCheckBox darkMode = new JCheckBox("Dark Mode");
    darkMode.setSelected(AppPreferences.isDarkMode());
    darkMode.addActionListener(e -> {
      AppTheme.toggleDark(darkMode.isSelected(), parent);
      AppPreferences.setDarkMode(darkMode.isSelected());
    });

    // Vollbild
    JCheckBox fullscreen = new JCheckBox("Vollbild");
    fullscreen.setSelected(AppPreferences.isFullscreen());
    fullscreen.addActionListener(e -> {
      parent.setExtendedState(fullscreen.isSelected()
              ? JFrame.MAXIMIZED_BOTH : JFrame.NORMAL);
      AppPreferences.setFullScreen(fullscreen.isSelected());
    });

    // Letzten Eintrag nach Löschvorgang auswählen
    JCheckBox selectAfterDelete = new JCheckBox("Letzten Eintrag nach Löschvorgang auswählen");
    selectAfterDelete.setSelected(AppPreferences.isSelectAfterDeleteOn());
    selectAfterDelete.addActionListener(e -> {
      outlineController.setSelectLastEntryOption(selectAfterDelete.isSelected());
      AppPreferences.setSelectAfterDelete(selectAfterDelete.isSelected());
    });

    // Icon anzeigen wenn Text vorhanden
    JCheckBox showIcon = new JCheckBox("Icon anzeigen, wenn Eintrag Text enthält");
    showIcon.setSelected(AppPreferences.isShowIcon());
    showIcon.addActionListener(e -> {
      AppPreferences.setShowIcon(showIcon.isSelected());
      outlineController.getTreeModel().nodeStructureChanged(outlineController.getRootNode());
      outlineController.getTree().repaint();
    });

    panel.add(section("Darstellung"));
    panel.add(darkMode);
    panel.add(fullscreen);
    panel.add(Box.createVerticalStrut(12));
    panel.add(section("Verhalten"));
    panel.add(selectAfterDelete);
    panel.add(showIcon);
    panel.add(Box.createVerticalGlue());

    return panel;
  }


  // ── Tab 2: Farben ────────────────────────────────────────────────────

  /**
   * Baut den "Farben"-Tab auf: eine Zeile pro Akzentfarbe
   * ({@link AppTheme#ACCENT}, {@link AppTheme#ACCENT_DARK},
   * {@link AppTheme#ACCENT_LIGHT}) mit Name, Farbvorschau-Button (öffnet den
   * {@link JColorChooser}) und Hex-Code, sowie einem Button zum Zurücksetzen
   * auf die Standardfarben.
   *
   * @param parent das Hauptfenster, auf das Farbänderungen angewendet werden
   * @return das fertig aufgebaute Tab-Panel
   */
  private JPanel buildColorPanel(JFrame parent) {
    JPanel panel = new JPanel();
    panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
    panel.setBorder(new EmptyBorder(12, 8, 8, 8));

    // Aktuell gewählte Farben aus AppTheme
    Color[] current = {
            AppTheme.ACCENT,
            AppTheme.ACCENT_DARK,
            AppTheme.ACCENT_LIGHT
    };
    String[] labels = { "Akzentfarbe", "Akzent (dunkel)", "Akzent (hell)" };

    JPanel colorGrid = new JPanel(new GridLayout(3, 3, 8, 10));

    for (int i = 0; i < 3; i++) {
      final int idx = i;

      JLabel nameLabel = new JLabel(labels[i]);
      nameLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));

      // Farbvorschau-Button — klickt man drauf, öffnet sich der Color Chooser
      JButton preview = new JButton();
      preview.setBackground(current[i]);
      preview.setOpaque(true);
      preview.setBorderPainted(false);
      preview.setPreferredSize(new Dimension(40, 24));
      preview.setToolTipText("Farbe wählen");

      JLabel hexLabel = new JLabel(toHex(current[i]));
      hexLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
      hexLabel.setForeground(UIManager.getColor("Label.foreground"));

      preview.addActionListener(e -> {
        Color chosen = JColorChooser.showDialog(
                this,
                "Farbe wählen – " + labels[idx],
                preview.getBackground()
        );
        if (chosen != null) {
          preview.setBackground(chosen);
          hexLabel.setText(toHex(chosen));
          applyColorChange(idx, chosen, parent);
        }
      });

      colorGrid.add(nameLabel);
      colorGrid.add(preview);
      colorGrid.add(hexLabel);
    }

    panel.add(section("App-Farben"));
    panel.add(colorGrid);
    panel.add(Box.createVerticalStrut(16));

    // Reset-Button
    JButton resetBtn = new JButton("Auf Standard zurücksetzen");
    resetBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
    resetBtn.addActionListener(e -> {
      AppTheme.resetToDefaults(parent);
      // Fenster neu öffnen damit Vorschau-Buttons aktualisiert werden
      dispose();
      new SettingsWindow(parent, null).setVisible(true);
    });

    panel.add(resetBtn);
    panel.add(Box.createVerticalGlue());

    return panel;
  }


  // ── Hilfsmethoden ────────────────────────────────────────────────────

  /**
   * Wendet die am Farb-Chooser gewählte Farbe auf die passende Akzentfarbe von
   * {@link AppTheme} an.
   *
   * @param colorIndex 0 für {@link AppTheme#ACCENT}, 1 für {@link AppTheme#ACCENT_DARK},
   *                   2 für {@link AppTheme#ACCENT_LIGHT}; andere Werte bewirken nichts
   * @param chosen     die neu gewählte Farbe
   * @param parent     das Fenster, das nach der Änderung neu gezeichnet wird
   */
  private void applyColorChange(int colorIndex, Color chosen, JFrame parent) {
    switch (colorIndex) {
      case 0 -> AppTheme.setAccent(chosen, parent);
      case 1 -> AppTheme.setAccentDark(chosen, parent);
      case 2 -> AppTheme.setAccentLight(chosen, parent);
    }
  }

  /**
   * Formatiert eine Farbe als Hex-Code im Format {@code #RRGGBB}.
   *
   * @param c die zu formatierende Farbe
   * @return der Hex-Code, z. B. {@code "#FA6D7B"}
   */
  private String toHex(Color c) {
    return String.format("#%02X%02X%02X", c.getRed(), c.getGreen(), c.getBlue());
  }

  /**
   * Erstellt eine fett gedruckte, dezent eingefärbte Abschnittsüberschrift für
   * die Tab-Panels.
   *
   * @param title der Überschriftentext
   * @return das fertig formatierte Label
   */
  private JLabel section(String title) {
    JLabel label = new JLabel(title);
    label.setFont(new Font("Segoe UI", Font.BOLD, 11));
    label.setForeground(UIManager.getColor("Label.disabledForeground"));
    label.setBorder(new EmptyBorder(0, 0, 4, 0));
    label.setAlignmentX(Component.LEFT_ALIGNMENT);
    return label;
  }
}