package com.github.wangji92.arthas.plugin.action.arthas;

import com.github.wangji92.arthas.plugin.constants.ArthasCommandConstants;
import com.github.wangji92.arthas.plugin.utils.ClipboardUtils;
import com.github.wangji92.arthas.plugin.utils.NotifyUtils;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;

/**
 * trace 命令  https://arthas.gitee.io/trace.html 默认打开 不跳过JDK的方法
 *
 * @author 汪小哥
 * @date 21-12-2019
 */
public class ArthasTraceCommandAction extends BaseArthasPluginAction {
    @Override
    public void doCommand(String className, String methodName, Project project, PsiElement psiElement) {

        String command = String.join(" ", "trace", className, methodName, "-n", ArthasCommandConstants.INVOKE_COUNT, ArthasCommandConstants.DEFAULT_CONDITION_EXPRESS, ArthasCommandConstants.DEFAULT_SKIP_JDK);

        ClipboardUtils.setClipboardString(command);
        NotifyUtils.notifyMessage(project, "支持ognl条件表达式(默认1==1) 更多搜索 [arthas 入门最佳实践] --skipJDKMethod 不跳过JDK 函数");

    }
}
