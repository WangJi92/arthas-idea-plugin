package com.github.idea.arthas.plugin.action.arthas;

import com.github.idea.arthas.plugin.common.command.CommandContext;
import com.github.idea.arthas.plugin.utils.ClipboardUtils;
import com.github.idea.arthas.plugin.utils.NotifyUtils;
import com.github.idea.arthas.plugin.common.enums.ShellScriptCommandEnum;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;

/**
 * trace 命令  https://arthas.aliyun.com/doc/trace.html 默认打开 不跳过JDK的方法
 *
 * @author 汪小哥
 * @date 21-12-2019
 */
public class ArthasTraceCommandAction extends BaseArthasPluginAction {

    public ArthasTraceCommandAction() {
        this.setSupportEnum(true);
    }

    @Override
    public void doCommand(String className, String methodName, Project project, PsiElement psiElement, Editor editor) {
        CommandContext commandContext = new CommandContext(project, psiElement);
        String command = ShellScriptCommandEnum.TRACE.getArthasCommand(commandContext);
        ClipboardUtils.setClipboardString(command);
        NotifyUtils.notifyMessageOpenTerminal(project, NotifyUtils.COMMAND_COPIED, command, editor);

    }
}
