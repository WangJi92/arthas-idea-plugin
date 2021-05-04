package com.github.wangji92.arthas.plugin.ui;

import com.github.wangji92.arthas.plugin.common.combox.CustomComboBoxItem;
import com.github.wangji92.arthas.plugin.common.enums.ShellScriptCommandEnum;
import com.github.wangji92.arthas.plugin.utils.CommonExecuteScriptUtils;
import com.github.wangji92.arthas.plugin.utils.StringUtils;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.WindowManager;

import javax.swing.*;
import javax.swing.plaf.basic.BasicComboBoxEditor;
import java.awt.*;
import java.awt.event.*;

public class ArthasShellScriptCommandDialog extends JDialog {
    private JPanel contentPane;
    private JComboBox shellScriptComboBox;
    private JButton shellScriptCommandButton;

    private Project project;

    private String className;

    private String fieldName;
    private String methodName;

    private String methodInfo;

    public ArthasShellScriptCommandDialog(Project project, String className, String fieldName, String methodName, String methodInfo) {
        this.className = className;
        this.project = project;
        this.fieldName = fieldName;
        this.methodName = methodName;
        this.methodInfo = methodInfo;
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

        init();
    }

    @SuppressWarnings("unchecked")
    private void init() {
        shellScriptCommandButton.addActionListener(e -> {
            Object selectedItem = shellScriptComboBox.getSelectedItem();
            assert selectedItem != null;
            String selectedItemStr = selectedItem.toString();
            String scCommand = "";
            if (selectedItemStr.contains("${CLASSLOADER_HASH_VALUE}")) {
                scCommand = String.join(" ", "sc", "-d", this.className);
            }
            if (StringUtils.isNotBlank(selectedItemStr)) {
                CommonExecuteScriptUtils.executeCommonScript(project, scCommand, selectedItemStr, "");
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
            if (shellScript.getNeedHash() && StringUtils.isBlank(this.className)) {
                continue;
            }
            String codeValue = shellScript.getCode();
            if (shellScript.getNeedClass()) {
                codeValue = codeValue.replace("${className}", this.className);
            }
            if (shellScript.getNeedField()) {
                codeValue = codeValue.replace("${fieldName}", this.fieldName);
            }
            if (shellScript.getNeedMethod()) {
                codeValue = codeValue.replace("${methodInfo}", this.methodInfo);
            }
            if (shellScript.getNeedMethod()) {
                codeValue = codeValue.replace("${methodName}", this.methodName);
            }
            if (shellScript.getNeedHash()) {
                codeValue = String.join(" ", codeValue, "-c", "${CLASSLOADER_HASH_VALUE}");
            }
            CustomComboBoxItem<ShellScriptCommandEnum> boxItem = new CustomComboBoxItem<ShellScriptCommandEnum>();
            boxItem.setContentObject(shellScript);
            boxItem.setDisplay(codeValue);
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

    public String getMethodInfo() {
        return methodInfo;
    }

    public void setMethodInfo(String methodInfo) {
        this.methodInfo = methodInfo;
    }
}
