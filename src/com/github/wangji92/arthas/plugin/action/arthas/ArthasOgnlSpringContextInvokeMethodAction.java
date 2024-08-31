package com.github.wangji92.arthas.plugin.action.arthas;

import com.github.wangji92.arthas.plugin.setting.AppSettingsState;
import com.github.wangji92.arthas.plugin.ui.ArthasActionStaticDialog;
import com.github.wangji92.arthas.plugin.utils.NotifyUtils;
import com.github.wangji92.arthas.plugin.utils.OgnlPsUtils;
import com.github.wangji92.arthas.plugin.utils.SpringStaticContextUtils;
import com.github.wangji92.arthas.plugin.utils.StringUtils;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
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
        if (OgnlPsUtils.isConstructor(psiElement)) {
            e.getPresentation().setEnabled(false);
            return;
        }
        if (OgnlPsUtils.psiElementInEnum(psiElement)) {
            e.getPresentation().setEnabled(false);
            return;
        }
        boolean staticField = OgnlPsUtils.isStaticField(psiElement);
        if (staticField) {
            e.getPresentation().setEnabled(false);
            return;
        }
        boolean anonymousClass = OgnlPsUtils.isAnonymousClass(psiElement);
        if (anonymousClass) {
            e.getPresentation().setEnabled(false);
            return;
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
        if (!SpringStaticContextUtils.booleanConfigStaticSpringContextFalseOpenConfig(project)) {
            return;
        }
        String lowCamelBeanName = OgnlPsUtils.getSpringBeanName(psiElement);
        String suffixMethodOrFieldBuild = OgnlPsUtils.getExecuteInfo(psiElement);
        //https://github.com/WangJi92/arthas-idea-plugin/issues/124
        // 放置在class 上面多了一个点
        String executeInfo = suffixMethodOrFieldBuild;
        if (StringUtils.isNotBlank(suffixMethodOrFieldBuild)) {
            executeInfo = "." + suffixMethodOrFieldBuild;
        }
        try {
            // 获取class的classloader @applicationContextProvider@context的前面部分 xxxApplicationContextProvider
            String classNameClassLoaderGet = SpringStaticContextUtils.getStaticSpringContextClassName(project);

            //#springContext=填充,#springContext.getBean("%s")
            String staticSpringContextGetBeanPrefix = SpringStaticContextUtils.getStaticSpringContextGetBeanPrefix(project, lowCamelBeanName);

            AppSettingsState instance = AppSettingsState.getInstance(project);
            String depthPrintProperty = instance.depthPrintProperty;

            String join = String.join(" ", "ognl", "-x", depthPrintProperty);

            // 构造表达式
            StringBuilder builder = new StringBuilder(join);
            builder.append(" '").append(staticSpringContextGetBeanPrefix).append(executeInfo).append("'");

            //#springContext=填充,#targetBean=#springContext.getBean("%s")
            String staticSpringContextGetBeanVariable = SpringStaticContextUtils.getStaticSpringContextGetBeanVariable(project, lowCamelBeanName);
            String aopTargetOgnlExpression = String.format(STATIC_SPRING_AOP_TARGET, staticSpringContextGetBeanVariable);

            new ArthasActionStaticDialog(project, classNameClassLoaderGet, builder.toString(), aopTargetOgnlExpression).open("Static spring context invoke【手动编辑填写参数】【bean名称可能不正确,可以手动修改】");
        } catch (Exception ex) {
            NotifyUtils.notifyMessage(project, ex.getMessage(), NotificationType.ERROR);
            return;
        }


    }


}
