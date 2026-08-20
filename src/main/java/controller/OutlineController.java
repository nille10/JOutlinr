package controller;

import model.OutlineNode;
import view.TextWindow;
import view.OutlinePanel;
import view.columns.ColumnHeader;
import view.columns.ColumnType;
import view.columns.OutlineColumn;

import javax.swing.*;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Controller für die Interaktion mit der Datenstruktur des Outliners
 * <p>
 *     Ist zuständig für die manipulation des JTrees.
 * </p>
 */
public class OutlineController {

    private final TextWindow textWindow;
    private final OutlinePanel outlinePanel;

    private final JTree tree;
    private final DefaultTreeModel treeModel;
    private final OutlineNode rootNode;
    private final JFrame mainWindow;
    private boolean dirty;

    private boolean selectLastEntryOption = true;

    public static final int MAX_LEVEL = 3; //Root = 0

    private final List<Runnable> updateListeners = new ArrayList<>();

    /**
     * Erstellt einen neuen {@code OutlineController}
     * @param outlinePanel Das Panel in dem die Nodes dargestellt werden
     * @param textWindow Das TextWindow der Nodes
     * @param mainWindow Das Main window um neue Fenster an der richtigen position anzuzeigen
     */
    public OutlineController(OutlinePanel outlinePanel, TextWindow textWindow, JFrame mainWindow) {

        this.textWindow = textWindow;
        this.outlinePanel = outlinePanel;

        this.tree = outlinePanel.getTree();
        this.treeModel = outlinePanel.getTreeModel();
        this.rootNode = outlinePanel.getRootNode();
        this.mainWindow = mainWindow;

        //createExampleData();
        registerListeners();
        expandAll();
    }

    /**
     *
     */
    private void registerListeners() {

        tree.addTreeSelectionListener(e -> {
            OutlineNode selected = getSelectedNode();
            textWindow.loadNode(selected);
            fireUpdate();
        });

        treeModel.addTreeModelListener(
                new javax.swing.event.TreeModelListener() {

                    @Override
                    public void treeNodesChanged(javax.swing.event.TreeModelEvent e) {

                        Object[] children = e.getChildren();

                        if (children == null || children.length == 0) {
                            return;
                        }

                        OutlineNode child = (OutlineNode) children[0];

                        child.setTitle(child.getUserObject().toString());

                        SwingUtilities.invokeLater(() -> {
                            tree.treeDidChange();
                            tree.repaint();
                        });

                        markDirty();
                    }

                    @Override
                    public void treeNodesInserted(
                            javax.swing.event.TreeModelEvent e) {
                        markDirty();
                    }

                    @Override
                    public void treeNodesRemoved(javax.swing.event.TreeModelEvent e) {
                        markDirty();
                    }

                    @Override
                    public void treeStructureChanged(javax.swing.event.TreeModelEvent e) {
                        markDirty();
                    }
                });
    }

    /**
     * Überprüft, ob eine weitere Outlinenode hinzugefügt werden kann, ohne die maximale tiefe {@link #MAX_LEVEL} des Baums zu überschreiten.
     * @param parent die Parent node der node die hinzugefügt werden soll
     * @return {@code true} wenn die neue Node das maximale Level nicht überschreitet, ansonsten
     *         {@code false}
     */
    public boolean canAddChildTo(OutlineNode parent) {
        if (parent == null) {
            return true;
        }
        return parent.getLevel() < MAX_LEVEL;
    }

    /**
     * Überprüft, ob eine weitere Outline Node unter der ausgewählten Node hinzugefügt werden kann (siehe {@link #canAddChildTo(OutlineNode)}).
     * @return {@code true} wenn die neue Node das maximale Level nicht überschreitet, ansonsten
     *         {@code false}
     */
    public boolean canAddChildToSelected() {
        OutlineNode selected = getSelectedNode();
        OutlineNode parent = selected != null ? selected : rootNode;
        return canAddChildTo(parent);
    }

    /**
     *
     * @param listener
     */
    public void addUpdateListener(Runnable listener) {
        updateListeners.add(listener);
        listener.run(); // initialer Zustand direkt setzen
    }

    private void fireUpdate() {
        for (Runnable r : updateListeners) {
            r.run();
        }
    }

