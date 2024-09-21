package com.github.idea.arthas.plugin.action.terminal;

import com.github.idea.arthas.plugin.utils.ArthasTerminalManager;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.ActionUpdateThread;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import org.jetbrains.annotations.NotNull;

/**
 * RerunAction
 * @author https://github.com/shuxiongwuziqi
 */
public class RerunAction extends AnAction {

    private final ArthasTerminalManager manager;

    public RerunAction(ArthasTerminalManager manager) {
        super("Rerun", "Rerun", AllIcons.Actions.Restart);
        this.manager = manager;
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        manager.rerun(null);
    }

    @Override
    public @NotNull ActionUpdateThread getActionUpdateThread() {
        return ActionUpdateThread.BGT;
    }
}
