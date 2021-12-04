package com.github.wangji92.arthas.plugin.ui;

import com.github.wangji92.arthas.plugin.common.combox.CustomComboBoxItem;
import com.github.wangji92.arthas.plugin.common.combox.CustomDefaultListCellRenderer;
import com.github.wangji92.arthas.plugin.constants.ArthasCommandConstants;
import com.github.wangji92.arthas.plugin.setting.AppSettingsState;
import com.github.wangji92.arthas.plugin.utils.*;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.WindowManager;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiEnumConstant;
import com.intellij.psi.PsiField;
import com.intellij.psi.PsiMethod;
import com.intellij.ui.components.labels.LinkLabel;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import javax.swing.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class ArthasOgnlEnumActionDialog extends JDialog {
    private JPanel contentPane;

    /**
     * 关闭按钮
     */
    private JButton shellCommandButton;

    /**
     * 复杂子类的信息
     */
    private JButton copyScCommand;
    /**
     * 枚举子类的方法列表
     */
    private JComboBox enumChildClazzMethodComboBox;
    private JTextField classloaderHashField;
    private JButton clearCacheButton;
    private JButton copyCommandButton;
    private LinkLabel ognlHelp;
    private LinkLabel ognlSpecial;
    private LinkLabel scClassloader;
    private JLabel tipField;


    private Project project;

    private PsiClass parentEnumClazz;


    private String className;


    private OgnlEnumCommandRequest ognlEnumCommandRequest;


    public ArthasOgnlEnumActionDialog(OgnlEnumCommandRequest ognlEnumCommandRequest) {
        this.project = ognlEnumCommandRequest.project;
        this.ognlEnumCommandRequest = ognlEnumCommandRequest;
        this.parentEnumClazz = ognlEnumCommandRequest.getParentEnumClazz();
        this.className = OgnlPsUtils.getCommonOrInnerOrAnonymousClassName(parentEnumClazz);
        setContentPane(contentPane);
        setModal(true);
        getRootPane().setDefaultButton(shellCommandButton);

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
        this.shellCommandButton.addActionListener(e -> onCancel());
        initClassLoaderHashValue();
        initShellCommand();
        initCopyCommand();

        initEnumInfo();

    }

    private void initEnumInfo() {
        this.parentEnumClazz = this.ognlEnumCommandRequest.getParentEnumClazz();
        String parentClazzName = OgnlPsUtils.getCommonOrInnerOrAnonymousClassName(this.ognlEnumCommandRequest.getParentEnumClazz());

        List<CustomComboBoxItem> customComboBoxItems = new ArrayList<>();
        //region 获取可以执行的静态字段
        if (parentEnumClazz.getFields().length != 0) {
            final List<PsiField> staticNonEnumStaticField = Arrays.stream(parentEnumClazz.getFields()).filter(OgnlPsUtils::isStaticField).collect(Collectors.toList());
            staticNonEnumStaticField.forEach(psiField -> {
                final String executeInfo = OgnlPsUtils.getExecuteInfo(psiField);
                CustomComboBoxItem<String> boxItem = new CustomComboBoxItem<String>();
                boxItem.setDisplay(String.format("@%s@%s", parentClazzName, executeInfo));
                boxItem.setTipText("static filed=" + executeInfo);
                if (psiField instanceof PsiEnumConstant) {
                    boxItem.setTipText("enum's class instance " + executeInfo);
                }
                customComboBoxItems.add(boxItem);
            });
        }
        //endregion

        //region 获取可以执行的静态方法
        if (parentEnumClazz.getMethods().length != 0) {
            final List<PsiMethod> staticMethodList = Arrays.stream(parentEnumClazz.getMethods()).filter(OgnlPsUtils::isStaticMethod).collect(Collectors.toList());

            staticMethodList.forEach(psiMethod -> {
                final String executeInfo = OgnlPsUtils.getExecuteInfo(psiMethod);
                CustomComboBoxItem<String> boxItem = new CustomComboBoxItem<String>();
                boxItem.setDisplay(String.format("@%s@%s", parentClazzName, executeInfo));
                boxItem.setTipText("static method=" + OgnlPsUtils.getMethodName(psiMethod) + " you can edit method params");
                customComboBoxItems.add(boxItem);
            });
        }

        //endregion

        //region 执行非静态字段
        final List<PsiField> enumFieldList = Arrays.stream(parentEnumClazz.getFields()).filter(psiField -> psiField instanceof PsiEnumConstant).collect(Collectors.toList());
        final List<PsiField> nonStaticFieldList = Arrays.stream(parentEnumClazz.getFields()).filter(psiField -> OgnlPsUtils.isNonStaticField(psiField) && !(psiField instanceof PsiEnumConstant)).collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(nonStaticFieldList)) {
            enumFieldList.forEach(enumField -> {
                nonStaticFieldList.forEach(nonStaticField -> {
                    //ognl xxxClass@xxxField.field
                    final String enumFieldExecuteInfo = OgnlPsUtils.getExecuteInfo(enumField);
                    final String nonStaticFieldExecuteInfo = OgnlPsUtils.getExecuteInfo(nonStaticField);
                    CustomComboBoxItem<String> boxItem = new CustomComboBoxItem<String>();
                    boxItem.setDisplay(String.format("@%s@%s.%s", parentClazzName, enumFieldExecuteInfo, nonStaticFieldExecuteInfo));
                    boxItem.setTipText("enum's class " + enumFieldExecuteInfo + " instance non static field=" + nonStaticFieldExecuteInfo);
                    customComboBoxItems.add(boxItem);
                });
            });
        }
        //endregion


        //region 非静态方法
        final List<PsiMethod> nonStaticMethodList = Arrays.stream(parentEnumClazz.getMethods()).filter(psiFileMethod -> OgnlPsUtils.isNonStaticMethod(psiFileMethod) && !OgnlPsUtils.isConstructor(psiFileMethod)).collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(nonStaticMethodList)) {
            enumFieldList.forEach(enumField -> {
                nonStaticMethodList.forEach(nonStaticMethod -> {
                    //ognl xxxClass@xxxField.method()
                    final String enumFieldExecuteInfo = OgnlPsUtils.getExecuteInfo(enumField);
                    final String nonStaticMethodExecuteInfo = OgnlPsUtils.getExecuteInfo(nonStaticMethod);
                    CustomComboBoxItem<String> boxItem = new CustomComboBoxItem<String>();
                    boxItem.setDisplay(String.format("@%s@%s.%s", parentClazzName, enumFieldExecuteInfo, nonStaticMethodExecuteInfo));
                    boxItem.setTipText("enum's class " + enumFieldExecuteInfo + " instance non static method=" + OgnlPsUtils.getMethodName(nonStaticMethod)+" you can edit method params");
                    customComboBoxItems.add(boxItem);
                });
            });
        }
        //endregion

        AppSettingsState instance = AppSettingsState.getInstance(project);
        String depthPrintProperty = instance.depthPrintProperty;
        String join = String.join(" ", "ognl", "-x", depthPrintProperty);

        enumChildClazzMethodComboBox.setRenderer(new CustomDefaultListCellRenderer(enumChildClazzMethodComboBox, this.tipField));
        customComboBoxItems.forEach(customComboBoxItem -> {
            final String display = customComboBoxItem.getDisplay();
            final String finalName = String.join(" ", join, "'", display + "'");
            customComboBoxItem.setDisplay(finalName);
            customComboBoxItem.setContentObject(display);
            enumChildClazzMethodComboBox.addItem(customComboBoxItem);
            if (display.equals(ognlEnumCommandRequest.getSelectKey())) {
                enumChildClazzMethodComboBox.setSelectedItem(customComboBoxItem);
                tipField.setText(customComboBoxItem.getTipText());
                tipField.setToolTipText(customComboBoxItem.getTipText());
            }
        });
    }

    private void initCopyCommand() {
        copyCommandButton.addActionListener(e -> {
            String hashClassloader = classloaderHashField.getText();
            String ognlCurrentExpression = enumChildClazzMethodComboBox.getSelectedItem().toString();
            if (StringUtils.isNotBlank(hashClassloader) && ognlCurrentExpression != null && !ognlCurrentExpression.contains("-c")) {
                ognlCurrentExpression = ognlCurrentExpression + " -c " + hashClassloader;
                PropertiesComponentUtils.setValue(project, ArthasCommandConstants.CLASSLOADER_HASH_VALUE, hashClassloader);
            }
            if (StringUtils.isNotBlank(ognlCurrentExpression)) {
                ClipboardUtils.setClipboardString(ognlCurrentExpression);
                NotifyUtils.notifyMessageDefault(project);
            }
            dispose();
        });
    }

    private void initClassLoaderHashValue() {
        String classloaderHash = PropertiesComponentUtils.getValue(project, ArthasCommandConstants.CLASSLOADER_HASH_VALUE);
        classloaderHashField.setText(classloaderHash);
        //clear cache的classloader的hash值的信息
        clearCacheButton.addActionListener(e -> onClearClassLoaderHashValue());

        copyScCommand.addActionListener(e -> onCopyScCommand());
    }

    private void initShellCommand() {
        shellCommandButton.addActionListener(e -> {
            String ognCurrentExpression = enumChildClazzMethodComboBox.getSelectedItem().toString();
            String scCommand = String.join(" ", "sc", "-d", className);
            CommonExecuteScriptUtils.executeCommonScript(project, scCommand, ognCurrentExpression, "");
            dispose();
        });
    }

    private void onClearClassLoaderHashValue() {
        classloaderHashField.setText("");
        PropertiesComponentUtils.setValue(project, ArthasCommandConstants.CLASSLOADER_HASH_VALUE, "");
    }

    private void onCopyScCommand() {
        String command = String.join(" ", "sc", "-d", className);
        ClipboardUtils.setClipboardString(command);
        NotifyUtils.notifyMessageDefault(project);
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
        scClassloader = ActionLinkUtils.newActionLink("https://arthas.aliyun.com/doc/sc.html");
        ognlHelp = ActionLinkUtils.newActionLink("https://arthas.aliyun.com/doc/ognl.html");
        ognlSpecial = ActionLinkUtils.newActionLink("https://github.com/alibaba/arthas/issues/71");
    }


    public static class OgnlEnumCommandRequest {
        /**
         * 工程信息
         */
        private Project project;
        /**
         * 父枚举信息
         */
        private PsiClass parentEnumClazz;

        private String selectKey;


        public Project getProject() {
            return project;
        }

        public void setProject(Project project) {
            this.project = project;
        }

        public PsiClass getParentEnumClazz() {
            return parentEnumClazz;
        }

        public void setParentEnumClazz(PsiClass parentEnumClazz) {
            this.parentEnumClazz = parentEnumClazz;
        }

        public String getSelectKey() {
            return selectKey;
        }

        public void setSelectKey(String selectKey) {
            this.selectKey = selectKey;
        }
    }
}
