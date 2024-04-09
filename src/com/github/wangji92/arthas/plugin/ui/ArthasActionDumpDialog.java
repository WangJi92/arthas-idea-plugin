package com.github.wangji92.arthas.plugin.ui;

import com.github.wangji92.arthas.plugin.constants.ArthasCommandConstants;
import com.github.wangji92.arthas.plugin.utils.ActionLinkUtils;
import com.github.wangji92.arthas.plugin.utils.ClipboardUtils;
import com.github.wangji92.arthas.plugin.utils.NotifyUtils;
import com.github.wangji92.arthas.plugin.utils.PropertiesComponentUtils;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.WindowManager;
import com.intellij.ui.components.ActionLink;
import org.apache.commons.lang3.StringUtils;

import javax.swing.*;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class ArthasActionDumpDialog extends JDialog {
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

    private ActionLink dumpOfficeActionLink;

    private JButton clearClassloaderHashValue;


    private String className;

    private String staticOgnlExpression;

    private Project project;

    private ActionLink classLoaderLinkLable;


    public ArthasActionDumpDialog(Project project, String className, String staticOgnlExpression) {
        this.project = project;
        setContentPane(this.contentPane);
        setModal(true);
        getRootPane().setDefaultButton(closeButton);
        this.className = className;
        this.staticOgnlExpression = staticOgnlExpression;

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
        String classloaderHash = PropertiesComponentUtils.getValue(project,ArthasCommandConstants.CLASSLOADER_HASH_VALUE);
        classloaderHashEditor.setText(classloaderHash);
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
            PropertiesComponentUtils.setValue(project,ArthasCommandConstants.CLASSLOADER_HASH_VALUE, hashClassloader);
        }
        if (StringUtils.isNotBlank(ognCurrentExpression)) {
            ClipboardUtils.setClipboardString(ognCurrentExpression);
            NotifyUtils.notifyMessageDefault(project);
        }
        dispose();
    }

    /**
     * 删除之前的缓存classloader的信息
     */
    private void onClearClassLoaderHashValue() {
        classloaderHashEditor.setText("");
        PropertiesComponentUtils.setValue(project,ArthasCommandConstants.CLASSLOADER_HASH_VALUE, "");
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

        dumpOfficeActionLink = ActionLinkUtils.newActionLink("https://arthas.aliyun.com/doc/dump");
        classLoaderLinkLable = ActionLinkUtils.newActionLink("https://arthas.aliyun.com/doc/ognl.html");

    }
}
