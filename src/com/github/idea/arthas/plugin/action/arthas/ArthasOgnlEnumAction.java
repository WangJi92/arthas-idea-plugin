package com.github.idea.arthas.plugin.action.arthas;

import com.github.idea.arthas.plugin.ui.ArthasOgnlEnumActionDialog;
import com.github.idea.arthas.plugin.ui.ArthasOgnlEnumActionDialog.OgnlEnumCommandRequest;
import com.github.idea.arthas.plugin.utils.OgnlPsUtils;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.psi.impl.source.PsiEnumConstantImpl;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.util.Arrays;

/**
 * 为了枚举特殊订正
 *
 * @author wangji
 * @date 2021/12/4 2:58 下午
 */
public class ArthasOgnlEnumAction extends AnAction {

    @Override
    public void update(@NotNull AnActionEvent e) {
        super.update(e);
        DataContext dataContext = e.getDataContext();
        Project project = CommonDataKeys.PROJECT.getData(dataContext);
        if (project == null) {
            e.getPresentation().setEnabled(false);
            return;
        }
        PsiElement psiElement = CommonDataKeys.PSI_ELEMENT.getData(dataContext);
        if (!OgnlPsUtils.isPsiFieldOrMethodOrClass(psiElement)) {
            e.getPresentation().setEnabled(false);
            return;
        }
        if (OgnlPsUtils.psiElementInEnum(psiElement)) {
            e.getPresentation().setEnabled(true);
            return;
        }
        e.getPresentation().setEnabled(false);
    }

    @Override
    public @NotNull ActionUpdateThread getActionUpdateThread() {
        return ActionUpdateThread.BGT;
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        DataContext dataContext = e.getDataContext();
        PsiClass parentEnumClazz = null;
        String selectKey = "";
        PsiElement currentPsiElement = CommonDataKeys.PSI_ELEMENT.getData(dataContext);
        if (currentPsiElement instanceof PsiClass) {
            parentEnumClazz = (PsiClass) currentPsiElement;
        } else if (currentPsiElement instanceof PsiEnumConstant) {
            //枚举常量字段
            parentEnumClazz = ((PsiEnumConstant) currentPsiElement).getContainingClass();
            selectKey = String.format("@%s@%s", OgnlPsUtils.getCommonOrInnerOrAnonymousClassName(parentEnumClazz), OgnlPsUtils.getFieldName(currentPsiElement));

        } else if (currentPsiElement instanceof PsiMethod) {
            if (((PsiMethod) currentPsiElement).getContainingClass() != null && ((PsiMethod) currentPsiElement).getContainingClass().isEnum()
                    && OgnlPsUtils.isNonStaticMethod(currentPsiElement)) {
                // 枚举里面的方法 非匿名方法
                parentEnumClazz = ((PsiMethod) currentPsiElement).getContainingClass();
                final PsiField defaultPsiField = Arrays.stream(parentEnumClazz.getAllFields()).filter(psiField -> psiField instanceof PsiEnumConstant).findFirst().orElse(null);
                selectKey = String.format("@%s@%s.%s", OgnlPsUtils.getCommonOrInnerOrAnonymousClassName(parentEnumClazz), OgnlPsUtils.getFieldName(defaultPsiField), OgnlPsUtils.getExecuteInfo(currentPsiElement));

            } else if (((PsiMethod) currentPsiElement).getParent() instanceof PsiEnumConstantInitializer) {
                //枚举里面的匿名常量 常量的匿名方法
                parentEnumClazz = ((PsiEnumConstantImpl) currentPsiElement.getParent().getParent()).getContainingClass();
                final PsiElement parent = ((PsiMethod) currentPsiElement).getParent();
                selectKey = String.format("@%s@%s.%s", OgnlPsUtils.getCommonOrInnerOrAnonymousClassName(parentEnumClazz), OgnlPsUtils.getFieldName(parent.getParent()), OgnlPsUtils.getExecuteInfo(currentPsiElement));
            } else {
                //静态方法...
                parentEnumClazz = ((PsiMethod) currentPsiElement).getContainingClass();
                selectKey = String.format("@%s@%s", OgnlPsUtils.getCommonOrInnerOrAnonymousClassName(parentEnumClazz), OgnlPsUtils.getExecuteInfo(currentPsiElement));

            }
        } else if (currentPsiElement instanceof PsiField) {
            PsiField psiField = (PsiField) currentPsiElement;
            parentEnumClazz = ((PsiField) currentPsiElement).getContainingClass();
            //是否为 静态的字段 枚举
            if (OgnlPsUtils.isStaticField(psiField) && !(psiField instanceof PsiEnumConstant)) {
                selectKey = String.format("@%s@%s", OgnlPsUtils.getCommonOrInnerOrAnonymousClassName(parentEnumClazz), OgnlPsUtils.getExecuteInfo(currentPsiElement));
            } else {
                final PsiField defaultPsiField = Arrays.stream(parentEnumClazz.getAllFields()).filter(psiField1 -> psiField1 instanceof PsiEnumConstant).findFirst().orElse(null);
                selectKey = String.format("@%s@%s.%s", OgnlPsUtils.getCommonOrInnerOrAnonymousClassName(parentEnumClazz), OgnlPsUtils.getFieldName(defaultPsiField), OgnlPsUtils.getExecuteInfo(currentPsiElement));
            }

        }

        OgnlEnumCommandRequest request = new OgnlEnumCommandRequest();
        request.setProject(e.getProject());
        request.setParentEnumClazz(parentEnumClazz);
        request.setSelectKey(selectKey);

        SwingUtilities.invokeLater(() -> {
            ArthasOgnlEnumActionDialog dialog = new ArthasOgnlEnumActionDialog(request);
            dialog.open("ognl invoke for enum class，you can edit method params ");
        });

    }
}
