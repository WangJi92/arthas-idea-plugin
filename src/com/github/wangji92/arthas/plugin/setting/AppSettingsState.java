package com.github.wangji92.arthas.plugin.setting;

import com.github.wangji92.arthas.plugin.constants.ArthasCommandConstants;
import com.github.wangji92.arthas.plugin.utils.PropertiesComponentUtils;
import com.github.wangji92.arthas.plugin.utils.StringUtils;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.project.Project;
import com.intellij.util.xmlb.XmlSerializerUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static com.github.wangji92.arthas.plugin.constants.ArthasCommandConstants.DEFAULT_SPRING_CONTEXT_SETTING;

/**
 * Supports storing the application settings in a persistent way.
 * The State and Storage annotations define the name of the data and the file name where
 * these persistent application settings are stored.
 *
 * @author 汪小哥
 * @date 14-08-2020
 */
@State(
        name = "arthas.idea.plugin",
        storages = {@Storage("setting.xml")}
)
public class AppSettingsState implements PersistentStateComponent<AppSettingsState> {

    /**
     * spring ognl 配置
     */
    public String staticSpringContextOgnl = DEFAULT_SPRING_CONTEXT_SETTING;

    /**
     * 跳过jdk trace
     */
    public boolean traceSkipJdk = false;
    /**
     * 调用次数
     */
    public String invokeMonitorCount = ArthasCommandConstants.INVOKE_MONITOR_COUNT;
    /**
     * 时间间隔
     */
    public String invokeMonitorInterval = ArthasCommandConstants.INVOKE_MONITOR_INTERVAL;

    /**
     * 调用次数
     */
    public String invokeCount = ArthasCommandConstants.INVOKE_COUNT;

    /**
     * 打印属性的深度
     */
    public String depthPrintProperty = ArthasCommandConstants.RESULT_X;

    /**
     * 是否展示默认的条件表达式
     */
    public boolean conditionExpressDisplay = true;

    /**
     * {@literal https://arthas.aliyun.com/doc/batch-support.html 批处理支持}
     * {@literal https://arthas.aliyun.com/doc/advanced-use.html --select}
     */
    public String selectProjectName;

    /**
     * oss endpoint  https://helpcdn.aliyun.com/document_detail/84781.html?spm=a2c4g.11186623.6.823.148d1144LOadRS
     */
    public String endpoint;

    /**
     * oss accessKeyId
     */
    public String accessKeyId;

    /**
     * oss accessKeySecret
     */
    public String accessKeySecret;

    /**
     * oss bucketName
     */
    public String bucketName;

    /**
     * oss 存储的路径
     */
    public String directoryPrefix = "arthas/";

    /**
     * 是否设置阿里云
     */
    public boolean aliYunOss = false;

    /**
     * spring context 全局默认配置
     */
    public boolean springContextGlobalSetting = true;

    /**
     * oss 全局默认配置
     */
    public boolean ossGlobalSetting = true;

    /**
     * 热更新完成后删除文件
     */
    public boolean hotRedefineDelete = true;

    /**
     * 热更新之前先编译
     */
    public boolean redefineBeforeCompile = false;

    /**
     * watch/trace/monitor support verbose option, print ConditionExpress result #1348
     */
    public boolean printConditionExpress = true;

    /**
     * 手动选择pid的配置
     */
    public boolean manualSelectPid = true;


    public static AppSettingsState getInstance(@NotNull Project project) {
        AppSettingsState appSettingsState = ServiceManager.getService(project, AppSettingsState.class);
        // 配置检查.. 兼容老版本
        if (appSettingsState.staticSpringContextOgnl.equals(ArthasCommandConstants.DEFAULT_SPRING_CONTEXT_SETTING) || StringUtils.isBlank(appSettingsState.staticSpringContextOgnl)) {
            String springContextValue = PropertiesComponentUtils.getValue(ArthasCommandConstants.SPRING_CONTEXT_STATIC_OGNL_EXPRESSION);
            // 最早的版本设置过配置的！ 使用那个配置作为当前工程的配置
            if (StringUtils.isNotBlank(springContextValue) && !ArthasCommandConstants.DEFAULT_SPRING_CONTEXT_SETTING.equals(springContextValue) && springContextValue.contains("@")) {
                appSettingsState.staticSpringContextOgnl = springContextValue;
                appSettingsState.springContextGlobalSetting = true;
            }

        }

        String endPoint1 = PropertiesComponentUtils.getValue("endpoint");
        String bucketName1 = PropertiesComponentUtils.getValue("bucketName");
        String accessKeyId1 = PropertiesComponentUtils.getValue("accessKeyId");
        String accessKeySecret1 = PropertiesComponentUtils.getValue("accessKeySecret");
        String directoryPrefix1 = PropertiesComponentUtils.getValue("directoryPrefix");
        // 如果之前有设置过就打开了
        if (!appSettingsState.aliYunOss && StringUtils.isBlank(appSettingsState.endpoint)
                && StringUtils.isBlank(appSettingsState.bucketName)
                && StringUtils.isBlank(appSettingsState.accessKeyId)
                && StringUtils.isBlank(appSettingsState.accessKeySecret)
                && StringUtils.isNotBlank(bucketName1)
                && StringUtils.isNotBlank(endPoint1)
                && StringUtils.isNotBlank(accessKeyId1)
                && StringUtils.isNotBlank(accessKeySecret1)) {
            appSettingsState.aliYunOss = true;
            appSettingsState.endpoint = endPoint1;
            appSettingsState.accessKeyId = accessKeyId1;
            appSettingsState.bucketName = bucketName1;
            appSettingsState.accessKeySecret = accessKeySecret1;
            appSettingsState.directoryPrefix = directoryPrefix1;
        }
        return appSettingsState;
    }

    @Nullable
    @Override
    public AppSettingsState getState() {
        return this;
    }

    @Override
    public void loadState(@NotNull AppSettingsState state) {
        XmlSerializerUtil.copyBean(state, this);
    }
}
