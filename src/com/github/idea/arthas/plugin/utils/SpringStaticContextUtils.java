package com.github.idea.arthas.plugin.utils;

import com.github.idea.arthas.plugin.setting.AppSettingsState;
import com.github.idea.arthas.plugin.constants.ArthasCommandConstants;
import com.google.common.base.Splitter;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.project.Project;

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
    public static String getStaticSpringContextGetBeanPrefix(Project project, String beanName) {
        String springStaticContextConfig = getStaticSpringContextPrefix(project);
        return String.format(SPRING_CONTEXT_GET_BEAN, springStaticContextConfig, beanName);
    }

    /**
     * #springContext=填充,#targetBean=#springContext.getBean("%s")
     *
     * @param beanName
     * @return
     */
    public static String getStaticSpringContextGetBeanVariable(Project project, String beanName) {
        String springStaticContextConfig = getStaticSpringContextPrefix(project);
        return String.format(SPRING_CONTEXT_TARGET_BEAN, springStaticContextConfig, beanName);
    }

    /**
     * 获取静态Spring context的前缀  #springContext=@applicationContextProvider@context
     *
     * @return
     */
    public static String getStaticSpringContextPrefix(Project project) {
        String springStaticContextConfig = getSpringStaticContextConfig(project);
        //#springContext=填充
        return "" + ArthasCommandConstants.SPRING_CONTEXT_PARAM + "=" + springStaticContextConfig;
    }

    public static String getStaticSpringContextClassName(Project project) {
        String springStaticContextConfig = getSpringStaticContextConfig(project);
        //#springContext=填充,#springContext.getBean("%s")
        // 获取class的classloader
        List<String> springContextCLassLists = Splitter.on('@').omitEmptyStrings().splitToList(springStaticContextConfig);
        if (springContextCLassLists.isEmpty()) {
            throw new IllegalArgumentException("Static Spring context requires manual configuration");
        }
        //@com.wj.study.demo.generator.ApplicationContextProvider@context  配置的是这个玩意
        return springContextCLassLists.get(0);
    }

    /**
     * 是否配置 static spring context 上下文
     *
     * @param project
     * @return
     */
    public static boolean booleanConfigStaticSpringContext(Project project) {
        try {
            getStaticSpringContextClassName(project);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 是否配置了 static  spring context
     *
     * @param project
     * @return
     */
    public static boolean booleanConfigStaticSpringContextFalseOpenConfig(Project project) {
        boolean staticSpringContextConfig = SpringStaticContextUtils.booleanConfigStaticSpringContext(project);
        if (!staticSpringContextConfig) {
            NotifyUtils.notifyMessage(project, "Static Spring context requires manual configuration <a href=\"https://www.yuque.com/arthas-idea-plugin/help/ugrc8n\">arthas idea setting</a>", NotificationType.ERROR);
        }
        return staticSpringContextConfig;
    }


    /**
     * 获取spring static context的配置
     */
    private static String getSpringStaticContextConfig(Project project) {
        // 这里换个获取配置的方式
        AppSettingsState instance = AppSettingsState.getInstance(project);
        String springContextValue = instance.staticSpringContextOgnl;
        if (StringUtils.isBlank(springContextValue) || ArthasCommandConstants.DEFAULT_SPRING_CONTEXT_SETTING.equals(springContextValue)) {
            throw new IllegalArgumentException("Static Spring context requires manual configuration");
        }
        if (springContextValue.endsWith(",")) {
            springContextValue = springContextValue.substring(0, springContextValue.length() - 2);
        }
        return springContextValue;
    }
}
