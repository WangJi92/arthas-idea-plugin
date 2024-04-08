package com.github.wangji92.arthas.plugin.action.arthas;

import com.github.wangji92.arthas.plugin.ui.ArthasAsyncProfileDialog;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

/**
 * 支持火焰图 async profiler https://arthas.aliyun.com/doc/profiler.html
 * https://wangji.blog.csdn.net/article/details/106934179
 * @author 汪小哥
 * @date 23-06-2020
 */
public class ArthasAsyncProfilerCommandAction extends AnAction {
    @Override
    public void actionPerformed(@NotNull AnActionEvent event) {
        DataContext dataContext = event.getDataContext();
        Project project = CommonDataKeys.PROJECT.getData(dataContext);
        if (project == null) {
            return;
        }
        new ArthasAsyncProfileDialog(project).open();
    }

    @Override
    public @NotNull ActionUpdateThread getActionUpdateThread() {
        return ActionUpdateThread.EDT;
    }
}
