package com.github.wangji92.arthas.plugin.ui;

import com.github.wangji92.arthas.plugin.constants.ArthasCommandConstants;
import com.github.wangji92.arthas.plugin.utils.*;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.WindowManager;
import com.intellij.ui.components.labels.LinkLabel;
import org.apache.commons.lang3.StringUtils;

import javax.swing.*;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/**
 * arthas ognl 获取静态信息的展示
 *
 * @author 汪小哥
 * @date 21-12-2019
 */
public class ArthasActionStaticDialog extends JDialog {
    /**
     * sc 复制信息命令
     */
    private JButton scCommandButton;
    /**
     * classloader hash 值复制命令
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
    private LinkLabel classLoaderLinkLabel;
    private LinkLabel ognlOfficeLinkLabel;
    private LinkLabel oglSpecialLink;
    private LinkLabel ognlDemoLink;
    private JButton clearClassloaderHashValue;
    private JButton springNonProxyTargetButton;
    /**
     * 直接执行脚本
     */
    private JButton shellScriptCommandButton;


    private String className;

    private String staticOgnlExpression;

    private Project project;

    private String aopTargetOgnlExpression;


    public ArthasActionStaticDialog(Project project, String className, String staticOgnlExpression, String aopTargetOgnlExpression) {
        this.project = project;
        setContentPane(this.contentPane);
        setModal(true);
        getRootPane().setDefaultButton(closeButton);
        this.className = className;
        this.staticOgnlExpression = staticOgnlExpression;
        this.aopTargetOgnlExpression = aopTargetOgnlExpression;


        closeButton.addActionListener(e -> onOK());

        //clear cache的classloader的hash值的信息
        clearClassloaderHashValue.addActionListener(e -> onClearClassLoaderHashValue());

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
        scCommandButton.addActionListener(e -> onCopyScCommand());
        ognlExpressionEditor.setText(this.staticOgnlExpression);
        String classloaderHash = PropertiesComponentUtils.getValue(project, ArthasCommandConstants.CLASSLOADER_HASH_VALUE);
        classloaderHashEditor.setText(classloaderHash);
        if (StringUtils.isBlank(aopTargetOgnlExpression)) {
            springNonProxyTargetButton.setVisible(false);
        } else {
            springNonProxyTargetButton.setVisible(true);
        }
        springNonProxyTargetButton.addActionListener(e -> {
            String hashClassloader = classloaderHashEditor.getText();
            StringBuilder builder = new StringBuilder(aopTargetOgnlExpression);
            builder.append(" -c ").append(hashClassloader);
            ClipboardUtils.setClipboardString(builder.toString());
            NotifyUtils.notifyMessage(project, "【命令已复制到剪切板】");
        });

        shellScriptCommandButton.addActionListener(e -> {
            String ognCurrentExpression = ognlExpressionEditor.getText();
            String scCommand = String.join(" ", "sc", "-d", className);
            CommonExecuteScriptUtils.executeCommonScript(project, scCommand, ognCurrentExpression, "");
            dispose();
        });
    }


    /**
     * 取人按钮回调
     */
    private void onOK() {
        String hashClassloader = classloaderHashEditor.getText();
        String ognCurrentExpression = ognlExpressionEditor.getText();
        if (StringUtils.isNotBlank(hashClassloader) && ognCurrentExpression != null && !ognCurrentExpression.contains("-c")) {
            StringBuilder builder = new StringBuilder(ognCurrentExpression);
            builder.append(" -c ").append(hashClassloader);
            ognCurrentExpression = builder.toString();
            PropertiesComponentUtils.setValue(project, ArthasCommandConstants.CLASSLOADER_HASH_VALUE, hashClassloader);
        }
        if (StringUtils.isNotBlank(ognCurrentExpression)) {
            ClipboardUtils.setClipboardString(ognCurrentExpression);
            NotifyUtils.notifyMessage(project, "【命令已复制到剪切板】");
        }
        dispose();
    }

    /**
     * 删除之前的缓存classloader的信息
     */
    private void onClearClassLoaderHashValue() {
        classloaderHashEditor.setText("");
        PropertiesComponentUtils.setValue(project, ArthasCommandConstants.CLASSLOADER_HASH_VALUE, "");
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
        classLoaderLinkLabel = ActionLinkUtils.newActionLink("https://arthas.aliyun.com/doc/ognl.html");
        ognlOfficeLinkLabel = ActionLinkUtils.newActionLink("https://commons.apache.org/proper/commons-ognl/language-guide.html");
        oglSpecialLink = ActionLinkUtils.newActionLink("https://github.com/alibaba/arthas/issues/71");
        ognlDemoLink = ActionLinkUtils.newActionLink("https://blog.csdn.net/u010634066/article/details/101013479");
    }
}
