package com.github.idea.arthas.plugin.ui;

import com.github.idea.arthas.plugin.utils.ActionLinkUtils;
import com.github.idea.arthas.plugin.utils.ClipboardUtils;
import com.github.idea.arthas.plugin.utils.CommonExecuteScriptUtils;
import com.github.idea.arthas.plugin.utils.NotifyUtils;
import com.google.common.collect.Lists;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.WindowManager;
import com.intellij.ui.components.ActionLink;
import org.apache.commons.lang3.StringUtils;

import javax.swing.*;
import java.awt.event.*;
import java.util.List;

/**
 * 动态更新日志等级
 * logger --name sample.mybatis.SampleXmlApplication --l warn
 */
public class ArthasLoggerDialog extends JDialog {
    private JPanel contentPane;
    private JButton updateLevelButton;
    private JTextField loggerExpressionEditor;
    private JButton scCommandButton;
    private JTextField classloaderHashEditor;
    private JComboBox logLevelComboBox;
    private JButton closeButton;
    private ActionLink helpLink;
    private ActionLink loggerBestLink;
    private JButton shellScriptCommandButton;


    private Project project;

    private String className;

    public ArthasLoggerDialog(Project project, String className) {
        this.project = project;
        this.className = className;
        setContentPane(contentPane);
        setModal(true);
        getRootPane().setDefaultButton(closeButton);

        // call onCancel() when cross is clicked
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                onCancel();
            }
        });

        // call onCancel() on ESCAPE
        contentPane.registerKeyboardAction(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                onCancel();
            }
        }, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        init();
    }

    private void onOK() {
        // add your code here
        List<String> commands = Lists.newArrayList();

        String loggerName = loggerExpressionEditor.getText();
        commands.add(loggerName);

        //更新level
        String currentLoggerLevel = (String) logLevelComboBox.getSelectedItem();
        if (StringUtils.isNotBlank(currentLoggerLevel)) {
            commands.add("--level");
            commands.add(currentLoggerLevel);
        }

        String hashClassloader = classloaderHashEditor.getText();
        if (StringUtils.isNotBlank(hashClassloader)) {
            commands.add("-c");
            commands.add(hashClassloader);
        }
        String joinCommands = String.join(" ", commands);
        ClipboardUtils.setClipboardString(joinCommands);
        NotifyUtils.notifyMessage(project, NotifyUtils.COMMAND_COPIED + "(logger level trace>debug>info>warn>error,-c classloader hash value,--l logger level");
    }

    private void onCancel() {
        // add your code here if necessary
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

    private void init() {
        this.scCommandButton.addActionListener(e -> getLoggerClassHashLoader());
        this.updateLevelButton.addActionListener(e -> onOK());
        this.closeButton.addActionListener(e -> onCancel());
        String loggerEx = String.join(" ", "logger", "--name", this.className);
        this.loggerExpressionEditor.setText(loggerEx);
        shellScriptCommandButton.addActionListener(e -> {
            List<String> commands = Lists.newArrayList();
            String loggerName = loggerExpressionEditor.getText();
            commands.add(loggerName);
            //更新level
            String currentLoggerLevel = (String) logLevelComboBox.getSelectedItem();
            if (StringUtils.isNotBlank(currentLoggerLevel)) {
                commands.add("--level");
                commands.add(currentLoggerLevel);
            }
            String joinCommands = String.join(" ", commands);
            // logger --name xxx class 这里的表达式不是 classLoaderHash   18b4aac2 统一一下格式
            String scCommand = String.join(" ", "logger", "--name", className);
            CommonExecuteScriptUtils.executeCommonScript(project, scCommand, joinCommands, "");
            dispose();
        });
    }

    private void getLoggerClassHashLoader() {
        String loggerName = loggerExpressionEditor.getText();
        ClipboardUtils.setClipboardString(loggerName);
        NotifyUtils.notifyMessage(project, NotifyUtils.COMMAND_COPIED + "(Get classloader hash value of class through logger - name)");
    }

    private void createUIComponents() {
        loggerBestLink = ActionLinkUtils.newActionLink("https://github.com/WangJi92/arthas-idea-plugin/issues/7");
        helpLink = ActionLinkUtils.newActionLink("https://arthas.aliyun.com/doc/logger.html");
    }
}
