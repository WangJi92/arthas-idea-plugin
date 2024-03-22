package com.github.wangji92.arthas.plugin.utils;



import com.github.wangji92.arthas.plugin.web.entity.Env;
import com.github.wangji92.arthas.plugin.web.service.AppNameService;
import com.github.wangji92.arthas.plugin.web.service.impl.AppNameServiceImpl;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * @author imyzt
 * @date 2023/08/11
 * @description Arthas Server Holder
 */
public class ProjectContextHolder {

    private static final AppNameService appNameService = new AppNameServiceImpl();

    public static List<String> getAgents(Env env) {
        return appNameService.getAppNames(env);
    }

    public static String getAgentName(Env env, String artifactId, String moduleName) {

        List<String> appNames = appNameService.getAppNames(env);

        // 先根据artifactId进行匹配，如果匹配不到，再使用name匹配
        Optional<String> matchedAppName = appNames.stream().filter(name -> name.contains(artifactId) && name.contains(env.getAgentServerInfo().getBindGitBranch().toLowerCase())).min(Comparator.comparingInt(String::length));
        if (!matchedAppName.isPresent() && StringUtils.isNoneBlank(moduleName)) {
            matchedAppName = appNames.stream().filter(name -> name.contains(moduleName) && name.contains(env.getAgentServerInfo().getBindGitBranch().toLowerCase())).min(Comparator.comparingInt(String::length));
        }
        return matchedAppName.orElse(null);
    }
}
