package com.github.wangji92.arthas.plugin.action.arthas;

import com.github.wangji92.arthas.plugin.ui.ArthasShellScriptCommandDialog;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;

/**
 * 直接执行脚本
 *
 * @author 汪小哥
 * @date 04-05-2021
 */
public class ArthasShellScriptCommandAction extends AnAction {
    @Override
    public void update(@NotNull AnActionEvent e) {
        super.update(e);
        DataContext dataContext = e.getDataContext();
        //获取当前事件触发时，光标所在的元素
        Project project = CommonDataKeys.PROJECT.getData(dataContext);
        if (project == null) {
            e.getPresentation().setEnabled(false);
            return;
        }
        VirtualFile[] virtualFileFiles = e.getData(CommonDataKeys.VIRTUAL_FILE_ARRAY);
        if (virtualFileFiles == null) {
            e.getPresentation().setEnabled(false);
            return;
        }
        if (virtualFileFiles.length >= 2) {
            e.getPresentation().setEnabled(false);
        }
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent event) {
        ArthasShellScriptCommandDialog dialog = new ArthasShellScriptCommandDialog(event);
        dialog.open("Quickly Get Available Command(shell command or common command)");
    }
}
