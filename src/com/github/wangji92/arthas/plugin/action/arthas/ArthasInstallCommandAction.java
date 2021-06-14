package com.github.wangji92.arthas.plugin.action.arthas;

import com.github.wangji92.arthas.plugin.constants.ArthasCommandConstants;
import com.github.wangji92.arthas.plugin.utils.ClipboardUtils;
import com.github.wangji92.arthas.plugin.utils.NotifyUtils;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

/**
 * 安装脚本处理
 *
 * @author 汪小哥
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
        NotifyUtils.notifyMessage(project, "安装脚本 as.sh 启动 使用alias 实现,原理为下载arthas-boot 包 通过 java -jar ~/.arthas-boot.jar --repo-mirror aliyun --use-http启动 arthas");

    }
}
