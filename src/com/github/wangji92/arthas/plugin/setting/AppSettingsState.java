package com.github.wangji92.arthas.plugin.setting;

import com.github.wangji92.arthas.plugin.constants.ArthasCommandConstants;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.project.Project;
import com.intellij.util.xmlb.XmlSerializerUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static com.github.wangji92.arthas.plugin.constants.ArthasCommandConstants.DEFAULT_SPRING_CONTEXT_SETTING;

/**
 * Supports storing the application settings in a persistent way.
 * The State and Storage annotations define the name of the data and the file name where
 * these persistent application settings are stored.
 *
 * @author 汪小哥
 * @date 14-08-2020
 */
@State(
        name = "arthas.idea.plugin",
        storages = {@Storage("setting.xml")}
)
public class AppSettingsState implements PersistentStateComponent<AppSettingsState> {

    /**
     * spring ognl 配置
     */
    public String staticSpringContextOgnl = DEFAULT_SPRING_CONTEXT_SETTING;

    /**
     * 跳过jdk trace
     */
    public boolean traceSkipJdk = true;
    /**
     * 调用次数
     */
    public String invokeMonitorCount = ArthasCommandConstants.INVOKE_MONITOR_COUNT;
    /**
     * 时间间隔
     */
    public String invokeMonitorInterval = ArthasCommandConstants.INVOKE_MONITOR_INTERVAL;

    /**
     * 调用次数
     */
    public String invokeCount = ArthasCommandConstants.INVOKE_COUNT;

    /**
     * 打印属性的深度
     */
    public String depthPrintProperty = ArthasCommandConstants.RESULT_X;



    public static AppSettingsState getInstance(@NotNull Project project) {
        return ServiceManager.getService(project, AppSettingsState.class);
    }

    @Nullable
    @Override
    public AppSettingsState getState() {
        return this;
    }

    @Override
    public void loadState(@NotNull AppSettingsState state) {
        XmlSerializerUtil.copyBean(state, this);
    }
}
