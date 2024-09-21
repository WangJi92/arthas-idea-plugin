package com.github.wangji92.arthas.plugin.common.pojo;


import lombok.Data;

/**
 * @author https://github.com/shuxiongwuziqi
 * @date 2024/03/22
 * @description ws arthas 请求体
 */
@Data
public class WsArthasRequest {
    public static final String READ = "read";
    private String action;

    private String data;

    public WsArthasRequest(String data) {
        this.data = data;
        this.action = READ;
    }
}
