package com.github.idea.arthas.plugin.common.pojo;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author https://github.com/imyzt
 * @date 2024/03/12
 * @description Arthas Server 地址信息
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TunnelServerInfo {

    /**
     * Agent Server 名称
     */
    private String name;

    /**
     * Tunnel Server 地址
     */
    private String tunnelAddress;

    /**
     * Tunnel Server Agent 地址
     */
    private String wsAddress;

    public Object[] toObjArr() {
        return new Object[]{this.name, this.tunnelAddress, this.wsAddress};
    }
}
