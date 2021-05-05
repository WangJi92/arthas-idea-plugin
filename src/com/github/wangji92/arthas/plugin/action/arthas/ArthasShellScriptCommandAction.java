package com.github.wangji92.arthas.plugin.action.arthas;

import com.github.wangji92.arthas.plugin.common.param.ScriptParam;
import com.github.wangji92.arthas.plugin.ui.ArthasShellScriptCommandDialog;
import com.github.wangji92.arthas.plugin.utils.OgnlPsUtils;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import org.jetbrains.annotations.NotNull;

/**
 * 直接执行脚本
 *
 * @author 汪小哥
 * @date 04-05-2021
 */
public class ArthasShellScriptCommandAction extends AnAction {
    @Override
    public void update(@NotNull AnActionEvent e) {
        super.update(e);
        DataContext dataContext = e.getDataContext();
        //获取当前事件触发时，光标所在的元素
        Project project = CommonDataKeys.PROJECT.getData(dataContext);
        if (project == null) {
            e.getPresentation().setEnabled(false);
            return;
        }
        VirtualFile[] virtualFileFiles = e.getData(CommonDataKeys.VIRTUAL_FILE_ARRAY);
        if (virtualFileFiles == null) {
            e.getPresentation().setEnabled(false);
            return;
        }
        if (virtualFileFiles.length >= 2) {
            e.getPresentation().setEnabled(false);
        }
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent event) {
        DataContext dataContext = event.getDataContext();
        Project project = CommonDataKeys.PROJECT.getData(dataContext);
        if (project == null) {
            return;
        }
        VirtualFile[] virtualFileFiles = CommonDataKeys.VIRTUAL_FILE_ARRAY.getData(dataContext);
        assert virtualFileFiles != null;
        PsiElement psiElement = CommonDataKeys.PSI_ELEMENT.getData(dataContext);
        ScriptParam scriptParam = new ScriptParam();
        scriptParam.setProject(project);
        if (virtualFileFiles.length == 1 && OgnlPsUtils.isPsiFieldOrMethodOrClass(psiElement)) {
            String className = OgnlPsUtils.getCommonOrInnerOrAnonymousClassName(psiElement);
            scriptParam.setClassName(className);
            String executeInfo = OgnlPsUtils.getExecuteInfo(psiElement);
            String methodName = OgnlPsUtils.getMethodName(psiElement);
            scriptParam.setMethodName(methodName);
            scriptParam.setExecuteInfo(executeInfo);
            if (psiElement instanceof PsiMethod) {
                PsiMethod psiMethod = (PsiMethod) psiElement;
                if (psiMethod.hasModifierProperty(PsiModifier.STATIC)) {
                    scriptParam.setModifierStatic(true);
                }
                if (!(psiMethod.getContainingClass() instanceof PsiAnonymousClass)) {
                    String lowCamelBeanName = OgnlPsUtils.getClassBeanName(psiMethod.getContainingClass());
                    scriptParam.setBeanName(lowCamelBeanName);
                } else {
                    scriptParam.setAnonymousClass(true);
                }
            }
            if (psiElement instanceof PsiField) {
                PsiField psiField = (PsiField) psiElement;
                String fieldName = psiField.getNameIdentifier().getText();
                scriptParam.setFieldName(fieldName);
                if (psiField.hasModifierProperty(PsiModifier.STATIC)) {
                    scriptParam.setModifierStatic(true);
                }
                if (!(psiField.getContainingClass() instanceof PsiAnonymousClass)) {
                    String lowCamelBeanName = OgnlPsUtils.getClassBeanName(psiField.getContainingClass());
                    scriptParam.setBeanName(lowCamelBeanName);
                } else {
                    scriptParam.setAnonymousClass(true);
                }
            }
        }


        ArthasShellScriptCommandDialog dialog = new ArthasShellScriptCommandDialog(scriptParam);
        dialog.open("shell script command");
    }
}
