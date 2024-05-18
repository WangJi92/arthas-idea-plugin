package com.github.wangji92.arthas.plugin.ui;

import com.github.wangji92.arthas.plugin.common.command.CommandContext;
import com.github.wangji92.arthas.plugin.utils.ActionLinkUtils;
import com.intellij.openapi.wm.WindowManager;
import com.intellij.psi.PsiMethod;
import com.intellij.ui.components.ActionLink;

import javax.swing.*;
import java.awt.event.*;

public class ArthasVmToolComplexDialog extends JDialog {
    private JPanel contentPane;
    private ActionLink vmtoolHelpLabel;
    private JTextField vmToolExpressTextField;
    private ActionLink classloaderHelpLabel;
    private JTextField classloaderHashValueTextField;
    private JButton clearCacheButton;
    private JButton copyScCommandButton;
    private JButton instancesCommandButton;
    private JButton copyCommandButton;
    private JTable table1;

    private CommandContext commandContext;

    public ArthasVmToolComplexDialog(CommandContext commandContext) {
        this.commandContext = commandContext;
        setContentPane(contentPane);
        setModal(false);
        //getRootPane().setDefaultButton(buttonOK);

//        buttonOK.addActionListener(new ActionListener() {
//            public void actionPerformed(ActionEvent e) {
//                onOK();
//            }
//        });

//        buttonCancel.addActionListener(new ActionListener() {
//            public void actionPerformed(ActionEvent e) {
//                onCancel();
//            }
//        });

        if(commandContext.getPsiElement() instanceof PsiMethod){
            table1.setVisible(true);
        }else{
            table1.setVisible(false);
        }


        // call onCancel() when cross is clicked
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                onCancel();
            }
        });

        // call onCancel() on ESCAPE
        contentPane.registerKeyboardAction(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onCancel();
            }
        }, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
    }

    private void onOK() {
        // add your code here
        dispose();
    }

    private void onCancel() {
        // add your code here if necessary
        dispose();
    }

    private void createUIComponents() {
        classloaderHelpLabel = ActionLinkUtils.newActionLink("https://arthas.aliyun.com/doc/sc.html");
        vmtoolHelpLabel = ActionLinkUtils.newActionLink("https://arthas.aliyun.com/doc/vmtool.html");
    }

    /**
     * 打开窗口
     */
    public void open(String title) {
        setTitle(title);
        pack();
        //两个屏幕处理出现问题，跳到主屏幕去了 https://blog.csdn.net/weixin_33919941/article/details/88129513
        setLocationRelativeTo(WindowManager.getInstance().getFrame(this.commandContext.getProject()));
        setVisible(true);
    }
}
