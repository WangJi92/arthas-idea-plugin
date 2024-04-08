package com.github.wangji92.arthas.plugin.action.arthas;

import com.github.wangji92.arthas.plugin.ui.ArthasSpecialDialog;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

/**
 * 特殊用法
 *
 * @author 汪小哥
 * @date 22-12-2019
 */
public class ArthasSpecialCommandAction extends AnAction {


    @Override
    public void actionPerformed(@NotNull AnActionEvent anActionEvent) {
        DataContext dataContext = anActionEvent.getDataContext();
        Project project = CommonDataKeys.PROJECT.getData(dataContext);
        if (project == null) {
            return;
        }
        new ArthasSpecialDialog(project).open();
    }

    @Override
    public @NotNull ActionUpdateThread getActionUpdateThread() {
        return ActionUpdateThread.EDT;
    }
}
