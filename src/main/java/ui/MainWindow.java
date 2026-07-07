package ui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

/**
 * MainWindow – Hauptfenster des Outliners.
 *
 * Layout:
 *   ┌──────────────────────────────────────┐
 *   │  Menüleiste (JMenuBar)               │
 *   ├──────────────────────────────────────┤
 *   │  Toolbar                             │
 *   ├──────────────────────────────────────┤
 *   │  OutlinePanel (JTree, oben)          │
 *   │──────────────────────────────────────│
 *   │  TextWindow (Notizbereich, unten)    │
 *   └──────────────────────────────────────┘
 */
public class MainWindow extends JFrame {

  private final OutlinePanel outlinePanel;
  private final TextWindow   textWindow;

  public MainWindow() {
    super("JOutlinr");
    setDefaultCloseOperation(EXIT_ON_CLOSE);
    setSize(900, 640);
    setMinimumSize(new Dimension(600, 400));
    setLocationRelativeTo(null);

    // Komponenten
    outlinePanel = new OutlinePanel();
    textWindow   = new TextWindow();
    outlinePanel.setTextWindow(textWindow);

    // Menüleiste
    setJMenuBar(buildMenuBar());

    // Toolbar
    JToolBar toolBar = buildToolBar();

    // SplitPane: Outline oben, Text unten
    JSplitPane splitPane = new JSplitPane(
        JSplitPane.VERTICAL_SPLIT,
        outlinePanel,
        textWindow
    );
    splitPane.setResizeWeight(0.6);
    splitPane.setDividerSize(5);
    splitPane.setDividerLocation(380);
    splitPane.setBorder(null);

    // Zusammenbauen
    getContentPane().setLayout(new BorderLayout());
    getContentPane().add(toolBar,   BorderLayout.NORTH);
    getContentPane().add(splitPane, BorderLayout.CENTER);

    // Status-Leiste
    JLabel statusBar = new JLabel(" Bereit");
    statusBar.setFont(new Font("Segoe UI", Font.PLAIN, 11));
    statusBar.setForeground(new Color(100, 100, 120));
    statusBar.setBorder(BorderFactory.createCompoundBorder(
        BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(200, 200, 210)),
        BorderFactory.createEmptyBorder(2, 8, 2, 8)
    ));
    getContentPane().add(statusBar, BorderLayout.SOUTH);
  }

  // ── Menüleiste ───────────────────────────────────────────────────────────

  private JMenuBar buildMenuBar() {
    JMenuBar menuBar = new JMenuBar();

    // Datei-Menü
    JMenu datei = new JMenu("Datei");
    datei.setMnemonic(KeyEvent.VK_D);
    datei.add(menuItem("Neu",          KeyStroke.getKeyStroke(KeyEvent.VK_N, InputEvent.CTRL_DOWN_MASK), e -> newDocument()));
    datei.add(menuItem("Öffnen…",      KeyStroke.getKeyStroke(KeyEvent.VK_O, InputEvent.CTRL_DOWN_MASK), e -> openDocument()));
    datei.add(menuItem("Speichern",    KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_DOWN_MASK), e -> saveDocument()));
    datei.add(menuItem("Speichern unter…", null, e -> saveDocumentAs()));
    datei.addSeparator();
    datei.add(menuItem("Beenden",      KeyStroke.getKeyStroke(KeyEvent.VK_Q, InputEvent.CTRL_DOWN_MASK), e -> System.exit(0)));
    menuBar.add(datei);

    // Bearbeiten-Menü
    JMenu bearbeiten = new JMenu("Bearbeiten");
    bearbeiten.setMnemonic(KeyEvent.VK_B);
    bearbeiten.add(menuItem("Rückgängig",  KeyStroke.getKeyStroke(KeyEvent.VK_Z, InputEvent.CTRL_DOWN_MASK), e -> undo()));
    bearbeiten.add(menuItem("Wiederholen", KeyStroke.getKeyStroke(KeyEvent.VK_Y, InputEvent.CTRL_DOWN_MASK), e -> redo()));
    bearbeiten.addSeparator();
    bearbeiten.add(menuItem("Ausschneiden", KeyStroke.getKeyStroke(KeyEvent.VK_X, InputEvent.CTRL_DOWN_MASK), e -> cut()));
    bearbeiten.add(menuItem("Kopieren",     KeyStroke.getKeyStroke(KeyEvent.VK_C, InputEvent.CTRL_DOWN_MASK), e -> copy()));
    bearbeiten.add(menuItem("Einfügen",     KeyStroke.getKeyStroke(KeyEvent.VK_V, InputEvent.CTRL_DOWN_MASK), e -> paste()));
    menuBar.add(bearbeiten);

    // Gliederung-Menü
    JMenu gliederung = new JMenu("Gliederung");
    gliederung.setMnemonic(KeyEvent.VK_G);
    gliederung.add(menuItem("Neuer Eintrag (Geschwister)",  KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, InputEvent.CTRL_DOWN_MASK),  e -> outlinePanel.addSibling()));
    gliederung.add(menuItem("Neuer Untereintrag (Kind)",    KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, InputEvent.CTRL_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK), e -> outlinePanel.addChild()));
    gliederung.add(menuItem("Eintrag löschen",              KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, InputEvent.CTRL_DOWN_MASK), e -> outlinePanel.deleteSelected()));
    gliederung.addSeparator();
    gliederung.add(menuItem("Ebene höher (Promote)",        KeyStroke.getKeyStroke(KeyEvent.VK_LEFT,   InputEvent.SHIFT_DOWN_MASK | InputEvent.ALT_DOWN_MASK), e -> outlinePanel.promoteSelected()));
    gliederung.add(menuItem("Ebene tiefer (Demote)",        KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT,  InputEvent.SHIFT_DOWN_MASK | InputEvent.ALT_DOWN_MASK), e -> outlinePanel.demoteSelected()));
    menuBar.add(gliederung);

    // Ansicht-Menü
    JMenu ansicht = new JMenu("Ansicht");
    ansicht.add(menuItem("Alles aufklappen",   null, e -> expandAll()));
    ansicht.add(menuItem("Alles zuklappen",    null, e -> collapseAll()));
    menuBar.add(ansicht);

    // Hilfe-Menü
    JMenu hilfe = new JMenu("Hilfe");
    hilfe.add(menuItem("Über…", null, e -> showAbout()));
    menuBar.add(hilfe);

    return menuBar;
  }

  // ── Toolbar ──────────────────────────────────────────────────────────────

  private JToolBar buildToolBar() {
    JToolBar tb = new JToolBar();
    tb.setFloatable(false);
    tb.setBackground(new Color(245, 245, 248));
    tb.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(200, 200, 210)));

    tb.add(toolButton("＋ Eintrag",      "Neuen Geschwister-Eintrag hinzufügen",  e -> outlinePanel.addSibling()));
    tb.add(toolButton("⤵ Untereintrag", "Neuen Kind-Eintrag hinzufügen",         e -> outlinePanel.addChild()));
    tb.add(toolButton("✕ Löschen",      "Ausgewählten Eintrag löschen",          e -> outlinePanel.deleteSelected()));
    tb.addSeparator();
    tb.add(toolButton("◀ Promote",      "Ebene nach oben verschieben",           e -> outlinePanel.promoteSelected()));
    tb.add(toolButton("▶ Demote",       "Ebene nach unten verschieben",          e -> outlinePanel.demoteSelected()));
    tb.addSeparator();
    tb.add(toolButton("⊞ Aufklappen",   "Alles aufklappen",                      e -> expandAll()));
    tb.add(toolButton("⊟ Zuklappen",    "Alles zuklappen",                       e -> collapseAll()));

    return tb;
  }

  // ── Hilfsmethoden ────────────────────────────────────────────────────────

  private JMenuItem menuItem(String text, KeyStroke ks, ActionListener al) {
    JMenuItem item = new JMenuItem(text);
    if (ks != null) item.setAccelerator(ks);
    item.addActionListener(al);
    return item;
  }

  private JButton toolButton(String text, String tooltip, ActionListener al) {
    JButton btn = new JButton(text);
    btn.setToolTipText(tooltip);
    btn.setFocusable(false);
    btn.setFont(new Font("Segoe UI", Font.PLAIN, 12));
    btn.addActionListener(al);
    return btn;
  }

  private void expandAll() {
    JTree tree = outlinePanel.getTree();
    for (int i = 0; i < tree.getRowCount(); i++) tree.expandRow(i);
  }

  private void collapseAll() {
    JTree tree = outlinePanel.getTree();
    for (int i = tree.getRowCount() - 1; i >= 0; i--) tree.collapseRow(i);
  }

  // ── Platzhalter-Aktionen (für spätere Implementierung) ───────────────────

  private void newDocument()    { JOptionPane.showMessageDialog(this, "Neu – noch nicht implementiert.");            }
  private void openDocument()   { JOptionPane.showMessageDialog(this, "Öffnen – noch nicht implementiert.");         }
  private void saveDocument()   { JOptionPane.showMessageDialog(this, "Speichern – noch nicht implementiert.");      }
  private void saveDocumentAs() { JOptionPane.showMessageDialog(this, "Speichern unter – noch nicht implementiert.");}
  private void undo()           { JOptionPane.showMessageDialog(this, "Rückgängig – noch nicht implementiert.");     }
  private void redo()           { JOptionPane.showMessageDialog(this, "Wiederholen – noch nicht implementiert.");    }
  private void cut()            { JOptionPane.showMessageDialog(this, "Ausschneiden – noch nicht implementiert.");   }
  private void copy()           { JOptionPane.showMessageDialog(this, "Kopieren – noch nicht implementiert.");       }
  private void paste()          { JOptionPane.showMessageDialog(this, "Einfügen – noch nicht implementiert.");       }

  private void showAbout() {
    JOptionPane.showMessageDialog(this,
        "Outliner v1.0\nErstellt mit Java Swing\n\nKlasse: MainWindow + OutlinePanel + TextWindow",
        "Über Outliner", JOptionPane.INFORMATION_MESSAGE);
  }

  // ── Getter ───────────────────────────────────────────────────────────────

  public OutlinePanel getOutlinePanel() { return outlinePanel; }
  public TextWindow   getTextWindow()   { return textWindow;   }
}