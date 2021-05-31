package com.github.wangji92.arthas.plugin.constants;

/**
 * arthas plugin 插件中使用的常量的信息
 *
 * @author 汪小哥
 * @date 21-12-2019
 */
public interface ArthasCommandConstants {

    String AT = "@";

    /**
     * mybatis mapper 热更新未知
     */
    String MYBATIS_MAPPER_RELOAD_BASH_PACKAGE_PATH = "$HOME/opt/arthas/mybatis/mapper";

    /**
     * arthas 热更新 基础包名称
     */
    String HOT_SWAMP_BASH_PACKAGE_PATH = "$HOME/opt/arthas/hotSwap/classes";

    /**
     * 默认条件表达式
     */
    String DEFAULT_CONDITION_EXPRESS = "'1==1'";

    /**
     * 跳过JDK 自带的方法
     */
    String DEFAULT_SKIP_JDK_FALSE = "--skipJDKMethod false";
    /**
     * 不跳过JDK 自带的方法
     */
    String DEFAULT_SKIP_JDK_TRUE = "--skipJDKMethod true";

    /**
     * ognl 内部类分割符 https://github.com/alibaba/arthas/issues/71
     */
    String OGNL_INNER_CLASS_SEPARATOR = "$";
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
    String AS_INSTALL_BASH = "curl -sk https://arthas.aliyun.com/arthas-boot.jar -o ~/.arthas-boot.jar  && echo \"alias as.sh='java -jar ~/.arthas-boot.jar --repo-mirror aliyun --use-http 2>&1'\" >> ~/.bashrc && source ~/.bashrc && echo \"source ~/.bashrc\" >> ~/.bash_profile && source ~/.bash_profile";

    /**
     * 获取所有的 map source的配置
     */
    String SPRING_ALL_MAP_PROPERTY = "#allProperties={},#propertySourceIterator=#standardServletEnvironment.getPropertySources().iterator(),#propertySourceIterator.{#key=#this.getName(),#allProperties.add(\"                \"),#allProperties.add(\"------------------------- name:\"+#key),#this.getSource() instanceof java.util.Map ?#this.getSource().entrySet().iterator.{#key=#this.key,#allProperties.add(#key+\"=\"+#standardServletEnvironment.getProperty(#key))}:#{}},#allProperties";
    /**
     * 获取spring 所有的环境变量的表达式
     */
    String SPRING_ALL_PROPERTY = "%s '%s,#standardServletEnvironment=#springContext.getEnvironment(),+" + SPRING_ALL_MAP_PROPERTY + "'";

    /**
     * 默认的 arthas zip 完整包的下载地址
     */
    String DEFAULT_ARTHAS_PACKAGE_ZIP_DOWNLOAD_URL = "https://arthas.aliyun.com/download/latest_version?mirror=aliyun";
    /**
     * 默认重新加载mapper文件的方法名称
     */
    String DEFAULT_MYBATIS_MAPPER_RELOAD_METHOD_NAME = "reloadAllSqlSessionFactoryMapper";
    /**
     * 默认重新加载 mapper的服务bean名称
     */
    String DEFAULT_MYBATIS_MAPPER_RELOAD_SERVICE_BEAN_NAME = "mybatisMapperXmlFileReloadService";


}
