package com.github.wangji92.arthas.plugin.ui;

import com.github.wangji92.arthas.plugin.constants.ArthasCommandConstants;
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
import java.awt.event.*;
import java.util.HashSet;
import java.util.Set;

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
    private Set<String> classSet = new HashSet<>(10);
    /**
     * 方法名称
     */
    private Set<String> methodSet = new HashSet<>(10);
    ;

    private static ArthasTraceMultipleCommandDialog arthasTraceMultipleCommandDialog;

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
            public void actionPerformed(ActionEvent e) {
                onOK();
            }
        });

        addTraceButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                continueToAdd();
            }
        });

        clearButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                destroyTraceData(project);
            }
        });

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

    /**
     * 完成处理
     */
    private void onOK() {
        String traceECommand = traceCommandTextField.getText();
        if (StringUtils.isNotBlank(traceECommand)) {
            ClipboardUtils.setClipboardString(traceECommand);
            NotifyUtils.notifyMessageDefault(project);
        }
        this.destroyTraceData(project);
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
        this.destroyTraceData(project);
    }

    /**
     * 展示命令信息
     */
    private void showTraceCommand() {
        String classNames = String.join("|", this.classSet);
        String methodNames = String.join("|", this.methodSet);

        String command = String.join(" ", "trace -E", classNames, methodNames, "-n", ArthasCommandConstants.INVOKE_COUNT);
        traceCommandTextField.setText(command);
    }

    /**
     * 获取当前对话框的实例
     *
     * @param project
     * @return
     */
    public static ArthasTraceMultipleCommandDialog getInstance(Project project) {
        //不是同一个工工程清除掉数据信息
        if (arthasTraceMultipleCommandDialog != null && arthasTraceMultipleCommandDialog.project != project) {
            arthasTraceMultipleCommandDialog.destroyTraceData(null);
            arthasTraceMultipleCommandDialog = null;
        }
        if (arthasTraceMultipleCommandDialog == null) {
            synchronized (ArthasTraceMultipleCommandDialog.class) {
                if (arthasTraceMultipleCommandDialog == null) {
                    arthasTraceMultipleCommandDialog = new ArthasTraceMultipleCommandDialog(project);
                    arthasTraceMultipleCommandDialog.open();
                }
            }
        }
        return arthasTraceMultipleCommandDialog;
    }

    /**
     * 添加方法和参数信息
     *
     * @param className
     * @param methodName
     */
    public void continueAddTrace(String className, String methodName) {
        this.classSet.add(className);
        this.methodSet.add(methodName);
    }

    /**
     * 清除数据
     *
     * @param project
     */
    private void destroyTraceData(Project project) {
        this.project = project;
        this.classSet.clear();
        this.methodSet.clear();
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
                BrowserUtil.browse("https://alibaba.github.io/arthas/trace.html");
            }
        });
        traceHelp.setPaintUnderline(false);
    }
}
