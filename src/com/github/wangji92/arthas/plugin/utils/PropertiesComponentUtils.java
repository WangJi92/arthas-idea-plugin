package com.github.wangji92.arthas.plugin.utils;

import com.github.wangji92.arthas.plugin.constants.ArthasCommandConstants;
import com.intellij.ide.util.PropertiesComponent;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * 设置配置信息
 * {@literal https://cloud.tencent.com/developer/article/1348741 }
 * {@literal https://www.jetbrains.org/intellij/sdk/docs/basics/persisting_state_of_components.html }
 * {@literal http://corochann.com/intellij-plugin-development-introduction-persiststatecomponent-903.html }
 * {@literal https://blog.xiaohansong.com/idea-plugin-development.html}
 * {@literal http://www.dcalabresi.com/blog/java/spring-context-static-class/}
 *
 * @author 汪小哥
 * @date 22-12-2019
 */
public class PropertiesComponentUtils {

    /**
     * 设置application级别的信息变量
     *
     * @param name
     * @param value
     */
    public static void setValue(@NotNull String name, @Nullable String value) {
        if (name == null) {
            return;
        }
        if (value == null) {
            return;
        }
        //获取 application 级别的 PropertiesComponent
        PropertiesComponent propertiesComponent = PropertiesComponent.getInstance();
        propertiesComponent.setValue(ArthasCommandConstants.PRO_PREFIX + name, value);
    }

    /**
     * 获取值得信息
     *
     * @param name
     * @return
     */
    public static String getValue(@NotNull String name) {
        if (name == null) {
            return "";
        }
        PropertiesComponent propertiesComponent = PropertiesComponent.getInstance();
        String value = propertiesComponent.getValue(ArthasCommandConstants.PRO_PREFIX + name);
        return value == null ? "" : value;
    }

    /**
     * 设置当前工程级别的配置
     *
     * @param project
     * @param name
     * @param value
     */
    public static void setValue(@NotNull Project project, @NotNull String name, @Nullable String value) {
        if (name == null) {
            return;
        }
        if (value == null) {
            return;
        }
        //获取 project 级别的 PropertiesComponent
        PropertiesComponent propertiesComponent = PropertiesComponent.getInstance(project);
        propertiesComponent.setValue(ArthasCommandConstants.PRO_PREFIX + name, value);
    }

    /**
     * 获取当前工程级别的配置
     *
     * @param project
     * @param name
     * @return
     */
    public static String getValue(@NotNull Project project, @NotNull String name) {
        if (name == null) {
            return "";
        }
        PropertiesComponent propertiesComponent = PropertiesComponent.getInstance(project);
        String value = propertiesComponent.getValue(ArthasCommandConstants.PRO_PREFIX + name);
        return value == null ? "" : value;
    }

}
