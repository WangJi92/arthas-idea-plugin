package com.github.wangji92.arthas.plugin.setting;


import com.github.wangji92.arthas.plugin.web.entity.AgentServerInfo;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.util.xmlb.XmlSerializerUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * @author wuziqi
 * @date 2024/02/20
 * @description 描述信息
 */
@State(
        name = "arthas.idea.plugin.global",
        storages = {@Storage("arthas-plus-setting.xml")}
)
public class ApplicationSettingsState implements PersistentStateComponent<ApplicationSettingsState> {
    @Override
    public @Nullable ApplicationSettingsState getState() {
        return this;
    }

    @Override
    public void loadState(@NotNull ApplicationSettingsState state) {
        XmlSerializerUtil.copyBean(state, this);
    }


    public Boolean enableForegroundColor;
    public Integer foregroundColor;
    public Boolean enableBackgroundColor;
    public Integer backgroundColor;
    public Boolean enableEffectColor;
    public Integer effectColor;
    public String effectStyle;

    public List<AgentServerInfo> agentServerInfoList = new ArrayList<>();

    public static ApplicationSettingsState getInstance() {
        return ServiceManager.getService(ApplicationSettingsState.class);
    }
}
