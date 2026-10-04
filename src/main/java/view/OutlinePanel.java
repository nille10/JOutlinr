package view;

import controller.OutlineTransferHandler;
import controller.columns.ColumnController;
import model.OutlineNode;
import model.columns.Column;
import model.columns.ColumnType;
import view.columns.ColumnPanel;
import view.columns.RowHoverManager;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeExpansionListener;
import javax.swing.tree.DefaultTreeCellEditor;
import javax.swing.tree.DefaultTreeModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.EventObject;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import javax.swing.tree.TreePath;
import java.io.IOException;


/**
 * Hauptpanel des Outliners.
 *
 * Aufbau:
 *
 * HeaderPanel  (nicht scrollend, passt Breite dynamisch an)
 * ---------------------------------------------------------
 *
 * JScrollPane
 *      |
 *      BodyPanel  (BorderLayout)
 *          WEST   = treeContainer  (wächst mit dem Tree)
 *          CENTER = columnPanel    (bekommt restlichen Platz)
 *
 * Synchronisation:
 * - TreeModelListener     → Knoten hinzugefügt / entfernt / geändert
 * - TreeExpansionListener → auf- / zugeklappt (ändert sichtbare Rows)
 * - ComponentListener     → treeContainer-Breite geändert
 *                           → HeaderPanel anpassen
 *
 * MVC-Umbau: Die Spalten-Verwaltung (bisher direkt über ColumnPanel/
 * OutlineColumn) läuft jetzt über den {@link ColumnController}, der ein
 * {@link model.columns.ColumnModel} besitzt und sowohl {@link ColumnPanel}
 * als auch {@link HeaderPanel} als Listener bedient. OutlinePanel kennt
 * dadurch keine Spalten-internen Details mehr, sondern nur noch den
 * Controller.
 */
public class OutlinePanel extends JPanel {


    private final JTree tree;

    private final DefaultTreeModel treeModel;

    private final OutlineNode rootNode;


    private final HeaderPanel headerPanel;

    private final ColumnPanel columnPanel;

    private final ColumnController columnController;

    private final BodyPanel bodyPanel;


    private final JScrollPane scrollPane;

    private final RowHoverManager hoverManager;

    private final MainWindow mainFrame;


    public OutlinePanel(MainWindow mainFrame) {
        this.mainFrame = mainFrame;

        setLayout(new BorderLayout());

        // Tree erstellen
        rootNode  = new OutlineNode("Outliner");
        treeModel = new DefaultTreeModel(rootNode) {
            @Override
            public void valueForPathChanged(TreePath path, Object newValue) {
                if (path == null) {
                    return;
                }
                super.valueForPathChanged(path, newValue);
            }
        };

        tree = new JTree(treeModel);

        tree.setRootVisible(false);
        tree.setShowsRootHandles(true);
        tree.setEditable(true);
        tree.setInvokesStopCellEditing(true);
        tree.setDragEnabled(true);
        tree.setDropMode(DropMode.ON_OR_INSERT);
        tree.setTransferHandler(new OutlineTransferHandler(mainFrame));

        //FONT
        Font treeFont = new Font("Segoe UI", Font.PLAIN, 13);
        if (treeFont.getFamily().equals("Dialog")) {
            // Segoe UI nicht gefunden — Linux-Fallback
            treeFont = new Font("DejaVu Sans", Font.PLAIN, 13);
        }
        tree.setFont(treeFont);
        tree.setRowHeight(24);

        //test
        // Explizit setzen — Linux-L&F hat andere Defaults
        ((javax.swing.plaf.basic.BasicTreeUI) tree.getUI()).setLeftChildIndent(8);
        ((javax.swing.plaf.basic.BasicTreeUI) tree.getUI()).setRightChildIndent(12);

        tree.setBorder(new EmptyBorder(4, 4, 4, 4));
        //Nur wenn Looks und Feel aus ist
        tree.putClientProperty("JTree.lineStyle", "None");

        Icon rightArrow = loadIcon("/icons/right.png", 12);
        Icon downArrow  = loadIcon("/icons/down.png", 12);

        tree.putClientProperty("JTree.lineStyle", "None");

        //renderer setzen
        hoverManager = new RowHoverManager();

        // Eigener Renderer für den Tree
        HoverTreeCellRenderer renderer = new HoverTreeCellRenderer(hoverManager);
        tree.setCellRenderer(renderer);

        //dann editor bauen - KI generiert
        tree.setCellEditor(
                new DefaultTreeCellEditor(tree, renderer) {

                    //umbenennen passiert hier
                    @Override
                    public boolean isCellEditable(EventObject e) {
                        if (e == null) {
                            return true;
                        }
                        if (e instanceof MouseEvent me) {
                            return me.getClickCount() >= 2;
                        }
                        return false;
                    }

                    @Override
                    public Component getTreeCellEditorComponent(
                            JTree tree, Object value, boolean isSelected,
                            boolean expanded, boolean leaf, int row) {

                        Component editingComponent = super.getTreeCellEditorComponent(
                                tree, value, isSelected, expanded, leaf, row);

                        if (editingComponent instanceof JTextField textField) {
                            // Feste, großzügige Breite statt automatischer Berechnung
                            // basierend auf dem aktuellen (kurzen) Text
                            Dimension size = textField.getPreferredSize();
                            size.width = Math.max(size.width, tree.getWidth() - 60);
                            textField.setPreferredSize(size);
                        }

                        return editingComponent;
                    }
                });

        tree.getInputMap(JComponent.WHEN_FOCUSED).put(
                KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()),
                "renameNode"
        );

