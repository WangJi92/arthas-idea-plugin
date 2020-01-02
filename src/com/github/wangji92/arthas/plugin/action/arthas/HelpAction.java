package com.github.wangji92.arthas.plugin.action.arthas;

import com.intellij.ide.BrowserUtil;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import org.jetbrains.annotations.NotNull;

/**
 * 帮助文档链接
 *
 * @author jet
 * @date 23-12-2019
 */
public class HelpAction extends AnAction {
    @Override
    public void actionPerformed(@NotNull AnActionEvent anActionEvent) {
        BrowserUtil.browse("https://github.com/WangJi92/arthas-idea-plugin/blob/master/README.md");
    }
}
