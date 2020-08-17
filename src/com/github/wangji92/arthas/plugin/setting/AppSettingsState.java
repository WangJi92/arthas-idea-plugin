package com.github.wangji92.arthas.plugin.setting;

import com.github.wangji92.arthas.plugin.constants.ArthasCommandConstants;
import com.github.wangji92.arthas.plugin.utils.PropertiesComponentUtils;
import com.github.wangji92.arthas.plugin.utils.StringUtils;
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
    public boolean traceSkipJdk = false;
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

    /**
     * 是否展示默认的条件表达式
     */
    public boolean conditionExpressDisplay = true;

    /**
     * {@literal https://arthas.aliyun.com/doc/batch-support.html 批处理支持}
     * {@literal https://arthas.aliyun.com/doc/advanced-use.html --select}
     */
    public String selectProjectName;


    public static AppSettingsState getInstance(@NotNull Project project) {
        AppSettingsState appSettingsState = ServiceManager.getService(project, AppSettingsState.class);
        // 配置检查.. 兼容老版本
        if (appSettingsState.staticSpringContextOgnl.equals(ArthasCommandConstants.DEFAULT_SPRING_CONTEXT_SETTING)) {
            String springContextValue = PropertiesComponentUtils.getValue(ArthasCommandConstants.SPRING_CONTEXT_STATIC_OGNL_EXPRESSION);
            // 最早的版本设置过配置的！ 使用那个配置作为当前工程的配置
            if (StringUtils.isNotBlank(springContextValue) && !ArthasCommandConstants.DEFAULT_SPRING_CONTEXT_SETTING.equals(springContextValue) && springContextValue.contains("@")) {
                appSettingsState.staticSpringContextOgnl = springContextValue;
            }

        }
        return appSettingsState;
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
