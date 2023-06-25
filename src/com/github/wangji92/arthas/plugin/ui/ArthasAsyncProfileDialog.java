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
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;

public class ArthasAsyncProfileDialog extends JDialog {
    private JPanel contentPane;
    private JComboBox eventComboBox;

    private JComboBox eventModeComboBox;
    /**
     * 开始
     */
    private JButton startCommandButton;
    /**
     * 停止
     */
    private JButton stopCommandButton;
    /**
     * 其他的命令
     */
    private JComboBox otherCommandComboBox;
    /**
     * 输出格式
     */
    private JComboBox outputFileFormatComboBox;
    /**
     * 其他的命令
     */
    private JButton otherCommandButton;
    /**
     * 获取所有的样本
     */
    private JButton getSampleCommandButton;
    private LinkLabel help;
    private JButton closeButton;
    private JRadioButton differentThreadsSeparatelyRadioButton;
    private LinkLabel asyncExample;
    /**
     * 持续时间
     */
    private JTextField durationTextField;

    /**
     * 持续时间自动结束的命令
     */
    private JButton autoStopCommandButton;

    private Project project;

    public ArthasAsyncProfileDialog(Project project) {
        this.project = project;
        setContentPane(this.contentPane);
        setModal(false);
        getRootPane().setDefaultButton(closeButton);

        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                onCancel();
            }
        });

        // call onCancel() on ESCAPE
        contentPane.registerKeyboardAction(e -> onCancel(), KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);

        closeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                onCancel();
            }
        });
        initEvent();
    }

    private void initEvent() {

        // https://github.com/jvm-profiling-tools/async-profiler
        // Wall-clock profiler is most useful in per-thread mode: -t
        eventComboBox.addItemListener(e -> {
            if ("wall".equals(e.getItem().toString())) {
                differentThreadsSeparatelyRadioButton.setSelected(true);
            } else {
                differentThreadsSeparatelyRadioButton.setSelected(false);
            }
        });

        startCommandButton.addActionListener((event) -> {
            startCommand();

        });
        autoStopCommandButton.addActionListener((event) -> {
            autoStopCommand();
        });
        stopCommandButton.addActionListener((event) -> {
            stopCommand();
        });
        getSampleCommandButton.addActionListener((event) -> {
            getSample();
        });
        otherCommandButton.addActionListener((event) -> {
            otherCommand();

        });
    }

    private void getSample() {
        List<String> commands = new ArrayList<>();
        commands.add("profiler");
        commands.add("getSamples");
        String commandFinal = String.join(" ", commands);
        ClipboardUtils.setClipboardString(commandFinal);
        NotifyUtils.notifyMessage(project, "获取当前profiler 收集的样本的数量");
    }

    private void otherCommand() {
        List<String> commands = new ArrayList<>();
        commands.add("profiler");
        String otherCommandComboBoxStr = otherCommandComboBox.getSelectedItem() != null ? otherCommandComboBox.getSelectedItem().toString() : "";
        if (StringUtils.isNotBlank(otherCommandComboBoxStr)) {
            commands.add(otherCommandComboBoxStr);
        }
        String commandFinal = String.join(" ", commands);
        ClipboardUtils.setClipboardString(commandFinal);
        NotifyUtils.notifyMessage(project, "list(all supported events),actions(all supported actions)");
    }

    private void stopCommand() {
        List<String> commands = new ArrayList<>();
        commands.add("profiler");
        commands.add("stop");
        String outputFileFormatComboBoxStr = outputFileFormatComboBox.getSelectedItem() != null ? outputFileFormatComboBox.getSelectedItem().toString() : "";
        if (StringUtils.isNotBlank(outputFileFormatComboBoxStr)) {
            commands.add("--format");
            commands.add(outputFileFormatComboBoxStr);
        }
        String commandFinal = String.join(" ", commands);
        ClipboardUtils.setClipboardString(commandFinal);
        NotifyUtils.notifyMessage(project, "分析火焰图X轴越长,代表用的越多(重点关注),Y轴是调用堆栈信息,和颜色无关 eg cpu占用率高");
    }

    private void startCommand() {
        List<String> commands = getStartCommandList();
        String commandFinal = String.join(" ", commands);
        ClipboardUtils.setClipboardString(commandFinal);
        NotifyUtils.notifyMessage(project, "默认收集cpu  alluser(user-mode events) allkernel(kernel-mode events) 频率单位纳秒");
    }

    /**
     * 完成后自动关闭
     */
    private void autoStopCommand() {
        List<String> commands = getStartCommandList();

        String outputFileFormatComboBoxStr = outputFileFormatComboBox.getSelectedItem() != null ? outputFileFormatComboBox.getSelectedItem().toString() : "";
        if (StringUtils.isNotBlank(outputFileFormatComboBoxStr)) {
            commands.add("--format");
            commands.add(outputFileFormatComboBoxStr);
        }

        String durationTextFieldText = durationTextField.getText();
        if (StringUtils.isNotBlank(durationTextFieldText)) {
            commands.add("--duration");
            commands.add(durationTextFieldText);
        }
        if (differentThreadsSeparatelyRadioButton.isSelected()) {
            commands.add("--threads");
        }
        String commandFinal = String.join(" ", commands);
        ClipboardUtils.setClipboardString(commandFinal);
        NotifyUtils.notifyMessage(project, " 自动完成后自动Stop 详情见 Arthas special Use link");

    }

    /**
     * 获取启动命令的构造 List
     *
     * @return
     */
    @NotNull
    private List<String> getStartCommandList() {
        List<String> commands = new ArrayList<>();
        commands.add("profiler");
        commands.add("start");
        commands.add("--event");
        String eventComboBoxStr = eventComboBox.getSelectedItem() != null ? eventComboBox.getSelectedItem().toString() : "";
        commands.add(eventComboBoxStr);
        // 具体的参数规则 https://github.com/jvm-profiling-tools/async-profiler/blob/v1.6/src/arguments.cpp#L34 allkernel alluser 虽然  ./profiler.sh help 不一样 由于脚本处理了
        String eventModeComboBoxStr = eventModeComboBox.getSelectedItem() != null ? eventModeComboBox.getSelectedItem().toString() : "";
        if (StringUtils.isNotBlank(eventModeComboBoxStr)) {
            commands.add("--" + eventModeComboBoxStr);
        }
        commands.add("--interval");
        commands.add("10000000");

        if (differentThreadsSeparatelyRadioButton.isSelected()) {
            commands.add("--threads");
        }
        return commands;
    }


    private void onCancel() {
        // add your code here if necessary
        dispose();
    }

    /**
     * 打开窗口
     */
    public void open() {
        setTitle("async-profiler you can use --include/--exclude filter stack traces,more read doc");
        pack();
        //两个屏幕处理出现问题，跳到主屏幕去了 https://blog.csdn.net/weixin_33919941/article/details/88129513
        setLocationRelativeTo(WindowManager.getInstance().getFrame(this.project));
        setVisible(true);
    }

    private void createUIComponents() {
        help = new ActionLink("", AllIcons.Ide.Link, new AnAction() {
            @Override
            public void actionPerformed(AnActionEvent anActionEvent) {
                BrowserUtil.browse("https://arthas.aliyun.com/doc/profiler.html");
            }
        });
        help.setPaintUnderline(false);

        asyncExample = new ActionLink("", AllIcons.Ide.Link, new AnAction() {
            @Override
            public void actionPerformed(AnActionEvent anActionEvent) {
                BrowserUtil.browse("https://www.cnblogs.com/leihuazhe/p/11630466.html");
            }
        });
        asyncExample.setPaintUnderline(false);
    }
}