    /**
     * Erstellt eine neue Spalte im Outliner
     * <p>
     *   Öffnet ein Fenster um zu ermitteln welche art von Spalte hinzugefügt werden soll und fügt diese dann hinzu.
     *   Ruft {@link #markDirty()} auf um einen ungespeicherten Zustand zu markieren.
     * </p>
     */
    public void addColumn() {

        commitEditing();

        JPanel panel = new JPanel(new GridLayout(2, 2, 5, 5));

        JTextField titleField = new JTextField("Neue Spalte");
        JComboBox<ColumnType> typeBox = new JComboBox<>(ColumnType.values());

        panel.add(new JLabel("Titel:"));
        panel.add(titleField);
        panel.add(new JLabel("Typ:"));
        panel.add(typeBox);

        int result = JOptionPane.showConfirmDialog(
            mainWindow,           // <-- statt "this"
            panel,
            "Neue Spalte",
            JOptionPane.OK_CANCEL_OPTION);

        if (result == JOptionPane.OK_OPTION) {

            String title = titleField.getText().trim();

            if (title.isEmpty()) {
                title = "Spalte";
            }

            ColumnType type = (ColumnType) typeBox.getSelectedItem();

            outlinePanel.addColumn(title, type); // <-- jetzt gültig, da Feld existiert
        }
        markDirty();
    }

    /**
     * Fügt eine neue Spalte hinzu.
     * <p>
     *   Wird genutzt um beim importieren von Dateien die Spalten, des Outliners wieder aufzubauen.
     * </p>
     * @param title Titel der Spalte
     * @param type Typ der Spalte
     */
    public void addColumn(String title, ColumnType type) {

        commitEditing();

        outlinePanel.addColumn(title, type);

        saved();
    }

    /**
     * Prüft, ob eine Outline Node im Baum ausgerückt werden kann.
     * @return {@code true} wenn die Node nicht die RootNode ist und ungleich null ist, ansonsten {@code false}
     */
    public boolean canPromoteSelected() { // Ausrücken / Outdent

        commitEditing();

        OutlineNode selected = getSelectedNode();

        if (selected == null) {
            return false;
        }

        OutlineNode parent = (OutlineNode) selected.getParent();

        return parent != null && parent != rootNode;
    }

    /**
     * Prüft, ob eine Outline Node im Baum eingerückt werden kann.
     * <p>
     *   Überprüft ob die Node selber die RootNode ist, ob eine Node zum unterordnen existiert und ob die maximale Tiefe überschritten wird.
     * </p>
     * @return {@code true} wenn die Node eingerückt werden kann, ansonsten {@code false}
     */
    public boolean canDemoteSelected() { // Einrücken / Indent

        OutlineNode selected = getSelectedNode();

        if (selected == null) {
            return false;
        }

        OutlineNode parent = (OutlineNode) selected.getParent();

        if (parent == null) {
            return false; // Root selbst
        }

        int index = parent.getIndex(selected);

        if (index == 0) {
            return false; // kein vorheriger Geschwisterknoten zum "Andocken"
        }

        // Tiefster Level im gesamten Teilbaum (inkl. aller Kinder/Enkel etc.)
        int deepestLevelInSubtree = getMaxLevelInSubtree(selected);

        // Nach dem Demote rückt der GESAMTE Teilbaum eine Ebene tiefer
        return deepestLevelInSubtree < MAX_LEVEL;
    }

    /**
     * Ermittelt die tiefste Verschachtelungsebene innerhalb eines Teilbaums
     * (inklusive des Knotens selbst).
     */
    // KI generiert
    private int getMaxLevelInSubtree(OutlineNode node) {

        int max = node.getLevel();

        for (int i = 0; i < node.getChildCount(); i++) {
            OutlineNode child = (OutlineNode) node.getChildAt(i);
            int childMax = getMaxLevelInSubtree(child);
            if (childMax > max) {
                max = childMax;
            }
        }

        return max;
    }

    /**
     * Überprüft, ob bein einfügen eines Teilbaums die maximale Tiefe {@link #MAX_LEVEL} überschritten wird.
     * @param parent Der Eltern Knoten von {@code nodeToPaste}
     * @param nodeToPaste Der Knoten der eingefügt werden soll
     * @return {@code true} wenn möglich, ansonsten {@code false}
     */
    // KI generiert
    public boolean canPasteInto(OutlineNode parent, OutlineNode nodeToPaste) {

        if (parent == null || nodeToPaste == null) {
            return true;
        }

        // Tiefster Level, den der eingefügte Teilbaum relativ zu SICH SELBST hat
        // (0 = nodeToPaste selbst, +1 pro Verschachtelungsebene seiner Kinder)
        int subtreeDepth = getRelativeDepth(nodeToPaste);

        // Nach dem Einfügen läge der tiefste Nachkomme auf: parent.getLevel() + 1 + subtreeDepth
        int resultingMaxLevel = parent.getLevel() + 1 + subtreeDepth;

        return resultingMaxLevel <= MAX_LEVEL;
    }

