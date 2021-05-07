package com.github.wangji92.arthas.plugin.common.param;

import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;

/**
 * 通用脚本构造参数
 *
 * @author 汪小哥
 * @date 05-05-2021
 */
public class ScriptParam {

    /**
     * 工程名称
     */
    private Project project;

    /**
     * 当前选中的元素的信息
     */
    private PsiElement psiElement;

    public Project getProject() {
        return project;
    }

    public void setProject(Project project) {
        this.project = project;
    }

    public PsiElement getPsiElement() {
        return psiElement;
    }

    public void setPsiElement(PsiElement psiElement) {
        this.psiElement = psiElement;
    }
}
