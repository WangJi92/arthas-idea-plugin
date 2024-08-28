package com.github.wangji92.arthas.plugin.component;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.github.wangji92.arthas.plugin.common.pojo.AgentInfo;
import com.github.wangji92.arthas.plugin.utils.HttpUtil;
import com.github.wangji92.arthas.plugin.utils.TunnelServerPath;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author imyzt
 * @date 2024/08/28
 * @description 封装与Arthas Tunnel Server通信部分逻辑
 */
public class ArthasTunnelServerService {

    /**
     * 根据TunnelServer, 获取应用列表
     * @param tunnelAddress TunnelServer地址
     * @return 应用列表
     */
    public List<String> getAppIdList(String tunnelAddress) {
        String appsUrl = TunnelServerPath.getAppsPath(tunnelAddress);
        String resp = HttpUtil.get(appsUrl);
        return JSON.parseArray(resp).toJavaList(String.class).stream().sorted().collect(Collectors.toList());
    }

    /**
     * 根据TunnelServer+AppId, 获取实例列表
     * @param tunnelAddress TunnelServer地址
     * @param appId 应用名称
     * @return 实例列表
     */
    public Map<String, AgentInfo> getAgentInfoMap(String tunnelAddress, String appId) {
        String agentsUrl = TunnelServerPath.getAgentInfoPath(tunnelAddress, appId);
        String resp = HttpUtil.get(agentsUrl);
        return getAgentInfoMap(resp);
    }

    /**
     * 把agentId填充到AgentInfo中，方便后续处理
     *
     * @param res 返回
     * @return AgentInfoMap
     */
    private static Map<String, AgentInfo> getAgentInfoMap(String res) {
        return Optional.ofNullable(res).map(str -> JSON.parseObject(str, new TypeReference<Map<String, AgentInfo>>() {
        })).map(infoMap -> {
            infoMap.keySet().forEach(id -> infoMap.get(id).setAgentId(id));
            return infoMap;
        }).orElse(null);
    }
}
