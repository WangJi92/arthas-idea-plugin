package com.github.wangji92.arthas.plugin.utils;

import com.github.wangji92.arthas.plugin.constants.ArthasCommandConstants;
import com.google.common.base.Splitter;

import java.util.List;

/**
 * spring 静态的context 工具类
 *
 * @author 汪小哥
 * @date 13-08-2020
 */
public class SpringStaticContextUtils {

    /**
     * 获取构造Bean的表达式
     */
    private static final String SPRING_CONTEXT_GET_BEAN = "%s,#springContext.getBean(\"%s\")";

    /**
     * 构造targetBean
     */
    private static final String SPRING_CONTEXT_TARGET_BEAN = "%s,#targetBean=#springContext.getBean(\"%s\")";

    /**
     * 获取静态Spring context的前缀
     * //这个是配置的  @com.wj.study.demo.generator.ApplicationContextProvider@context  这个是配置的
     * //例子 #springContext=@com.wj.study.demo.generator.ApplicationContextProvider@context,#springContext.getBean("userService")
     * <p> 构造一个如下的例子
     * // #springContext=填充,#springContext.getBean("%s")
     *
     * @return
     */
    public static String getStaticSpringContextGetBeanPrefix(String beanName) {
        String springStaticContextConfig = getStaticSpringContextPrefix();
        return String.format(SPRING_CONTEXT_GET_BEAN, springStaticContextConfig, beanName);
    }

    /**
     * #springContext=填充,#targetBean=#springContext.getBean("%s")
     *
     * @param beanName
     * @return
     */
    public static String getStaticSpringContextGetBeanVariable(String beanName) {
        String springStaticContextConfig = getStaticSpringContextPrefix();
        return String.format(SPRING_CONTEXT_TARGET_BEAN, springStaticContextConfig, beanName);
    }

    /**
     * 获取静态Spring context的前缀  #springContext=@applicationContextProvider@context
     *
     * @return
     */
    public static String getStaticSpringContextPrefix() {
        String springStaticContextConfig = getSpringStaticContextConfig();
        //#springContext=填充
        return "" + ArthasCommandConstants.SPRING_CONTEXT_PARAM + "=" + springStaticContextConfig;
    }

    public static String getStaticSpringContextClassName() {
        String springStaticContextConfig = getSpringStaticContextConfig();
        //#springContext=填充,#springContext.getBean("%s")
        // 获取class的classloader
        List<String> springContextCLassLists = Splitter.on('@').omitEmptyStrings().splitToList(springStaticContextConfig);
        if (springContextCLassLists.isEmpty()) {
            throw new IllegalArgumentException("Static Spring context 需要手动配置，具体参考Arthas Idea Plugin Help 命令获取相关文档");
        }
        //@com.wj.study.demo.generator.ApplicationContextProvider@context  配置的是这个玩意
        return springContextCLassLists.get(0);
    }


    /**
     * 获取spring static context的配置
     */
    private static String getSpringStaticContextConfig() {
        String springContextValue = PropertiesComponentUtils.getValue(ArthasCommandConstants.SPRING_CONTEXT_STATIC_OGNL_EXPRESSION);
        if (StringUtils.isBlank(springContextValue) || ArthasCommandConstants.DEFAULT_SPRING_CONTEXT_SETTING.equals(springContextValue)) {
            throw new IllegalArgumentException("Static Spring context 需要手动配置，具体参考Arthas Idea Plugin Help 命令获取相关文档");
        }
        if (springContextValue.endsWith(",")) {
            springContextValue = springContextValue.substring(0, springContextValue.length() - 2);
        }
        return springContextValue;
    }
}
