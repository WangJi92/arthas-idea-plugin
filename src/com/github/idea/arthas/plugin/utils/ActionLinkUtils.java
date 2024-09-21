package com.github.idea.arthas.plugin.utils;

import com.intellij.ide.BrowserUtil;
import com.intellij.ui.components.ActionLink;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;


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
        return new ActionLink(linkUrl, new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent e) {
                BrowserUtil.browse(linkUrl);
            }
        } );

        //

    }
}
