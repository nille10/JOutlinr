package view;

import action.actions.EditActions;
import action.actions.FileActions;
import action.actions.OutlineActions;
import action.manager.UndoManager;
import controller.*;

import javax.swing.*;
import java.awt.*;

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
        setMinimumSize(new Dimension(600, 400));
        setLocationRelativeTo(null);

        outlinePanel = new OutlinePanel();
        textWindow = new TextWindow();
        //Global UndoManager
        UndoManager undoManager = new UndoManager();

        // Controller
        EditController editController = new EditController(this, undoManager); // Das this wird in dem Controller nur für die Platzhalter benötigt
        FileController fileController = new FileController(this); // Das this wird in dem Controller nur für die Platzhalter benötigt
        OutlineController outlineController = new OutlineController(outlinePanel, textWindow);


        // Actions
        EditActions editActions = new EditActions(editController);
        FileActions fileActions = new FileActions(fileController);
        OutlineActions outlineActions = new OutlineActions(outlineController, undoManager);

        // Menüleiste
        Menubar menubar = new Menubar(fileActions, outlineActions, editActions, this);
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
}