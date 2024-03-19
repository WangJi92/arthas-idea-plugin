package com.github.wangji92.arthas.plugin.action.arthas;

import com.github.wangji92.arthas.plugin.gui.MyBatisLogManager;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import org.jetbrains.annotations.NotNull;

/**
 * StopAction
 * @author huangxingguang
 */
public class StopAction extends AnAction {

    private final MyBatisLogManager manager;

    public StopAction(MyBatisLogManager manager) {
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