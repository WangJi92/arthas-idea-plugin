package com.github.wangji92.arthas.plugin.action.arthas;

import com.github.wangji92.arthas.plugin.constants.ArthasCommandConstants;
import com.github.wangji92.arthas.plugin.setting.AppSettingsState;
import com.github.wangji92.arthas.plugin.utils.ClipboardUtils;
import com.github.wangji92.arthas.plugin.utils.NotifyUtils;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;

/**
 * trace 命令  https://arthas.aliyun.com/doc/trace.html 默认打开 不跳过JDK的方法
 *
 * @author 汪小哥
 * @date 21-12-2019
 */
public class ArthasTraceCommandAction extends BaseArthasPluginAction {
    @Override
    public void doCommand(String className, String methodName, Project project, PsiElement psiElement) {
        AppSettingsState instance = AppSettingsState.getInstance(project);
        String invokeCount = instance.invokeCount;
        boolean skipJdkMethod = instance.traceSkipJdk;
        String skpJdkMethodCommand = skipJdkMethod ? "" : ArthasCommandConstants.DEFAULT_SKIP_JDK_FALSE;
        String conditionExpressDisplay = instance.conditionExpressDisplay ? ArthasCommandConstants.DEFAULT_CONDITION_EXPRESS : "";
        String command = String.join(" ", "trace", className, methodName, "-n", invokeCount, conditionExpressDisplay, skpJdkMethodCommand);
        ClipboardUtils.setClipboardString(command);
        NotifyUtils.notifyMessage(project, "支持ognl条件表达式(默认1==1) 更多搜索 [arthas 入门最佳实践] --skipJDKMethod 不跳过JDK 函数");

    }
}
