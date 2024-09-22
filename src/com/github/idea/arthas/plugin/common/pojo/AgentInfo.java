package com.github.idea.arthas.plugin.common.pojo;

import lombok.Data;

/**
 * @author: https://github.com/shuxiongwuziqi
 * @description: agent信息
 * @date: 2023/8/9
 */
@Data
public class AgentInfo {
    /**
     * 主机IP地址  应用启动arthas-agent 的地址
     */
    private String host;

    /**
     * 端口号
     */
    private Integer port;
    /**
     * agentID
     */
    private String agentId;

    /**
     * arthas版本号
     */
    private String arthasVersion;

    /**
     * 客户端连接IP地址 (arthas-tunnel server 的 ip)
     */
    private String clientConnectHost;
}