        tree.getActionMap().put("renameNode", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                OutlineNode selected = (OutlineNode) tree.getLastSelectedPathComponent();
                if (selected != null) {
                    TreePath path = new TreePath(treeModel.getPathToRoot(selected));
                    tree.startEditingAtPath(path);
                }
            }
        });

        //KI Vorschlag
        // Zusätzlich weiterhin für Menüs (MenuSelectionManager reagiert
        // oft schneller/zuverlässiger als der reine Fokus-Wechsel bei Menüs)
        javax.swing.MenuSelectionManager.defaultManager().addChangeListener(e -> {
            if (tree.isEditing()) {
                tree.getCellEditor().stopCellEditing();
            }
        });


        // --------------------------------
        // Panels - KI generiert
        // --------------------------------

        // Spalten-Controller besitzt das Model und erzeugt/entfernt die
        // Zell-Views; HeaderPanel und ColumnPanel melden sich als Listener an,
        // um auf dieselben Ereignisse unabhängig voneinander zu reagieren.
        columnController = new ColumnController(hoverManager);

        headerPanel = new HeaderPanel(columnController);
        columnPanel = new ColumnPanel();
        columnController.addViewListener(columnPanel);
        columnController.addViewListener(headerPanel);

        bodyPanel   = new BodyPanel(tree, columnPanel);

        scrollPane = new JScrollPane(bodyPanel);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        add(headerPanel, BorderLayout.NORTH);
        add(scrollPane,  BorderLayout.CENTER);


        // --------------------------------
        // Hover-System initialisieren - KI generiert
        // --------------------------------

        // Maus-Bewegungen im Tree an den Manager melden
        installTreeHoverListener();

        // markDirty bei jeder Änderung an den Spalten (hinzufügen, entfernen,
        // umbenennen, Zellenwert geändert) - vorher wurde markDirty() nur
        // manuell in removeColumn() aufgerufen, jetzt einheitlich für alle
        // Änderungsarten über den Controller
        columnController.setOnChange(() -> mainFrame.getOutlineController().markDirty());

        installTreeListeners();
    }



    // -------------------------------------------------------
    // Listener - KI generiert
    // -------------------------------------------------------

    /**
     * Meldet Mausbewegungen im Tree als Hover-Row an den RowHoverManager.
     *
     * tree.getRowForLocation() liefert die Row unter dem Mauszeiger (0-basiert)
     * oder -1 wenn keine Row getroffen wurde.
     */
    private void installTreeHoverListener() {

        tree.addMouseMotionListener(new MouseAdapter() {

            @Override
            public void mouseMoved(MouseEvent e) {
                int row = tree.getRowForLocation(e.getX(), e.getY());
                hoverManager.setHoveredRow(row); // -1 = RowHoverManager.NONE → kein Highlight
            }
        });

        tree.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseExited(MouseEvent e) {
                hoverManager.clearHover();
            }
        });

        // Hover-Änderungen → Tree neu zeichnen
        hoverManager.addChangeListener(tree::repaint);
    }

    //KI generiert:

    /**
     * Reagiert auf Baum-Änderungen (Knoten hinzufügen/entfernen/klappen)
     * und synchronisiert die Spalten-Zeilen.
     */
    private void installTreeListeners() {

        treeModel.addTreeModelListener(new TreeModelListener() {

            @Override
            public void treeNodesChanged(TreeModelEvent e) {
                scheduleSyncColumns();
            }

            @Override
            public void treeNodesInserted(TreeModelEvent e) {
                scheduleSyncColumns();
            }

            @Override
            public void treeNodesRemoved(TreeModelEvent e) {
                // Werte gelöschter Knoten aus allen Spalten entfernen, damit
                // die Werte-Map jeder Column nicht mit toten Knoten wächst
                columnController.handleNodesRemoved(e.getChildren());
                scheduleSyncColumns();
            }

            @Override
            public void treeStructureChanged(TreeModelEvent e) {
                scheduleSyncColumns();
            }
        });

        tree.addTreeExpansionListener(new TreeExpansionListener() {

            @Override
            public void treeExpanded(TreeExpansionEvent e) {
                scheduleSyncColumns();
            }

            @Override
            public void treeCollapsed(TreeExpansionEvent e) {
                scheduleSyncColumns();
            }
        });
    }


    private void scheduleSyncColumns() {
        SwingUtilities.invokeLater(this::syncColumns);
    }



    // -------------------------------------------------------
    // Öffentliche API
    // -------------------------------------------------------

    public Column addColumn(String title, ColumnType type) {

        Column column = columnController.addColumn(title, type);

        bodyPanel.showColumns();

        syncColumns();

        return column;
    }


    public void removeColumn(Column column) {

        columnController.removeColumn(column);

        if (!columnController.hasColumns()) {
            bodyPanel.hideColumns();
        }
    }


    /**
     * Liest alle sichtbaren Rows aus dem Tree und übergibt sie
     * an das ColumnPanel. Passt auch die Scroll-Höhe an.
     */
    public void syncColumns() {

        int count = tree.getRowCount();

        OutlineNode[] nodes = new OutlineNode[count];

        for (int i = 0; i < count; i++) {

            Object obj =
                    tree.getPathForRow(i)
                            .getLastPathComponent();

            if (obj instanceof OutlineNode node) {
                nodes[i] = node;
            }
        }

        columnController.syncWithTree(nodes, tree.getRowHeight());

        bodyPanel.updatePreferredHeight(count, tree.getRowHeight());
    }



    // -------------------------------------------------------
    // Getter
    // -------------------------------------------------------

    public JTree getTree() {
        return tree;
    }

    public DefaultTreeModel getTreeModel() {
        return treeModel;
    }

    public OutlineNode getRootNode() {
        return rootNode;
    }

    public HeaderPanel getHeaderPanel() {
        return headerPanel;
    }

    public ColumnPanel getColumnPanel() {
        return columnPanel;
    }

    public ColumnController getColumnController() {
        return columnController;
    }

    private Icon loadIcon(String path, int size) {
        try {
            var stream = getClass().getResourceAsStream(path);
            if (stream == null) {
                System.err.println("Icon nicht gefunden: " + path);
                return null;
            }
            BufferedImage img = ImageIO.read(stream);
            Image scaled = img.getScaledInstance(size, size, Image.SCALE_SMOOTH);
            return new ImageIcon(scaled);
        } catch (IOException e) {
            System.err.println("Icon konnte nicht geladen werden: " + path);
            return null;
        }
    }

}