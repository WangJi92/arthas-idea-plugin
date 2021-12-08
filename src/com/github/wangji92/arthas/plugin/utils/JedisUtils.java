package com.github.wangji92.arthas.plugin.utils;

import com.intellij.openapi.diagnostic.Logger;
import redis.clients.jedis.Jedis;

/**
 * 简单的redis 操作类
 *
 * @author 汪小哥
 * @date 01-01-2021
 */
public class JedisUtils {
    private static final Logger LOG = Logger.getInstance(JedisUtils.class);


    /**
     * 构建 jedis 简单的连接
     *
     * @param host
     * @param port
     * @param timeout
     * @param password
     * @return
     */
    public static Jedis buildJedisClient(String host, Integer port, Integer timeout, String password) {
        if (StringUtils.isBlank(host)) {
            throw new IllegalArgumentException("配置redis host 错误");
        }
        if (port == null) {
            throw new IllegalArgumentException("配置redis port 错误");
        }
        if (timeout == null) {
            timeout = 10000;
        }
        Jedis jedis = new Jedis(host, port, timeout);
        if (StringUtils.isNotBlank(password)) {
            jedis.auth(password);
        }
        return jedis;
    }

    /**
     * 检查连接的情况
     *
     * @param jedis
     */
    public static void checkRedisClient(Jedis jedis) {
        try {
            jedis.ping();
        } catch (Exception e) {
            LOG.error("checkRedisClient",e);
            throw new IllegalArgumentException("配置redis信息错误 " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        Jedis jedis = JedisUtils.buildJedisClient("127.0.0.1", 6379, 1000, "123");
        JedisUtils.checkRedisClient(jedis);
        jedis.setex("test", 10, "wangji");
        System.out.println(jedis.get("test"));
    }
}