    /**
     * Ermittelt die relative Tiefe eines (noch nicht eingehängten) Teilbaums,
     * unabhängig von seinem aktuellen Level im Baum (der bei einer Kopie z.B. 0 sein kann).
     */
    // Ki generiert
    public int getRelativeDepth(OutlineNode node) {

        int max = 0;

        for (int i = 0; i < node.getChildCount(); i++) {
            OutlineNode child = (OutlineNode) node.getChildAt(i);
            int childDepth = 1 + getRelativeDepth(child);
            if (childDepth > max) {
                max = childDepth;
            }
        }

        return max;
    }

    /**
     * Klappt alle Knoten im Outliner auf.
     */
    public void expandAll() {

        commitEditing();

        for (int i = 0; i < tree.getRowCount(); i++) {
            tree.expandRow(i);
        }
    }

    /**
     * Klappt den ausgewählten Knoten auf.
     */
    public void expandNode() {

        commitEditing();

        OutlineNode node = getSelectedNode();
        if (node == null) return;

        TreePath path = new TreePath(treeModel.getPathToRoot(node));
        tree.expandPath(path);
    }

    /**
     * Klappt einen Knoten und seine Kinder auf
     * @param node Knoten dessen Kinder aufgeklappt werden sollen
     */
    public void expandSubtree(OutlineNode node) {

        if (node == null) {
            return;
        }

        TreePath path = new TreePath(treeModel.getPathToRoot(node));
        tree.expandPath(path);

        for (int i = 0; i < node.getChildCount(); i++) {
            expandSubtree((OutlineNode) node.getChildAt(i));
        }
    }

    /**
     * Klappt alle Knoten im Outliner zu.
     */
    public void collapseAll() {

        commitEditing();

        for (int i = 0; i < tree.getRowCount(); i++) {
            tree.collapseRow(i);
        }
    }

    /**
     * Klappt den ausgewählten Knoten ein.
     */
    public void collapseNode() {

        commitEditing();

        OutlineNode selected = getSelectedNode();
        if (selected == null) return;

        TreePath path = new TreePath(treeModel.getPathToRoot(selected));
        tree.collapsePath(path);
    }

    /**
     * Ermittelt die ausgewählten Knoten
     * @return {@code selectedNode} wenn ein Knoten ausgewählt wurde, ansonsten {@code null}
     */
    public OutlineNode getSelectedNode() {

        TreePath path = tree.getSelectionPath();

        if (path == null) {
            return null;
        }

        return (OutlineNode) path.getLastPathComponent();
    }

    /**
     * Wählt einen Knoten aus.
     * @param node Der Knoten der ausgewählt werden soll
     */
    public void selectNode(OutlineNode node) {

        commitEditing();

        TreePath path = new TreePath(treeModel.getPathToRoot(node));

        tree.setSelectionPath(path);
        tree.scrollPathToVisible(path);
    }

    /**
     * Wählt mehrere Knoten aus.
     * @param nodes Liste der Knoten die ausgewählt werden sollen
     */
    public void selectNodes(List<OutlineNode> nodes) {

        commitEditing();

        if (nodes.isEmpty()) {
            return;
        }

        TreePath[] paths = new TreePath[nodes.size()];

        for (int i = 0; i < nodes.size(); i++) {
            paths[i] = new TreePath(treeModel.getPathToRoot(nodes.get(i)));
        }

        tree.setSelectionPaths(paths);

        // Auf den letzten wiederhergestellten Knoten scrollen, damit er sichtbar ist
        tree.scrollPathToVisible(paths[paths.length - 1]);
    }

    /**
     *
     * @param node
     */
    public void startEditing(OutlineNode node) {

        if (tree.isEditing()) {
            tree.getCellEditor().cancelCellEditing();
        }

        TreePath path = new TreePath(treeModel.getPathToRoot(node));

        tree.setSelectionPath(path);

        SwingUtilities.invokeLater(() -> tree.startEditingAtPath(path));
    }

    /**
     * Entfernt einen Knoten aus dem Outliner und ruft {@link #markDirty()} auf um einen ungespeicherten Zustand zu markieren.
     * @param node Knoten der entfernt werden soll
     */
    public void deleteNode(OutlineNode node) {

        commitEditing();

        treeModel.removeNodeFromParent(node);

        textWindow.loadNode(null);

        markDirty();
    }

    /**
     * Ermittelt alle markieren Knoten im Outliner.
     * @return {@code java.util.List<OutlineNode>} mit allen markieren Knoten
     */
    public java.util.List<OutlineNode> getSelectedNodes() {

        TreePath[] paths = tree.getSelectionPaths();

        if (paths == null) {
            return java.util.Collections.emptyList();
        }

        java.util.List<OutlineNode> nodes = new ArrayList<>();

        for (TreePath path : paths) {
            nodes.add((OutlineNode) path.getLastPathComponent());
        }

        return nodes;
    }

