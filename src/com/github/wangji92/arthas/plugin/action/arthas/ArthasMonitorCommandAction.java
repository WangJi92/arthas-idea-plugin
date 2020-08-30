package com.github.wangji92.arthas.plugin.action.arthas;

import com.github.wangji92.arthas.plugin.constants.ArthasCommandConstants;
import com.github.wangji92.arthas.plugin.setting.AppSettingsState;
import com.github.wangji92.arthas.plugin.utils.ClipboardUtils;
import com.github.wangji92.arthas.plugin.utils.NotifyUtils;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;

/**
 * Monitor method execution statistics, e.g. total/success/failure count, average rt, fail rate, etc.
 *
 * @author 汪小哥
 * @date 09-01-2020
 */
public class ArthasMonitorCommandAction extends BaseArthasPluginAction {

    @Override
    public void doCommand(String className, String methodName, Project project, PsiElement psiElement) {
        AppSettingsState instance = AppSettingsState.getInstance(project);
        String invokeMonitorCount = instance.invokeMonitorCount;
        String invokeMonitorInterval = instance.invokeMonitorInterval;
        // 好像官方还不支持 先写上 -v 参数都有了
        String conditionExpressDisplay = instance.conditionExpressDisplay ? ArthasCommandConstants.DEFAULT_CONDITION_EXPRESS : "";
        String printConditionExpress = instance.printConditionExpress ? "-v" : "";
        // 给 arthas 使用者 配置的需求 来源 交流群
        String command = String.join(" ", "monitor", className, methodName,printConditionExpress,"-n", invokeMonitorCount, "--cycle", invokeMonitorInterval,conditionExpressDisplay);
        ClipboardUtils.setClipboardString(command);
        NotifyUtils.notifyMessage(project, "方法执行监控,非实时 -c 统计周期（10秒）-n 执行次数统计(10次) 可以手动修改大一点，详情参看 help monitor");
    }
}
