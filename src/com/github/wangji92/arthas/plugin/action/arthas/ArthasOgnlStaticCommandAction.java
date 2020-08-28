package com.github.wangji92.arthas.plugin.action.arthas;

import com.github.wangji92.arthas.plugin.setting.AppSettingsState;
import com.github.wangji92.arthas.plugin.ui.ArthasActionStaticDialog;
import com.github.wangji92.arthas.plugin.utils.OgnlPsUtils;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import org.jetbrains.annotations.NotNull;

/**
 * static 方法处理
 *
 * @author 汪小哥
 * @date 21-12-2019
 */
public class ArthasOgnlStaticCommandAction extends AnAction {

    @Override
    public void update(@NotNull AnActionEvent e) {
        super.update(e);
        DataContext dataContext = e.getDataContext();
        Editor editor = CommonDataKeys.EDITOR.getData(dataContext);
        if (editor == null) {
            e.getPresentation().setEnabled(false);
            return;
        }
        //获取当前事件触发时，光标所在的元素
        PsiElement psiElement = CommonDataKeys.PSI_ELEMENT.getData(dataContext);
        if (psiElement == null) {
            e.getPresentation().setEnabled(false);
            return;
        }
        if (psiElement instanceof PsiClass) {
            e.getPresentation().setEnabled(false);
            return;
        }

        //判断是否为静态方法
        if (psiElement instanceof PsiMethod) {
            /**
             * {@link https://www.programcreek.com/java-api-examples/?class=com.intellij.psi.PsiField&method=hasModifierProperty }
             */
            PsiMethod psiMethod = (PsiMethod) psiElement;
            if (!psiMethod.hasModifierProperty(PsiModifier.STATIC)) {
                e.getPresentation().setEnabled(false);
                return;
            }
        }

        if (psiElement instanceof PsiField) {
            PsiField psiField = (PsiField) psiElement;
            if (!psiField.hasModifierProperty(PsiModifier.STATIC)) {
                e.getPresentation().setEnabled(false);
                return;
            }
        }

        e.getPresentation().setEnabled(true);
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent event) {
        /**
         * {@link com.intellij.ide.actions.CopyReferenceAction}
         */
        DataContext dataContext = event.getDataContext();
        Editor editor = CommonDataKeys.EDITOR.getData(dataContext);
        Project project = CommonDataKeys.PROJECT.getData(dataContext);
        if (editor == null || project == null) {
            return;
        }
        PsiElement psiElement = CommonDataKeys.PSI_ELEMENT.getData(dataContext);
        String className = "";

        if (psiElement instanceof PsiClass) {
            return;
        }

        AppSettingsState instance = AppSettingsState.getInstance(project);
        String depthPrintProperty = instance.depthPrintProperty;

        String join = String.join(" ", "ognl", " -x ", depthPrintProperty);
        StringBuilder builder = new StringBuilder(join);

        if (psiElement instanceof PsiMethod) {
            PsiMethod psiMethod = (PsiMethod) psiElement;
            className = OgnlPsUtils.getCommonOrInnerOrAnonymousClassName(psiMethod);
            builder.append(" '").append("@").append(className).append("@");

            String methodParameterDefault = OgnlPsUtils.getMethodParameterDefault(psiMethod);
            builder.append(methodParameterDefault).append("'");

        }

        if (psiElement instanceof PsiField) {
            PsiField psiField = (PsiField) psiElement;
            if (!psiField.hasModifierProperty(PsiModifier.STATIC)) {
                return;
            }
            className = OgnlPsUtils.getCommonOrInnerOrAnonymousClassName(psiField);
            String fileName = psiField.getNameIdentifier().getText();
            builder.append(" '").append("@").append(className).append("@").append(fileName).append("'");
        }
        new ArthasActionStaticDialog(project, className, builder.toString(), "").open("Ognl To Get Static Method Field");
    }

}
