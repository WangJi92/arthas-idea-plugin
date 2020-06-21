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
        if (psiElement instanceof PsiField) {
            PsiField psiField = (PsiField) psiElement;
            String fieldName = psiField.getNameIdentifier().getText();
            if (!psiField.hasModifierProperty(PsiModifier.STATIC)) {
                //获取非静态字段的信息
                watchContentBuilder.append(",target.").append(fieldName);
            } else {
                //watch 获取静态字段的值
                watchContentBuilder.append(",@").append(className).append("@").append(fieldName);
            }
        }
        watchContentBuilder.append("}'");
        String command = String.join(" ", "watch", className, methodName, watchContentBuilder.toString(), "-n", ArthasCommandConstants.INVOKE_COUNT, "-x", ArthasCommandConstants.RESULT_X, ArthasCommandConstants.DEFAULT_CONDITION_EXPRESS);
        ClipboardUtils.setClipboardString(command);
        NotifyUtils.notifyMessage(project, "支持表达式(默认1==1) eg:'params[0].name=\"name\" and params.size == 1',可以将光标放置咋字段上watch获取值 ");
    }
}
