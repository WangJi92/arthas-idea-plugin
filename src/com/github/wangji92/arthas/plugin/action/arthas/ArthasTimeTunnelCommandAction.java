package com.github.wangji92.arthas.plugin.action.arthas;

import com.github.wangji92.arthas.plugin.constants.ArthasCommandConstants;
import com.github.wangji92.arthas.plugin.setting.AppSettingsState;
import com.github.wangji92.arthas.plugin.ui.ArthasTimeTunnelDialog;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;

/**
 * 方法执行数据的时空隧道，记录下指定方法每次调用的入参和返回信息，并能对这些不同的时间下调用进行观测
 *
 * @author 汪小哥
 * @date 11-01-2020
 */
public class ArthasTimeTunnelCommandAction extends BaseArthasPluginAction {
    public ArthasTimeTunnelCommandAction() {
        this.setSupportEnum(true);
    }

    @Override
    public void doCommand(String className, String methodName, Project project, PsiElement psiElement, Editor editor) {
        AppSettingsState instance = AppSettingsState.getInstance(project);
        String invokeCount = instance.invokeCount;
        String conditionExpressDisplay = instance.conditionExpressDisplay ? ArthasCommandConstants.DEFAULT_CONDITION_EXPRESS : "";
        String command = String.join(" ", "tt -t", className, methodName, "-n", invokeCount, conditionExpressDisplay);
        new ArthasTimeTunnelDialog(project, command).open("arthas time tunnel use");
    }
}
