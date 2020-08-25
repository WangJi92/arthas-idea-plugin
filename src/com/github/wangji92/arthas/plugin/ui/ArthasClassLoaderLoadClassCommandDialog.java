package com.github.wangji92.arthas.plugin.ui;

import com.github.wangji92.arthas.plugin.utils.ClipboardUtils;
import com.github.wangji92.arthas.plugin.utils.NotifyUtils;
import com.github.wangji92.arthas.plugin.utils.StringUtils;
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

public class ArthasClassLoaderLoadClassCommandDialog extends JDialog {
    private JPanel contentPane;
    private JTextField classloaderHashValueTextField;
    private JButton classLoaderHashValueCommandButton;
    private JButton scCommandButton;
    private JTextField scTextField;
    private JButton loadClassCommandButton;
    private JTextField classloaderLoadTextField;
    private JLabel classloaderListLabel;
    private LinkLabel helpLink;
    private LinkLabel scLink;
    private JButton closeButton;

    private Project project;
    private String className;

    public ArthasClassLoaderLoadClassCommandDialog(Project project, String className) {
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

    private void init() {
        String classLoaderCommand = String.join(" ", "classloader --load", className);
        classloaderLoadTextField.setText(classLoaderCommand);

        String scCommand = String.join(" ", "sc -d", className);
        scTextField.setText(scCommand);

        scCommandButton.addActionListener(e -> {
            String text = scTextField.getText();
            ClipboardUtils.setClipboardString(text);
            NotifyUtils.notifyMessage(project, "search all the classes loaded by jvm");
        });

        loadClassCommandButton.addActionListener(e -> {
            String classloaderHashValue = classloaderHashValueTextField.getText();
            if (StringUtils.isBlank(classloaderHashValue)) {
                NotifyUtils.notifyMessage(project, " use classLoader to load class, won't work without -c specified");
                return;
            }
            //classloader --load demo.MathGame
            String loadTextFieldText = classloaderLoadTextField.getText();
            String loadClassCommand = String.join(" ", loadTextFieldText, "-c", classloaderHashValue);
            ClipboardUtils.setClipboardString(loadClassCommand);
            NotifyUtils.notifyMessage(project, "if load class success, at once search all the classes loaded by jvm");
        });
        classLoaderHashValueCommandButton.addActionListener(e -> {
            ClipboardUtils.setClipboardString("classloader -l");
            NotifyUtils.notifyMessage(project, " display list info by classloader instance,select one hava value to load current class");
        });
        closeButton.addActionListener(e -> {
            onCancel();
        });

    }

    private void onOK() {
        // add your code here
        dispose();
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

    private void createUIComponents() {
        helpLink = new ActionLink("", AllIcons.Ide.Link, new AnAction() {
            @Override
            public void actionPerformed(AnActionEvent anActionEvent) {
                BrowserUtil.browse(" https://arthas.aliyun.com/doc/classloader");
            }
        });
        helpLink.setPaintUnderline(false);
        scLink = new ActionLink("", AllIcons.Ide.Link, new AnAction() {
            @Override
            public void actionPerformed(AnActionEvent anActionEvent) {
                BrowserUtil.browse("https://arthas.aliyun.com/doc/sc");
            }
        });
        scLink.setPaintUnderline(false);
    }
}
