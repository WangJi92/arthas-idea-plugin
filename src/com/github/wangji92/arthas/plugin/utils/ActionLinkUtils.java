package com.github.wangji92.arthas.plugin.utils;

import com.intellij.icons.AllIcons;
import com.intellij.ide.BrowserUtil;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.ui.components.labels.ActionLink;

/**
 * 构造 链接
 *
 * @author 汪小哥
 * @date 04-05-2021
 */
public class ActionLinkUtils {

    /**
     * 构造链接工具类
     *
     * @param linkUrl
     * @return
     */
    public static ActionLink newActionLink(String linkUrl) {
        ActionLink link = new ActionLink("", AllIcons.Ide.Link, new AnAction() {
            @Override
            public void actionPerformed(AnActionEvent anActionEvent) {
                BrowserUtil.browse(linkUrl);
            }
        });
        link.setPaintUnderline(false);
        return link;
    }
}
