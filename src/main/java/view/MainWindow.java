package view;

import action.actions.EditActions;
import action.actions.FileActions;
import action.actions.OutlineActions;
import action.actions.edit.CopyAction;
import action.actions.edit.CutAction;
import action.actions.edit.InsertAction;
import action.manager.UndoManager;
import action.outline.*;
import controller.*;
import controller.SaveStatus;
import model.OutlineNode;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import javax.imageio.ImageIO;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
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
    private final OutlineController outlineController;
    private final TextWindow textWindow;
    private final JLabel statusBar;
    private static final String BASE_TITLE = "JOutlinr";


    public MainWindow() {
        super(BASE_TITLE);
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setSize(950, 640);
        setIconImage(loadLogo());
        setMinimumSize(new Dimension(600, 400));
        setLocationRelativeTo(null);

        outlinePanel = new OutlinePanel(this);
        textWindow = new TextWindow();
        //Global UndoManager
        UndoManager undoManager = new UndoManager();

        // Controller
        outlineController = new OutlineController(outlinePanel, textWindow, this);
        FileController fileController = new FileController(outlineController, this);
        EditController editController = new EditController(this, undoManager, outlineController);

        // KeyBindings für den Tree registrieren
        //Wichtig, da sonst andere JavaSwing Elemente KeyBindings schlucken
        registerOutlineKeyBindings(outlinePanel.getTree(), editController, undoManager);

        // Actions
        EditActions editActions = new EditActions(editController);
        FileActions fileActions = new FileActions(fileController);
        OutlineActions outlineActions = new OutlineActions(outlineController, undoManager);

        // Menüleiste
        Menubar menubar = new Menubar(fileActions, outlineActions, editActions, this, outlinePanel, outlineController);
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
        statusBar = new JLabel(" Bereit");
        statusBar.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        //statusBar.setForeground(new Color(100, 100, 120));
        statusBar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(200, 200, 210)),
                BorderFactory.createEmptyBorder(2, 8, 2, 8)
        ));
        getContentPane().add(statusBar, BorderLayout.SOUTH);

        textWindow.setOnContentChanged(() -> {
            OutlineNode selected = outlineController.getSelectedNode();
            if (selected != null) {
                outlineController.getTreeModel().nodeChanged(selected);
            }
        });

        //Fenster schließen
        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                if(fileController.checkSavedStatus() != SaveStatus.SAVE_FAILED) {
                    fileController.saveLastPath();
                    dispose();
                }
            }
        });

        fileController.tryImportLastEditedFile();
        outlineController.saved();
    }

    private void registerOutlineKeyBindings(JTree tree, EditController editController, UndoManager undoManager) {

        InputMap inputMap = tree.getInputMap(JComponent.WHEN_FOCUSED);
        ActionMap actionMap = tree.getActionMap();

        inputMap.put(
            KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, KeyEvent.CTRL_DOWN_MASK),
            "promoteNode");

        actionMap.put("promoteNode", new PromoteEntryAction(outlineController));

        inputMap.put(
            KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, KeyEvent.CTRL_DOWN_MASK),
            "demoteNode");

        actionMap.put("demoteNode", new DemoteEntryAction(outlineController));

        inputMap.put(
            KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0),
            "expandNode");

        actionMap.put("expandNode", new ExpandEntryAction(outlineController));

        inputMap.put(
            KeyStroke.getKeyStroke(KeyEvent.VK_BACK_SPACE, 0),
            "collapseNode");

        actionMap.put("collapseNode", new CollapseEntryAction(outlineController));

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

        inputMap.put(
            KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0),
            "deleteNode");

        actionMap.put("deleteNode", new DeleteEntryAction(outlineController, undoManager));
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

    public JLabel getStatusBar() {return statusBar; }
    public OutlineController getOutlineController() {return outlineController; }
}