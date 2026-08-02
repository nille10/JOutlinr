package view;

import controller.OutlineTransferHandler;
import model.OutlineNode;
import view.columns.ColumnPanel;
import view.columns.ColumnType;
import view.columns.OutlineColumn;
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
 */
public class OutlinePanel extends JPanel {


    private final JTree tree;

    private final DefaultTreeModel treeModel;

    private final OutlineNode rootNode;


    private final HeaderPanel headerPanel;

    private final ColumnPanel columnPanel;

    private final BodyPanel bodyPanel;


    private final JScrollPane scrollPane;

    private final RowHoverManager hoverManager;



    public OutlinePanel(MainWindow mainFrame) {

        setLayout(new BorderLayout());


        // --------------------------------
        // Tree erstellen
        // --------------------------------

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

        tree.setBackground(new Color(248, 248, 252));
        tree.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        tree.setRowHeight(24);
        tree.setBorder(new EmptyBorder(4, 4, 4, 4));
        //Nur wenn Looks und Feel aus ist
        tree.putClientProperty("JTree.lineStyle", "None");

        Icon rightArrow = loadIcon("/icons/right.png", 12);
        Icon downArrow  = loadIcon("/icons/down.png", 12);

        tree.setUI(new javax.swing.plaf.basic.BasicTreeUI() {

            @Override
            protected void installDefaults() {
                super.installDefaults();
                setExpandedIcon(downArrow);
                setCollapsedIcon(rightArrow);
            }

            @Override
            protected void paintHorizontalLine(Graphics g, JComponent c, int y, int left, int right) {
                // absichtlich leer – keine horizontalen Linien
            }

            @Override
            protected void paintVerticalLine(Graphics g, JComponent c, int x, int top, int bottom) {
                // absichtlich leer – keine vertikalen Linien
            }

        });

        //renderer setzen
        hoverManager = new RowHoverManager();

        // Eigener Renderer für den Tree
        HoverTreeCellRenderer renderer = new HoverTreeCellRenderer(hoverManager);
        tree.setCellRenderer(renderer);

        //dann editor bauen
        tree.setCellEditor(
            new DefaultTreeCellEditor(tree, renderer) {

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
            });


        // --------------------------------
        // Panels
        // --------------------------------

        headerPanel = new HeaderPanel();
        columnPanel = new ColumnPanel();
        bodyPanel   = new BodyPanel(tree, columnPanel);

        scrollPane = new JScrollPane(bodyPanel);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        add(headerPanel, BorderLayout.NORTH);
        add(scrollPane,  BorderLayout.CENTER);


        // --------------------------------
        // Hover-System initialisieren
        // --------------------------------

        // Hover-Manager an ColumnPanel weitergeben (gilt für alle Spalten)
        columnPanel.setHoverManager(hoverManager);

        // Maus-Bewegungen im Tree an den Manager melden
        installTreeHoverListener();


        // --------------------------------
        // Weitere Listener
        // --------------------------------

        // HeaderPanel delegiert "Spalte entfernen" nach oben
        headerPanel.setOnColumnRemove(this::removeColumn);

        installTreeListeners();
    }



    // -------------------------------------------------------
    // Listener
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

    public OutlineColumn addColumn(String title, ColumnType type) {

        OutlineColumn column = columnPanel.addColumn(title, type);

        // Column mitgeben damit Header und Column beim Entfernen
        // koordiniert abgebaut werden können
        headerPanel.addColumnHeader(title, column);

        bodyPanel.showColumns();

        syncColumns();

        return column;
    }


    public void removeColumn(OutlineColumn column) {

        columnPanel.removeColumn(column);

        if (!columnPanel.hasColumns()) {
            bodyPanel.hideColumns();
            headerPanel.clearColumnHeaders();
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

        columnPanel.syncWithTree(nodes, tree.getRowHeight());

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