package com.github.wangji92.arthas.plugin.ui;

import com.github.wangji92.arthas.plugin.constants.ArthasCommandConstants;
import com.github.wangji92.arthas.plugin.utils.ActionLinkUtils;
import com.github.wangji92.arthas.plugin.utils.ClipboardUtils;
import com.github.wangji92.arthas.plugin.utils.NotifyUtils;
import com.github.wangji92.arthas.plugin.utils.PropertiesComponentUtils;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.WindowManager;
import com.intellij.ui.components.labels.LinkLabel;
import org.apache.commons.lang3.StringUtils;

import javax.swing.*;
import java.awt.event.*;

public class ArthasVmToolDialog extends JDialog {
    private JPanel contentPane;

    private JTextField vmToolExpressTextField;

    private LinkLabel classloaderHelpLabel;

    private JTextField classloaderHashValuetextField;

    private JButton clearCacheButton;

    private JButton copyScCommandButton;

    private JButton copyCommandButton;

    private LinkLabel vmtoolHelpLabel;

    private Project project;

    private String className;

    public ArthasVmToolDialog(Project project, String command, String className) {
        this.project = project;
        this.className = className;
        setContentPane(contentPane);
        setModal(true);
        getRootPane().setDefaultButton(copyCommandButton);

        String classloaderHash = PropertiesComponentUtils.getValue(project, ArthasCommandConstants.CLASSLOADER_HASH_VALUE);
        classloaderHashValuetextField.setText(classloaderHash);
        vmToolExpressTextField.setText(command);

        copyCommandButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                onOK();
            }
        });
        copyScCommandButton.addActionListener(e -> {
            String scCommand = String.join(" ", "sc -d", className);
            ClipboardUtils.setClipboardString(scCommand);
            NotifyUtils.notifyMessageDefault(project);

        });

        clearCacheButton.addActionListener(e -> {
            classloaderHashValuetextField.setText("");
            PropertiesComponentUtils.setValue(project, ArthasCommandConstants.CLASSLOADER_HASH_VALUE, "");
        });

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
    }

    private void onOK() {
        String hashClassloader = classloaderHashValuetextField.getText();
        String vmtoolExpress = vmToolExpressTextField.getText();
        if (StringUtils.isNotBlank(hashClassloader) && vmtoolExpress != null && !vmtoolExpress.contains("-c")) {
            vmtoolExpress = vmtoolExpress + " -c " + hashClassloader;
            PropertiesComponentUtils.setValue(project, ArthasCommandConstants.CLASSLOADER_HASH_VALUE, hashClassloader);
        }
        if (StringUtils.isNotBlank(vmtoolExpress)) {
            ClipboardUtils.setClipboardString(vmtoolExpress);
            NotifyUtils.notifyMessageDefault(project);
        }
        dispose();
    }

    private void onCancel() {
        // add your code here if necessary
        dispose();
    }

    /**
     * 打开窗口
     */
    public void open() {
        setTitle("vmtool command,you can edit params use ognl grammar");
        pack();
        //两个屏幕处理出现问题，跳到主屏幕去了 https://blog.csdn.net/weixin_33919941/article/details/88129513
        setLocationRelativeTo(WindowManager.getInstance().getFrame(this.project));
        setVisible(true);
    }


    private void createUIComponents() {
        classloaderHelpLabel = ActionLinkUtils.newActionLink("https://arthas.aliyun.com/doc/sc.html");
        vmtoolHelpLabel = ActionLinkUtils.newActionLink("https://arthas.aliyun.com/doc/vmtool.html");
    }
}
