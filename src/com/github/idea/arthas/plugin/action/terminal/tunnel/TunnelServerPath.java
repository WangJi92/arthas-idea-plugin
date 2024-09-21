package com.github.idea.arthas.plugin.action.terminal.tunnel;

import com.github.idea.arthas.plugin.common.pojo.TunnelServerInfo;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

/**
 * @author https://github.com/imyzt
 * @date 2023/08/11
 * @description 环境包装类
 */
@ToString
@Getter
@AllArgsConstructor
public class TunnelServerPath {

    private static final String APPS_PATH = "%s/api/tunnelApps";
    private static final String AGENT_INFO_PATH = "%s/api/tunnelAgentInfo?app=%s";
    private static final String WS_URL = "%s/ws?method=connectArthas&id=%s&targetServer=%s";

    public static String getAppsPath(String host) {
        return String.format(APPS_PATH, host);
    }

    public static String getAgentInfoPath(String host, String appId) {
        return String.format(AGENT_INFO_PATH, host, appId);
    }

    public static String getWsUrl(String agentId, String agentIp, TunnelServerInfo tunnelServerInfo) {
        return String.format(WS_URL, tunnelServerInfo.getWsAddress(), agentId, agentIp);
    }
}
