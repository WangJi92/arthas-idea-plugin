package com.github.wangji92.arthas.plugin.web.entity;

import com.github.wangji92.arthas.plugin.setting.AppSettingsState;
import com.github.wangji92.arthas.plugin.setting.ApplicationSettingsState;
import com.intellij.openapi.application.ApplicationManager;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.ToString;
import org.jetbrains.annotations.Nullable;

import java.net.URL;

/**
 * @author imyzt
 * @date 2023/08/11
 * @description 环境包装类
 */
@ToString
@Getter
@AllArgsConstructor
public class Env {

    private static final String APPS_PATH = "%s/api/tunnelApps";
    private static final String AGENT_INFO_PATH = "%s/api/tunnelAgentInfo?app=%s";
    private static final String WS_URL = "wss://%s:7777/ws?method=connectArthas&id=%s&targetServer=%s";

    private final AgentServerInfo agentServerInfo;

    public static Env valueOf(String selectRowValue) {

        ApplicationSettingsState service = ApplicationSettingsState.getInstance();
        return service.agentServerInfoList.stream().filter(a -> a.getName().equals(selectRowValue)).findFirst()
                .map(Env::new).orElse(null);
    }

    @SneakyThrows
    public String getWsUrl(String agentId, String host) {
        String domain = getDomain();
        return String.format(WS_URL, new URL(domain).getHost(), agentId, host);
    }

    @Nullable
    private String getDomain() {
        return this.agentServerInfo.getAddress();
    }

    public static String getEnvNameByGitBranch(String gitBranch) {
        if (gitBranch == null) {
            return null;
        }
        gitBranch = gitBranch.toUpperCase();

        ApplicationSettingsState service = ApplicationSettingsState.getInstance();
        for (AgentServerInfo agentServerInfo : service.agentServerInfoList) {
            if (gitBranch.contains(agentServerInfo.getBindGitBranch())) {
                return agentServerInfo.getName();
            }
        }

        return null;
    }

    public String getAppListUrl() {
        return String.format(APPS_PATH, this.agentServerInfo.getAddress());
    }

    public String getAgentInfoUrl(String appName) {
        String domain = getDomain();
        return String.format(AGENT_INFO_PATH, domain, appName);
    }
}
