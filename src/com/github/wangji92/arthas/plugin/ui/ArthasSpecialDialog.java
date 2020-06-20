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
 * @author 汪小哥
 * @date 22-12-2019
 */
public class ArthasSpecialDialog extends JDialog {
    private LinkLabel specialOgnlLinkLabel;
    private LinkLabel ttGetSpringContextLinkLabel;
    private LinkLabel errorFilterLinkLabel;
    private LinkLabel dubboArthasLinkLabel;
    private LinkLabel redefineLinLabel;
    private JPanel contentPane;
    private LinkLabel ognlUseLinkLabel;
    private LinkLabel userCase;
    private LinkLabel tt;
    private LinkLabel conditionArthas;
    private LinkLabel watchSpringContext;

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
        specialOgnlLinkLabel = new ActionLink("", AllIcons.Ide.Link, new AnAction() {
            @Override
            public void actionPerformed(AnActionEvent anActionEvent) {
                BrowserUtil.browse("https://github.com/alibaba/arthas/issues/71");
            }
        });
        specialOgnlLinkLabel.setPaintUnderline(false);
        ttGetSpringContextLinkLabel = new ActionLink("", AllIcons.Ide.Link, new AnAction() {
            @Override
            public void actionPerformed(AnActionEvent anActionEvent) {
                BrowserUtil.browse("https://github.com/alibaba/arthas/issues/482");
            }
        });
        ttGetSpringContextLinkLabel.setPaintUnderline(false);
        errorFilterLinkLabel = new ActionLink("", AllIcons.Ide.Link, new AnAction() {
            @Override
            public void actionPerformed(AnActionEvent anActionEvent) {
                BrowserUtil.browse("https://github.com/alibaba/arthas/issues/429");
            }
        });
        errorFilterLinkLabel.setPaintUnderline(false);
        dubboArthasLinkLabel = new ActionLink("", AllIcons.Ide.Link, new AnAction() {
            @Override
            public void actionPerformed(AnActionEvent anActionEvent) {
                BrowserUtil.browse("http://hengyunabc.github.io/dubbo-meet-arthas/");
            }
        });
        dubboArthasLinkLabel.setPaintUnderline(false);

        redefineLinLabel = new ActionLink("", AllIcons.Ide.Link, new AnAction() {
            @Override
            public void actionPerformed(AnActionEvent anActionEvent) {
                BrowserUtil.browse("http://hengyunabc.github.io/arthas-online-hotswap/");
            }
        });
        redefineLinLabel.setPaintUnderline(false);
        ognlUseLinkLabel = new ActionLink("", AllIcons.Ide.Link, new AnAction() {
            @Override
            public void actionPerformed(AnActionEvent anActionEvent) {
                BrowserUtil.browse("https://blog.csdn.net/u010634066/article/details/101013479");
            }
        });
        ognlUseLinkLabel.setPaintUnderline(false);
        userCase = new ActionLink("", AllIcons.Ide.Link, new AnAction() {
            @Override
            public void actionPerformed(AnActionEvent anActionEvent) {
                BrowserUtil.browse("https://github.com/alibaba/arthas/issues?utf8=%E2%9C%93&q=+is%3Aissue+label%3Auser-case+");
            }
        });
        userCase.setPaintUnderline(false);
        conditionArthas = new ActionLink("", AllIcons.Ide.Link, new AnAction() {
            @Override
            public void actionPerformed(AnActionEvent anActionEvent) {
                BrowserUtil.browse("https://github.com/WangJi92/arthas-idea-plugin/issues/6");
            }
        });
        conditionArthas.setPaintUnderline(false);
        tt = new ActionLink("", AllIcons.Ide.Link, new AnAction() {
            @Override
            public void actionPerformed(AnActionEvent anActionEvent) {
                BrowserUtil.browse("https://github.com/WangJi92/arthas-idea-plugin/issues/4");
            }
        });
        tt.setPaintUnderline(false);
        watchSpringContext = new ActionLink("", AllIcons.Ide.Link, new AnAction() {
            @Override
            public void actionPerformed(AnActionEvent anActionEvent) {
                BrowserUtil.browse("https://github.com/WangJi92/arthas-idea-plugin/issues/5");
            }
        });
        watchSpringContext.setPaintUnderline(false);
    }

    /**
     * 打开窗口
     */
    public void open() {
        setTitle("arthas special use");
        pack();
        setMinimumSize(new Dimension(378,242));
        //两个屏幕处理出现问题，跳到主屏幕去了 https://blog.csdn.net/weixin_33919941/article/details/88129513
        setLocationRelativeTo(WindowManager.getInstance().getFrame(this.project));
        setVisible(true);
    }
}
