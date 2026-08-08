package view;

import model.OutlineNode;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class TextWindow extends JPanel {

  private final JTextArea textArea;
  private final JLabel nodeLabel;
  private OutlineNode currentNode;
  private Runnable onContentChanged;

  public TextWindow() {
    setLayout(new BorderLayout(0, 4));
    setBorder(new EmptyBorder(6, 8, 8, 8));

    JPanel header = new JPanel(new BorderLayout());
    header.setOpaque(false);

    nodeLabel = new JLabel("Kein Element ausgewählt");
    nodeLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
    nodeLabel.setBorder(new EmptyBorder(0, 0, 4, 0));
    header.add(nodeLabel, BorderLayout.WEST);
    add(header, BorderLayout.NORTH);

    textArea = new JTextArea();
    textArea.setFont(new Font("Segoe UI", Font.PLAIN, 13));
    textArea.setLineWrap(true);
    textArea.setWrapStyleWord(true);
    textArea.setEnabled(false);
    textArea.setMargin(new Insets(8, 8, 8, 8));

    textArea.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
      public void insertUpdate(javax.swing.event.DocumentEvent e)  { saveToNode(); }
      public void removeUpdate(javax.swing.event.DocumentEvent e)  { saveToNode(); }
      public void changedUpdate(javax.swing.event.DocumentEvent e) { saveToNode(); }
    });

    JScrollPane scrollPane = new JScrollPane(textArea);
    scrollPane.setBorder(BorderFactory.createEmptyBorder());
    add(scrollPane, BorderLayout.CENTER);
  }

  /** Wird aufgerufen wenn sich der Content eines Nodes ändert. */
  public void setOnContentChanged(Runnable callback) {
    this.onContentChanged = callback;
  }

  public void loadNode(OutlineNode node) {
    currentNode = node;
    if (node == null) {
      textArea.setText("");
      textArea.setEnabled(false);
      nodeLabel.setText("Kein Element ausgewählt");
    } else {
      textArea.setEnabled(true);
      textArea.setText(node.getContent());
      textArea.setCaretPosition(0);
      nodeLabel.setText("✏  " + node.getTitle());
    }
  }

  private void saveToNode() {
    if (currentNode != null) {
      currentNode.setContent(textArea.getText());
      if (onContentChanged != null) onContentChanged.run();
    }
  }
}