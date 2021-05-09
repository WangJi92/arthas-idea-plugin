package com.github.wangji92.arthas.plugin.ui;

import com.github.wangji92.arthas.plugin.common.combox.CustomComboBoxItem;
import com.github.wangji92.arthas.plugin.common.combox.CustomDefaultListCellRenderer;
import com.github.wangji92.arthas.plugin.common.enums.ShellScriptCommandEnum;
import com.github.wangji92.arthas.plugin.common.enums.ShellScriptConstantEnum;
import com.github.wangji92.arthas.plugin.common.enums.ShellScriptVariableEnum;
import com.github.wangji92.arthas.plugin.common.param.ScriptParam;
import com.github.wangji92.arthas.plugin.constants.ArthasCommandConstants;
import com.github.wangji92.arthas.plugin.setting.AppSettingsState;
import com.github.wangji92.arthas.plugin.utils.*;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.WindowManager;

import javax.swing.*;
import java.awt.event.*;
import java.util.HashMap;
import java.util.Map;

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

    private Project project;

    private ScriptParam scriptParam;

    private String className;


    /**
     * 当前执行上下文
     */
    private Map<String, String> contextParams = new HashMap<>(10);

    public ArthasShellScriptCommandDialog(ScriptParam scriptParam) {
        this.project = scriptParam.getProject();
        this.scriptParam = scriptParam;
        setContentPane(contentPane);
//        setMinimumSize(new Dimension(800,400));
//        setMaximumSize(new Dimension(1000,400));
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
        initContextParam();

        init();
    }

    /**
     * 初始化上下文信息
     */
    private void initContextParam() {
        AppSettingsState instance = AppSettingsState.getInstance(project);
        String methodName = OgnlPsUtils.getMethodName(scriptParam.getPsiElement());
        String className = OgnlPsUtils.getCommonOrInnerOrAnonymousClassName(scriptParam.getPsiElement());
        this.className = className;
        String fieldName = OgnlPsUtils.getFieldName(scriptParam.getPsiElement());
        String executeInfo = OgnlPsUtils.getExecuteInfo(scriptParam.getPsiElement());
        Map<String, String> params = new HashMap<>(10);
        params.put(ShellScriptVariableEnum.PROPERTY_DEPTH.getEnumMsg(), instance.depthPrintProperty);
        params.put(ShellScriptVariableEnum.CLASS_NAME.getEnumMsg(), className);
        params.put(ShellScriptVariableEnum.METHOD_NAME.getEnumMsg(), methodName);
        params.put(ShellScriptVariableEnum.EXECUTE_INFO.getEnumMsg(), executeInfo);
        params.put(ShellScriptVariableEnum.FIELD_NAME.getEnumMsg(), fieldName);
        params.put(ShellScriptVariableEnum.SPRING_CONTEXT.getEnumMsg(), instance.staticSpringContextOgnl);
        params.put(ShellScriptVariableEnum.INVOKE_COUNT.getEnumMsg(), instance.invokeCount);
        params.put(ShellScriptVariableEnum.INVOKE_MONITOR_COUNT.getEnumMsg(), instance.invokeMonitorCount);
        params.put(ShellScriptVariableEnum.INVOKE_MONITOR_INTERVAL.getEnumMsg(), instance.invokeMonitorInterval);
        String skpJdkMethodCommand = instance.traceSkipJdk ? "" : ArthasCommandConstants.DEFAULT_SKIP_JDK_FALSE;
        params.put(ShellScriptVariableEnum.SKIP_JDK_METHOD.getEnumMsg(), skpJdkMethodCommand);
        String printConditionExpress = instance.printConditionExpress ? "-v" : "";
        params.put(ShellScriptVariableEnum.PRINT_CONDITION_RESULT.getEnumMsg(), printConditionExpress);
        params.put(ShellScriptVariableEnum.CLASSLOADER_HASH_VALUE.getEnumMsg(), "${CLASSLOADER_HASH_VALUE}");
        String methodNameNotStar = "*".equals(methodName) ? "" : methodName;
        params.put(ShellScriptVariableEnum.METHOD_NAME_NOT_STAR.getEnumMsg(), methodNameNotStar);
        String conditionExpressDisplay = instance.conditionExpressDisplay ? ArthasCommandConstants.DEFAULT_CONDITION_EXPRESS : "";
        params.put(ShellScriptVariableEnum.CONDITION_EXPRESS_DEFAULT.getEnumMsg(), conditionExpressDisplay);
        String beanName = OgnlPsUtils.getSpringBeanName(scriptParam.getPsiElement());
        params.put(ShellScriptVariableEnum.SPRING_BEAN_NAME.getEnumMsg(), beanName);
        params.put(ShellScriptVariableEnum.DEFAULT_FIELD_VALUE.getEnumMsg(), OgnlPsUtils.getFieldDefaultValue(scriptParam.getPsiElement()));
        if (StringUtils.isNotBlank(fieldName)) {
            String capitalizeFieldName = StringUtils.capitalize(fieldName);
            params.put(ShellScriptVariableEnum.CAPITALIZE_FIELD_VALUE.getEnumMsg(), capitalizeFieldName);
        }
        this.contextParams = params;
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
            if (selectedItemStr.contains(ShellScriptVariableEnum.CLASSLOADER_HASH_VALUE.getCode())) {
                scCommand = String.join(" ", "sc", "-d", this.className);
                if (selectedItemStr.contains("#springContext=")) {
                    // 获取class的classloader @applicationContextProvider@context的前面部分 xxxApplicationContextProvider
                    String springContextClassName = SpringStaticContextUtils.getStaticSpringContextClassName(project);
                    scCommand = String.join(" ", "sc", "-d", springContextClassName);
                } else if (selectedItemStr.contains("logger --name")) {
                    scCommand = String.join(" ", "logger", "--name", this.className);
                }
            }
            // 这里再次处理一下上下文信息
            String finalStr = StringUtils.stringSubstitutorFromText(selectedItemStr, contextParams);
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
            if (selectedItemStr.contains(ShellScriptVariableEnum.CLASSLOADER_HASH_VALUE.getCode())) {
                NotifyUtils.notifyMessage(project, "命令已复制到剪切板,部分命令需要classloader hash value 直接执行不可以");
            } else {
                NotifyUtils.notifyMessage(project, "命令已复制到剪切板,到服务启动arthas 粘贴执行");
            }


        });
        shellScriptComboBox.addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                if (e.getItem() instanceof CustomComboBoxItem) {
                    CustomComboBoxItem item = (CustomComboBoxItem) e.getItem();
                    dyTipLabel.setText(item.getTipText());
                }

            }
        });
        shellScriptComboBox.setRenderer(new CustomDefaultListCellRenderer(shellScriptComboBox));
        for (ShellScriptCommandEnum shellScript : ShellScriptCommandEnum.values()) {
            if (!shellScript.support(this.scriptParam)) {
                continue;
            }
            String codeValue = shellScript.getCode();
            String displayCode = StringUtils.stringSubstitutorFromText(codeValue, contextParams);
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
        for (ShellScriptConstantEnum scriptConstantEnum : ShellScriptConstantEnum.values()) {
            CustomComboBoxItem<ShellScriptConstantEnum> boxItem = new CustomComboBoxItem<ShellScriptConstantEnum>();
            boxItem.setContentObject(scriptConstantEnum);
            boxItem.setDisplay(scriptConstantEnum.getCode());
            boxItem.setTipText(scriptConstantEnum.getEnumMsg());
            commonShellScriptComboBox.addItem(boxItem);
        }
        commonShellScriptComboBox.addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                if (e.getItem() instanceof CustomComboBoxItem) {
                    CustomComboBoxItem item = (CustomComboBoxItem) e.getItem();
                    constantLabel.setText(item.getTipText());
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
            if (selectedItemStr.contains(ShellScriptVariableEnum.CLASSLOADER_HASH_VALUE.getCode())) {
                NotifyUtils.notifyMessage(project, "命令已复制到剪切板,部分命令需要classloader hash value 直接执行不可以");
            } else {
                NotifyUtils.notifyMessage(project, "命令已复制到剪切板,到服务启动arthas 粘贴执行（部分为批量脚本不能执行，需手动修改)");
            }


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
