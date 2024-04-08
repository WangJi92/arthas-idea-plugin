package com.github.wangji92.arthas.plugin.action.arthas;

import com.github.wangji92.arthas.plugin.setting.AppSettingsState;
import com.github.wangji92.arthas.plugin.utils.ClipboardUtils;
import com.github.wangji92.arthas.plugin.utils.NotifyUtils;
import com.github.wangji92.arthas.plugin.utils.OgnlPsUtils;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiField;
import com.intellij.psi.PsiModifier;
import org.jetbrains.annotations.NotNull;

/**
 * https://arthas.aliyun.com/doc/getstatic.html 简单的版本的获取静态变量
 *
 * @author 汪小哥
 * @date 01-08-2020
 */
public class ArthasSimpleGetStaticCommandAction extends AnAction {

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
        if (psiElement instanceof PsiField) {
            PsiField psiField = (PsiField) psiElement;
            if (!psiField.hasModifierProperty(PsiModifier.STATIC)) {
                e.getPresentation().setEnabled(false);
                return;
            } else {
                //只支持static field
                e.getPresentation().setEnabled(true);
            }
            return;
        }

        e.getPresentation().setEnabled(false);
    }

    @Override
    public @NotNull ActionUpdateThread getActionUpdateThread() {
        return ActionUpdateThread.BGT;
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent event) {
        DataContext dataContext = event.getDataContext();
        Editor editor = CommonDataKeys.EDITOR.getData(dataContext);
        Project project = CommonDataKeys.PROJECT.getData(dataContext);
        if (editor == null || project == null) {
            return;
        }
        PsiElement psiElement = CommonDataKeys.PSI_ELEMENT.getData(dataContext);
        if (psiElement instanceof PsiField) {
            PsiField psiField = (PsiField) psiElement;
            if (!psiField.hasModifierProperty(PsiModifier.STATIC)) {
                return;
            }
            // 处理内部类问题
            String className = OgnlPsUtils.getCommonOrInnerOrAnonymousClassName(psiField);
            String fileName = psiField.getNameIdentifier().getText();
            AppSettingsState instance = AppSettingsState.getInstance(project);

            String depthPrintProperty = instance.depthPrintProperty;
            String command = String.join(" ", "getstatic", className, fileName, "-x", depthPrintProperty);
            ClipboardUtils.setClipboardString(command);
            NotifyUtils.notifyMessage(project, NotifyUtils.COMMAND_COPIED + "(Simply get the value of static variables, if multiple classloader's can't get the information)");
        }
    }
}
