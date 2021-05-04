package com.github.wangji92.arthas.plugin.ui;

import com.github.wangji92.arthas.plugin.common.combox.CustomComboBoxItem;
import com.github.wangji92.arthas.plugin.common.enums.ShellScriptCommandEnum;
import com.github.wangji92.arthas.plugin.common.enums.ShellScriptVariableEnum;
import com.github.wangji92.arthas.plugin.constants.ArthasCommandConstants;
import com.github.wangji92.arthas.plugin.setting.AppSettingsState;
import com.github.wangji92.arthas.plugin.utils.CommonExecuteScriptUtils;
import com.github.wangji92.arthas.plugin.utils.StringUtils;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.WindowManager;

import javax.swing.*;
import javax.swing.plaf.basic.BasicComboBoxEditor;
import java.awt.*;
import java.awt.event.*;
import java.util.HashMap;
import java.util.Map;

public class ArthasShellScriptCommandDialog extends JDialog {
    private JPanel contentPane;
    private JComboBox shellScriptComboBox;
    private JButton shellScriptCommandButton;

    private Project project;

    private String className;

    private String fieldName;
    private String methodName;

    private String executeInfo;

    private boolean modifierStatic;


    private Map<String, String> contextParams = new HashMap<>(10);

    public ArthasShellScriptCommandDialog(Project project, String className, String fieldName, String methodName, String executeInfo, boolean modifierStatic) {
        this.className = className;
        this.project = project;
        this.fieldName = fieldName;
        this.methodName = methodName;
        this.executeInfo = executeInfo;
        this.modifierStatic = modifierStatic;
        setContentPane(contentPane);
        setModal(true);
        getRootPane().setDefaultButton(shellScriptCommandButton);

        shellScriptCommandButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                onOK();
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
        Map<String, String> params = new HashMap<>(10);
        params.put(ShellScriptVariableEnum.PROPERTY_DEPTH.getEnumMsg(), instance.depthPrintProperty);
        params.put(ShellScriptVariableEnum.CLASS_NAME.getEnumMsg(), this.className);
        params.put(ShellScriptVariableEnum.METHOD_NAME.getEnumMsg(), this.methodName);
        params.put(ShellScriptVariableEnum.FIELD_NAME.getEnumMsg(), this.fieldName);
        params.put(ShellScriptVariableEnum.SPRING_CONTEXT.getEnumMsg(), instance.staticSpringContextOgnl);
        params.put(ShellScriptVariableEnum.INVOKE_COUNT.getEnumMsg(), instance.invokeCount);
        params.put(ShellScriptVariableEnum.INVOKE_MONITOR_COUNT.getEnumMsg(), instance.invokeMonitorCount);
        params.put(ShellScriptVariableEnum.INVOKE_MONITOR_INTERVAL.getEnumMsg(), instance.invokeMonitorInterval);
        String skpJdkMethodCommand = instance.traceSkipJdk ? "" : ArthasCommandConstants.DEFAULT_SKIP_JDK_FALSE;
        params.put(ShellScriptVariableEnum.SKIP_JDK_METHOD.getEnumMsg(), skpJdkMethodCommand);
        String conditionExpressDisplay = instance.conditionExpressDisplay ? ArthasCommandConstants.DEFAULT_CONDITION_EXPRESS : "";
        params.put(ShellScriptVariableEnum.PRINT_CONDITION_RESULT.getEnumMsg(), conditionExpressDisplay);
        params.put(ShellScriptVariableEnum.EXECUTE_INFO.getEnumMsg(), conditionExpressDisplay);
        params.put(ShellScriptVariableEnum.CLASSLOADER_HASH_VALUE.getEnumMsg(), "${CLASSLOADER_HASH_VALUE}");
        this.contextParams = params;
    }

    @SuppressWarnings("unchecked")
    private void init() {
        shellScriptCommandButton.addActionListener(e -> {
            Object selectedItem = shellScriptComboBox.getSelectedItem();
            assert selectedItem != null;
            String selectedItemStr = selectedItem.toString();
            String scCommand = "";
            if (selectedItemStr.contains(ShellScriptVariableEnum.CLASSLOADER_HASH_VALUE.getCode())) {
                scCommand = String.join(" ", "sc", "-d", this.className);
            }
            // 这里再次处理一下上下文信息
            String finalStr = StringUtils.stringSubstitutorFromText(selectedItemStr, contextParams);
            if (StringUtils.isNotBlank(finalStr)) {
                CommonExecuteScriptUtils.executeCommonScript(project, scCommand, finalStr, "");
            }
        });
        //值太长展示不全处理 https://www.java-forums.org/awt-swing/16196-item-too-big-jshellScriptComboBox.html
        shellScriptComboBox.setRenderer(new DefaultListCellRenderer() {

            @Override
            public Component getListCellRendererComponent(JList list, Object value,
                                                          int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index,
                        isSelected, cellHasFocus);
                String tipText = value.toString();
                if (value instanceof CustomComboBoxItem) {
                    tipText = ((CustomComboBoxItem) value).getTipText();
                }
                if (isSelected) {
                    shellScriptComboBox.setToolTipText(tipText);
                }
                setToolTipText(tipText);
                return this;
            }

        });
        shellScriptComboBox.setEditor(new BasicComboBoxEditor());


        for (ShellScriptCommandEnum shellScript : ShellScriptCommandEnum.values()) {
            if (shellScript.getNeedClass() && StringUtils.isBlank(this.className)) {
                continue;
            }
            if (shellScript.getNeedField() && StringUtils.isBlank(this.fieldName)) {
                continue;
            }
            if (shellScript.getNeedMethod() && StringUtils.isBlank(this.methodName)) {
                continue;
            }
            if (shellScript.getNeedStatic() && Boolean.FALSE.equals(this.modifierStatic)) {
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
        setMinimumSize(new Dimension(800, 50));
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

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getFieldName() {
        return fieldName;
    }

    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }

    public String getExecuteInfo() {
        return executeInfo;
    }

    public void setExecuteInfo(String executeInfo) {
        this.executeInfo = executeInfo;
    }
}
