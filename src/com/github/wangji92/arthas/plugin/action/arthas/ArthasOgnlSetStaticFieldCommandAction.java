package com.github.wangji92.arthas.plugin.action.arthas;

import com.github.wangji92.arthas.plugin.constants.ArthasCommandConstants;
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
 * 通过反射获取字段，通过ognl 设置字段的值
 * https://github.com/alibaba/arthas/issues/641
 *
 * @author 汪小哥
 * @date 17-01-2020
 */
public class ArthasOgnlSetStaticFieldCommandAction extends AnAction {
    /**
     * 反射语句 https://github.com/alibaba/arthas/issues/641
     */
    private static final String INVOKE_STATIC_FIELD = "'#field=@%s@class.getDeclaredField(\"%s\"),#field.setAccessible(true),#field.set(null,%s)'";

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
        }
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
            String join = String.join(" ", "ognl", "-x", ArthasCommandConstants.RESULT_X, " ");
            StringBuilder builder = new StringBuilder(join);
            PsiField psiField = (PsiField) psiElement;
            if (!psiField.hasModifierProperty(PsiModifier.STATIC)) {
                return;
            }
            String className = psiField.getContainingClass().getQualifiedName();
            String fileName = psiField.getNameIdentifier().getText();

            //获取字段的默认值
            String defaultFieldValue = OgnlPsUtils.getDefaultString(psiField.getType());

            //#field=@className@class.getDeclaredField("fileName"),#field.setAccessible(true),#field.set(null,'')
            String invokeCommand = String.format(INVOKE_STATIC_FIELD, className, fileName, defaultFieldValue);
            builder.append(invokeCommand);
            new ArthasActionStaticDialog(project, className, builder.toString()).open("arthas ognl set static field");
        }
    }
}
