package com.github.wangji92.arthas.plugin.utils;

import com.aliyun.oss.OSS;
import com.github.wangji92.arthas.plugin.setting.AppSettingsState;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import org.codehaus.groovy.runtime.StackTraceUtils;
import redis.clients.jedis.Jedis;

import java.util.Date;
import java.util.UUID;
import java.util.function.Consumer;

/**
 * 执行上传构建 脚本
 *
 * @author 汪小哥
 * @date 03-05-2021
 */
public class DirectScriptUtils {
    private static final Logger LOG = Logger.getInstance(DirectScriptUtils.class);

    /**
     * base 转换为shell 脚本 | base64 --decode >arthas-idea-plugin-hot-swap.sh;chmod a+x arthas-idea-plugin-hot-swap.sh;./arthas-idea-plugin-hot-swap.sh;
     */
    private static final String BASE_64_TO_SHELL = "%s | base64 --decode >%s;chmod a+x %s;./%s;";

    /**
     * oss 获取到链接
     */
    private static final String OSS_HOT_REDEFINE = "curl -Lk  \"%s\" ";
    /**
     * 剪切板处理字符串
     */
    private static final String CLIPBOARD_HOT_REDEFINE = "echo \"%s\" ";

    /**
     * redis
     */
    private static final String REDIS_HOT_REDEFINE = "echo `redis-cli -h '%s' -p %s  get %s` ";


    /**
     * 信息回传
     */
    public static class DirectScriptResult {
        /**
         * 提示信息
         */
        private StringBuilder tip;

        private Boolean result;


        public Boolean getResult() {
            return result;
        }

        public void setResult(Boolean result) {
            this.result = result;
        }

        public StringBuilder getTip() {
            return tip;
        }

        public void setTip(StringBuilder tip) {
            this.tip = tip;
        }
    }


    /**
     * 构建 脚本
     *
     * @param project
     * @param settings
     * @param base64ShelText
     * @param shellFileName
     * @param consumer
     */
    public static void buildDirectScript(Project project, AppSettingsState settings, String base64ShelText, String shellFileName, Consumer<DirectScriptResult> consumer) {
        if (settings.aliYunOss) {
            DirectScriptUtils.uploadBase64FileToOss(project, settings, base64ShelText, shellFileName, consumer);
        } else if (settings.hotRedefineRedis) {
            DirectScriptUtils.uploadBase64FileToRedis(project, settings, base64ShelText, shellFileName, consumer);
        } else {
            DirectScriptUtils.uploadBase64FileToClipboard(project, settings, base64ShelText, shellFileName, consumer);
        }
    }

    /**
     * 处理到剪切板
     *
     * @param project
     * @param settings
     * @param base64RedefineSh
     * @param consumer
     */
    private static void uploadBase64FileToClipboard(Project project, AppSettingsState settings, String base64RedefineSh, String shellFileName, Consumer<DirectScriptResult> consumer) {
        boolean result = true;
        try {
            String command = String.format(CLIPBOARD_HOT_REDEFINE, base64RedefineSh);
            String finalCommand = String.format(BASE_64_TO_SHELL, command, shellFileName, shellFileName, shellFileName);
            ClipboardUtils.setClipboardString(finalCommand);
        } catch (Exception e) {
            result = false;
            LOG.error("upload  to clipboard error", e);
        }
        StringBuilder tipsBuilder = new StringBuilder("命令已复制到剪切板 去服务器粘贴执行无需打开arthas");
        tipsBuilder.append("【没有配置存储 执行的脚本比较长,推荐配置阿里云oss or redis】");

        DirectScriptResult directScriptResult = new DirectScriptResult();
        directScriptResult.setResult(result);
        directScriptResult.setTip(tipsBuilder);
        consumer.accept(directScriptResult);

    }

    /**
     * 保存数据上传到redis
     *
     * @param project
     * @param settings
     * @param base64RedefineSh
     * @param consumer
     */
    private static void uploadBase64FileToRedis(Project project, AppSettingsState settings, String base64RedefineSh, String shellFileName, Consumer<DirectScriptResult> consumer) {
        boolean result = true;
        try (Jedis jedis = JedisUtils.buildJedisClient(settings.redisAddress, settings.redisPort, 5000, settings.redisAuth)) {

            StringBuilder portAndAuth = new StringBuilder("" + settings.redisPort);
            if (!StringUtils.isBlank(settings.redisAuth)) {
                portAndAuth.append(" -a ").append(settings.redisAuth);
            }

            String cacheKey = settings.redisCacheKey + "_" + UUID.randomUUID().toString();
            jedis.setex(cacheKey, settings.redisCacheKeyTtl, base64RedefineSh);
            String command = String.format(REDIS_HOT_REDEFINE, settings.redisAddress, portAndAuth, cacheKey);
            String finalCommand = String.format(BASE_64_TO_SHELL, command, shellFileName, shellFileName, shellFileName);

            ClipboardUtils.setClipboardString(finalCommand);
        } catch (Exception e) {
            LOG.error("upload to redis error", e);
            NotifyUtils.notifyMessage(project, "上传文件到redis 失败" + e.getMessage(), NotificationType.ERROR);
            result = false;
        }

        StringBuilder tipsBuilder = new StringBuilder("命令已复制到剪切板 去服务器粘贴执行无需打开arthas");
        tipsBuilder.append("【服务器服务器需要有redis cli命令】");
        DirectScriptResult directScriptResult = new DirectScriptResult();
        directScriptResult.setResult(result);
        directScriptResult.setTip(tipsBuilder);
        consumer.accept(directScriptResult);
    }

    /**
     * 上传热更新 文件到oss
     *
     * @param project
     * @param settings
     * @param base64RedefineSh
     * @param shellFileName
     * @param consumer
     */
    private static void uploadBase64FileToOss(Project project, AppSettingsState settings, String base64RedefineSh, String shellFileName, Consumer<DirectScriptResult> consumer) {
        boolean result = true;
        OSS oss = null;
        try {
            oss = AliyunOssUtils.buildOssClient(project);
            String filePathKey = settings.directoryPrefix + UUID.randomUUID().toString();
            String urlEncodeKeyPath = AliyunOssUtils.putFile(oss, settings.bucketName, filePathKey, base64RedefineSh);
            String presignedUrl = AliyunOssUtils.generatePresignedUrl(oss, settings.bucketName, urlEncodeKeyPath, new Date(System.currentTimeMillis() + 3600L * 1000));
            String command = String.format(OSS_HOT_REDEFINE, presignedUrl);
            String finalCommand = String.format(BASE_64_TO_SHELL, command, shellFileName, shellFileName, shellFileName);
            ClipboardUtils.setClipboardString(finalCommand);
        } catch (Exception e) {
            LOG.error("upload to oss error", e);
            NotifyUtils.notifyMessage(project, "上传文件到oss 失败" + e.getMessage(), NotificationType.ERROR);
            StackTraceUtils.printSanitizedStackTrace(e);
            result = false;
        } finally {
            if (oss != null) {
                oss.shutdown();
            }
        }

        StringBuilder tipsBuilder = new StringBuilder("命令已复制到剪切板 去服务器粘贴执行无需打开arthas");
        DirectScriptResult directScriptResult = new DirectScriptResult();
        directScriptResult.setResult(result);
        directScriptResult.setTip(tipsBuilder);
        consumer.accept(directScriptResult);
    }


}
