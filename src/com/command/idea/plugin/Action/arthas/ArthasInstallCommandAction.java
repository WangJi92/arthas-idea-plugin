package com.command.idea.plugin.Action.arthas;

import com.command.idea.plugin.constants.ArthasCommandConstants;
import com.command.idea.plugin.utils.ClipboardUtils;
import com.command.idea.plugin.utils.NotifyUtils;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

/**
 * 安装脚本处理
 *
 * @author jet
 * @date 22-12-2019
 */
public class ArthasInstallCommandAction extends AnAction {
    @Override
    public void actionPerformed(@NotNull AnActionEvent event) {
        DataContext dataContext = event.getDataContext();
        Project project = CommonDataKeys.PROJECT.getData(dataContext);
        if (project == null) {
            return;
        }
        ClipboardUtils.setClipboardString(ArthasCommandConstants.AS_INSTALL_BASH);
        NotifyUtils.notifyMessageDefault(project);

    }
}
