package com.command.idea.plugin.Action.arthas;

import com.command.idea.plugin.utils.ClipboardUtils;
import com.command.idea.plugin.utils.NotifyUtils;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiField;
import com.intellij.psi.PsiMethod;
import org.jetbrains.annotations.NotNull;

/**
 * @author jet
 * @date 21-12-2019
 */
public abstract class BaseArthasPluginAction extends AnAction {

    @Override
    public void update(@NotNull AnActionEvent e) {
        super.update(e);
        DataContext dataContext = e.getDataContext();
        Editor editor = CommonDataKeys.EDITOR.getData(dataContext);
        boolean enabled = true;
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
            enabled = true;
        } else if (psiElement instanceof PsiMethod) {
            enabled = true;
        } else if (psiElement instanceof PsiField) {
            enabled = true;
        } else {
            enabled = false;
        }

        e.getPresentation().setEnabled(enabled);


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
        String methodName = "";
        if (psiElement instanceof PsiMethod) {
            PsiMethod psiMethod = (PsiMethod) psiElement;
            className = psiMethod.getContainingClass().getQualifiedName();
            methodName = psiMethod.getNameIdentifier().getText();
        }
        if (psiElement instanceof PsiClass) {
            PsiClass psiClass = (PsiClass) psiElement;
            className = psiClass.getQualifiedName();
            methodName = "*";
        }

        if (psiElement instanceof PsiField) {
            PsiField psiField = (PsiField) psiElement;
            className = psiField.getContainingClass().getQualifiedName();
            methodName = "*";
        }
        String command = doBuildCommand(className, methodName);
        ClipboardUtils.setClipboardString(command);

        NotifyUtils.notifyMessageDefault(project);
    }

    /**
     * 构造命令信息
     *
     * @param className
     * @param methodName
     * @return
     */
    public String doBuildCommand(String className, String methodName) {
        return "";
    }

}
