package com.github.idea.arthas.plugin.action.arthas;

import com.intellij.ide.BrowserUtil;
import com.intellij.openapi.actionSystem.ActionUpdateThread;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import org.jetbrains.annotations.NotNull;

/**
 * arthas 命令的文档的主页
 *
 * @author 汪小哥
 * @date 13-08-2020
 */
public class HelpArthasDocumentPageAction extends AnAction {
    @Override
    public void actionPerformed(@NotNull AnActionEvent anActionEvent) {
        BrowserUtil.browse("https://arthas.aliyun.com/doc/advanced-use.html");
    }

    @Override
    public @NotNull ActionUpdateThread getActionUpdateThread() {
        return ActionUpdateThread.EDT;
    }
}
