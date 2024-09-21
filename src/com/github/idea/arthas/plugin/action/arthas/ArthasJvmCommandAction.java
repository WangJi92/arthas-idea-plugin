package com.github.idea.arthas.plugin.action.arthas;

import com.github.idea.arthas.plugin.utils.ClipboardUtils;
import com.github.idea.arthas.plugin.utils.NotifyUtils;
import com.intellij.openapi.actionSystem.ActionUpdateThread;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;

/**
 *[jvm 命令](https://arthas.aliyun.com/doc/jvm.html)
 * @author 汪小哥
 * @date 20-06-2020
 */
public class ArthasJvmCommandAction extends BaseArthasPluginAction {

    @Override
    public void doCommand(String className, String methodName, Project project, PsiElement psiElement, Editor editor) {
        String command = "jvm";
        ClipboardUtils.setClipboardString(command);
        NotifyUtils.notifyMessageOpenTerminal(project, NotifyUtils.COMMAND_COPIED, command, editor);
    }

    @Override
    public @NotNull ActionUpdateThread getActionUpdateThread() {
        return ActionUpdateThread.EDT;
    }
}
