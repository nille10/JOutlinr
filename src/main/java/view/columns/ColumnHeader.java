package view.columns;

import javax.swing.*;
import javax.swing.border.MatteBorder;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * Einheitlicher Header für Tree- und Datenspalten.
 *
 * Eigenschaften:
 * - Sieht wie ein Tabellenheader aus.
 * - Doppelklick startet die Bearbeitung.
 * - Enter oder Fokusverlust beendet die Bearbeitung.
 */
public class ColumnHeader extends JPanel {

  public static final int HEADER_HEIGHT = 28;

  private final JTextField textField;

  public ColumnHeader(String title) {

    setLayout(new BorderLayout());

    setPreferredSize(new Dimension(0, HEADER_HEIGHT));
    setMinimumSize(new Dimension(0, HEADER_HEIGHT));
    setMaximumSize(new Dimension(Integer.MAX_VALUE, HEADER_HEIGHT));

    setBackground(new Color(235, 235, 240));

    setBorder(new MatteBorder(
        0,
        1,
        1,
        0,
        new Color(205, 205, 215)));

    textField = new JTextField(title);

    textField.setEditable(false);
    textField.setBorder(BorderFactory.createEmptyBorder(2, 8, 2, 8));
    textField.setOpaque(false);
    textField.setBackground(getBackground());
    textField.setFont(new Font("Segoe UI", Font.BOLD, 12));
    textField.setForeground(new Color(70, 70, 90));

    add(textField, BorderLayout.CENTER);

    installEditingBehaviour();
  }

  private void installEditingBehaviour() {

    textField.addMouseListener(new MouseAdapter() {

      @Override
      public void mouseClicked(MouseEvent e) {

        if (e.getClickCount() == 2) {
          startEditing();
        }

      }

    });

    textField.addActionListener(e -> stopEditing());

    textField.addFocusListener(new FocusAdapter() {

      @Override
      public void focusLost(FocusEvent e) {
        stopEditing();
      }

    });
  }

  private void startEditing() {

    textField.setEditable(true);
    textField.requestFocusInWindow();
    textField.selectAll();

  }

  private void stopEditing() {

    textField.setEditable(false);

  }

  // --------------------------------------------------

  public String getTitle() {
    return textField.getText();
  }

  public void setTitle(String title) {
    textField.setText(title);
  }

  public JTextField getTextField() {
    return textField;
  }
}