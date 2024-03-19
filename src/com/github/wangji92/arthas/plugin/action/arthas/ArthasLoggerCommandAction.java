package com.github.wangji92.arthas.plugin.action.arthas;

import com.github.wangji92.arthas.plugin.ui.ArthasLoggerDialog;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;

/**
 * logger --name sample.mybatis.SampleXmlApplication --l warn
 *
 * @author 汪小哥
 * @date 18-04-2020
 */
public class ArthasLoggerCommandAction extends BaseArthasPluginAction {

    @Override
    public void doCommand(String className, String methodName, Project project, PsiElement psiElement, Editor editor) {

        new ArthasLoggerDialog(project, className).open("Print logger info, and update the logger level");
    }
}
