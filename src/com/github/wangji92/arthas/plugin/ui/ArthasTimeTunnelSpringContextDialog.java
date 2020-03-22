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

public class ArthasTimeTunnelSpringContextDialog extends JDialog {
    private JButton closeButton;

    /**
     * 全量表达式
     */
    private JTextField ognlExpressionEditor;

    private JPanel contentPane;
    private LinkLabel ognlOfficeLinkLabel;
    private LinkLabel oglSpecialLink;
    private LinkLabel ognlDemoLink;
    private LinkLabel ttInvokeAfterLink;
    private JTextField ttRequestMappingHandlerAdapterInvokeField;
    private JTextField timeTunnelIndexField;
    private JButton ttBeginButton;
    private LinkLabel ttInvokeBeforeHelp;
    private LinkLabel ttIndexLabel;


    private String className;

    private String staticOgnlExpression;

    private Project project;


    public ArthasTimeTunnelSpringContextDialog(Project project, String className, String staticOgnlExpression) {
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
        ttBeginButton.addActionListener(e -> {
            String text = ttRequestMappingHandlerAdapterInvokeField.getText();
            ClipboardUtils.setClipboardString(text);
            NotifyUtils.notifyMessage(project, "通过tt 获取spring context的命令可以多次使用,第一次使用需要触发一下一个接口的调用");
        });
    }


    /**
     * 关闭按钮回调
     */
    private void onOK() {
        String ognCurrentExpression = ognlExpressionEditor.getText();
        String timeTunnelIndex = timeTunnelIndexField.getText();
        if (StringUtils.isBlank(timeTunnelIndex)) {
            timeTunnelIndex = "1000";
        }
        if (StringUtils.isNotBlank(ognCurrentExpression)) {
            String invokeCommand = String.join(" ", ognCurrentExpression,"-x","3","-i", timeTunnelIndex);
            ClipboardUtils.setClipboardString(invokeCommand);
            NotifyUtils.notifyMessage(project,"这里的-i 参数必须是通过tt 获取spring context的命令的time tunnel index的值");
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
                BrowserUtil.browse("https://github.com/WangJi92/arthas-idea-plugin/issues/4");
            }
        });
        ognlDemoLink.setPaintUnderline(false);

        //https://github.com/WangJi92/arthas-idea-plugin/issues/5

        ttInvokeBeforeHelp = new ActionLink("", AllIcons.Ide.Link, new AnAction() {
            @Override
            public void actionPerformed(AnActionEvent anActionEvent) {
                BrowserUtil.browse("https://github.com/alibaba/arthas/issues/482");
            }
        });
        ttInvokeBeforeHelp.setPaintUnderline(false);

        ttIndexLabel = new ActionLink("", AllIcons.Ide.Link, new AnAction() {
            @Override
            public void actionPerformed(AnActionEvent anActionEvent) {
                BrowserUtil.browse("https://alibaba.github.io/arthas/tt");
            }
        });
        ttIndexLabel.setPaintUnderline(false);

        ttInvokeAfterLink = new ActionLink("", AllIcons.Ide.Link, new AnAction() {
            @Override
            public void actionPerformed(AnActionEvent anActionEvent) {
                BrowserUtil.browse("https://github.com/WangJi92/arthas-idea-plugin/issues/4");
            }
        });
        ttInvokeAfterLink.setPaintUnderline(false);
    }

}
