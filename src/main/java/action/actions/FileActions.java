package action.actions;

import action.file.*;
import controller.FileController;

import javax.swing.*;

public class FileActions {

    public final Action closeAction;
    public final Action newFileAction;
    public final Action openFileAction;
    public final Action saveAsFileAction;
    public final Action saveFileAction;
    public final Action exportAsLatexAction;

    public FileActions(FileController fileController) {
        closeAction = new CloseAction(fileController);
        newFileAction = new NewFileAction(fileController);
        openFileAction = new OpenFileAction(fileController);
        saveAsFileAction = new SaveAsFileAction(fileController);
        saveFileAction = new SaveFileAction(fileController);
        exportAsLatexAction = new ExportAsLatexAction(fileController);
    }
}
