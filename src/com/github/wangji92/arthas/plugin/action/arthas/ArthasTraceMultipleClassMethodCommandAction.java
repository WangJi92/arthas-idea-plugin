package com.github.wangji92.arthas.plugin.action.arthas;

import com.github.wangji92.arthas.plugin.ui.ArthasTraceMultipleCommandDialog;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;

/**
 * trace -E  classA|classB|classC  methodA|methodB|methodC 模式支持
 *
 * @author 汪小哥
 * @date 03-01-2020
 */
public class ArthasTraceMultipleClassMethodCommandAction extends BaseArthasPluginAction {

    @Override
    public void doCommand(String className, String methodName, Project project, PsiElement psiElement) {
        ArthasTraceMultipleCommandDialog instance = new ArthasTraceMultipleCommandDialog(project);
        instance.continueAddTrace(className,methodName);
        instance.showDialog();
    }
}
