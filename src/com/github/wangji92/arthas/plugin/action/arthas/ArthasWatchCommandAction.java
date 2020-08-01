package com.github.wangji92.arthas.plugin.action.arthas;

import com.github.wangji92.arthas.plugin.constants.ArthasCommandConstants;
import com.github.wangji92.arthas.plugin.utils.ClipboardUtils;
import com.github.wangji92.arthas.plugin.utils.NotifyUtils;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiField;
import com.intellij.psi.PsiModifier;

/**
 * watch展开结构
 *
 * @author 汪小哥
 * @date 21-12-2019
 */
public class ArthasWatchCommandAction extends BaseArthasPluginAction {
    @Override
    public void doCommand(String className, String methodName, Project project, PsiElement psiElement) {
        StringBuilder watchContentBuilder = new StringBuilder("'{params,returnObj,throwExp");
        //这里针对放置在字段上获取字段的值的信息进行处理增强
        String conditionExpress = ArthasCommandConstants.DEFAULT_CONDITION_EXPRESS;
        if (psiElement instanceof PsiField) {
            PsiField psiField = (PsiField) psiElement;
            String fieldName = psiField.getNameIdentifier().getText();
            if (!psiField.hasModifierProperty(PsiModifier.STATIC)) {
                //获取非静态字段的信息
                watchContentBuilder.append(",target.").append(fieldName);
                // 当获取某个非静态的字段的时候，只能是调用构造、或者非静态方法才可以，这里增加一个表达式判断逻辑
                // 默认为 1== 1
                // 如果调用静态方法 获取 target.xxxField 这样会报错的~ 这里要添加一个条件限制一下
                // 具体可以参考 src/main/java/com/taobao/arthas/core/advisor/ArthasMethod.java
                conditionExpress = "'method.initMethod(),method.constructor!=null || !@java.lang.reflect.Modifier@isStatic(method.method.getModifiers())'";
            } else {
                //watch 获取静态字段的值
                watchContentBuilder.append(",@").append(className).append("@").append(fieldName);
            }
        }
        watchContentBuilder.append("}'");

        String command = String.join(" ", "watch", className, methodName, watchContentBuilder.toString(), "-n", ArthasCommandConstants.INVOKE_COUNT, "-x", ArthasCommandConstants.RESULT_X, conditionExpress);
        ClipboardUtils.setClipboardString(command);
        NotifyUtils.notifyMessage(project, "支持表达式(默认1==1) 更多搜索 [arthas 入门最佳实践],可以将光标放置在字段上watch获取值 ");
    }
}
