package com.command.idea.plugin.ui;

import com.command.idea.plugin.utils.ClipboardUtils;
import com.command.idea.plugin.utils.NotifyUtils;
import com.intellij.icons.AllIcons;
import com.intellij.ide.BrowserUtil;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.intellij.ui.components.labels.ActionLink;
import com.intellij.ui.components.labels.LinkLabel;
import org.apache.commons.lang3.StringUtils;

import javax.swing.*;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/**
 * arthas ognl 获取静态信息的展示
 * @author jet
 * @date 21-12-2019
 */
public class ArthasActionStaticDialog extends  JDialog {
    /**
     * sc 复制信息命令
     */
    private JButton scCommandButton;
    /**
     * classload hash 值复制命令
     */
    private JTextField classloaderHashEditor;
    /**
     * 结束命令按钮
     */
    private JButton closeButton;

    /**
     * 构造好的ognl 表达式
     */
    private JTextField ognlExpressionEditor;

    private JPanel contentPane;
    private LinkLabel classLoadLinkLable;
    private LinkLabel ognlOfficeLinkLabel;
    private LinkLabel oglSpecialLink;
    private LinkLabel ognlDemoLink;


    private String className;

    private String staticOgnlExpression;

    private Project project;



    public ArthasActionStaticDialog(Project project,String className, String staticOgnlExpression) {
        this.project = project;
        setContentPane(this.contentPane);
        setModal(true);
        getRootPane().setDefaultButton(closeButton);
        this.className=className;
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

    private void onCopyScCommand() {
        String command = String.join(" ", "sc", "-d", className);
        ClipboardUtils.setClipboardString(command);
        NotifyUtils.notifyMessageDefault(project);
    }

    private void init() {
        scCommandButton.addActionListener(e->onCopyScCommand());
        ognlExpressionEditor.setText(this.staticOgnlExpression);
    }

    /**
     * 取人按钮回调
     */
    private void onOK() {
        String hashClassloader= classloaderHashEditor.getText();
        String ognCurrentExpression = ognlExpressionEditor.getText();
        if(StringUtils.isNotBlank(hashClassloader) && ognCurrentExpression !=null &&  !ognCurrentExpression.contains("-c")){
            StringBuilder builder = new StringBuilder(ognCurrentExpression);
            builder.append(" -c ").append( hashClassloader);
            ognCurrentExpression  = builder.toString();
        }
        if(StringUtils.isNotBlank(ognCurrentExpression)){
            ClipboardUtils.setClipboardString(ognCurrentExpression);
            NotifyUtils.notifyMessageDefault(project);
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
        setLocationRelativeTo(null);
        setVisible(true);

    }


    private void createUIComponents() {
        classLoadLinkLable = new ActionLink("", AllIcons.Ide.Link, new AnAction() {
            @Override
            public void actionPerformed(AnActionEvent anActionEvent) {
                BrowserUtil.browse("https://alibaba.github.io/arthas/ognl.html");
            }
        });
        classLoadLinkLable.setPaintUnderline(false);

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
                BrowserUtil.browse("https://blog.csdn.net/u010634066/article/details/101013479");
            }
        });
        ognlDemoLink.setPaintUnderline(false);
    }
}
