package com.github.wangji92.arthas.plugin.action.arthas;

import com.github.wangji92.arthas.plugin.constants.ArthasCommandConstants;
import com.github.wangji92.arthas.plugin.ui.ArthasActionStaticDialog;
import com.github.wangji92.arthas.plugin.utils.NotifyUtils;
import com.github.wangji92.arthas.plugin.utils.OgnlPsUtils;
import com.github.wangji92.arthas.plugin.utils.SpringStaticContextUtils;
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
 * 通过ognl 调用获取spring context 然后调用方法、field处理
 * 通过获取静态的的spring context 然后进行获取到Bean的信息进行处理
 * {@literal http://www.dcalabresi.com/blog/java/spring-context-static-class/}
 *
 * @author 汪小哥
 * @date 22-12-2019
 */
public class ArthasOgnlSpringContextInvokeMethodAction extends AnAction {

    /**
     * spring aop 获取target  %s = #springContext=填充,#targetBean=#springContext.getBean("%s")
     */
    public static final String STATIC_SPRING_AOP_TARGET = "ognl '%s,#isProxy=:[ @org.springframework.aop.support.AopUtils@isAopProxy(#this)?true:false],#isJdkDynamicProxy =:[@org.springframework.aop.support.AopUtils@isJdkDynamicProxy(#this) ? true :false ],#cglibTarget =:[#hField =#this.getClass().getDeclaredField(\"CGLIB$CALLBACK_0\"),#hField.setAccessible(true),#dynamicAdvisedInterceptor=#hField.get(#this),#fieldAdvised=#dynamicAdvisedInterceptor.getClass().getDeclaredField(\"advised\"),#fieldAdvised.setAccessible(true),1==1? #fieldAdvised.get(#dynamicAdvisedInterceptor).getTargetSource().getTarget():null],#jdkTarget=:[ #hField=#this.getClass().getSuperclass().getDeclaredField(\"h\"),#hField.setAccessible(true),#aopProxy=#hField.get(#this),#advisedField=#aopProxy.getClass().getDeclaredField(\"advised\"),#advisedField.setAccessible(true),1==1?#advisedField.get(#aopProxy).getTargetSource().getTarget():null],#nonProxyResultFunc = :[!#isProxy(#this) ? #this :#isJdkDynamicProxy(#this)? #isJdkDynamicProxy(#this) : #cglibTarget(#this)],#nonProxyTarget=#nonProxyResultFunc(#targetBean),#nonProxyTarget'  -x 1";


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
            //抽象方法不处理
//            if (psiMethod.hasModifierProperty(PsiModifier.ABSTRACT)) {
//                e.getPresentation().setEnabled(false);
//                return;
//            }
            //默认方法不处理
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
        PsiElement psiElement = CommonDataKeys.PSI_ELEMENT.getData(dataContext);
        if (editor == null || project == null) {
            return;
        }
        if (psiElement == null) {
            NotifyUtils.notifyMessage(project, "未知错误", NotificationType.ERROR);
            return;
        }
        if (psiElement instanceof PsiClass) {

            return;
        }

        String lowCamelBeanName = "";
        String suffixMethodOrFieldBuild = "";

        //支持方法
        if (psiElement instanceof PsiMethod) {
            PsiMethod psiMethod = (PsiMethod) psiElement;
            if (psiMethod.getContainingClass() instanceof PsiAnonymousClass) {
                NotifyUtils.notifyMessage(project, "匿名类不支持", NotificationType.ERROR);
                return;
            }
            lowCamelBeanName = OgnlPsUtils.getClassBeanName(psiMethod.getContainingClass());
            // complexParameterCall(#{" ":" "})
            suffixMethodOrFieldBuild = OgnlPsUtils.getMethodParameterDefault(psiMethod);
        }

        //支持field
        if (psiElement instanceof PsiField) {
            PsiField psiField = (PsiField) psiElement;
            if (psiField.getContainingClass() instanceof PsiAnonymousClass) {
                NotifyUtils.notifyMessage(project, "匿名类不支持", NotificationType.ERROR);
                return;
            }
            suffixMethodOrFieldBuild = psiField.getNameIdentifier().getText();
            lowCamelBeanName = OgnlPsUtils.getClassBeanName(psiField.getContainingClass());
        }

        try {
            // 获取class的classloader @applicationContextProvider@context的前面部分 xxxApplicationContextProvider
            String classNameClassLoaderGet = SpringStaticContextUtils.getStaticSpringContextClassName(project);

            //#springContext=填充,#springContext.getBean("%s")
            String staticSpringContextGetBeanPrefix = SpringStaticContextUtils.getStaticSpringContextGetBeanPrefix(project,lowCamelBeanName);

            String join = String.join(" ", "ognl", "-x", ArthasCommandConstants.RESULT_X);

            // 构造表达式
            StringBuilder builder = new StringBuilder(join);
            builder.append(" '").append(staticSpringContextGetBeanPrefix).append(".").append(suffixMethodOrFieldBuild).append("'");

            //#springContext=填充,#targetBean=#springContext.getBean("%s")
            String staticSpringContextGetBeanVariable = SpringStaticContextUtils.getStaticSpringContextGetBeanVariable(project,lowCamelBeanName);
            String aopTargetOgnlExpression = String.format(STATIC_SPRING_AOP_TARGET, staticSpringContextGetBeanVariable);

            new ArthasActionStaticDialog(project, classNameClassLoaderGet, builder.toString(), aopTargetOgnlExpression).open("arthas ognl invoke spring bean method、field");
        } catch (Exception ex) {
            NotifyUtils.notifyMessage(project, ex.getMessage(), NotificationType.ERROR);
            return;
        }


    }


}