    /**
     * Entfernt mehrere Knoten aus dem Outliner und ruft {@link #markDirty()} auf um einen ungespeicherten Zustand zu markieren.
     * @param nodesToDelete Knoten die aus dem Outliner entfernt werden sollen.
     */
    public void deleteNodes(java.util.List<OutlineNode> nodesToDelete) {

        if (nodesToDelete.isEmpty()) {
            return;
        }

        for (OutlineNode node : nodesToDelete) {
            treeModel.removeNodeFromParent(node);
        }

        textWindow.loadNode(null);

        markDirty();
    }

    /**
     * Fügt einen neuen Knoten im Outliner hinzu.
     * @param parent Eltern Knoten unter dem der neue Knoten eingefügt werden soll.
     * @param node Knoten der eingefügt werden soll.
     * @param index Index an dem der neue Knoten eingefügt werden soll.
     */
    public void insertNode(OutlineNode parent,
                           OutlineNode node,
                           int index) {

        commitEditing();

        treeModel.insertNodeInto(node, parent, index);

        selectNode(node);

        markDirty();
    }

    /**
     * Setzt die {@link #dirty} flag auf True um einen ungespeicherten Zustand zu markieren.
     * Fügt vorm Titel des Outliners ein * ein, um diesen Zustand zu markieren (siehe {@link #updateTitle()}).
     */
    public void markDirty() {
        dirty = true;
        updateTitle();
        fireUpdate();
    }

    /**
     * @return {@code true} wenn es ungespeicherte Elemente gibt, ansonsten {@code false}
     */
    public boolean isDirty() {
        return dirty;
    }

    /**
     * Setzt die {@link #dirty} flag auf false und aktualisiert den Titel des Outliners (siehe {@link #updateTitle()}).
     */
    public void saved() {
        dirty = false;
        updateTitle();
    }

    /**
     * Fügt dem Titel ein * hinzu, wenn {@link #dirty} true ist, ansonsten wird der * entfernt.
     */
    private void updateTitle() {
        mainWindow.setTitle((dirty ? "* " : "") + "JOutlinr");
    }

    /**
     *
     */
    public void cancelActiveEditing() {
        if (tree.isEditing()) {
            tree.getCellEditor().cancelCellEditing();
        }
    }

    public void setSelectLastEntryOption(boolean isSelected){
        this.selectLastEntryOption = isSelected;
    }

    public boolean getSelectLastEntryOption(){
        return this.selectLastEntryOption;
    }

    /**
     * @return
     */
    public ColumnHeader getTreeHeader(){
        return outlinePanel.getHeaderPanel().getTreeHeader();
    }

    /**
     *
     * @return
     */
    public List<ColumnHeader> getHeaderList(){
        return outlinePanel.getHeaderPanel().getHeaders();
    }

    public List<OutlineColumn> getColumns(){
        return outlinePanel.getColumnPanel().getColumns();
    }

    public void syncColumns(OutlineNode[] nodes, int rowHeight){
        outlinePanel.getColumnPanel().syncWithTree(nodes, rowHeight);
    }

    /**
     * Entfernt alle einträge aus dem Outliner.
     */
    public void deleteAllColumns() {
        List<OutlineColumn> columns = getColumns();
        int columnSize = columns.size();
        for (int i = columnSize - 1; i >= 0; i--) {
            outlinePanel.removeColumn(columns.get(i));
        }
        outlinePanel.getHeaderPanel().clearColumnHeaders();
    }

    /**
     *
     */
    public void commitEditing() {
        JTree tree = outlinePanel.getTree();

        if (tree.isEditing()) {
            tree.stopEditing();
        }
    }

    /**
     * Setzt die Überschrift des Dokuments.
     * @param title Überschrift des Dokuments
     */
    public void setHeaderTitle(String title) {outlinePanel.getHeaderPanel().getTreeHeader().setTitle(title); }

    public OutlineNode getRootNode() {
        return rootNode;
    }

    public DefaultTreeModel getTreeModel() {
        return treeModel;
    }

    public String getHeaderTitle() {return outlinePanel.getHeaderPanel().getTreeHeader().getTitle(); }

    public List<ColumnHeader> getSpaltenNamen() {return outlinePanel.getHeaderPanel().getHeaders(); }

    public List<OutlineColumn> getSpalten() {return outlinePanel.getColumnPanel().getColumns();}

    public JTree getTree(){
        return this.tree;
    }
}
