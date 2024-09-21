package com.github.idea.arthas.plugin.action.arthas;

import com.github.idea.arthas.plugin.utils.ClipboardUtils;
import com.github.idea.arthas.plugin.utils.NotifyUtils;
import com.intellij.openapi.actionSystem.ActionUpdateThread;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;

/**
 * classloader 使用
 * <p>
 * classloader -t  继承tree
 * classloader -l  按类加载实例查看统计信息
 * classloader -c 327a647b  查看URLClassLoader实际的urls
 * classloader -c 327a647b -r java/lang/String.class  使用ClassLoader去查找资源
 * classloader -a 所有加载的类的信息
 * classloader -a -c 327a647b 当前classloader 加载类的信息
 * classloader -c 659e0bfd --load demo.MathGame  使用这个classloader 去加载类
 *
 * @author 汪小哥
 * @date 20-06-2020
 */
public class ArthasClassloaderCommandAction extends BaseArthasPluginAction {

    @Override
    public void doCommand(String className, String methodName, Project project, PsiElement psiElement, Editor editor) {
        String command = "classloader -l";
        ClipboardUtils.setClipboardString(command);
        NotifyUtils.notifyMessageOpenTerminal(project, NotifyUtils.COMMAND_COPIED, command, editor);
    }

    @Override
    public @NotNull ActionUpdateThread getActionUpdateThread() {
        return ActionUpdateThread.EDT;
    }
}
