package com.github.wangji92.arthas.plugin.action.arthas;

import com.github.wangji92.arthas.plugin.ui.ArthasActionDumpDialog;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;

/**
 * Dump class byte array from JVM
 * https://arthas.aliyun.com/doc/dump
 * <p>
 * dump java.lang.String
 * dump -d /tmp/output java.lang.String
 * dump org/apache/commons/lang/StringUtils
 * dump *StringUtils
 * dump -E org\\.apache\\.commons\\.lang\\.StringUtils
 *
 * @author 汪小哥
 * @date 17-03-2020
 */
public class ArthasDumpCommandAction extends BaseArthasPluginAction {

    @Override
    public void doCommand(String className, String methodName, Project project, PsiElement psiElement, Editor editor) {
        //增强，不用一定要选择在class 上面，只要是可以确定这个class 即可
        String join = String.join(" ", "dump", className, "-d /tmp/output");
        new ArthasActionDumpDialog(project, className, join).open("Dump Class Byte Array from JVM");
    }
}
