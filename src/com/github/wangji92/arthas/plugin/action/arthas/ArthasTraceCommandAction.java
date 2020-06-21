package com.github.wangji92.arthas.plugin.action.arthas;

import com.github.wangji92.arthas.plugin.constants.ArthasCommandConstants;
import com.github.wangji92.arthas.plugin.utils.ClipboardUtils;
import com.github.wangji92.arthas.plugin.utils.NotifyUtils;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;

/**
 * trace 命令
 *
 * @author 汪小哥
 * @date 21-12-2019
 */
public class ArthasTraceCommandAction extends BaseArthasPluginAction {
    @Override
    public void doCommand(String className, String methodName, Project project, PsiElement psiElement) {

        String command = String.join(" ", "trace", className, methodName, "-n", ArthasCommandConstants.INVOKE_COUNT,ArthasCommandConstants.DEFAULT_CONDITION_EXPRESS);

        ClipboardUtils.setClipboardString(command);
        NotifyUtils.notifyMessage(project, "支持ognl条件表达式(默认1==1) eg:'throwExp != null && params[0]==\"name\"' eg:'returnObj instanceof java.lang.String && returnObj.length>5'");

    }
}
