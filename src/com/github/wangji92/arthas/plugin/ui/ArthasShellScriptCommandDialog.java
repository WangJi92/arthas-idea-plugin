package com.github.wangji92.arthas.plugin.ui;

import com.github.wangji92.arthas.plugin.common.combox.CustomComboBoxItem;
import com.github.wangji92.arthas.plugin.common.combox.CustomDefaultListCellRenderer;
import com.github.wangji92.arthas.plugin.common.command.CommandContext;
import com.github.wangji92.arthas.plugin.common.enums.ShellScriptCommandEnum;
import com.github.wangji92.arthas.plugin.common.enums.ShellScriptConstantEnum;
import com.github.wangji92.arthas.plugin.common.enums.ShellScriptVariableEnum;
import com.github.wangji92.arthas.plugin.setting.AppSettingsState;
import com.github.wangji92.arthas.plugin.utils.*;
import com.google.common.collect.Sets;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.WindowManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.github.wangji92.arthas.plugin.utils.NotifyUtils.COMMAND_COPIED;

/**
 * 快捷命令
 *
 * @author 汪小哥
 * @date 05-05-2021
 */
public class ArthasShellScriptCommandDialog extends JDialog {
    private JPanel contentPane;
    private JComboBox shellScriptComboBox;
    private JButton shellScriptCommandButton;
    /**
     * 通用脚本
     */
    private JComboBox commonShellScriptComboBox;
    /**
     * 通用脚本点击
     */
    private JButton commonShellScriptCommandButton;
    private JButton closeScriptButton;
    private JRadioButton selectCommandCloseDialogRadioButton;
    private JButton dyCopyCommandButton;
    private JButton commonCopyCommandButton;
    private JLabel dyTipLabel;
    private JLabel constantLabel;

    private CommandContext commandContext;

    private Project project;

    /**
     * 当前选择的动态脚本
     */
    private CustomComboBoxItem currentSelectDyScriptVariableEnum;

    /**
     * 防止手动修改 恶意添加了 classloader
     */
    public static Set<String> NEED_CLASSLOADER_COMMAND = Sets.newHashSet("logger --name", "dump", "jad", "vmtool", "ognl", "${CLASSLOADER_HASH_VALUE}");


