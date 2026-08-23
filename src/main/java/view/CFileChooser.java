package view;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.io.File;

public class CFileChooser extends JFileChooser {

    public CFileChooser(String dialogTitle, FileNameExtensionFilter... filters) {
        this.setDialogTitle(dialogTitle);
        for (FileNameExtensionFilter filter : filters)
            this.addChoosableFileFilter(filter);
        this.setAcceptAllFileFilterUsed(false);
    }

    public int getSaveResult(JFrame mainFrame) {
        return this.showSaveDialog(mainFrame);
    }

    public int getOpenResult(JFrame mainFrame) {return this.showOpenDialog(mainFrame); }

    public File getFile(){
        return this.getSelectedFile();
    }
}
