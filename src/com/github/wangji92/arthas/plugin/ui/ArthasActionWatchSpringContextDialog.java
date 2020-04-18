package com.github.wangji92.arthas.plugin.ui;

import com.github.wangji92.arthas.plugin.utils.ClipboardUtils;
import com.github.wangji92.arthas.plugin.utils.NotifyUtils;
import com.intellij.icons.AllIcons;
import com.intellij.ide.BrowserUtil;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.WindowManager;
import com.intellij.ui.components.labels.ActionLink;
import com.intellij.ui.components.labels.LinkLabel;
import org.apache.commons.lang3.StringUtils;

import javax.swing.*;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class ArthasActionWatchSpringContextDialog extends JDialog {

    private JButton closeButton;

    private JTextField ognlExpressionEditor;

    private JPanel contentPane;
    private LinkLabel ognlOfficeLinkLabel;
    private LinkLabel oglSpecialLink;
    private LinkLabel ognlDemoLink;
    private LinkLabel watchHelpLink;


    private String className;

    private String staticOgnlExpression;

    private Project project;


    public ArthasActionWatchSpringContextDialog(Project project, String className, String staticOgnlExpression) {
        this.project = project;
        setContentPane(this.contentPane);
        setModal(true);
        getRootPane().setDefaultButton(closeButton);
        this.className = className;
        this.staticOgnlExpression = staticOgnlExpression;

        closeButton.addActionListener(e -> onOK());


        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                onCancel();
            }
        });

        // call onCancel() on ESCAPE
        contentPane.registerKeyboardAction(e -> onCancel(), KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);

        init();
    }


    private void init() {
        ognlExpressionEditor.setText(this.staticOgnlExpression);
    }


    /**
     * 取人按钮回调
     */
    private void onOK() {
        String ognCurrentExpression = ognlExpressionEditor.getText();
        if (StringUtils.isNotBlank(ognCurrentExpression)) {
            ClipboardUtils.setClipboardString(ognCurrentExpression);
            NotifyUtils.notifyMessage(project,"Bean 名称可能不正确可以手动修改,由于使用watch 触发ognl的调用，必须要触发一次Mvc接口的调用，Static Spring Context 调用不同");
        }
        dispose();
    }


    /**
     * 关闭
     */
    private void onCancel() {
        dispose();
    }

    /**
     * 打开窗口
     */
    public void open(String title) {
        setTitle(title);
        pack();
        //两个屏幕处理出现问题，跳到主屏幕去了
        setLocationRelativeTo(WindowManager.getInstance().getFrame(this.project));
        setVisible(true);

    }


    private void createUIComponents() {
        ognlOfficeLinkLabel = new ActionLink("", AllIcons.Ide.Link, new AnAction() {
            @Override
            public void actionPerformed(AnActionEvent anActionEvent) {
                BrowserUtil.browse("https://commons.apache.org/proper/commons-ognl/language-guide.html");
            }
        });
        ognlOfficeLinkLabel.setPaintUnderline(false);

        oglSpecialLink = new ActionLink("", AllIcons.Ide.Link, new AnAction() {
            @Override
            public void actionPerformed(AnActionEvent anActionEvent) {
                BrowserUtil.browse("https://github.com/alibaba/arthas/issues/71");
            }
        });
        oglSpecialLink.setPaintUnderline(false);
        ognlDemoLink = new ActionLink("", AllIcons.Ide.Link, new AnAction() {
            @Override
            public void actionPerformed(AnActionEvent anActionEvent) {
                BrowserUtil.browse("https://github.com/WangJi92/arthas-idea-plugin/issues/5");
            }
        });
        ognlDemoLink.setPaintUnderline(false);

        //https://github.com/WangJi92/arthas-idea-plugin/issues/5

        watchHelpLink = new ActionLink("", AllIcons.Ide.Link, new AnAction() {
            @Override
            public void actionPerformed(AnActionEvent anActionEvent) {
                BrowserUtil.browse("   https://alibaba.github.io/arthas/watch");
            }
        });
        watchHelpLink.setPaintUnderline(false);
    }
}
