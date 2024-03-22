package com.github.wangji92.arthas.plugin.web.entity;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author imyzt
 * @date 2024/03/12
 * @description Arthas Server 地址信息
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AgentServerInfo {

    /**
     * Agent Server 名称
     */
    private String name;

    /**
     * Agent Server 地址
     */
    private String address;

    /**
     * 绑定的git分支(用于自动推断)
     */
    private String bindGitBranch;
}
