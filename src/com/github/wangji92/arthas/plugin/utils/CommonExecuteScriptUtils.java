package com.github.wangji92.arthas.plugin.utils;

import com.github.wangji92.arthas.plugin.common.enums.ShellScriptVariableEnum;
import com.github.wangji92.arthas.plugin.setting.AppSettingsState;
import com.google.common.collect.Maps;
import com.google.common.io.BaseEncoding;
import com.intellij.openapi.project.Project;

import java.util.Map;

/**
 * 通用执行脚本工具类
 *
 * @author 汪小哥
 * @date 04-05-2021
 */
public class CommonExecuteScriptUtils {
    /**
     * 直接执行脚本的命令
     *
     * @param project
     * @param scCommand 获取classloader hash value（不一定是sc -d  也可以是 logger --name xxx class）
     * @param command
     * @param appendTip
     */
    public static void executeCommonScript(Project project, String scCommand, String command, String appendTip) {
        AppSettingsState settings = AppSettingsState.getInstance(project);
        Map<String, String> params = Maps.newHashMap();
        params.put("arthasIdeaPluginApplicationName", settings.selectProjectName);
        params.put("arthasPackageZipDownloadUrl", settings.arthasPackageZipDownloadUrl);
        if (StringUtils.isNotBlank(scCommand) && !command.contains(ShellScriptVariableEnum.CLASSLOADER_HASH_VALUE.getCode())) {
            command = String.join(" ", command, "-c", ShellScriptVariableEnum.CLASSLOADER_HASH_VALUE.getCode());
        }
        params.put("SC_COMMAND", scCommand);

        // 坑 这里需要对 "" 中的 "进行转义
        command = command.replaceAll("\"", "\\\\\"");
        params.put("arthasCommonScriptCommand", command);

        String commonFunctionSh = StringUtils.stringSubstitutorFromFilePath("/template/plugin-common-function.sh", params);
        String mybatisMapperReloadSh = StringUtils.stringSubstitutorFromFilePath("/template/common-execution-script.sh", params);
        mybatisMapperReloadSh = commonFunctionSh + "\n" + mybatisMapperReloadSh;
        String base64MybatisMapperReloadSh = BaseEncoding.base64().encode(mybatisMapperReloadSh.getBytes());
        DirectScriptUtils.buildDirectScript(project, settings, base64MybatisMapperReloadSh, "arthas-idea-plugin-common-execution-script.sh", directScriptResult -> {
            if (directScriptResult.getResult()) {
                if (StringUtils.isNotBlank(appendTip)) {
                    directScriptResult.getTip().append(appendTip);
                }
                NotifyUtils.notifyMessage(project, directScriptResult.getTip().toString());
            }
        });
    }
}
