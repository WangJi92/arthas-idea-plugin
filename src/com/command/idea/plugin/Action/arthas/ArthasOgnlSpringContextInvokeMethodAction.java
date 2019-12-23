package com.command.idea.plugin.Action.arthas;

import com.command.idea.plugin.constants.ArthasCommandConstants;
import com.command.idea.plugin.ui.ArthasActionStaticDialog;
import com.command.idea.plugin.utils.NotifyUtils;
import com.command.idea.plugin.utils.OgnlPsUtils;
import com.command.idea.plugin.utils.PropertiesComponentUtils;
import com.command.idea.plugin.utils.StringUtils;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import org.jetbrains.annotations.NotNull;

/**
 * 通过获取静态的的spring context 然后进行获取到Bean的信息进行处理
 * {@literal http://www.dcalabresi.com/blog/java/spring-context-static-class/}
 *
 * @author jet
 * @date 22-12-2019
 */
public class ArthasOgnlSpringContextInvokeMethodAction extends AnAction {


    public void update(@NotNull AnActionEvent e) {
        super.update(e);
        DataContext dataContext = e.getDataContext();
        Editor editor = (Editor) CommonDataKeys.EDITOR.getData(dataContext);
        if (editor == null) {
            e.getPresentation().setEnabled(false);
            return;
        }
        //获取当前事件触发时，光标所在的元素
        PsiElement psiElement = (PsiElement) CommonDataKeys.PSI_ELEMENT.getData(dataContext);
        if (psiElement == null) {
            e.getPresentation().setEnabled(false);
            return;
        }
        if (psiElement instanceof PsiClass) {
            e.getPresentation().setEnabled(false);
            return;
        }
        if (psiElement instanceof PsiField) {
            e.getPresentation().setEnabled(false);
            return;
        }

        //判断是否为静态方法
        if (psiElement instanceof PsiMethod) {
            /**
             * {@link https://www.programcreek.com/java-api-examples/?class=com.intellij.psi.PsiField&method=hasModifierProperty }
             */
            PsiMethod psiMethod = (PsiMethod) psiElement;
            if (psiMethod.hasModifierProperty(PsiModifier.STATIC)) {
                e.getPresentation().setEnabled(false);
                return;
            }
            //抽象方法不处理
            if (psiMethod.hasModifierProperty(PsiModifier.ABSTRACT)) {
                e.getPresentation().setEnabled(false);
                return;
            }
            //默认方法不处理
            if (psiMethod.hasModifierProperty(PsiModifier.DEFAULT)) {
                e.getPresentation().setEnabled(false);
                return;
            }
            //native 方法不处理
            if (psiMethod.hasModifierProperty(PsiModifier.NATIVE)) {
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
        Editor editor = (Editor) CommonDataKeys.EDITOR.getData(dataContext);
        Project project = (Project) CommonDataKeys.PROJECT.getData(dataContext);
        if (editor == null || project == null) {
            return;
        }
        PsiElement psiElement = (PsiElement) CommonDataKeys.PSI_ELEMENT.getData(dataContext);
        String className = "";
        String methodName = "";

        if (psiElement instanceof PsiClass) {
            return;
        }
        String join = String.join(" ", "ognl", " -x ", ArthasCommandConstants.RESULT_X);
        StringBuilder builder = new StringBuilder(join);

        if (psiElement instanceof PsiMethod) {
            PsiMethod psiMethod = (PsiMethod) psiElement;
            className = psiMethod.getContainingClass().getQualifiedName();
           // String lowCamelBeanName = StringUtils.toLowerFristChar(className);

            String lowCamelBeanName =OgnlPsUtils.getClassBeanName(psiMethod.getContainingClass());
            methodName = psiMethod.getName();

            //这里获取spring context的信息
            String springContextValue = PropertiesComponentUtils.getValue(ArthasCommandConstants.SPRING_CONTEXT_STATIC_OGNL_EXPRESSION);
            if (StringUtils.isBlank(springContextValue)) {
                NotifyUtils.notifyMessage(project, "配置 arthas 插件spring context 获取的信息", NotificationType.ERROR);
                return;
            }
            if (!springContextValue.endsWith(",")) {
                springContextValue = springContextValue + ",";
            }

            //构建表达式
            builder.append("  '").append(springContextValue).append(ArthasCommandConstants.SPRING_CONTEXT_PARAM).append(".getBean(")
                    .append("\"")
                    .append(lowCamelBeanName)
                    .append("\"")
                    .append(").").append(methodName).append("(");

            //处理参数
            PsiParameter[] parameters = psiMethod.getParameterList().getParameters();
            if (parameters.length > 0) {
                int index = 0;
                for (PsiParameter parameter : parameters) {
                    String defaulParamValue = OgnlPsUtils.getDefaultString(parameter.getType());
                    builder.append(defaulParamValue);
                    if (!(index == parameters.length - 1)) {
                        builder.append(",");
                    }
                    index++;
                }
            }
            builder.append(")").append("'");

        }
        new ArthasActionStaticDialog(project, className, builder.toString()).open("arthas ognl invoke spring bean method");
    }


}
