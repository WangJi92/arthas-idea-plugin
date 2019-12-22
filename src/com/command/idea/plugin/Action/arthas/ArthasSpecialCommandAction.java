package com.command.idea.plugin.Action.arthas;

import com.command.idea.plugin.ui.ArthasSpecialDialog;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

/**
 * 特殊用法
 *
 * @author jet
 * @date 22-12-2019
 */
public class ArthasSpecialCommandAction extends AnAction {


    @Override
    public void actionPerformed(@NotNull AnActionEvent anActionEvent) {
        DataContext dataContext = anActionEvent.getDataContext();
        Project project = (Project) CommonDataKeys.PROJECT.getData(dataContext);
        if (project == null) {
            return;
        }
        new ArthasSpecialDialog(project).open();
    }
}
