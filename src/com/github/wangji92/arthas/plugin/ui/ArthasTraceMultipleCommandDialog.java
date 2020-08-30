package com.github.wangji92.arthas.plugin.ui;

import com.github.wangji92.arthas.plugin.constants.ArthasCommandConstants;
import com.github.wangji92.arthas.plugin.setting.AppSettingsState;
import com.github.wangji92.arthas.plugin.utils.ClipboardUtils;
import com.github.wangji92.arthas.plugin.utils.NotifyUtils;
import com.google.common.collect.Sets;
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
import java.awt.event.*;
import java.util.Set;

/**
 * 支持trace -E
 *
 * @author 汪小哥
 * @date 3-1-2020
 */
public class ArthasTraceMultipleCommandDialog extends JDialog {
    private JPanel contentPane;
    /**
     * 完成命令
     */
    private JButton commandOk;
    /**
     * 继续添加命令
     */
    private JButton addTraceButton;
    /**
     * 展示数据命令
     */
    private JTextField traceCommandTextField;
    /**
     * 清除命令
     */
    private JButton clearButton;

    /**
     * 帮助命令
     */
    private LinkLabel traceHelp;

    /**
     * 当前工程命令
     */
    private Project project;

    /**
     * 类名称
     */
    private static Set<String> CLASS_SET = Sets.newConcurrentHashSet();
    /**
     * 方法名称
     */
    private static Set<String> METHOD_SET = Sets.newConcurrentHashSet();


    public ArthasTraceMultipleCommandDialog(Project project) {
        this.project = project;
        setContentPane(contentPane);
        setModal(true);
        getRootPane().setDefaultButton(commandOk);
        initEvent();
    }

    /**
     * 初始化事件
     */
    private void initEvent() {
        commandOk.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                onOK();
            }
        });

        addTraceButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                continueToAdd();
            }
        });

        clearButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                destroyTraceData(project);
            }
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

    /**
     * 完成处理
     */
    private void onOK() {
        String traceECommand = traceCommandTextField.getText();
        if (StringUtils.isNotBlank(traceECommand)) {
            AppSettingsState instance = AppSettingsState.getInstance(project);
            boolean skipJdkMethod = instance.traceSkipJdk;
            String skpJdkMethodCommand = skipJdkMethod ? "" : ArthasCommandConstants.DEFAULT_SKIP_JDK_FALSE;
            String printConditionExpress = instance.printConditionExpress ? "-v" : "";
            String command = String.join(" ", traceECommand, printConditionExpress, skpJdkMethodCommand,ArthasCommandConstants.DEFAULT_CONDITION_EXPRESS);
            ClipboardUtils.setClipboardString(command);
            NotifyUtils.notifyMessage(project, "支持ognl条件表达式(默认1==1) 更多搜索 [arthas 入门最佳实践],trace -E 支持trace多个方法,方法中的方法");
        }
        // modify by wangji 同事意见 多次trace 可能需要增加其他的 最好是自己手动清除
        // this.destroyTraceData(project);
        dispose();
    }

    /**
     * 继续添加
     */
    private void continueToAdd() {
        dispose();
    }

    private void onCancel() {
        dispose();
        //清除数据
        // this.destroyTraceData(project);
    }

    /**
     * 展示命令信息
     */
    private void showTraceCommand() {
        String classNames = String.join("|", CLASS_SET);
        String methodNames = String.join("|", METHOD_SET);

        // java.util.regex.PatternSyntaxException: Dangling meta character '*' near index
        // eg trace -E com.common.A |com.common.B  *| list  will be error before
        String replaceMethodName = methodNames.replace("*", "\\\\*");

        AppSettingsState instance = AppSettingsState.getInstance(project);
        String invokeCount = instance.invokeCount;
        String command = String.join(" ", "trace -E", classNames, replaceMethodName, "-n", invokeCount);
        traceCommandTextField.setText(command);
    }


    /**
     * 添加方法和参数信息
     *
     * @param className
     * @param methodName
     */
    public void continueAddTrace(String className, String methodName) {
        CLASS_SET.add(className);
        METHOD_SET.add(methodName);
    }

    /**
     * 清除数据
     *
     * @param project
     */
    private void destroyTraceData(Project project) {
        this.project = project;
        CLASS_SET.clear();
        METHOD_SET.clear();
        this.traceCommandTextField.setText("trace -E ");
    }

    /**
     * 展示对话框
     */
    public void showDialog() {
        this.showTraceCommand();
        this.open();
        this.setVisible(true);
    }

    /**
     * 打开窗口
     */
    public void open() {
        setTitle("arthas trace -E");
        pack();
        //两个屏幕处理出现问题，跳到主屏幕去了 https://blog.csdn.net/weixin_33919941/article/details/88129513
        setLocationRelativeTo(WindowManager.getInstance().getFrame(this.project));
        setVisible(false);
    }

    /**
     * 自定义UI 系统自动调用
     */
    private void createUIComponents() {
        traceHelp = new ActionLink("", AllIcons.Ide.Link, new AnAction() {
            @Override
            public void actionPerformed(AnActionEvent anActionEvent) {
                BrowserUtil.browse("https://arthas.aliyun.com/doc/trace.html");
            }
        });
        traceHelp.setPaintUnderline(false);
    }
}
