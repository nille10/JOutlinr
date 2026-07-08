package view;

import action.actions.OutlineActions;
import action.outline.*;

import javax.swing.*;
import java.awt.*;

public class Toolbar extends JToolBar {

    public Toolbar(OutlineActions outlineActions) {
        this.setFloatable(false);
        this.setBackground(new Color(245, 245, 248));
        this.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(200, 200, 210)));

        JButton newEntry = toolButton(outlineActions.newEntryAction);
        newEntry.setText("+ Eintrag");
        this.add(newEntry);

        JButton newChildEntry = toolButton(outlineActions.newChildEntryAction);
        newChildEntry.setText("⤵ Untereintrag");
        this.add(newChildEntry);

        JButton delete = toolButton(outlineActions.deleteEntryAction);
        delete.setText("✕ Löschen");
        this.add(delete);

        JButton promote = toolButton(outlineActions.promoteEntryAction);
        promote.setText("◀ Promote");
        this.add(promote);

        JButton demote = toolButton(outlineActions.demoteEntryAction);
        demote.setText("▶ Demote");
        this.add(demote);

        JButton expandAll = toolButton(outlineActions.expandAllAction);
        expandAll.setText("⊞ Aufklappen");
        this.add(expandAll);

        JButton collapseAll = toolButton(outlineActions.collapseAllAction);
        collapseAll.setText("⊟ Zuklappen");
        this.add(collapseAll);
    }

    private JButton toolButton(Action action) {
        JButton btn = new JButton(action);
        btn.setFocusable(false);
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        return btn;
    }
}