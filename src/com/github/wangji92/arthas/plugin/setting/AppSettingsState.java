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
import org.apache.commons.lang3.math.NumberUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static com.github.wangji92.arthas.plugin.constants.ArthasCommandConstants.*;

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
     * 是否展示默认的条件表达式 [修改 部分低版本不支持]
     */
    public boolean conditionExpressDisplay = false;

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
     * aws
     */
    public boolean awsS3 = false;


    public String s3Endpoint;

    public String s3Region;

    /**
     * accessKeyId
     */
    public String s3AccessKeyId;

    /**
     * accessKeySecret
     */
    public String s3AccessKeySecret;

    /**
     * bucketName
     */
    public String s3BucketName;

    /**
     * 存储的路径
     */
    public String s3DirectoryPrefix = "arthas/";


    public boolean s3GlobalConfig = true;

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
    public boolean printConditionExpress = false;

    /**
     * 手动选择pid的配置
     */
    public boolean manualSelectPid = true;

    /**
     * 是否使用redis
     */
    public boolean hotRedefineRedis = false;

    /**
     * 剪切板
     */
    public boolean hotRedefineClipboard = true;

    /**
     * redis 的链接地址
     */
    public String redisAddress = "127.0.0.1";

    /**
     * redis 的端口
     */
    public Integer redisPort = 6379;

    /**
     * redis 密码
     */
    public String redisAuth;

    /**
     * 热更新的缓存Key
     */
    public String redisCacheKey = "arthasIdeaPluginRedefineCacheKey";

    /**
     * 缓存过期时间
     */
    public Integer redisCacheKeyTtl = 60 * 60 * 2;

    /**
     * 默认下载地址
     */
    public String arthasPackageZipDownloadUrl = DEFAULT_ARTHAS_PACKAGE_ZIP_DOWNLOAD_URL;

    /**
     * {@literal https://github.com/WangJi92/mybatis-mapper-reload-spring-boot-start}
     */
    public String mybatisMapperReloadServiceBeanName = DEFAULT_MYBATIS_MAPPER_RELOAD_SERVICE_BEAN_NAME;
    /**
     * {@literal https://github.com/WangJi92/mybatis-mapper-reload-spring-boot-start}
     */
    public String mybatisMapperReloadMethodName = DEFAULT_MYBATIS_MAPPER_RELOAD_METHOD_NAME;

    /**
     * 快捷脚本 选择命令后关闭窗口
     */
    public String scriptDialogCloseWhenSelectedCommand = "y";

    /**
     * 自动转换为 Unicode 编码将中文信息
     */
    public boolean autoToUnicode = true;

    /**
     * 获取工程的名称
     *
     * @return
     */
    public static Project getProject() {
        return projectInfo;
    }

    private static Project projectInfo;

    /**
     * 上次选择的环境
     */
    public String lastSelectEnv;

    /**
     * 上次选择的agent
     */
    public String lastSelectAgent;


    public static AppSettingsState getInstance(@NotNull Project project) {
        projectInfo = project;
        AppSettingsState appSettingsState = ServiceManager.getService(project, AppSettingsState.class);
        // 检测全局的static spring context
        checkGlobalStaticSpringContextAndSettingCurrentProjectIfEmpty(appSettingsState);

        checkGlobalStorageType(appSettingsState);

        // 检测 全局的阿里云oss
        checkGlobalAliyunOssAndSettingCurrentProjectIfEmpty(appSettingsState);

        //aws
        checkGlobalS3AndSettingCurrentProjectIfEmpty(appSettingsState);

        // 检测全局的redis 配置
        checkGlobalRedisAndSettingCurrentProjectIfEmpty(appSettingsState);

        // 检查全局的 arthas zip 包的下载地址
        checkGlobalArthasPackageZipDownloadUrl(appSettingsState);

        // 检查设置全局的mybatis mapper bean的名称
        checkGlobalMybatisMapper(appSettingsState);

        /**
         * 检查快捷脚本是否关闭窗口
         */
        checkGlobalScriptDialogCloseWhenSelectedCommand(appSettingsState);

        checkGlobalAutoToUnicode(appSettingsState);

        return appSettingsState;
    }

    /**
     * 设置信息
     *
     * @param appSettingsState
     */
    private static void checkGlobalS3AndSettingCurrentProjectIfEmpty(AppSettingsState appSettingsState) {
        String endPoint1 = PropertiesComponentUtils.getValue("s3Endpoint");
        String bucketName1 = PropertiesComponentUtils.getValue("s3BucketName");
        String accessKeyId1 = PropertiesComponentUtils.getValue("s3AccessKeyId");
        String accessKeySecret1 = PropertiesComponentUtils.getValue("s3AccessKeySecret");
        String directoryPrefix1 = PropertiesComponentUtils.getValue("s3DirectoryPrefix");
        String region = PropertiesComponentUtils.getValue("s3Region");
        if (StringUtils.isNotBlank(bucketName1)
                && StringUtils.isNotBlank(endPoint1)
                && StringUtils.isNotBlank(accessKeyId1)
                && StringUtils.isNotBlank(accessKeySecret1)) {
            appSettingsState.s3Endpoint = endPoint1;
            appSettingsState.s3AccessKeyId = accessKeyId1;
            appSettingsState.s3BucketName = bucketName1;
            appSettingsState.s3AccessKeySecret = accessKeySecret1;
            appSettingsState.s3DirectoryPrefix = directoryPrefix1;
            appSettingsState.s3Region = region;
        }
    }

    /**
     * 全局检测设置信息 只要一个工程修改了 全部都修改
     *
     * @param appSettingsState
     */
    private static void checkGlobalStorageType(AppSettingsState appSettingsState) {
        String storageType = PropertiesComponentUtils.getValue("storageType");
        if (StringUtils.isBlank(storageType)) {
            storageType = "hotRedefineClipboard";
        }
        switch (storageType) {
            case "hotRedefineClipboard": {
                appSettingsState.hotRedefineClipboard = true;
                appSettingsState.hotRedefineRedis = false;
                appSettingsState.aliYunOss = false;
                appSettingsState.awsS3 = false;
                break;
            }
            case "hotRedefineRedis": {
                appSettingsState.hotRedefineClipboard = false;
                appSettingsState.hotRedefineRedis = true;
                appSettingsState.aliYunOss = false;
                appSettingsState.awsS3 = false;
                break;
            }
            case "aliYunOss": {
                appSettingsState.hotRedefineClipboard = false;
                appSettingsState.hotRedefineRedis = false;
                appSettingsState.aliYunOss = true;
                appSettingsState.awsS3 = false;
                break;
            }
            case "awsS3": {
                appSettingsState.hotRedefineClipboard = false;
                appSettingsState.hotRedefineRedis = false;
                appSettingsState.aliYunOss = false;
                appSettingsState.awsS3 = true;
                break;
            }
            default: {
                appSettingsState.hotRedefineClipboard = true;
                appSettingsState.hotRedefineRedis = false;
                appSettingsState.aliYunOss = false;
                appSettingsState.awsS3 = false;
            }
        }
    }

    /**
     * 使用全局的配置  自动转换为 Unicode 编码将中文信息
     *
     * @param appSettingsState
     */
    private static void checkGlobalAutoToUnicode(AppSettingsState appSettingsState) {
        String autoToUnicode = PropertiesComponentUtils.getValue("autoToUnicode");
        if (StringUtils.isNotBlank(autoToUnicode)) {
            appSettingsState.autoToUnicode = "y".equals(autoToUnicode);
        }
    }

    /**
     * 选择命令后关闭窗口
     *
     * @param appSettingsState
     */
    private static void checkGlobalScriptDialogCloseWhenSelectedCommand(AppSettingsState appSettingsState) {
        String globalScriptDialogCloseWhenSelectedCommand = PropertiesComponentUtils.getValue("scriptDialogCloseWhenSelectedCommand");
        if (StringUtils.isNotBlank(globalScriptDialogCloseWhenSelectedCommand)) {
            appSettingsState.scriptDialogCloseWhenSelectedCommand = globalScriptDialogCloseWhenSelectedCommand;
        }
    }

    /**
     * 设置全局的 mapper bean的名称
     *
     * @param appSettingsState
     */
    private static void checkGlobalMybatisMapper(AppSettingsState appSettingsState) {
        String globalMybatisMapperReloadServiceBeanName = PropertiesComponentUtils.getValue("mybatisMapperReloadServiceBeanName");
        String globalMybatisMapperReloadMethodName = PropertiesComponentUtils.getValue("mybatisMapperReloadMethodName");

        if (StringUtils.isNotBlank(globalMybatisMapperReloadServiceBeanName)) {
            appSettingsState.mybatisMapperReloadServiceBeanName = globalMybatisMapperReloadServiceBeanName;
        }
        if (StringUtils.isNotBlank(globalMybatisMapperReloadMethodName)) {
            appSettingsState.mybatisMapperReloadMethodName = globalMybatisMapperReloadMethodName;
        }
    }


    /**
     * 设置一个 arthas zip 包的下载地址 （部分内网无法访问需要自己下载上传）
     *
     * @param appSettingsState
     */
    private static void checkGlobalArthasPackageZipDownloadUrl(AppSettingsState appSettingsState) {

        String globalArthasPackageZipDownloadUrl = PropertiesComponentUtils.getValue("arthasPackageZipDownloadUrl");
        if (StringUtils.isNotBlank(globalArthasPackageZipDownloadUrl)) {
            appSettingsState.arthasPackageZipDownloadUrl = globalArthasPackageZipDownloadUrl;
        }
    }

    /**
     * 检测全局配置中是否有redis，且当前 oss 没有配置  【优先级 阿里云oss > redis > console 】
     *
     * @param appSettingsState
     */
    private static void checkGlobalRedisAndSettingCurrentProjectIfEmpty(AppSettingsState appSettingsState) {
        String redisAddress = PropertiesComponentUtils.getValue("redisAddress");
        String redisPort = PropertiesComponentUtils.getValue("redisPort");
        String redisAuth = PropertiesComponentUtils.getValue("redisAuth");
        String redisCacheKey = PropertiesComponentUtils.getValue("redisCacheKey");
        String redisCacheKeyTtl = PropertiesComponentUtils.getValue("redisCacheKeyTtl");
        if (StringUtils.isNotBlank(appSettingsState.redisAddress)) {
            appSettingsState.redisAddress = redisAddress;
            if (NumberUtils.isDigits(redisPort)) {
                appSettingsState.redisPort = Integer.parseInt(redisPort);
            }
            appSettingsState.redisAuth = redisAuth;
            appSettingsState.redisCacheKey = redisCacheKey;
            if (NumberUtils.isDigits(redisCacheKeyTtl)) {
                appSettingsState.redisCacheKeyTtl = Integer.parseInt(redisCacheKeyTtl);
            }
            if (!appSettingsState.aliYunOss && !appSettingsState.hotRedefineClipboard
                    && !appSettingsState.awsS3) {
                appSettingsState.hotRedefineRedis = true;
            }
        }

    }


    /**
     * 检测全局是否配置static spring context  如果当前工程为空配置
     *
     * @param appSettingsState
     */
    private static void checkGlobalStaticSpringContextAndSettingCurrentProjectIfEmpty(AppSettingsState appSettingsState) {
        // 配置检查.. 兼容老版本
        if (appSettingsState.staticSpringContextOgnl.equals(ArthasCommandConstants.DEFAULT_SPRING_CONTEXT_SETTING)) {
            String springContextValue = PropertiesComponentUtils.getValue(ArthasCommandConstants.SPRING_CONTEXT_STATIC_OGNL_EXPRESSION);
            // 最早的版本设置过配置的！ 使用那个配置作为当前工程的配置
            if (StringUtils.isNotBlank(springContextValue) && !ArthasCommandConstants.DEFAULT_SPRING_CONTEXT_SETTING.equals(springContextValue) && springContextValue.contains(AT)) {
                appSettingsState.staticSpringContextOgnl = springContextValue;
                appSettingsState.springContextGlobalSetting = true;
            }

        }
    }

    /**
     * 检查全局是否有配置阿里云oss 如果存在且当前工程没有配置 带入配置
     *
     * @param appSettingsState
     */
    private static void checkGlobalAliyunOssAndSettingCurrentProjectIfEmpty(AppSettingsState appSettingsState) {
        String endPoint1 = PropertiesComponentUtils.getValue("endpoint");
        String bucketName1 = PropertiesComponentUtils.getValue("bucketName");
        String accessKeyId1 = PropertiesComponentUtils.getValue("accessKeyId");
        String accessKeySecret1 = PropertiesComponentUtils.getValue("accessKeySecret");
        String directoryPrefix1 = PropertiesComponentUtils.getValue("directoryPrefix");
        // 如果之前有设置过就打开了
        if (StringUtils.isNotBlank(bucketName1)
                && StringUtils.isNotBlank(endPoint1)
                && StringUtils.isNotBlank(accessKeyId1)
                && StringUtils.isNotBlank(accessKeySecret1)) {
            if (!appSettingsState.hotRedefineRedis
                    && !appSettingsState.hotRedefineClipboard
                    && !appSettingsState.awsS3) {
                appSettingsState.aliYunOss = true;
            }
            appSettingsState.endpoint = endPoint1;
            appSettingsState.accessKeyId = accessKeyId1;
            appSettingsState.bucketName = bucketName1;
            appSettingsState.accessKeySecret = accessKeySecret1;
            appSettingsState.directoryPrefix = directoryPrefix1;
        }
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
