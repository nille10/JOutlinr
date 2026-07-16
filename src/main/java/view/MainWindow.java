package view;

import action.actions.EditActions;
import action.actions.FileActions;
import action.actions.OutlineActions;
import action.edit.CopyAction;
import action.edit.CutAction;
import action.edit.InsertAction;
import action.manager.UndoManager;
import controller.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import javax.imageio.ImageIO;
import java.io.IOException;

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
    private final TextWindow textWindow;

    public MainWindow() {
        super("JOutlinr");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(900, 640);
        setIconImage(loadLogo());
        setMinimumSize(new Dimension(600, 400));
        setLocationRelativeTo(null);

        outlinePanel = new OutlinePanel();
        textWindow = new TextWindow();
        //Global UndoManager
        UndoManager undoManager = new UndoManager();

        // Controller
        FileController fileController = new FileController(outlinePanel.getTreeModel(), this);
        OutlineController outlineController = new OutlineController(outlinePanel, textWindow);
        EditController editController = new EditController(this, undoManager, outlineController);

        // KeyBindings für den Tree registrieren
        //Wichtig, da sonst andere JavaSwing Elemente KeyBindings schlucken
        registerOutlineKeyBindings(outlinePanel.getTree(), editController);

        // Actions
        EditActions editActions = new EditActions(editController);
        FileActions fileActions = new FileActions(fileController);
        OutlineActions outlineActions = new OutlineActions(outlineController, undoManager);

        // Menüleiste
        Menubar menubar = new Menubar(fileActions, outlineActions, editActions, this, outlinePanel);
        setJMenuBar(menubar);

        // Toolbar
        Toolbar toolbar = new Toolbar(outlineActions);

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
        getContentPane().add(toolbar, BorderLayout.NORTH);
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

    private void registerOutlineKeyBindings(JTree tree, EditController editController) {

        InputMap inputMap = tree.getInputMap(JComponent.WHEN_FOCUSED);
        ActionMap actionMap = tree.getActionMap();

        inputMap.put(
            KeyStroke.getKeyStroke(KeyEvent.VK_X, KeyEvent.CTRL_DOWN_MASK),
            "cutNode");

        actionMap.put("cutNode", new CutAction(editController));

        inputMap.put(
            KeyStroke.getKeyStroke(KeyEvent.VK_C, KeyEvent.CTRL_DOWN_MASK),
            "copyNode");

        actionMap.put("copyNode", new CopyAction(editController));

        inputMap.put(
            KeyStroke.getKeyStroke(KeyEvent.VK_V, KeyEvent.CTRL_DOWN_MASK),
            "pasteNode");

        actionMap.put("pasteNode", new InsertAction(editController));
    }

    private Image loadLogo() {

        try {
            return ImageIO.read(
                getClass().getResource("/images/joutliner_logo.png")
            );

        } catch (IOException | IllegalArgumentException e) {
            System.err.println("Logo konnte nicht geladen werden");
            return null;
        }
    }
}