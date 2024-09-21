package com.github.idea.arthas.plugin.action.terminal.tunnel.action;

import com.github.idea.arthas.plugin.ui.ArthasTerminalOptionsDialog;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.ActionUpdateThread;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

/**
 * ModifyRerunAction
 * @author https://github.com/imyzt
 */
public class ModifyRerunAction extends AnAction {

    private final Project project;
    private final Editor editor;
    private final String cmd;

    public ModifyRerunAction(Project project, Editor editor, String cmd) {
        super("Modify", "ModifyCmd", AllIcons.Actions.Edit);
        this.project = project;
        this.editor = editor;
        this.cmd = cmd;
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        new ArthasTerminalOptionsDialog(project, this.cmd, this.editor).open();
    }

    @Override
    public @NotNull ActionUpdateThread getActionUpdateThread() {
        return ActionUpdateThread.BGT;
    }
}
