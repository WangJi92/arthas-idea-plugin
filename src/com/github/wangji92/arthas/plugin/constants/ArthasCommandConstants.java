package com.github.wangji92.arthas.plugin.constants;

/**
 * arthas plugin 插件中使用的常量的信息
 *
 * @author 汪小哥
 * @date 21-12-2019
 */
public interface ArthasCommandConstants {

    /**
     * 默认条件表达式
     */
    String DEFAULT_CONDITION_EXPRESS = "'1==1'";

    /**
     * 跳过JDK 自带的方法
     */
    String DEFAULT_SKIP_JDK = "--skipJDKMethod false";
    /**
     * 调用次数
     */
    String INVOKE_COUNT = "5";
    /**
     * 调用监控的次数
     */
    String INVOKE_MONITOR_COUNT = "10";
    /**
     * 调用监控的时间间隔
     */
    String INVOKE_MONITOR_INTERVAL = "10";
    /**
     * 展开的结构
     */
    String RESULT_X = "3";

    String PRO_PREFIX = "ArthasIdeaPlugin";
    /**
     * 缓存hashValue的信息
     */
    String CLASSLOADER_HASH_VALUE = "CLASSLOADER_HASH_VALUE";

    /**
     * spring 获取表达式
     */
    String SPRING_CONTEXT_STATIC_OGNL_EXPRESSION = "springContextStaticOgnlExpression";

    /**
     * 默认static spring context
     */
    String DEFAULT_SPRING_CONTEXT_SETTING = "@applicationContextProvider@context";

    /**
     * spring 表达式信息
     */
    String SPRING_CONTEXT_PARAM = "#springContext";
    /**
     * 安装脚本
     */
    String AS_INSTALL_BASH = "curl -sk https://arthas.gitee.io/arthas-boot.jar -o ~/.arthas-boot.jar  && echo \"alias as.sh='java -jar ~/.arthas-boot.jar --repo-mirror aliyun --use-http'\" >> ~/.bashrc && source ~/.bashrc";
    /**
     * 获取spring 所有的环境变量的表达式
     */
    String SPRING_ALL_PROPERTY = "%s '%s#allProperties={},#standardServletEnvironment=#propertySourceIterator=%s.getEnvironment(),#propertySourceIterator=#standardServletEnvironment.getPropertySources().iterator(),#propertySourceIterator.{#key=#this.getName(),#allProperties.add(\"                \"),#allProperties.add(\"------------------------- name:\"+#key),#this.getSource() instanceof java.util.Map ?#this.getSource().entrySet().iterator.{#key=#this.key,#allProperties.add(#key+\"=\"+#standardServletEnvironment.getProperty(#key))}:#{}},#allProperties'";
}
