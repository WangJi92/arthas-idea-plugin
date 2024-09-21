package com.github.idea.arthas.plugin.utils;

import com.intellij.execution.Executor;
import com.intellij.execution.ExecutorRegistry;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

/**
 * ArthasTerminalExecutor
 *
 * @author https://github.com/shuxiongwuziqi
 */
public class ArthasTerminalExecutor extends Executor {

    public static final String TOOL_WINDOW_ID = "Arthas Terminal";

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
        return Icons.FAVICON;
    }

    @Override
    public Icon getDisabledIcon() {
        return Icons.FAVICON;
    }

    @Override
    public String getDescription() {
        return "Arthas Terminal";
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