    public ArthasShellScriptCommandDialog(AnActionEvent event) {
        this.commandContext = new CommandContext(event);
        this.project = commandContext.getProject();
        setContentPane(contentPane);
        setMinimumSize(new Dimension(800, 340));
        setModal(true);
        getRootPane().setDefaultButton(closeScriptButton);
        closeScriptButton.addActionListener(e -> onOK());
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


    @SuppressWarnings("unchecked")
    private void init() {
        this.initDynamicShellScript();
        this.commonScriptInit();
        this.initSelectCommandCloseDialogSetting();
    }

    private void initSelectCommandCloseDialogSetting() {
        selectCommandCloseDialogRadioButton.addItemListener(e -> {
            if (e.getSource().equals(selectCommandCloseDialogRadioButton)) {
                if (e.getStateChange() == ItemEvent.DESELECTED) {
                    PropertiesComponentUtils.setValue("scriptDialogCloseWhenSelectedCommand", "n");
                } else if (e.getStateChange() == ItemEvent.SELECTED) {
                    PropertiesComponentUtils.setValue("scriptDialogCloseWhenSelectedCommand", "y");
                }
            }
        });
        AppSettingsState instance = AppSettingsState.getInstance(project);
        if ("y".equalsIgnoreCase(instance.scriptDialogCloseWhenSelectedCommand)) {
            selectCommandCloseDialogRadioButton.setSelected(true);
        }
    }

    /**
     * 初始化动态执行脚本
     */
    @SuppressWarnings("unchecked")
    private void initDynamicShellScript() {
        shellScriptCommandButton.addActionListener(e -> {
            Object selectedItem = shellScriptComboBox.getSelectedItem();
            assert selectedItem != null;
            String selectedItemStr = selectedItem.toString();
            String scCommand = "";
            if (this.currentSelectDyScriptVariableEnum != null) {
                ShellScriptCommandEnum shellScriptCommandEnum = (ShellScriptCommandEnum) currentSelectDyScriptVariableEnum.getContentObject();
                scCommand = shellScriptCommandEnum.getScCommand(this.commandContext);
                // 这里要处理一下 手动修改了comboBox的值 获取不到 是否需要classloader
                if (StringUtils.isNotBlank(scCommand)) {
                    final String selectedItemStrFinal = selectedItemStr;
                    AtomicBoolean containSc = new AtomicBoolean(false);
                    NEED_CLASSLOADER_COMMAND.forEach(needSc -> {
                        if (selectedItemStrFinal.contains(needSc)) {
                            containSc.set(true);
                        }
                    });
                    if (containSc.get()) {
                        selectedItemStr = String.join(" ", selectedItemStr, "-c", ShellScriptVariableEnum.CLASSLOADER_HASH_VALUE.getCode());
                    } else {
                        scCommand = "";
                    }
                }
            }
            // 这里再次处理一下上下文信息
            String finalStr = commandContext.getCommandCode(selectedItemStr);
            if (StringUtils.isNotBlank(finalStr)) {
                CommonExecuteScriptUtils.executeCommonScript(project, scCommand, finalStr, "");
            }
            this.doCloseDialog();
        });

        dyCopyCommandButton.addActionListener(e -> {
            Object selectedItem = shellScriptComboBox.getSelectedItem();
            assert selectedItem != null;
            String selectedItemStr = selectedItem.toString();
            ClipboardUtils.setClipboardString(selectedItemStr);
            NotifyUtils.notifyMessage(project, COMMAND_COPIED + "(Some commands need classloader hash value to be executed directly)");
        });
        shellScriptComboBox.addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                if (e.getItem() instanceof CustomComboBoxItem) {
                    CustomComboBoxItem item = (CustomComboBoxItem) e.getItem();
                    dyTipLabel.setText(item.getTipText());
                    this.currentSelectDyScriptVariableEnum = item;
                }

            }
        });
        shellScriptComboBox.setRenderer(new CustomDefaultListCellRenderer(shellScriptComboBox));

        for (ShellScriptCommandEnum shellScript : ShellScriptCommandEnum.values()) {
            if (!shellScript.support(this.commandContext)) {
                continue;
            }
            String displayCode = shellScript.getArthasCommand(commandContext);
            CustomComboBoxItem<ShellScriptCommandEnum> boxItem = new CustomComboBoxItem<ShellScriptCommandEnum>();
            boxItem.setContentObject(shellScript);
            boxItem.setDisplay(displayCode);
            boxItem.setTipText(shellScript.getEnumMsg());
            shellScriptComboBox.addItem(boxItem);
        }
    }

    /**
     * 关闭窗口
     */
    private void doCloseDialog() {
        AppSettingsState instance = AppSettingsState.getInstance(project);
        if ("y".equalsIgnoreCase(instance.scriptDialogCloseWhenSelectedCommand)) {
            dispose();
        }
    }

    /**
     * 常用脚本添加
     */
    @SuppressWarnings("unchecked")
    private void commonScriptInit() {
        boolean constantLabel = false;
        for (ShellScriptConstantEnum scriptConstantEnum : ShellScriptConstantEnum.values()) {
            CustomComboBoxItem<ShellScriptConstantEnum> boxItem = new CustomComboBoxItem<ShellScriptConstantEnum>();
            boxItem.setContentObject(scriptConstantEnum);
            boxItem.setDisplay(scriptConstantEnum.getCode());
            boxItem.setTipText(scriptConstantEnum.getEnumMsg());
            commonShellScriptComboBox.addItem(boxItem);
            if (!constantLabel) {
                constantLabel = true;
                this.constantLabel.setText(scriptConstantEnum.getEnumMsg());
            }

        }
        commonShellScriptComboBox.addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                if (e.getItem() instanceof CustomComboBoxItem) {
                    CustomComboBoxItem item = (CustomComboBoxItem) e.getItem();
                    this.constantLabel.setText(item.getTipText());
                }
            }
        });
        commonShellScriptComboBox.setRenderer(new CustomDefaultListCellRenderer(commonShellScriptComboBox));
        commonShellScriptCommandButton.addActionListener(e -> {
            Object selectedItem = commonShellScriptComboBox.getSelectedItem();
            assert selectedItem != null;
            String selectedItemStr = selectedItem.toString();
            if (StringUtils.isNotBlank(selectedItemStr)) {
                CommonExecuteScriptUtils.executeCommonScript(project, "", selectedItemStr, "");
            }
            this.doCloseDialog();
        });
        commonCopyCommandButton.addActionListener(e -> {
            Object selectedItem = commonShellScriptComboBox.getSelectedItem();
            assert selectedItem != null;
            String selectedItemStr = selectedItem.toString();
            ClipboardUtils.setClipboardString(selectedItemStr);
            NotifyUtils.notifyMessage(project, COMMAND_COPIED + "(some of the batch scripts cannot be executed need to be modified manually)");


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

    public Project getProject() {
        return project;
    }

    public void setProject(Project project) {
        this.project = project;
    }

}
