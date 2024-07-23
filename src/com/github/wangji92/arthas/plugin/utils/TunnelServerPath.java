package com.github.wangji92.arthas.plugin.utils;

import com.github.wangji92.arthas.plugin.common.pojo.TunnelServerInfo;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.ToString;

import java.net.URL;

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
    private static final String WS_URL = "ws://%s:7777/ws?method=connectArthas&id=%s&targetServer=%s";
    private static final String WSS_URL = "wss://%s:7777/ws?method=connectArthas&id=%s&targetServer=%s";

    public static String getAppsPath(String host) {
        return String.format(APPS_PATH, host);
    }

    public static String getAgentInfoPath(String host, String appId) {
        return String.format(AGENT_INFO_PATH, host, appId);
    }

    @SneakyThrows
    public static String getWsUrl(String agentId, String agentIp, TunnelServerInfo tunnelServerInfo) {
        String url = tunnelServerInfo.getAddress().contains("https") ? WSS_URL : WS_URL;
        return String.format(url, new URL(tunnelServerInfo.getAddress()).getHost(), agentId, agentIp);
    }
}
