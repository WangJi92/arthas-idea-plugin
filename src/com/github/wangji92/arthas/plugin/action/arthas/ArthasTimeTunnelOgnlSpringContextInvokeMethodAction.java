package com.github.wangji92.arthas.plugin.action.arthas;

import com.github.wangji92.arthas.plugin.ui.ArthasTimeTunnelSpringContextDialog;
import com.github.wangji92.arthas.plugin.utils.OgnlPsUtils;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiField;
import com.intellij.psi.PsiMethod;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

/**
 * tt 处理获取spring context 进行调用
 * https://github.com/WangJi92/arthas-idea-plugin/issues/4
 * https://github.com/alibaba/arthas/issues/482
 *
 * @author 汪小哥
 * @date 22-03-2020
 */
public class ArthasTimeTunnelOgnlSpringContextInvokeMethodAction extends AnAction {

    /**
     * tt 获取spring context 进行处理
     */
    private static final String TT_SPRING_CONTEXT = "tt -w 'target.getApplicationContext().getBean(\"%s\").%s'";

    /**
     * spring aop 获取target
     */
    public static final String TT_SPRING_AOP_TARGET = "tt -w '#beanName=\"%s\",#targetBean=target.getApplicationContext().getBean(#beanName),#isProxy=:[ @org.springframework.aop.support.AopUtils@isAopProxy(#this)?true:false],#isJdkDynamicProxy =:[@org.springframework.aop.support.AopUtils@isJdkDynamicProxy(#this) ? true :false ],#cglibTarget =:[#hField =#this.getClass().getDeclaredField(\"CGLIB$CALLBACK_0\"),#hField.setAccessible(true),#dynamicAdvisedInterceptor=#hField.get(#this),#fieldAdvised=#dynamicAdvisedInterceptor.getClass().getDeclaredField(\"advised\"),#fieldAdvised.setAccessible(true),1==1? #fieldAdvised.get(#dynamicAdvisedInterceptor).getTargetSource().getTarget():null],#jdkTarget=:[ #hField=#this.getClass().getSuperclass().getDeclaredField(\"h\"),#hField.setAccessible(true),#aopProxy=#hField.get(#this),#advisedField=#aopProxy.getClass().getDeclaredField(\"advised\"),#advisedField.setAccessible(true),1==1?#advisedField.get(#aopProxy).getTargetSource().getTarget():null],#nonProxyResultFunc = :[!#isProxy(#this) ? #this :#isJdkDynamicProxy(#this)? #isJdkDynamicProxy(#this) : #cglibTarget(#this)],#nonProxyTarget=#nonProxyResultFunc(#targetBean),#nonProxyTarget'";

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
        if (!OgnlPsUtils.isPsiFieldOrMethodOrClass(psiElement)) {
            e.getPresentation().setEnabled(false);
            return;
        }
        boolean anonymousClass = OgnlPsUtils.isAnonymousClass(psiElement);
        if (anonymousClass) {
            e.getPresentation().setEnabled(false);
            return;
        }
        // 构造方法不支持
        if (OgnlPsUtils.isConstructor(psiElement)) {
            e.getPresentation().setEnabled(false);
            return;
        }
        if (OgnlPsUtils.psiElementInEnum(psiElement)) {
            e.getPresentation().setEnabled(false);
            return;
        }
        if (OgnlPsUtils.isNonStaticMethodOrField(psiElement)) {
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
        PsiClass psiClass = null;

        if (psiElement instanceof PsiClass) {
            return;
        }

        StringBuilder builder = new StringBuilder("");

        //支持方法
        if (psiElement instanceof PsiMethod) {
            PsiMethod psiMethod = (PsiMethod) psiElement;
            psiClass = psiMethod.getContainingClass();
            className = psiMethod.getContainingClass().getQualifiedName();
            //构建表达式
            String methodParameterDefault = OgnlPsUtils.getMethodParameterDefault(psiMethod);
            builder.append(methodParameterDefault);

        }

        //支持field
        if (psiElement instanceof PsiField) {
            PsiField psiField = (PsiField) psiElement;
            className = psiField.getContainingClass().getQualifiedName();
            String fileName = psiField.getNameIdentifier().getText();
            psiClass = psiField.getContainingClass();
            //构建 field的信息
            builder.append(fileName);
        }

        String lowCamelBeanName = OgnlPsUtils.getClassBeanName(psiClass);
        String watchSpringOgnlExpression = String.format(TT_SPRING_CONTEXT, lowCamelBeanName, builder.toString());
        //这里不需要方法
        String aopTargetOgnlExpression = String.format(TT_SPRING_AOP_TARGET, lowCamelBeanName);
        final String finalClassName = className;
        SwingUtilities.invokeLater(() -> {
            new ArthasTimeTunnelSpringContextDialog(project, finalClassName, watchSpringOgnlExpression, aopTargetOgnlExpression).open("time tunnel ognl get spring context invoke method field");
        });
    }
}
