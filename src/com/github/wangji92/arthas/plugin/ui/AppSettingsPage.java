package com.github.wangji92.arthas.plugin.ui;

import com.aliyun.oss.OSS;
import com.amazonaws.services.s3.AmazonS3;
import com.github.wangji92.arthas.plugin.constants.ArthasCommandConstants;
import com.github.wangji92.arthas.plugin.setting.AppSettingsState;
import com.github.wangji92.arthas.plugin.setting.ApplicationSettingsState;
import com.github.wangji92.arthas.plugin.utils.ActionLinkUtils;
import com.github.wangji92.arthas.plugin.utils.AliyunOssUtils;
import com.github.wangji92.arthas.plugin.utils.JedisUtils;
import com.github.wangji92.arthas.plugin.utils.NotifyUtils;
import com.github.wangji92.arthas.plugin.utils.OsS3Utils;
import com.github.wangji92.arthas.plugin.utils.PropertiesComponentUtils;
import com.github.wangji92.arthas.plugin.utils.StringUtils;
import com.github.wangji92.arthas.plugin.web.entity.AgentServerInfo;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.project.Project;
import com.intellij.ui.JBColor;
import com.intellij.ui.components.labels.LinkLabel;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.Nullable;
import redis.clients.jedis.Jedis;

import javax.swing.*;
import javax.swing.event.TableModelEvent;
import javax.swing.table.DefaultTableModel;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.Objects;

