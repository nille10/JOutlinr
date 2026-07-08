package controller;

import javax.swing.*;

public class FileController {

    private final JFrame jFrame;

    public FileController(JFrame jFrame) {
        this.jFrame = jFrame;
    }

    // Platzhalter
    public void newDocument()    { JOptionPane.showMessageDialog(jFrame, "Neu – noch nicht implementiert.");            }
    public void openDocument()   { JOptionPane.showMessageDialog(jFrame, "Öffnen – noch nicht implementiert.");         }
    public void saveDocument()   { JOptionPane.showMessageDialog(jFrame, "Speichern – noch nicht implementiert.");      }
    public void saveDocumentAs() { JOptionPane.showMessageDialog(jFrame, "Speichern unter – noch nicht implementiert.");}
}
