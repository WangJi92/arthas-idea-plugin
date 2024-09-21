package com.github.idea.arthas.plugin.action.arthas;

import com.github.idea.arthas.plugin.utils.ClipboardUtils;
import com.github.idea.arthas.plugin.utils.NotifyUtils;
import com.intellij.openapi.actionSystem.ActionUpdateThread;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;

/**
 * 查看，更新VM诊断相关的参数
 *
 * @author 汪小哥
 * @date 20-06-2020
 */
public class ArthasVmOptionCommandAction extends BaseArthasPluginAction {

    private static final String MESSAGE = NotifyUtils.COMMAND_COPIED + "(View and update VM diagnosis related parameters such as vmoption PrintGCDetails true)";

    @Override
    public void doCommand(String className, String methodName, Project project, PsiElement psiElement, Editor editor) {
        String command = "vmoption";
        ClipboardUtils.setClipboardString(command);
        NotifyUtils.notifyMessageOpenTerminal(project, MESSAGE, command, editor);
    }

    @Override
    public @NotNull ActionUpdateThread getActionUpdateThread() {
        return ActionUpdateThread.EDT;
    }
}
