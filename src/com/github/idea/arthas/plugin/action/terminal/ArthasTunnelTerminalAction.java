package com.github.idea.arthas.plugin.action.terminal;

import com.github.idea.arthas.plugin.ui.ArthasTunnelTerminalPretreatmentDialog;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;

/**
 * @author https://github.com/imyzt
 */
public class ArthasTunnelTerminalAction extends AnAction {

    @Override
    public void actionPerformed(AnActionEvent e) {
        DataContext dataContext = e.getDataContext();
        Project project = CommonDataKeys.PROJECT.getData(dataContext);
        Editor editor = e.getRequiredData(CommonDataKeys.EDITOR);
        new ArthasTunnelTerminalPretreatmentDialog(project, "dashboard", editor).open();
    }
}
