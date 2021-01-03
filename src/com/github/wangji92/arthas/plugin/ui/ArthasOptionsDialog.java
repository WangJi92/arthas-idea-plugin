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

import javax.swing.*;
import java.awt.event.*;

/**
 * options
 *
 * @author 汪小哥
 * @date 01-01-2021
 */
public class ArthasOptionsDialog extends JDialog {

    private JButton optionUnsafeButton;
    private LinkLabel moreInfo;
    private JButton optionsButton;
    private JButton optionDumpButton;
    private JButton optionJsonButton;
    private JButton optionSaveResultButton;

    private JPanel contentPane;

    private Project project;

    public ArthasOptionsDialog(Project project) {
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

        init();

    }

    private void init() {
        optionsButton.addActionListener(new OptionActionListener("options", project, "获取全局配置"));
        optionUnsafeButton.addActionListener(new OptionActionListener("options unsafe true", project, "支持对系统级别的类进行增强，打开该开关可能导致把JVM搞挂，请慎重选择！"));
        optionDumpButton.addActionListener(new OptionActionListener("options dump true", project, "trace、watch等增强class动作增强class会被dump到具体目录"));
        optionJsonButton.addActionListener(new OptionActionListener("options json-format true", project, "观察表达式返回值以json输出"));
        optionSaveResultButton.addActionListener(new OptionActionListener("options save-result true", project, "观察表达式返回的结果保存到具体文件 ~/logs/arthas-cache/result.log"));
    }

    public static class OptionActionListener implements ActionListener {

        private String command;
        private Project project;
        private String desc;

        public OptionActionListener(String command, Project project, String desc) {
            this.command = command;
            this.project = project;
            this.desc = desc;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            ClipboardUtils.setClipboardString(command);
            NotifyUtils.notifyMessage(this.project, "选中的" + command + "命令已经复制到剪切板;" + desc);
        }
    }

    /**
     * 关闭
     */
    private void onCancel() {
        dispose();
    }

    private void createUIComponents() {
        moreInfo = new ActionLink("", AllIcons.Ide.Link, new AnAction() {
            @Override
            public void actionPerformed(AnActionEvent anActionEvent) {
                BrowserUtil.browse("https://arthas.aliyun.com/doc/options.html");
            }
        });
        moreInfo.setPaintUnderline(false);
    }

    /**
     * 打开窗口
     */
    public void open() {
        setTitle("arthas options use");
        pack();
        //两个屏幕处理出现问题，跳到主屏幕去了 https://blog.csdn.net/weixin_33919941/article/details/88129513
        setLocationRelativeTo(WindowManager.getInstance().getFrame(this.project));
        setVisible(true);
    }
}
