package com.github.wangji92.arthas.plugin.action.terminal;

import com.github.wangji92.arthas.plugin.utils.ArthasTerminalManager;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import org.jetbrains.annotations.NotNull;

/**
 * StopAction
 * @author https://github.com/shuxiongwuziqi
 */
public class StopAction extends AnAction {

    private final ArthasTerminalManager manager;

    public StopAction(ArthasTerminalManager manager) {
        super("Stop", "Stop", AllIcons.Actions.Suspend);
        this.manager = manager;
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        manager.stop();
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        e.getPresentation().setEnabled(manager.isRunning());
    }

}
