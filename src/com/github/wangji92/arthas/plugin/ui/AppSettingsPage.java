package com.github.wangji92.arthas.plugin.ui;

import com.aliyun.oss.OSS;
import com.github.wangji92.arthas.plugin.constants.ArthasCommandConstants;
import com.github.wangji92.arthas.plugin.setting.AppSettingsState;
import com.github.wangji92.arthas.plugin.utils.AliyunOssUtils;
import com.github.wangji92.arthas.plugin.utils.NotifyUtils;
import com.github.wangji92.arthas.plugin.utils.PropertiesComponentUtils;
import com.github.wangji92.arthas.plugin.utils.StringUtils;
import com.intellij.icons.AllIcons;
import com.intellij.ide.BrowserUtil;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.project.Project;
import com.intellij.ui.JBColor;
import com.intellij.ui.components.labels.ActionLink;
import com.intellij.ui.components.labels.LinkLabel;
import org.codehaus.groovy.runtime.StackTraceUtils;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

/**
 * https://jetbrains.org/intellij/sdk/docs/reference_guide/settings_guide.html 属性配置 参考
 * https://github.com/pwielgolaski/shellcheck-plugin
 *
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
    private JRadioButton traceSkipJdkRadio;
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
    /**
     * 是否展示默认的条件表达式
     */
    private JRadioButton conditionExpressDisplayRadio;

    private JTextField selectProjectNameTextField;

    private LinkLabel selectLink;
    private LinkLabel batchSupportLink;
    private LinkLabel redefineHelpLinkLabel;
    private LinkLabel ossHelpLink;
    /**
     * 主pane
     */
    private JTabbedPane settingTabPane;
    /**
     * 基础设置pane
     */
    private JPanel basicSettingPane;
    /**
     * 热更新 面板
     */
    private JPanel hotRedefineSettingPane;
    /**
     * 设置选中 剪切板
     */
    private JRadioButton clipboardRadioButton;
    /**
     * 设置选中 阿里云
     */
    private JRadioButton aliYunOssRadioButton;
    /**
     * oss 配置信息 Endpoint
     */
    private JTextField ossEndpointTextField;
    /**
     * oss 配置信息 AccessKeyId
     */
    private JPasswordField ossAccessKeyIdPasswordField;
    /**
     * oss 配置信息 AccessKeySecret
     */
    private JPasswordField ossAccessKeySecretPasswordField;
    /**
     * oss 配置信息 DirectoryPrefix
     */
    private JTextField ossDirectoryPrefixTextField;
    /**
     * oss 配置信息 BucketName
     */
    private JTextField ossBucketNameTextField;
    /**
     * 检测 oss 配置是否正确 button
     */
    private JButton ossSettingCheckButton;

    /**
     * 检测异常的信息
     */
    private JLabel ossCheckMsgLabel;

    /**
     * 阿里云Oss Setting Pane
     */
    private JPanel aliyunOssSettingPane;
    /**
     * 全局spring context 开关
     */
    private JRadioButton springContextGlobalSettingRadioButton;
    /**
     * oss 全局开关
     */
    private JRadioButton ossGlobalSettingRadioButton;


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

    /**
     * 自动构造  idea 会携带当前的project 参数信息
     *
     * @param project
     */
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

        selectLink = new ActionLink("", AllIcons.Ide.Link, new AnAction() {
            @Override
            public void actionPerformed(AnActionEvent anActionEvent) {
                BrowserUtil.browse("https://arthas.aliyun.com/doc/advanced-use.html");
            }
        });
        selectLink.setPaintUnderline(false);

        batchSupportLink = new ActionLink("", AllIcons.Ide.Link, new AnAction() {
            @Override
            public void actionPerformed(AnActionEvent anActionEvent) {
                BrowserUtil.browse("https://arthas.aliyun.com/doc/batch-support.html");
            }
        });
        batchSupportLink.setPaintUnderline(false);

        redefineHelpLinkLabel = new ActionLink("", AllIcons.Ide.Link, new AnAction() {
            @Override
            public void actionPerformed(AnActionEvent anActionEvent) {
                BrowserUtil.browse("https://arthas.aliyun.com/doc/redefine.html#");
            }
        });
        redefineHelpLinkLabel.setPaintUnderline(false);

        ossHelpLink = new ActionLink("", AllIcons.Ide.Link, new AnAction() {
            @Override
            public void actionPerformed(AnActionEvent anActionEvent) {
                BrowserUtil.browse("https://helpcdn.aliyun.com/document_detail/84781.html?spm=a2c4g.11186623.6.823.148d1144LOadRS");
            }
        });
        ossHelpLink.setPaintUnderline(false);
    }

    @Nullable
    @Override
    public JComponent createComponent() {
        return contentPane;
    }

    @Override
    public boolean isModified() {
        boolean modify = !springContextStaticOgnlExpressionTextFiled.getText().equals(settings.staticSpringContextOgnl)
                || !invokeCountField.getValue().toString().equals(settings.invokeCount)
                || !invokeMonitorCountField.getValue().toString().equals(settings.invokeMonitorCount)
                || !invokeMonitorIntervalField.getValue().toString().equals(settings.invokeMonitorInterval)
                || !depthPrintPropertyField.getValue().toString().equals(settings.depthPrintProperty)
                || !selectProjectNameTextField.getText().equals(settings.selectProjectName)
                || traceSkipJdkRadio.isSelected() != settings.traceSkipJdk
                || conditionExpressDisplayRadio.isSelected() != settings.conditionExpressDisplay
                || ossGlobalSettingRadioButton.isSelected() != settings.ossGlobalSetting
                || springContextGlobalSettingRadioButton.isSelected() != settings.springContextGlobalSetting
                || aliYunOssRadioButton.isSelected() != settings.aliYunOss;

        if (modify) {
            return modify;
        }
        if (aliYunOssRadioButton.isSelected()) {
            modify = !settings.endpoint.equals(ossEndpointTextField.getText())
                    || !settings.accessKeyId.equals(String.valueOf(ossAccessKeyIdPasswordField.getPassword()))
                    || !settings.accessKeySecret.equals(String.valueOf(ossAccessKeySecretPasswordField.getPassword()))
                    || !settings.bucketName.equals(ossBucketNameTextField.getText())
                    || !settings.directoryPrefix.equals(ossDirectoryPrefixTextField.getText());
        }
        return modify;
    }

    @Override
    public void apply() {
        saveSettings();
    }

    @Override
    public void reset() {
        loadSettings();
    }

    /**
     * 保存配置
     */
    private void saveSettings() {
        StringBuilder error = new StringBuilder();
        String staticOgnlExpressionTextFiledText = springContextStaticOgnlExpressionTextFiled.getText();
        if (StringUtils.isBlank(staticOgnlExpressionTextFiledText) || !staticOgnlExpressionTextFiledText.contains("@")) {
            error.append("配置静态spring context 错误");
        } else {
            if (!springContextStaticOgnlExpressionTextFiled.getText().equals(ArthasCommandConstants.DEFAULT_SPRING_CONTEXT_SETTING)) {
                String springContextValue = PropertiesComponentUtils.getValue(ArthasCommandConstants.SPRING_CONTEXT_STATIC_OGNL_EXPRESSION);
                // 有一个地方设置 默认设置为全局的！
                if (StringUtils.isBlank(springContextValue) || springContextValue.equals(ArthasCommandConstants.DEFAULT_SPRING_CONTEXT_SETTING)) {
                    PropertiesComponentUtils.setValue(ArthasCommandConstants.SPRING_CONTEXT_STATIC_OGNL_EXPRESSION, springContextStaticOgnlExpressionTextFiled.getText());
                }
            }
            settings.staticSpringContextOgnl = springContextStaticOgnlExpressionTextFiled.getText();
            settings.springContextGlobalSetting = springContextGlobalSettingRadioButton.isSelected();
            //全局设置
            if (springContextGlobalSettingRadioButton.isSelected()) {
                PropertiesComponentUtils.setValue(ArthasCommandConstants.SPRING_CONTEXT_STATIC_OGNL_EXPRESSION, springContextStaticOgnlExpressionTextFiled.getText());
            }

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
        settings.traceSkipJdk = traceSkipJdkRadio.isSelected();
        settings.conditionExpressDisplay = conditionExpressDisplayRadio.isSelected();
        settings.selectProjectName = selectProjectNameTextField.getText();
        if (clipboardRadioButton.isSelected()) {
            settings.aliYunOss = false;
        } else {
            OSS oss = null;
            try {
                oss = AliyunOssUtils.buildOssClient(ossEndpointTextField.getText(), String.valueOf(ossAccessKeyIdPasswordField.getPassword()), String.valueOf(ossAccessKeySecretPasswordField.getPassword()), ossBucketNameTextField.getText(), ossDirectoryPrefixTextField.getText());
                AliyunOssUtils.checkBuckNameExist(ossBucketNameTextField.getText(), oss);
                settings.endpoint = ossEndpointTextField.getText();
                settings.accessKeyId = String.valueOf(ossAccessKeyIdPasswordField.getPassword());
                settings.accessKeySecret = String.valueOf(ossAccessKeySecretPasswordField.getPassword());
                settings.bucketName = ossBucketNameTextField.getText();
                settings.directoryPrefix = ossDirectoryPrefixTextField.getText();
                settings.aliYunOss = true;
                settings.ossGlobalSetting = ossGlobalSettingRadioButton.isSelected();
                if (ossGlobalSettingRadioButton.isSelected()) {
                    PropertiesComponentUtils.setValue("endpoint", settings.endpoint);
                    PropertiesComponentUtils.setValue("accessKeyId", settings.accessKeyId);
                    PropertiesComponentUtils.setValue("accessKeySecret", settings.accessKeySecret);
                    PropertiesComponentUtils.setValue("bucketName", settings.bucketName);
                    PropertiesComponentUtils.setValue("directoryPrefix", settings.directoryPrefix);
                }
                oss.shutdown();
            } catch (Exception e) {
                StackTraceUtils.printSanitizedStackTrace(e);
                error.append(e.getMessage());
            } finally {
                if (oss != null) {
                    oss.shutdown();
                }
            }
        }


        if (StringUtils.isNotBlank(error)) {
            NotifyUtils.notifyMessage(project, error.toString(), NotificationType.ERROR);
        }

    }

    /**
     * 加载配置
     */
    private void loadSettings() {
        springContextStaticOgnlExpressionTextFiled.setText(settings.staticSpringContextOgnl);
        invokeCountField.setValue(Integer.parseInt(settings.invokeCount));
        invokeMonitorCountField.setValue(Integer.parseInt(settings.invokeMonitorCount));
        invokeMonitorIntervalField.setValue(Integer.parseInt(settings.invokeMonitorInterval));
        depthPrintPropertyField.setValue(Integer.parseInt(settings.depthPrintProperty));
        traceSkipJdkRadio.setSelected(settings.traceSkipJdk);
        conditionExpressDisplayRadio.setSelected(settings.conditionExpressDisplay);
        selectProjectNameTextField.setText(settings.selectProjectName);

        ossEndpointTextField.setText(settings.endpoint);
        ossAccessKeyIdPasswordField.setText(settings.accessKeyId);
        ossAccessKeySecretPasswordField.setText(settings.accessKeySecret);
        ossBucketNameTextField.setText(settings.bucketName);
        ossDirectoryPrefixTextField.setText(settings.directoryPrefix);
        if (settings.aliYunOss) {
            aliYunOssRadioButton.setSelected(true);
            clipboardRadioButton.setSelected(false);
            aliyunOssSettingPane.setVisible(true);
        } else {
            clipboardRadioButton.setSelected(true);
            aliYunOssRadioButton.setSelected(false);
            aliyunOssSettingPane.setVisible(false);
        }
        springContextGlobalSettingRadioButton.setSelected(settings.springContextGlobalSetting);
        ossGlobalSettingRadioButton.setSelected(settings.ossGlobalSetting);
        initEvent();
    }

    void initEvent() {
        ossCheckMsgLabel.setText("");
        ossCheckMsgLabel.setForeground(JBColor.BLACK);
        ossSettingCheckButton.addActionListener(e -> {
            OSS oss = null;
            try {
                oss = AliyunOssUtils.buildOssClient(ossEndpointTextField.getText(), String.valueOf(ossAccessKeyIdPasswordField.getPassword()), String.valueOf(ossAccessKeySecretPasswordField.getPassword()), ossBucketNameTextField.getText(), ossDirectoryPrefixTextField.getText());
                AliyunOssUtils.checkBuckNameExist(ossBucketNameTextField.getText(), oss);
                oss.shutdown();
                ossCheckMsgLabel.setText("oss setting check success");
                ossCheckMsgLabel.setForeground(JBColor.BLACK);
            } catch (Exception ex) {
                ossCheckMsgLabel.setText(ex.getMessage());
                ossCheckMsgLabel.setForeground(JBColor.RED);
            } finally {
                if (oss != null) {
                    oss.shutdown();
                }
            }
        });

        ItemListener itemListener = e -> {
            if (e.getSource().equals(aliYunOssRadioButton) && e.getStateChange() == ItemEvent.SELECTED || e.getSource().equals(clipboardRadioButton) && e.getStateChange() == ItemEvent.DESELECTED) {
                aliyunOssSettingPane.setVisible(true);
                clipboardRadioButton.setSelected(false);
                aliYunOssRadioButton.setSelected(true);
                ossCheckMsgLabel.setText("");
            } else if (e.getSource().equals(clipboardRadioButton) && e.getStateChange() == ItemEvent.SELECTED || e.getSource().equals(aliYunOssRadioButton) && e.getStateChange() == ItemEvent.DESELECTED) {
                aliyunOssSettingPane.setVisible(false);
                aliYunOssRadioButton.setSelected(false);
                clipboardRadioButton.setSelected(true);
                ossCheckMsgLabel.setText("");
            }
        };
        aliYunOssRadioButton.addItemListener(itemListener);
        clipboardRadioButton.addItemListener(itemListener);
    }

    @Override
    public void disposeUIResources() {
        contentPane = null;
    }


}
