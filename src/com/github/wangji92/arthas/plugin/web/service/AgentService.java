package com.github.wangji92.arthas.plugin.web.service;



import com.github.wangji92.arthas.plugin.web.entity.AgentInfo;
import com.github.wangji92.arthas.plugin.web.entity.Env;

import java.util.Map;

/**
 * @author: wuziqi
 * @description: agent服务
 * @date: 2023/8/9
 */
public interface AgentService {

    Map<String, AgentInfo> getAgentInfoArtifactId(String appName, Env env);
}
