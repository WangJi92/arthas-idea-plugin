package com.github.wangji92.arthas.plugin.web;

import lombok.Data;

/**
 * @author: wuziqi
 * @description: TODO
 * @date: 2023/8/6
 */
@Data
public class ArthasCmdRequest {
    public static final String DEFAULT_ACTION = "read";
    public static final String RETURN_STR = "\\r";
    private String action;

    private String data;

    public ArthasCmdRequest() {
        this.action = DEFAULT_ACTION;
    }

    public ArthasCmdRequest(String cmd) {
        this.action = DEFAULT_ACTION;
        this.data = cmd + RETURN_STR;
    }
}
