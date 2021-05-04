package com.github.wangji92.arthas.plugin.action.arthas;

import com.github.wangji92.arthas.plugin.ui.ArthasShellScriptCommandDialog;
import com.github.wangji92.arthas.plugin.utils.OgnlPsUtils;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiField;
import com.intellij.psi.PsiMethod;
import org.jetbrains.annotations.NotNull;

/**
 * 直接执行脚本
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
        String className = "";
        String fieldName = "";
        String methodName = "";
        String methodInfo = "";
        if (virtualFileFiles.length == 1 && OgnlPsUtils.isPsiFieldOrMethodOrClass(psiElement)) {
            if (psiElement instanceof PsiMethod) {
                PsiMethod psiMethod = (PsiMethod) psiElement;
                //处理内部类 匿名类获取class的问题
                className = OgnlPsUtils.getCommonOrInnerOrAnonymousClassName(psiMethod);
                // complexParameterCall(#{" ":" "})
                methodInfo = OgnlPsUtils.getMethodParameterDefault(psiMethod);
                methodName = psiMethod.getNameIdentifier().getText();
                if (psiMethod.isConstructor()) {
                    methodName = "<init>";
                }
            }
            if (psiElement instanceof PsiClass) {
                PsiClass psiClass = (PsiClass) psiElement;
                className = OgnlPsUtils.getCommonOrInnerOrAnonymousClassName(psiClass);
                methodName = "*";
            }

            if (psiElement instanceof PsiField) {
                PsiField psiField = (PsiField) psiElement;
                className = OgnlPsUtils.getCommonOrInnerOrAnonymousClassName(psiField);
                fieldName = psiField.getNameIdentifier().getText();
                methodName = "*";
            }
        }
        ArthasShellScriptCommandDialog dialog = new ArthasShellScriptCommandDialog(project, className, fieldName, methodName, methodInfo);
        dialog.open("shell script command");
    }
}