import static com.github.wangji92.arthas.plugin.constants.ArthasCommandConstants.AT;

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
    private LinkLabel springContextProviderLink;

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
    private JTextField ossAccessKeyIdPasswordField;
    /**
     * oss 配置信息 AccessKeySecret
     */
    private JTextField ossAccessKeySecretPasswordField;
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
    /**
     * 热更新完成后删除文件
     */
    private JRadioButton hotRedefineDeleteFileRadioButton;
    /**
     * watch/trace/monitor support verbose option, print ConditionExpress result #1348
     */
    private JRadioButton printConditionExpressRadioButton;

    private LinkLabel printConditionExpressLink;
    /**
     * 热更新之前先编译
     */
    private JRadioButton redefineBeforeCompileRadioButton;
    private JRadioButton manualSelectPidRadioButton;
    private JRadioButton preConfigurationSelectPidRadioButton;
    private JPanel preConfigurationSelectPidPanel;
    /**
     * redis 选择 按钮
     */
    private JRadioButton redisRadioButton;
    private JPanel redisSettingPane;
    /**
     * redis 地址
     */
    private JTextField redisAddressTextField;
    /**
     * redis 端口
     */
    private JSpinner redisPortField;
    /**
     * redis 密码
     */
    private JTextField redisPasswordField;
    /**
     * redis 检测 button
     */
    private JButton redisCheckConfigButton;
    /**
     * 错误信息
     */
    private JLabel redisMessageLabel;
    /**
     * cache key
     */
    private JTextField redisCacheKeyTextField;
    /**
     * cache key ttl
     */
    private JSpinner redisCacheKeyTtl;

    /**
     * arthas zip 信息的地址
     */
    private JTextField arthasPackageZipDownloadUrlTextField;
    /**
     * spring service bean 的名称
     */
    private JTextField mybatisMapperReloadServiceBeanNameTextField;
    /**
     * spring bean 方法的名称
     */
    private JTextField mybatisMapperReloadMethodNameTextField;
    /**
     * 更多链接
     */
    private LinkLabel mybatisMapperReloadHelpLink;
    /**
     * retransform 帮助文档
     */
    private LinkLabel retransformHelpLink;
    /**
     * arthas retransformer 热更新功能浅析
     */
    private LinkLabel analysisRetransformerLink;
    /**
     * github 地址
     */
    private LinkLabel arthasIdeaGithubLink;
    /**
     * demo 地址
     */
    private LinkLabel arthasIdeaDemoLink;
    /**
     * 语雀知识库链接
     */
    private LinkLabel arthasYuQueDocumentLink;
    /**
     * 自动转换为中文编码
     */
    private JRadioButton autoToUnicodeRadioButton;
    /**
     * 自动将中文转换为 unicode 编码
     */
    private LinkLabel howToInputChineseParamLink;

    private JRadioButton s3RadioButton;

    private JTextField s3EndPointField;

    private JTextField s3AkField;

    private JTextField s3SkField;

    private JTextField s3BucketNameField;

    private JTextField s3DirPrefixField;

    private JRadioButton s3GlobalConfigField;

    private JTextField s3RegionField;

    private JPanel s3Panel;

    private JLabel s3CheckMessageLabel;
    private JButton s3CheckButton;



    private JButton addArthasAgentButton;
    private JButton removeArthasAgentButton;
    private JPanel agentTablePanel;
    private JTable agentTable;
    public static DefaultTableModel tableModel;



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
     * 全局设置信息
     */
    private ApplicationSettingsState applicationSettingsState;

    /**
     * 自动构造  idea 会携带当前的project 参数信息
     *
     * @param project
     */
    public AppSettingsPage(Project project) {
        this.project = project;
        settings = AppSettingsState.getInstance(this.project);
        applicationSettingsState = ApplicationSettingsState.getInstance();
    }

    private void createUIComponents() {
        this.springContextProviderLink = ActionLinkUtils.newActionLink("https://github.com/WangJi92/arthas-plugin-demo/blob/master/src/main/java/com/wangji92/arthas/plugin/demo/common/ApplicationContextProvider.java");
        this.selectLink = ActionLinkUtils.newActionLink("https://arthas.aliyun.com/doc/advanced-use.html");
        this.batchSupportLink = ActionLinkUtils.newActionLink("https://arthas.aliyun.com/doc/batch-support.html");
        this.redefineHelpLinkLabel = ActionLinkUtils.newActionLink("https://arthas.aliyun.com/doc/redefine.html#");
        this.ossHelpLink = ActionLinkUtils.newActionLink("https://helpcdn.aliyun.com/document_detail/84781.html?spm=a2c4g.11186623.6.823.148d1144LOadRS");
        this.printConditionExpressLink = ActionLinkUtils.newActionLink("https://github.com/alibaba/arthas/issues/1348");
        this.mybatisMapperReloadHelpLink = ActionLinkUtils.newActionLink("https://github.com/WangJi92/mybatis-mapper-reload-spring-boot-start");
        this.retransformHelpLink = ActionLinkUtils.newActionLink("https://arthas.aliyun.com/doc/retransform.html");
        this.analysisRetransformerLink = ActionLinkUtils.newActionLink("https://www.yuque.com/arthas-idea-plugin/help/lyevb2");
        this.arthasIdeaGithubLink = ActionLinkUtils.newActionLink("https://github.com/WangJi92/arthas-idea-plugin");
        this.arthasIdeaDemoLink = ActionLinkUtils.newActionLink("https://github.com/WangJi92/arthas-plugin-demo");
        this.arthasYuQueDocumentLink = ActionLinkUtils.newActionLink("https://www.yuque.com/arthas-idea-plugin");
        this.howToInputChineseParamLink = ActionLinkUtils.newActionLink("https://github.com/alibaba/arthas/blob/master/site/src/site/sphinx/faq.md#%E8%BE%93%E5%85%A5%E4%B8%AD%E6%96%87unicode%E5%AD%97%E7%AC%A6");

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
                || aliYunOssRadioButton.isSelected() != settings.aliYunOss
                || redisRadioButton.isSelected() != settings.hotRedefineRedis
                || s3RadioButton.isSelected() != settings.awsS3
                || hotRedefineDeleteFileRadioButton.isSelected() != settings.hotRedefineDelete
                || redefineBeforeCompileRadioButton.isSelected() != settings.redefineBeforeCompile
                || printConditionExpressRadioButton.isSelected() != settings.printConditionExpress
                || manualSelectPidRadioButton.isSelected() != settings.manualSelectPid
                || !arthasPackageZipDownloadUrlTextField.getText().equalsIgnoreCase(settings.arthasPackageZipDownloadUrl)
                || !mybatisMapperReloadMethodNameTextField.getText().equalsIgnoreCase(settings.mybatisMapperReloadMethodName)
                || !mybatisMapperReloadServiceBeanNameTextField.getText().equalsIgnoreCase(settings.mybatisMapperReloadServiceBeanName)
                || autoToUnicodeRadioButton.isSelected() != settings.autoToUnicode;

        if (modify) {
            return modify;
        }
        if (aliYunOssRadioButton.isSelected()) {
            modify = !settings.endpoint.equals(ossEndpointTextField.getText())
                    || !settings.accessKeyId.equals(String.valueOf(ossAccessKeyIdPasswordField.getText()))
                    || !settings.accessKeySecret.equals(String.valueOf(ossAccessKeySecretPasswordField.getText()))
                    || !settings.bucketName.equals(ossBucketNameTextField.getText())
                    || !settings.directoryPrefix.equals(ossDirectoryPrefixTextField.getText());
        }

        if (s3RadioButton.isSelected()) {
            modify = !settings.s3Endpoint.equals(s3EndPointField.getText())
                    || !settings.s3AccessKeyId.equals(String.valueOf(s3AkField.getText()))
                    || !settings.s3AccessKeySecret.equals(String.valueOf(s3SkField.getText()))
                    || !settings.s3BucketName.equals(s3BucketNameField.getText())
                    || !settings.s3Region.equals(s3RegionField.getText())
                    || !settings.s3DirectoryPrefix.equals(s3DirPrefixField.getText());
        }

        if (redisRadioButton.isSelected()) {
            modify = !settings.redisAddress.equals(redisAddressTextField.getText())
                    || !settings.redisPort.equals((redisPortField.getValue()))
                    || !settings.redisAuth.equals(String.valueOf(redisPasswordField.getText()))
                    || !settings.redisCacheKey.equals(redisCacheKeyTextField.getText())
                    || !settings.redisCacheKeyTtl.equals(redisCacheKeyTtl.getValue());
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
        this.saveStaticSpringContext(error);
        this.saveMybatisMapperSetting();
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
        settings.manualSelectPid = manualSelectPidRadioButton.isSelected();
        settings.hotRedefineDelete = hotRedefineDeleteFileRadioButton.isSelected();
        settings.redefineBeforeCompile = redefineBeforeCompileRadioButton.isSelected();
        settings.printConditionExpress = printConditionExpressRadioButton.isSelected();
        settings.arthasPackageZipDownloadUrl = arthasPackageZipDownloadUrlTextField.getText();
        settings.autoToUnicode = autoToUnicodeRadioButton.isSelected();
        PropertiesComponentUtils.setValue("autoToUnicode", autoToUnicodeRadioButton.isSelected() ? "y" : "n");
        // 设置到全局
        PropertiesComponentUtils.setValue("arthasPackageZipDownloadUrl", arthasPackageZipDownloadUrlTextField.getText());
        if (clipboardRadioButton.isSelected()) {
            settings.hotRedefineClipboard = true;
            settings.aliYunOss = false;
            settings.hotRedefineRedis = false;
            settings.awsS3 = false;
            PropertiesComponentUtils.setValue("storageType", "hotRedefineClipboard");
        } else if (aliYunOssRadioButton.isSelected()) {
            this.saveAliyunOssConfig(error);
        } else if (redisRadioButton.isSelected()) {
            this.saveRedisConfig(error);
        } else if (s3RadioButton.isSelected()) {
            this.saveS3Config(error);
        }

        if (StringUtils.isNotBlank(error)) {
            NotifyUtils.notifyMessage(project, error.toString(), NotificationType.ERROR);
        }

    }

    /**
     * 保存mybatis mapper reload 配置
     */
    private void saveMybatisMapperSetting() {
        String mybatisMapperReloadServiceBeanNameTex = mybatisMapperReloadServiceBeanNameTextField.getText();
        String mybatisMapperReloadMethodNameText = mybatisMapperReloadMethodNameTextField.getText();
        if (StringUtils.isBlank(mybatisMapperReloadMethodNameText) || StringUtils.isBlank(mybatisMapperReloadServiceBeanNameTex)) {
            return;
        }
        settings.mybatisMapperReloadMethodName = mybatisMapperReloadMethodNameText;
        settings.mybatisMapperReloadServiceBeanName = mybatisMapperReloadServiceBeanNameTex;
        PropertiesComponentUtils.setValue("mybatisMapperReloadMethodName", mybatisMapperReloadMethodNameText);
        PropertiesComponentUtils.setValue("mybatisMapperReloadServiceBeanName", mybatisMapperReloadServiceBeanNameTex);
    }

    /**
     * 保存spring static 配置信息
     *
     * @param error
     */
    private void saveStaticSpringContext(StringBuilder error) {
        String staticOgnlExpressionTextFiledText = springContextStaticOgnlExpressionTextFiled.getText();
        if (StringUtils.isBlank(staticOgnlExpressionTextFiledText)) {
            settings.staticSpringContextOgnl = "";
            //全局设置
            if (springContextGlobalSettingRadioButton.isSelected() && !ArthasCommandConstants.DEFAULT_SPRING_CONTEXT_SETTING.equals(settings.staticSpringContextOgnl)) {
                PropertiesComponentUtils.setValue(ArthasCommandConstants.SPRING_CONTEXT_STATIC_OGNL_EXPRESSION, staticOgnlExpressionTextFiledText);
            }
            return;
        }

        if (!staticOgnlExpressionTextFiledText.contains(AT)) {
            error.append("配置静态spring context 错误");
            return;
        }
        settings.staticSpringContextOgnl = staticOgnlExpressionTextFiledText;
        settings.springContextGlobalSetting = springContextGlobalSettingRadioButton.isSelected();
        //全局设置
        if (springContextGlobalSettingRadioButton.isSelected() && !ArthasCommandConstants.DEFAULT_SPRING_CONTEXT_SETTING.equals(settings.staticSpringContextOgnl)) {
            PropertiesComponentUtils.setValue(ArthasCommandConstants.SPRING_CONTEXT_STATIC_OGNL_EXPRESSION, staticOgnlExpressionTextFiledText);
        }
    }

    /**
     * 保存redis的配置信息
     *
     * @param error
     */
    private void saveRedisConfig(StringBuilder error) {
        if (((int) redisCacheKeyTtl.getValue()) <= 0) {
            error.append("redisCacheKeyTtl <= 0 ");
        } else {
            settings.redisCacheKeyTtl = (Integer) redisCacheKeyTtl.getValue();
        }
        if (StringUtils.isBlank(redisCacheKeyTextField.getText())) {
            settings.redisCacheKey = "arthasIdeaPluginRedefineCacheKey";
        }
        try (Jedis jedis = JedisUtils.buildJedisClient(redisAddressTextField.getText(), (Integer) redisPortField.getValue(), 5000, String.valueOf(redisPasswordField.getText()));) {
            JedisUtils.checkRedisClient(jedis);
            settings.redisAddress = redisAddressTextField.getText();
            settings.redisPort = (Integer) redisPortField.getValue();
            settings.redisAuth = String.valueOf(redisPasswordField.getText());
            settings.hotRedefineRedis = true;
            settings.aliYunOss = false;
            settings.awsS3 = false;
            settings.hotRedefineClipboard = false;
            PropertiesComponentUtils.setValue("storageType", "hotRedefineRedis");
            PropertiesComponentUtils.setValue("redisAddress", settings.redisAddress);
            PropertiesComponentUtils.setValue("redisPort", "" + settings.redisPort);
            PropertiesComponentUtils.setValue("redisAuth", settings.redisAuth);
            PropertiesComponentUtils.setValue("redisCacheKey", settings.redisCacheKey);
            PropertiesComponentUtils.setValue("redisCacheKeyTtl", "" + settings.redisCacheKeyTtl);
        } catch (Exception ex) {
            error.append(ex.getMessage());
        }
    }

    /**
     * 保存 阿里云oss的配置
     *
     * @param error
     */
    private void saveAliyunOssConfig(StringBuilder error) {
        OSS oss = null;
        try {
            oss = AliyunOssUtils.buildOssClient(ossEndpointTextField.getText(), String.valueOf(ossAccessKeyIdPasswordField.getText()), String.valueOf(ossAccessKeySecretPasswordField.getText()), ossBucketNameTextField.getText(), ossDirectoryPrefixTextField.getText());
            AliyunOssUtils.checkBuckNameExist(ossBucketNameTextField.getText(), oss);
            settings.endpoint = ossEndpointTextField.getText();
            settings.accessKeyId = String.valueOf(ossAccessKeyIdPasswordField.getText());
            settings.accessKeySecret = String.valueOf(ossAccessKeySecretPasswordField.getText());
            settings.bucketName = ossBucketNameTextField.getText();
            settings.directoryPrefix = ossDirectoryPrefixTextField.getText();
            settings.aliYunOss = true;
            settings.hotRedefineRedis = false;
            settings.awsS3 = false;
            settings.hotRedefineClipboard = false;
            settings.ossGlobalSetting = ossGlobalSettingRadioButton.isSelected();
            if (ossGlobalSettingRadioButton.isSelected()) {
                PropertiesComponentUtils.setValue("storageType", "aliYunOss");
                PropertiesComponentUtils.setValue("endpoint", settings.endpoint);
                PropertiesComponentUtils.setValue("accessKeyId", settings.accessKeyId);
                PropertiesComponentUtils.setValue("accessKeySecret", settings.accessKeySecret);
                PropertiesComponentUtils.setValue("bucketName", settings.bucketName);
                PropertiesComponentUtils.setValue("directoryPrefix", settings.directoryPrefix);
            }
            oss.shutdown();
        } catch (Exception e) {
            error.append(e.getMessage());
        } finally {
            if (oss != null) {
                oss.shutdown();
            }
        }
    }

    /**
     * aws3
     *
     * @param error
     */
    private void saveS3Config(StringBuilder error) {
        AmazonS3 s3 = null;
        try {
            s3 = OsS3Utils.buildS3Client(s3EndPointField.getText(),
                    String.valueOf(s3AkField.getText()),
                    String.valueOf(s3SkField.getText()),
                    s3BucketNameField.getText(),
                    s3RegionField.getText(),
                    s3DirPrefixField.getText());
            OsS3Utils.checkBuckNameExist(s3BucketNameField.getText(), s3);
            settings.s3Endpoint = s3EndPointField.getText();
            settings.s3AccessKeyId = String.valueOf(s3AkField.getText());
            settings.s3AccessKeySecret = String.valueOf(s3SkField.getText());
            settings.s3BucketName = s3BucketNameField.getText();
            settings.s3DirectoryPrefix = s3DirPrefixField.getText();
            settings.s3Region = s3RegionField.getText();
            settings.awsS3 = true;
            settings.hotRedefineRedis = false;
            settings.aliYunOss = false;
            settings.hotRedefineClipboard = false;
            settings.s3GlobalConfig = s3GlobalConfigField.isSelected();
            if (s3GlobalConfigField.isSelected()) {
                PropertiesComponentUtils.setValue("storageType", "awsS3");
                PropertiesComponentUtils.setValue("s3Endpoint", settings.s3Endpoint);
                PropertiesComponentUtils.setValue("s3AccessKeyId", settings.s3AccessKeyId);
                PropertiesComponentUtils.setValue("s3AccessKeySecret", settings.s3AccessKeySecret);
                PropertiesComponentUtils.setValue("s3BucketName", settings.s3BucketName);
                PropertiesComponentUtils.setValue("s3DirectoryPrefix", settings.s3DirectoryPrefix);
                PropertiesComponentUtils.setValue("s3Region", settings.s3Region);
            }
        } catch (Exception e) {
            error.append(e.getMessage());
        } finally {
            if (s3 != null) {
                s3.shutdown();
            }
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
        hotRedefineDeleteFileRadioButton.setSelected(settings.hotRedefineDelete);
        redefineBeforeCompileRadioButton.setSelected(settings.redefineBeforeCompile);
        printConditionExpressRadioButton.setSelected(settings.printConditionExpress);
        autoToUnicodeRadioButton.setSelected(settings.autoToUnicode);
        selectProjectNameTextField.setText(settings.selectProjectName);

        ossEndpointTextField.setText(settings.endpoint);
        ossAccessKeyIdPasswordField.setText(settings.accessKeyId);
        ossAccessKeySecretPasswordField.setText(settings.accessKeySecret);
        ossBucketNameTextField.setText(settings.bucketName);
        ossDirectoryPrefixTextField.setText(settings.directoryPrefix);
        redisAddressTextField.setText(settings.redisAddress);
        redisPortField.setValue(settings.redisPort);
        redisPasswordField.setText(settings.redisAuth);
        redisCacheKeyTtl.setValue(settings.redisCacheKeyTtl);
        redisCacheKeyTextField.setText(settings.redisCacheKey);

        s3EndPointField.setText(settings.s3Endpoint);
        s3AkField.setText(settings.s3AccessKeyId);
        s3SkField.setText(settings.s3AccessKeySecret);
        s3BucketNameField.setText(settings.s3BucketName);
        s3DirPrefixField.setText(settings.s3DirectoryPrefix);
        s3RegionField.setText(settings.s3Region);
        s3GlobalConfigField.setSelected(settings.s3GlobalConfig);

        if (settings.aliYunOss) {
            // 阿里云oss
            aliYunOssRadioButton.setSelected(true);
            redisSettingPane.setVisible(false);
            s3Panel.setVisible(false);
            aliyunOssSettingPane.setVisible(true);
        } else if (settings.hotRedefineRedis) {
            // redis
            aliyunOssSettingPane.setVisible(false);
            redisRadioButton.setSelected(true);
            s3Panel.setVisible(false);
            redisSettingPane.setVisible(true);
        } else if (settings.awsS3) {
            s3RadioButton.setSelected(true);
            redisSettingPane.setVisible(false);
            s3Panel.setVisible(true);
            aliyunOssSettingPane.setVisible(false);
        } else {
            // 剪切板
            clipboardRadioButton.setSelected(true);
            aliyunOssSettingPane.setVisible(false);
            redisSettingPane.setVisible(false);
            s3Panel.setVisible(false);
        }
        if (settings.manualSelectPid) {
            preConfigurationSelectPidPanel.setVisible(false);
            preConfigurationSelectPidRadioButton.setSelected(false);
            manualSelectPidRadioButton.setSelected(true);
        } else {
            preConfigurationSelectPidPanel.setVisible(true);
            preConfigurationSelectPidRadioButton.setSelected(true);
            manualSelectPidRadioButton.setSelected(false);
        }
        springContextGlobalSettingRadioButton.setSelected(settings.springContextGlobalSetting);
        ossGlobalSettingRadioButton.setSelected(settings.ossGlobalSetting);

        // 设置远程的下载地址
        arthasPackageZipDownloadUrlTextField.setText(settings.arthasPackageZipDownloadUrl);
        mybatisMapperReloadMethodNameTextField.setText(settings.mybatisMapperReloadMethodName);
        mybatisMapperReloadServiceBeanNameTextField.setText(settings.mybatisMapperReloadServiceBeanName);

        // 设置agent列表
        tableModel = new DefaultTableModel(0, 0);
        tableModel.setColumnIdentifiers(new String[]{"ServerName","Address","BindGitBranch"});
        agentTable.setModel(tableModel);
        agentTable.setRowHeight(20);

        this.applicationSettingsState.agentServerInfoList.forEach((agentServerInfo) -> {
            tableModel.addRow(new Object[]{agentServerInfo.getName(), agentServerInfo.getAddress(), agentServerInfo.getBindGitBranch()});
        });

        initEvent();
    }

    private void initEvent() {
        ossCheckMsgLabel.setText("");
        ossCheckMsgLabel.setForeground(JBColor.BLACK);
        ossSettingCheckButton.addActionListener(e -> {
            OSS oss = null;
            try {
                oss = AliyunOssUtils.buildOssClient(ossEndpointTextField.getText(), String.valueOf(ossAccessKeyIdPasswordField.getText()), String.valueOf(ossAccessKeySecretPasswordField.getText()), ossBucketNameTextField.getText(), ossDirectoryPrefixTextField.getText());
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

        s3CheckButton.addActionListener(e -> {
            AmazonS3 s3 = null;
            try {
                s3 = OsS3Utils.buildS3Client(s3EndPointField.getText(),
                        String.valueOf(s3AkField.getText()),
                        String.valueOf(s3SkField.getText()),
                        s3BucketNameField.getText(),
                        s3RegionField.getText(),
                        s3DirPrefixField.getText());
                OsS3Utils.checkBuckNameExist(s3BucketNameField.getText(), s3);
                s3.shutdown();
                s3CheckMessageLabel.setText("s3 setting check success");
                s3CheckMessageLabel.setForeground(JBColor.BLACK);
            } catch (Exception ex) {
                s3CheckMessageLabel.setText(ex.getMessage());
                s3CheckMessageLabel.setForeground(JBColor.RED);
            } finally {
                if (s3 != null) {
                    s3.shutdown();
                }
            }
        });

        redisCheckConfigButton.addActionListener(e -> {
            try (Jedis jedis = JedisUtils.buildJedisClient(redisAddressTextField.getText(), (Integer) redisPortField.getValue(), 5000, String.valueOf(redisPasswordField.getText()));) {
                JedisUtils.checkRedisClient(jedis);
                redisMessageLabel.setText("redis setting check success");
                redisMessageLabel.setForeground(JBColor.BLACK);
            } catch (Exception ex) {
                redisMessageLabel.setText(ex.getMessage());
                redisMessageLabel.setForeground(JBColor.RED);
            }
        });

        ButtonGroup group = new ButtonGroup();
        group.add(aliYunOssRadioButton);
        group.add(clipboardRadioButton);
        group.add(redisRadioButton);
        group.add(s3RadioButton);
        ItemListener itemListener = e -> {
            if (e.getSource().equals(aliYunOssRadioButton) && e.getStateChange() == ItemEvent.SELECTED) {
                aliyunOssSettingPane.setVisible(true);
                redisSettingPane.setVisible(false);
                s3Panel.setVisible(false);
            } else if (e.getSource().equals(clipboardRadioButton) && e.getStateChange() == ItemEvent.SELECTED) {
                aliyunOssSettingPane.setVisible(false);
                redisSettingPane.setVisible(false);
                s3Panel.setVisible(false);
            } else if (e.getSource().equals(redisRadioButton) && e.getStateChange() == ItemEvent.SELECTED) {
                aliyunOssSettingPane.setVisible(false);
                redisSettingPane.setVisible(true);
                s3Panel.setVisible(false);
            } else if (e.getSource().equals(s3RadioButton) && e.getStateChange() == ItemEvent.SELECTED) {
                aliyunOssSettingPane.setVisible(false);
                redisSettingPane.setVisible(false);
                s3Panel.setVisible(true);
            }
            ossCheckMsgLabel.setText("");
            redisMessageLabel.setText("");
            s3CheckMessageLabel.setText("");
        };
        aliYunOssRadioButton.addItemListener(itemListener);
        clipboardRadioButton.addItemListener(itemListener);
        redisRadioButton.addItemListener(itemListener);
        s3RadioButton.addItemListener(itemListener);

        // 设置是否手动选择pid
        ItemListener itemListenerSelectPid = e -> {
            if (e.getSource().equals(manualSelectPidRadioButton) && e.getStateChange() == ItemEvent.SELECTED || e.getSource().equals(preConfigurationSelectPidRadioButton) && e.getStateChange() == ItemEvent.DESELECTED) {
                preConfigurationSelectPidPanel.setVisible(false);
                preConfigurationSelectPidRadioButton.setSelected(false);
                manualSelectPidRadioButton.setSelected(true);
            } else if (e.getSource().equals(preConfigurationSelectPidRadioButton) && e.getStateChange() == ItemEvent.SELECTED || e.getSource().equals(manualSelectPidRadioButton) && e.getStateChange() == ItemEvent.DESELECTED) {
                preConfigurationSelectPidPanel.setVisible(true);
                preConfigurationSelectPidRadioButton.setSelected(true);
                manualSelectPidRadioButton.setSelected(false);
            }
        };
        manualSelectPidRadioButton.addItemListener(itemListenerSelectPid);
        preConfigurationSelectPidRadioButton.addItemListener(itemListenerSelectPid);

        addArthasAgentButton.addActionListener(e -> new AddAgentServer().open());
        removeArthasAgentButton.addActionListener(e -> {
            int selectedRow = agentTable.getSelectedRow();
            if (selectedRow != -1) {
                tableModel.removeRow(selectedRow);
                this.applicationSettingsState.agentServerInfoList.remove(selectedRow);
            }
        });
        tableModel.addTableModelListener(e -> {
            if (e.getType() == TableModelEvent.UPDATE) {
                System.out.println("value change last-row -> "+e.getLastRow()+" column -> "+e.getColumn() + " change to ->"+tableModel.getValueAt(e.getLastRow(), e.getColumn()).toString());
                AgentServerInfo agentServerInfo = applicationSettingsState.agentServerInfoList.get(e.getLastRow());
                if (Objects.isNull(agentServerInfo)) {
                    return;
                }
                int column = e.getColumn();
                String value = tableModel.getValueAt(e.getLastRow(), e.getColumn()).toString();
                switch (column) {
                    case 0:
                      agentServerInfo.setName(value);
                      break;
                    case 1:
                        agentServerInfo.setAddress(value);
                        break;
                    case 2:
                        agentServerInfo.setBindGitBranch(value);
                        break;
                    default:
                        System.out.println("column not match error");
                }
            }
        });
    }

    @Override
    public void disposeUIResources() {
        contentPane = null;
    }


}
