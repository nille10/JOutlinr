package controller;

import javax.swing.*;

public class EditController {

    private final JFrame jFrame;

    public EditController(JFrame jFrame) {
        this.jFrame = jFrame;
    }

    // Platzhalter
    public void undo()           { JOptionPane.showMessageDialog(jFrame, "Rückgängig – noch nicht implementiert.");     }
    public void redo()           { JOptionPane.showMessageDialog(jFrame, "Wiederholen – noch nicht implementiert.");    }
    public void cut()            { JOptionPane.showMessageDialog(jFrame, "Ausschneiden – noch nicht implementiert.");   }
    public void copy()           { JOptionPane.showMessageDialog(jFrame, "Kopieren – noch nicht implementiert.");       }
    public void insert()          { JOptionPane.showMessageDialog(jFrame, "Einfügen – noch nicht implementiert.");       }
}
