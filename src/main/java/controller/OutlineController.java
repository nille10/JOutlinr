package controller;

import model.OutlineNode;
import model.columns.Column;
import model.columns.ColumnType;
import view.TextWindow;
import view.OutlinePanel;
import view.columns.ColumnHeader;

import javax.swing.*;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Controller für die Interaktion mit der Datenstruktur des Outliners zuständig.
 * <p>
 *     Ist zuständig für die manipulation des JTrees. Operationen welche eine Undo funktion benötigen werden direkt über die {@link action.actions.commands} gesteuert.
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

    private boolean isSelected = true;

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
     * Registriert die Listener für Auswahländerungen im Baum (lädt den
     * ausgewählten Knoten ins {@link TextWindow} und benachrichtigt
     * {@link #updateListeners}) sowie für Baum-Änderungen (Knoten umbenannt,
     * eingefügt, entfernt, Struktur geändert), die jeweils {@link #markDirty()}
     * auslösen. Beim Umbenennen wird zusätzlich der Titel des geänderten
     * Knotens aus dem {@code userObject} übernommen und der Baum neu gezeichnet.
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
     * Überprüft, ob unter der ausgewählten Outline Node eine weitere Node
     * hinzugefügt werden kann, ohne das maximale Level zu überschreiten.
     * Ist keine Node ausgewählt, wird die Root Node als übergeordnete Node verwendet.
     *
     * @return {@code true}, wenn eine weitere Node hinzugefügt werden kann,
     *         andernfalls {@code false}
     */
    public boolean canAddChildToSelected() {
        OutlineNode selected = getSelectedNode();
        OutlineNode parent = selected != null ? selected : rootNode;

        return parent == null || parent.getLevel() < MAX_LEVEL;
    }

    /**
     * Registriert einen Listener, der bei jeder Statusänderung (z. B.
     * Baum-Auswahl) benachrichtigt wird.
     *
     * @param listener der zu registrierende Listener; wird sofort einmal
     *                  aufgerufen, um den aktuellen Zustand zu übernehmen
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
     *
     * @param node der Wurzelknoten des zu durchsuchenden Teilbaums
     * @return die tiefste gefundene Ebene innerhalb des Teilbaums
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
     *
     * @param node der Wurzelknoten des zu vermessenden Teilbaums
     * @return die relative Tiefe (0, wenn der Knoten keine Kinder hat)
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
     * Startet die Inline-Bearbeitung des angegebenen Knotens im Baum.
     *
     * <p>Eine eventuell laufende Bearbeitung wird zuerst abgebrochen (verworfen,
     * siehe {@link #cancelActiveEditing()}). Der Knoten wird ausgewählt und die
     * Bearbeitung über {@link SwingUtilities#invokeLater(Runnable)} gestartet,
     * damit die Auswahl zuvor vollständig verarbeitet ist.</p>
     *
     * @param node der zu bearbeitende Knoten
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
     * Sammelt einen Knoten und alle seine Nachkommen (Kinder, Enkel, usw.) in
     * eine flache Liste ein.
     *
     * <p>Wird beim Löschen verwendet, damit beim Entfernen eines Teilbaums
     * nicht nur der Wurzelknoten des Teilbaums, sondern auch alle
     * Nachkommen beim Aufräumen der Spaltenwerte berücksichtigt werden (siehe
     * {@link #deleteNode(OutlineNode)}, {@link #deleteNodes(List)}). Ohne das
     * blieben Spaltenwerte von Enkelknoten nach dem Löschen eines größeren
     * Teilbaums als tote Einträge in den Spalten-Werte-Maps zurück.</p>
     *
     * @param node der Wurzelknoten des einzusammelnden Teilbaums
     * @return eine neue Liste mit {@code node} selbst und all seinen Nachkommen,
     *         in Vorordnung (Knoten vor seinen Kindern)
     */
    private List<OutlineNode> collectSubtree(OutlineNode node) {
        List<OutlineNode> nodes = new ArrayList<>();
        collectSubtree(node, nodes);
        return nodes;
    }

    /**
     * Rekursiver Helfer für {@link #collectSubtree(OutlineNode)}: fügt
     * {@code node} und alle seine Nachkommen in {@code collector} ein.
     *
     * @param node      der aktuell zu verarbeitende Knoten
     * @param collector die Liste, in die eingesammelt wird
     */
    private void collectSubtree(OutlineNode node, List<OutlineNode> collector) {
        collector.add(node);
        for (int i = 0; i < node.getChildCount(); i++) {
            collectSubtree((OutlineNode) node.getChildAt(i), collector);
        }
    }

    /**
     * Entfernt einen Knoten aus dem Outliner und ruft {@link #markDirty()} auf um einen ungespeicherten Zustand zu markieren.
     *
     * <p>Bugfix-Historie: Räumt die Spaltenwerte des Knotens hier bewusst
     * <strong>nicht</strong> auf. Diese Methode wird u. a. von
     * {@code DeleteEntryCommand} aufgerufen, dessen Löschen über
     * {@link action.manager.UndoManager} jederzeit rückgängig gemacht werden
     * kann ({@code undo()} fügt denselben {@link OutlineNode} unverändert
     * wieder ein). Ein sofortiges Aufräumen hier würde die Spaltenwerte schon
     * beim Löschen entfernen, bevor feststeht, ob der Knoten per Undo
     * zurückkommt — nach einem Undo wären die Werte dann unwiederbringlich
     * weg, obwohl der Knoten selbst wiederhergestellt wurde. Echtes,
     * endgültiges Löschen von Spaltenwerten für nie wiederhergestellte Knoten
     * ist aktuell nicht implementiert (möglicher künftiger Ansatz: Aufräumen
     * erst dann, wenn eine {@code DeleteEntryCommand} endgültig vom
     * Undo/Redo-Stack verschwindet, siehe {@link #collectSubtree(OutlineNode)}
     * für das dafür nötige Einsammeln ganzer Teilbäume).</p>
     *
     * @param node Knoten der entfernt werden soll
     */
    public void deleteNode(OutlineNode node) {

        commitEditing();

        treeModel.removeNodeFromParent(node);

        textWindow.loadNode(null);

        markDirty();
    }

    /**
     * Entfernt mehrere Knoten aus dem Outliner und ruft {@link #markDirty()} auf um einen ungespeicherten Zustand zu markieren.
     *
     * <p>Räumt die Spaltenwerte der entfernten Knoten bewusst <strong>nicht</strong>
     * auf — siehe {@link #deleteNode(OutlineNode)} für die ausführliche
     * Begründung (Löschen über diese Methode ist über
     * {@code DeleteEntryCommand} per Undo jederzeit rückgängig machbar).</p>
     *
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
     * Ersetzt die RootNode im JTree mit einer neuen.
     * <p>
     *   Entfernt alle Kindknoten des aktuellen Wurzelknoten. Passt den Titel und Content des Wurzelknoten an und fügt alle neuen Kindknoten hinzu.
     *   Lädt danach einmal das TreeModel neu, damit alles angezeigt wird
     * </p>
     * @param parsedRoot der neue Wurzelknoten
     */
    public void replaceRootContents(OutlineNode parsedRoot) {
        OutlineNode actualRoot = getRootNode();

        actualRoot.removeAllChildren();
        actualRoot.setTitle(parsedRoot.getTitle());
        actualRoot.setContent(parsedRoot.getContent());

        while (parsedRoot.getChildCount() > 0) {
            OutlineNode child = (OutlineNode) parsedRoot.getChildAt(0);
            actualRoot.add(child);
        }

        getTreeModel().reload();
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
     * Bricht die Bearbeitung des Namens einer Zeile ab, falls gerade eine läuft
     * — die bisher eingegebene Änderung wird dabei verworfen, nicht übernommen.
     * Gegenstück zu {@link #commitEditing()}, das die Änderung speichert.
     */
    public void cancelActiveEditing() {
        if (tree.isEditing()) {
            tree.getCellEditor().cancelCellEditing();
        }
    }

    /**
     * Setzt, ob nach dem Löschen eines Knotens automatisch ein anderer Knoten
     * ausgewählt werden soll.
     *
     * @param isSelected {@code true}, um die automatische Auswahl zu aktivieren
     */
    public void setSelectLastEntryOption(boolean isSelected){
        this.selectLastEntryOption = isSelected;
    }

    /**
     * Liefert, ob nach dem Löschen eines Knotens automatisch ein anderer Knoten
     * ausgewählt werden soll.
     *
     * @return {@code true}, wenn die automatische Auswahl aktiv ist
     */
    public boolean getSelectLastEntryOption(){
        return this.selectLastEntryOption;
    }

    /**
     * Liefert den Header des Baums (nicht der Zusatzspalten).
     *
     * @return der {@link ColumnHeader} des Baums
     */
    public ColumnHeader getTreeHeader(){
        return outlinePanel.getHeaderPanel().getTreeHeader();
    }

    /**
     * Liefert die Header-Komponenten aller aktuell vorhandenen Zusatzspalten.
     *
     * <p>Identisch mit {@link #getSpaltenNamen()} — beide Methoden rufen
     * denselben Code auf und geben dieselbe Liste zurück.</p>
     *
     * @return die Liste der Spalten-Header in Anzeigereihenfolge
     */
    public List<ColumnHeader> getHeaderList(){
        return outlinePanel.getHeaderPanel().getHeaders();
    }

    /**
     * Liefert alle aktuell vorhandenen Zusatzspalten.
     *
     * <p>Identisch mit {@link #getSpalten()} — beide Methoden rufen denselben
     * Code auf und geben dieselbe Liste zurück.</p>
     *
     * @return die Liste der Spalten in Anzeigereihenfolge
     */
    public List<Column> getColumns(){
        return outlinePanel.getColumnController().getColumns();
    }

    /**
     * Synchronisiert die Zeilen aller Zusatzspalten mit den übergebenen Knoten
     * des Baums.
     *
     * @param nodes     die aktuell sichtbaren Knoten des Baums in Zeilenreihenfolge
     * @param rowHeight die Zeilenhöhe des Baums in Pixeln
     */
    public void syncColumns(OutlineNode[] nodes, int rowHeight){
        outlinePanel.getColumnController().syncWithTree(nodes, rowHeight);
    }

    /**
     * Entfernt alle Einträge aus dem Outliner.
     */
    public void deleteAllColumns() {
        List<Column> columns = getColumns();
        int columnSize = columns.size();
        for (int i = columnSize - 1; i >= 0; i--) {
            outlinePanel.removeColumn(columns.get(i));
        }
        // clearColumnHeaders() entfällt: jede Spalte entfernt beim Löschen über
        // removeColumn() bereits ihren eigenen Header (HeaderPanel.columnViewRemoved
        // reagiert direkt auf ColumnController.removeColumn())
    }

    /**
     * Speichert das Bearbeiten einer Zeile, wie wenn man Enter drückt — eine
     * eventuell laufende Inline-Bearbeitung im Baum wird beendet und die
     * Änderung übernommen. Gegenstück zu {@link #cancelActiveEditing()}, das
     * die Änderung verwirft.
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

    /**
     * Liefert den (unsichtbaren) Wurzelknoten des Baums.
     *
     * @return der Wurzelknoten
     */
    public OutlineNode getRootNode() {
        return rootNode;
    }

    /**
     * Liefert das Tree-Model hinter dem Baum.
     *
     * @return das {@link DefaultTreeModel}
     */
    public DefaultTreeModel getTreeModel() {
        return treeModel;
    }

    /**
     * Liefert den aktuellen Titel des Baum-Headers.
     *
     * @return der Titel des Baum-Headers
     */
    public String getHeaderTitle() {return outlinePanel.getHeaderPanel().getTreeHeader().getTitle(); }

    /**
     * Liefert die Header-Komponenten aller aktuell vorhandenen Zusatzspalten.
     *
     * <p>Identisch mit {@link #getHeaderList()} — beide Methoden rufen
     * denselben Code auf und geben dieselbe Liste zurück. Der Name legt eine
     * Liste von reinen Spaltennamen (Strings) nahe, tatsächlich sind es aber
     * {@link ColumnHeader}-Komponenten.</p>
     *
     * @return die Liste der Spalten-Header in Anzeigereihenfolge
     */
    public List<ColumnHeader> getSpaltenNamen() {return outlinePanel.getHeaderPanel().getHeaders(); }

    /**
     * Liefert alle aktuell vorhandenen Zusatzspalten.
     *
     * <p>Identisch mit {@link #getColumns()} — beide Methoden rufen denselben
     * Code auf und geben dieselbe Liste zurück.</p>
     *
     * @return die Liste der Spalten in Anzeigereihenfolge
     */
    public List<Column> getSpalten() {return outlinePanel.getColumnController().getColumns();}

    /**
     * Liefert den Baum des Outliners.
     *
     * @return der {@link JTree}
     */
    public JTree getTree(){
        return this.tree;
    }

    /**
     * Liefert das allgemeine Auswahl-Flag ({@link #isSelected}).
     *
     * @return der aktuelle Wert des Flags
     */
    public boolean getBoolean(){
        return this.isSelected;
    }
}