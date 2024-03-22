package com.github.wangji92.arthas.plugin.gui;

import com.github.wangji92.arthas.plugin.common.constant.Icons;
import com.intellij.execution.Executor;
import com.intellij.execution.ExecutorRegistry;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

/**
 * MyBatisLogExecutor
 * 
 * @author huangxingguang
 */
class ArthasPlusExecutor extends Executor {

    public static final String TOOL_WINDOW_ID = "Arthas Plus";

    @Override
    public @NotNull String getToolWindowId() {
        return TOOL_WINDOW_ID;
    }

    @Override
    public @NotNull Icon getToolWindowIcon() {
        return getIcon();
    }

    @Override
    public @NotNull Icon getIcon() {
        return Icons.MY_BATIS;
    }

    @Override
    public Icon getDisabledIcon() {
        return Icons.MY_BATIS;
    }

    @Override
    public String getDescription() {
        return "Arthas Plus";
    }

    @NotNull
    @Override
    public String getActionName() {
        return getDescription();
    }

    @NotNull
    @Override
    public String getId() {
        return TOOL_WINDOW_ID;
    }

    @NotNull
    @Override
    public String getStartActionText() {
        return getDescription();
    }

    @Override
    public String getContextActionId() {
        return getDescription();
    }

    @Override
    public String getHelpId() {
        return TOOL_WINDOW_ID;
    }

    public static Executor getInstance() {
        return ExecutorRegistry.getInstance().getExecutorById(TOOL_WINDOW_ID);
    }
}