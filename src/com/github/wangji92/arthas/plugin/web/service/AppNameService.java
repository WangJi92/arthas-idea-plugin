package com.github.wangji92.arthas.plugin.web.service;


import com.github.wangji92.arthas.plugin.web.entity.Env;

import java.util.List;

/**
 * @author: wuziqi
 * @description: 应用列表服务
 * @date: 2023/8/9
 */
public interface AppNameService {

    /**
     * 根据环境获取应用列表
     * @param env 环境
     * @return 应用列表
     */
    List<String> getAppNames(Env env);
}
