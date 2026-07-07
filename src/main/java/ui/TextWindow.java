package ui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * TextWindow zeigt den Textinhalt des aktuell gewählten Outline-Knotens an.
 * Es befindet sich unten im MainWindow und erlaubt das Bearbeiten von Notizen.
 */
public class TextWindow extends JPanel {

  private final JTextArea textArea;
  private final JLabel nodeLabel;
  private OutlineNode currentNode;

  public TextWindow() {
    setLayout(new BorderLayout(0, 4));
    setBorder(new EmptyBorder(6, 8, 8, 8));
    setBackground(new Color(245, 245, 248));

    // Header mit Knotenname
    JPanel header = new JPanel(new BorderLayout());
    header.setOpaque(false);

    nodeLabel = new JLabel("Kein Element ausgewählt");
    nodeLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
    nodeLabel.setForeground(new Color(100, 100, 120));
    nodeLabel.setBorder(new EmptyBorder(0, 0, 4, 0));
    header.add(nodeLabel, BorderLayout.WEST);

    add(header, BorderLayout.NORTH);

    // Textbereich
    textArea = new JTextArea();
    textArea.setFont(new Font("Segoe UI", Font.PLAIN, 13));
    textArea.setLineWrap(true);
    textArea.setWrapStyleWord(true);
    textArea.setEnabled(false);
    textArea.setBackground(Color.WHITE);
    textArea.setForeground(new Color(30, 30, 40));
    textArea.setCaretColor(new Color(60, 80, 200));
    textArea.setMargin(new Insets(8, 8, 8, 8));
    textArea.setDisabledTextColor(new Color(150, 150, 160));

    // Änderungen sofort im Knoten speichern
    textArea.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
      public void insertUpdate(javax.swing.event.DocumentEvent e)  { saveToNode(); }
      public void removeUpdate(javax.swing.event.DocumentEvent e)  { saveToNode(); }
      public void changedUpdate(javax.swing.event.DocumentEvent e) { saveToNode(); }
    });

    JScrollPane scrollPane = new JScrollPane(textArea);
    scrollPane.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 210)));
    scrollPane.setBackground(Color.WHITE);

    add(scrollPane, BorderLayout.CENTER);
  }

  /**
   * Lädt den Inhalt eines Knotens in den Textbereich.
   */
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

  /**
   * Speichert den aktuellen Text zurück in den Knoten.
   */
  private void saveToNode() {
    if (currentNode != null) {
      currentNode.setContent(textArea.getText());
    }
  }

  public JTextArea getTextArea() {
    return textArea;
  }
}