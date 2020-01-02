package com.github.wangji92.arthas.plugin.ui;

import com.intellij.icons.AllIcons;
import com.intellij.ide.BrowserUtil;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.WindowManager;
import com.intellij.ui.components.labels.ActionLink;
import com.intellij.ui.components.labels.LinkLabel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/**
 * arthas 特殊用法
 *
 * @author jet
 * @date 22-12-2019
 */
public class ArthasSpecialDialog extends JDialog {
    private LinkLabel specialOgnlLinkLable;
    private LinkLabel ttGetSpringContextLinkLabel;
    private LinkLabel errorFilterLinkLable;
    private LinkLabel dubboArthasLinkLable;
    private LinkLabel redfineLinLable;
    private JPanel contentPane;
    private LinkLabel ognlUseLinkLable;

    private Project project;

    public ArthasSpecialDialog(Project project) {
        this.project = project;
        setContentPane(this.contentPane);
        setModal(false);

        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                onCancel();
            }
        });

        // call onCancel() on ESCAPE
        contentPane.registerKeyboardAction(e -> onCancel(), KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);


    }

    /**
     * 关闭
     */
    private void onCancel() {
        dispose();
    }

    private void createUIComponents() {
        specialOgnlLinkLable = new ActionLink("", AllIcons.Ide.Link, new AnAction() {
            @Override
            public void actionPerformed(AnActionEvent anActionEvent) {
                BrowserUtil.browse("https://github.com/alibaba/arthas/issues/71");
            }
        });
        specialOgnlLinkLable.setPaintUnderline(false);
        ttGetSpringContextLinkLabel = new ActionLink("", AllIcons.Ide.Link, new AnAction() {
            @Override
            public void actionPerformed(AnActionEvent anActionEvent) {
                BrowserUtil.browse("https://github.com/alibaba/arthas/issues/482");
            }
        });
        ttGetSpringContextLinkLabel.setPaintUnderline(false);
        errorFilterLinkLable = new ActionLink("", AllIcons.Ide.Link, new AnAction() {
            @Override
            public void actionPerformed(AnActionEvent anActionEvent) {
                BrowserUtil.browse("https://github.com/alibaba/arthas/issues/429");
            }
        });
        errorFilterLinkLable.setPaintUnderline(false);
        dubboArthasLinkLable = new ActionLink("", AllIcons.Ide.Link, new AnAction() {
            @Override
            public void actionPerformed(AnActionEvent anActionEvent) {
                BrowserUtil.browse("http://hengyunabc.github.io/dubbo-meet-arthas/");
            }
        });
        dubboArthasLinkLable.setPaintUnderline(false);

        redfineLinLable = new ActionLink("", AllIcons.Ide.Link, new AnAction() {
            @Override
            public void actionPerformed(AnActionEvent anActionEvent) {
                BrowserUtil.browse("http://hengyunabc.github.io/arthas-online-hotswap/");
            }
        });
        redfineLinLable.setPaintUnderline(false);
        ognlUseLinkLable = new ActionLink("", AllIcons.Ide.Link, new AnAction() {
            @Override
            public void actionPerformed(AnActionEvent anActionEvent) {
                BrowserUtil.browse("https://blog.csdn.net/u010634066/article/details/101013479");
            }
        });
        ognlUseLinkLable.setPaintUnderline(false);
    }

    /**
     * 打开窗口
     */
    public void open() {
        setTitle("arthas special use");
        pack();
        setMinimumSize(new Dimension(404,300));
        //两个屏幕处理出现问题，跳到主屏幕去了 https://blog.csdn.net/weixin_33919941/article/details/88129513
        setLocationRelativeTo(WindowManager.getInstance().getFrame(this.project));
        setVisible(true);
    }
}
