package com.github.wangji92.arthas.plugin.web.entity;

import lombok.Data;

/**
 * @author: wuziqi
 * @description: agent信息
 * @date: 2023/8/9
 */
@Data
public class AgentInfo {
    /**
     * arthas版本号
     */
    private String arthasVersion;

    /**
     * 客户端连接IP地址
     */
    private String clientConnectHost;

    /**
     * 主机IP地址
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
}
