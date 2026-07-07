package ui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.tree.*;
import java.awt.*;
import java.awt.event.*;
import java.util.EventObject;

/**
 * OutlinePanel zeigt den Outline-Baum (JTree) an.
 * Hier können Einträge hinzugefügt, gelöscht und umbenannt werden.
 */
public class OutlinePanel extends JPanel {

  private final JTree tree;
  private final DefaultTreeModel treeModel;
  private final OutlineNode rootNode;
  private TextWindow textWindow;

  public OutlinePanel() {
    setLayout(new BorderLayout());
    setBackground(new Color(248, 248, 252));

    // Root-Knoten (unsichtbar)
    rootNode = new OutlineNode("Outliner");
    treeModel = new DefaultTreeModel(rootNode);

    // Beispielstruktur
    OutlineNode k1 = new OutlineNode("Kapitel 1");
    OutlineNode k2 = new OutlineNode("Kapitel 2");
    OutlineNode k1a = new OutlineNode("Abschnitt 1.1");
    OutlineNode k1b = new OutlineNode("Abschnitt 1.2");
    k1.add(k1a);
    k1.add(k1b);
    rootNode.add(k1);
    rootNode.add(k2);

    tree = new JTree(treeModel);
    tree.setRootVisible(false);
    tree.setShowsRootHandles(true);
    tree.setBackground(new Color(248, 248, 252));
    tree.setFont(new Font("Segoe UI", Font.PLAIN, 13));
    tree.setRowHeight(24);
    tree.setBorder(new EmptyBorder(4, 4, 4, 4));

    // Inline-Umbenennung per Doppelklick
    tree.setEditable(true);
    tree.setCellEditor(new DefaultTreeCellEditor(tree, (DefaultTreeCellRenderer) tree.getCellRenderer()) {
      @Override
      public boolean isCellEditable(EventObject event) {
        if (event instanceof MouseEvent) {
          return ((MouseEvent) event).getClickCount() >= 2;
        }
        return false;
      }
    });

    // Knoten-Auswahl → TextWindow aktualisieren
    tree.addTreeSelectionListener(e -> {
      OutlineNode selected = getSelectedNode();
      if (textWindow != null) {
        textWindow.loadNode(selected);
      }
    });

    // Umbenennung im Modell übernehmen
    treeModel.addTreeModelListener(new javax.swing.event.TreeModelListener() {
      public void treeNodesChanged(javax.swing.event.TreeModelEvent e) {
        OutlineNode changed = (OutlineNode) e.getTreePath().getLastPathComponent();
        Object[] children = e.getChildren();
        if (children != null && children.length > 0) {
          OutlineNode child = (OutlineNode) children[0];
          child.setTitle(child.getUserObject().toString());
          if (textWindow != null && child == textWindow.getTextArea().getClientProperty("node")) {
            textWindow.loadNode(child);
          }
        }
      }
      public void treeNodesInserted(javax.swing.event.TreeModelEvent e) {}
      public void treeNodesRemoved(javax.swing.event.TreeModelEvent e) {}
      public void treeStructureChanged(javax.swing.event.TreeModelEvent e) {}
    });

    // Alles expandieren
    for (int i = 0; i < tree.getRowCount(); i++) tree.expandRow(i);

    JScrollPane scrollPane = new JScrollPane(tree);
    scrollPane.setBorder(BorderFactory.createEmptyBorder());
    add(scrollPane, BorderLayout.CENTER);
  }

  public void setTextWindow(TextWindow textWindow) {
    this.textWindow = textWindow;
  }

  // ── Knoten-Aktionen ──────────────────────────────────────────────────────

  /** Fügt einen neuen Geschwister-Knoten nach dem ausgewählten ein. */
  public void addSibling() {
    OutlineNode selected = getSelectedNode();
    OutlineNode parent = selected != null
        ? (OutlineNode) selected.getParent()
        : rootNode;
    if (parent == null) parent = rootNode;

    OutlineNode newNode = new OutlineNode("Neuer Eintrag");
    int index = parent == rootNode || selected == null
        ? parent.getChildCount()
        : parent.getIndex(selected) + 1;
    treeModel.insertNodeInto(newNode, parent, index);
    startEditingNode(newNode);
  }

  /** Fügt einen Kind-Knoten unter dem ausgewählten ein. */
  public void addChild() {
    OutlineNode selected = getSelectedNode();
    OutlineNode parent = selected != null ? selected : rootNode;

    OutlineNode newNode = new OutlineNode("Neuer Unterknoten");
    treeModel.insertNodeInto(newNode, parent, parent.getChildCount());
    startEditingNode(newNode);
  }

  /** Löscht den ausgewählten Knoten. */
  public void deleteSelected() {
    OutlineNode selected = getSelectedNode();
    if (selected == null) return;
    int confirm = JOptionPane.showConfirmDialog(this,
        "\"" + selected.getTitle() + "\" wirklich löschen?",
        "Löschen bestätigen", JOptionPane.YES_NO_OPTION);
    if (confirm == JOptionPane.YES_OPTION) {
      treeModel.removeNodeFromParent(selected);
      if (textWindow != null) textWindow.loadNode(null);
    }
  }

  /** Knotenebene nach oben verschieben (Elterntausch). */
  public void promoteSelected() {
    OutlineNode selected = getSelectedNode();
    if (selected == null) return;
    OutlineNode parent = (OutlineNode) selected.getParent();
    if (parent == null || parent == rootNode) return;
    OutlineNode grandParent = (OutlineNode) parent.getParent();
    if (grandParent == null) return;

    int idx = grandParent.getIndex(parent) + 1;
    treeModel.removeNodeFromParent(selected);
    treeModel.insertNodeInto(selected, grandParent, idx);
    selectNode(selected);
  }

  /** Knotenebene nach unten verschieben (wird Kind des vorherigen Geschwister). */
  public void demoteSelected() {
    OutlineNode selected = getSelectedNode();
    if (selected == null) return;
    OutlineNode parent = (OutlineNode) selected.getParent();
    if (parent == null) return;
    int idx = parent.getIndex(selected);
    if (idx == 0) return;

    OutlineNode prevSibling = (OutlineNode) parent.getChildAt(idx - 1);
    treeModel.removeNodeFromParent(selected);
    treeModel.insertNodeInto(selected, prevSibling, prevSibling.getChildCount());
    selectNode(selected);
  }

  // ── Hilfsmethoden ────────────────────────────────────────────────────────

  private OutlineNode getSelectedNode() {
    TreePath path = tree.getSelectionPath();
    if (path == null) return null;
    return (OutlineNode) path.getLastPathComponent();
  }

  private void selectNode(OutlineNode node) {
    TreePath path = new TreePath(treeModel.getPathToRoot(node));
    tree.setSelectionPath(path);
    tree.scrollPathToVisible(path);
  }

  private void startEditingNode(OutlineNode node) {
    TreePath path = new TreePath(treeModel.getPathToRoot(node));
    tree.setSelectionPath(path);
    tree.scrollPathToVisible(path);
    SwingUtilities.invokeLater(() -> tree.startEditingAtPath(path));
  }

  public JTree getTree() {
    return tree;
  }

  public DefaultTreeModel getTreeModel() {
    return treeModel;
  }
}