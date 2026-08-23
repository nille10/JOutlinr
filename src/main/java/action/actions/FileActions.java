package action.actions;

import action.actions.file.*;
import controller.EditController;
import controller.FileController;

import javax.swing.*;

/**
 * Fasst alle File Actions (siehe {@link action.actions.file}) zusammen.
 */
public class FileActions {

    public final Action closeAction;
    public final Action newFileAction;
    public final Action openFileAction;
    public final Action saveAsFileAction;
    public final Action saveFileAction;
    public final Action exportAsLatexAction;

    /**
     * Erstellt alle FileActions und initialisiert diese mit einem {@link FileController}.
     * @param fileController FileController vom Outliner, darf nicht {@code null} sein.
     */
    public FileActions(FileController fileController) {
        closeAction = new CloseAction(fileController);
        newFileAction = new NewFileAction(fileController);
        openFileAction = new OpenFileAction(fileController);
        saveAsFileAction = new SaveAsFileAction(fileController);
        saveFileAction = new SaveFileAction(fileController);
        exportAsLatexAction = new ExportAsLatexAction(fileController);
    }
}
