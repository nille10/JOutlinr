package view;

import model.OutlineNode;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * Notizbereich für den Inhalt des aktuell ausgewählten Baumknotens.
 *
 * <p>Zeigt den Titel des ausgewählten Knotens als Überschrift und seinen Inhalt
 * in einem mehrzeiligen Textfeld an. Jede Texteingabe wird unmittelbar in den
 * zugehörigen {@link OutlineNode} zurückgeschrieben ({@link #saveToNode()}) und
 * danach über {@link #onContentChanged} gemeldet, damit z. B. der Baum neu
 * gezeichnet werden kann. Ist kein Knoten geladen, ist das Textfeld deaktiviert.</p>
 */
public class TextWindow extends JPanel {

  /** Das Textfeld für den Inhalt des aktuell geladenen Knotens. */
  private final JTextArea textArea;

  /** Zeigt den Titel des aktuell geladenen Knotens an, oder einen Platzhaltertext. */
  private final JLabel nodeLabel;

  /** Der aktuell geladene Knoten, oder {@code null}, wenn keiner ausgewählt ist. */
  private OutlineNode currentNode;

  /** Callback, der nach jeder Inhaltsänderung des aktuellen Knotens aufgerufen wird. */
  private Runnable onContentChanged;

  /**
   * Baut den Notizbereich auf: Kopfzeile mit Titel-Label und darunter ein
   * scrollbares, zeilenumbrechendes Textfeld, das zunächst deaktiviert ist, da
   * noch kein Knoten geladen wurde.
   */
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

  /**
   * Setzt den Callback, der nach jeder Inhaltsänderung des aktuellen Knotens
   * aufgerufen wird.
   *
   * <p>Der Callback trägt keine Information darüber, was sich geändert hat —
   * zu diesem Zeitpunkt wurde der neue Text bereits in den {@link OutlineNode}
   * geschrieben; der Callback dient nur als Signal, z. B. um den Baum neu zu
   * zeichnen.</p>
   *
   * @param callback der auszuführende Callback, oder {@code null}
   */
  public void setOnContentChanged(Runnable callback) {
    this.onContentChanged = callback;
  }

  /**
   * Lädt einen Knoten in den Notizbereich.
   *
   * <p>Bei {@code null} wird das Textfeld geleert und deaktiviert und der
   * Platzhaltertext angezeigt. Andernfalls wird das Textfeld aktiviert, mit dem
   * Inhalt des Knotens befüllt, der Cursor an den Anfang gesetzt und der Titel
   * des Knotens (mit einem Stift-Symbol) als Überschrift angezeigt.</p>
   *
   * @param node der zu ladende Knoten, oder {@code null}, um den Bereich zu leeren
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
   * Schreibt den aktuellen Text des Textfelds in {@link #currentNode} und ruft
   * danach {@link #onContentChanged}, sofern gesetzt. Ist kein Knoten geladen,
   * geschieht nichts.
   */
  private void saveToNode() {
    if (currentNode != null) {
      currentNode.setContent(textArea.getText());
      if (onContentChanged != null) onContentChanged.run();
    }
  }
}