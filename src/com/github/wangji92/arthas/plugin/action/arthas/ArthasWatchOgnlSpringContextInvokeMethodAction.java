package com.github.wangji92.arthas.plugin.action.arthas;

import com.github.wangji92.arthas.plugin.ui.ArthasActionWatchSpringContextDialog;
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
 * @author 汪小哥
 * @date 22-03-2020
 */
public class ArthasWatchOgnlSpringContextInvokeMethodAction  extends AnAction {

    /**
     * watch 获取spring context 进行处理
     */
    private  static final String WATCH_SPRING_CONTEXT="watch -x 3 -n 1  org.springframework.web.servlet.DispatcherServlet doDispatch '@org.springframework.web.context.support.WebApplicationContextUtils@getWebApplicationContext(params[0].getServletContext()).getBean(\"%s\").%s'";

    /**
     * spring aop 获取target
     */
    public static final String  WATCH_SPRING_AOP_TARGET = "watch -x 1 -n 1  org.springframework.web.servlet.DispatcherServlet doDispatch '#beanName=\"%s\",#targetBean=@org.springframework.web.context.support.WebApplicationContextUtils@getWebApplicationContext(params[0].getServletContext()).getBean(#beanName),#isProxy=:[ @org.springframework.aop.support.AopUtils@isAopProxy(#this)?true:false],#isJdkDynamicProxy =:[@org.springframework.aop.support.AopUtils@isJdkDynamicProxy(#this) ? true :false ],#cglibTarget =:[#hField =#this.getClass().getDeclaredField(\"CGLIB$CALLBACK_0\"),#hField.setAccessible(true),#dynamicAdvisedInterceptor=#hField.get(#this),#fieldAdvised=#dynamicAdvisedInterceptor.getClass().getDeclaredField(\"advised\"),#fieldAdvised.setAccessible(true),1==1? #fieldAdvised.get(#dynamicAdvisedInterceptor).getTargetSource().getTarget():null],#jdkTarget=:[ #hField=#this.getClass().getSuperclass().getDeclaredField(\"h\"),#hField.setAccessible(true),#aopProxy=#hField.get(#this),#advisedField=#aopProxy.getClass().getDeclaredField(\"advised\"),#advisedField.setAccessible(true),1==1?#advisedField.get(#aopProxy).getTargetSource().getTarget():null],#nonProxyResultFunc = :[!#isProxy(#this) ? #this :#isJdkDynamicProxy(#this)? #isJdkDynamicProxy(#this) : #cglibTarget(#this)],#nonProxyTarget=#nonProxyResultFunc(#targetBean),#nonProxyTarget'";

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

        if (psiElement instanceof PsiField) {
            PsiField psiField = (PsiField) psiElement;
            if (psiField.hasModifierProperty(PsiModifier.STATIC)) {
                e.getPresentation().setEnabled(false);
                return;
            }
            e.getPresentation().setEnabled(true);
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
//            //抽象方法不处理
//            if (psiMethod.hasModifierProperty(PsiModifier.ABSTRACT)) {
//                e.getPresentation().setEnabled(false);
//                return;
//            }
//            //默认方法不处理
//            if (psiMethod.hasModifierProperty(PsiModifier.DEFAULT)) {
//                e.getPresentation().setEnabled(false);
//                return;
//            }
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
        Editor editor = CommonDataKeys.EDITOR.getData(dataContext);
        Project project = CommonDataKeys.PROJECT.getData(dataContext);
        if (editor == null || project == null) {
            return;
        }
        PsiElement psiElement = CommonDataKeys.PSI_ELEMENT.getData(dataContext);
        String className = "";
        PsiClass psiClass =null;

        if (psiElement instanceof PsiClass) {
            return;
        }

        StringBuilder builder = new StringBuilder("");

        //支持方法
        if (psiElement instanceof PsiMethod) {
            PsiMethod psiMethod = (PsiMethod) psiElement;
            psiClass = psiMethod.getContainingClass();
            String methodName = psiMethod.getNameIdentifier().getText();
            //构建表达式
            builder.append(methodName).append("(");
            //处理参数
            PsiParameter[] parameters = psiMethod.getParameterList().getParameters();
            if (parameters.length > 0) {
                int index = 0;
                for (PsiParameter parameter : parameters) {
                    String defaultParamValue = OgnlPsUtils.getDefaultString(parameter.getType());
                    builder.append(defaultParamValue);
                    if (!(index == parameters.length - 1)) {
                        builder.append(",");
                    }
                    index++;
                }
            }
            builder.append(")");

        }

        //支持field
        if (psiElement instanceof PsiField) {
            PsiField psiField = (PsiField) psiElement;
            String fileName = psiField.getNameIdentifier().getText();
            psiClass = psiField.getContainingClass();
            //构建 field的信息
            builder.append(fileName);
        }

        String lowCamelBeanName = OgnlPsUtils.getClassBeanName(psiClass);
        String watchSpringOgnlExpression = String.format(WATCH_SPRING_CONTEXT, lowCamelBeanName, builder.toString());
        String aopTargetOgnlExpression = String.format(WATCH_SPRING_AOP_TARGET, lowCamelBeanName);
        new ArthasActionWatchSpringContextDialog(project, null, watchSpringOgnlExpression,aopTargetOgnlExpression).open("arthas watch ognl get spring context invoke method field 要触发任意的接口调用");
    }
}
