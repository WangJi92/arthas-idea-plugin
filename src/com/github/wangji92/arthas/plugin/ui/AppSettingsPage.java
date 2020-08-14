package com.github.wangji92.arthas.plugin.ui;

import com.github.wangji92.arthas.plugin.setting.AppSettingsState;
import com.github.wangji92.arthas.plugin.utils.NotifyUtils;
import com.github.wangji92.arthas.plugin.utils.StringUtils;
import com.intellij.icons.AllIcons;
import com.intellij.ide.BrowserUtil;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.project.Project;
import com.intellij.ui.components.labels.ActionLink;
import com.intellij.ui.components.labels.LinkLabel;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

/**
 * @author 汪小哥
 * @date 15-08-2020
 */
public class AppSettingsPage implements Configurable {
    /**
     * arthas 的设置
     */
    private JTextField springContextStaticOgnlExpressionTextFiled;
    /**
     * arthas -n
     */
    private JSpinner invokeCountField;
    private LinkLabel linkLabel;

    private JPanel contentPane;
    /**
     * 跳过jdk trace
     */
    private JRadioButton traceSkipJdkButton;
    /**
     * 调用次数
     */
    private JSpinner invokeMonitorCountField;
    /**
     * 时间间隔
     */
    private JSpinner invokeMonitorIntervalField;

    /**
     * 打印属性的深度
     */
    private JSpinner depthPrintPropertyField;

    @Nls(capitalization = Nls.Capitalization.Title)
    @Override
    public String getDisplayName() {
        return "Arthas Idea Plugin";
    }

    @Override
    public JComponent getPreferredFocusedComponent() {
        return springContextStaticOgnlExpressionTextFiled;
    }

    private Project project;

    /**
     * 设置信息
     */
    private AppSettingsState settings;


    public AppSettingsPage(Project project) {
        this.project = project;
        settings = AppSettingsState.getInstance(this.project);
        springContextStaticOgnlExpressionTextFiled.setText(settings.staticSpringContextOgnl);
        invokeCountField.setValue(1);


    }

    private void createUIComponents() {
        linkLabel = new ActionLink("", AllIcons.Ide.Link, new AnAction() {
            @Override
            public void actionPerformed(AnActionEvent anActionEvent) {
                BrowserUtil.browse("https://github.com/WangJi92/arthas-plugin-demo/blob/master/src/main/java/com/wangji92/arthas/plugin/demo/common/ApplicationContextProvider.java");
            }
        });
        linkLabel.setPaintUnderline(false);
    }

    @Nullable
    @Override
    public JComponent createComponent() {
        return contentPane;
    }

    @Override
    public boolean isModified() {
        return !springContextStaticOgnlExpressionTextFiled.getText().equals(settings.staticSpringContextOgnl)
                || !invokeCountField.getValue().toString().equals(settings.invokeCount)
                || !invokeMonitorCountField.getValue().toString().equals(settings.invokeMonitorCount)
                || !invokeMonitorIntervalField.getValue().toString().equals(settings.invokeMonitorInterval)
                || !depthPrintPropertyField.getValue().toString().equals(settings.depthPrintProperty)
                || traceSkipJdkButton.isSelected() != settings.traceSkipJdk;
    }

    @Override
    public void apply() {
        saveSettings();
    }

    @Override
    public void reset() {
        loadSettings();
    }

    private void saveSettings() {
        StringBuilder error = new StringBuilder("");
        String staticOgnlExpressionTextFiledText = springContextStaticOgnlExpressionTextFiled.getText();
        if (StringUtils.isBlank(staticOgnlExpressionTextFiledText) || !staticOgnlExpressionTextFiledText.contains("@")) {
            error.append("配置静态spring context 错误");
        } else {
            settings.staticSpringContextOgnl = springContextStaticOgnlExpressionTextFiled.getText();
        }
        if (((int) invokeCountField.getValue()) <= 0) {
            error.append("invokeCountField <= 0 ");
        } else {
            settings.invokeCount = invokeCountField.getValue().toString();
        }
        if (((int) invokeMonitorCountField.getValue()) <= 0) {
            error.append("invokeMonitorCount <= 0 ");
        } else {
            settings.invokeMonitorCount = invokeMonitorCountField.getValue().toString();
        }
        if (((int) invokeMonitorIntervalField.getValue()) <= 0) {
            error.append("invokeMonitorCount <= 0 ");
        } else {
            settings.invokeMonitorInterval = invokeMonitorIntervalField.getValue().toString();
        }
        if (((int) depthPrintPropertyField.getValue()) <= 0) {
            error.append("invokeMonitorCount <= 0 ");
        } else {
            settings.depthPrintProperty = depthPrintPropertyField.getValue().toString();
        }
        settings.traceSkipJdk = traceSkipJdkButton.isSelected();

        if (StringUtils.isNotBlank(error)) {
            NotifyUtils.notifyMessage(project, error.toString(), NotificationType.ERROR);
        }

    }

    private void loadSettings() {
        springContextStaticOgnlExpressionTextFiled.setText(settings.staticSpringContextOgnl);
        invokeCountField.setValue(Integer.parseInt(settings.invokeCount));
        invokeMonitorCountField.setValue(Integer.parseInt(settings.invokeMonitorCount));
        invokeMonitorIntervalField.setValue(Integer.parseInt(settings.invokeMonitorInterval));
        depthPrintPropertyField.setValue(Integer.parseInt(settings.depthPrintProperty));
        traceSkipJdkButton.setSelected(settings.traceSkipJdk);
    }

    @Override
    public void disposeUIResources() {
        contentPane = null;
    }


}
