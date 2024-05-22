package com.github.idea.json.parser.action;

import com.github.idea.json.parser.PsiParserToJson;
import com.github.wangji92.arthas.plugin.utils.ClipboardUtils;
import com.github.wangji92.arthas.plugin.utils.NotifyUtils;
import com.github.wangji92.arthas.plugin.utils.OgnlPsUtils;
import com.github.wangji92.arthas.plugin.utils.StringUtils;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.actionSystem.*;
import com.intellij.psi.*;
import org.jetbrains.annotations.NotNull;

/**
 * json 为json
 *
 * @author wangji
 * @date 2024/5/21 21:03
 */
public class ToolParseJsonAction extends AnAction {

    @Override
    public void update(@NotNull AnActionEvent e) {
        super.update(e);
        DataContext dataContext = e.getDataContext();
        //获取当前事件触发时，光标所在的元素

        PsiElement psiElement = CommonDataKeys.PSI_ELEMENT.getData(dataContext);
        boolean anonymousClass = OgnlPsUtils.isAnonymousClass(psiElement);
        if (anonymousClass) {
            e.getPresentation().setEnabled(false);
            return;
        }

        if (OgnlPsUtils.isPsiFieldOrMethodOrClass(psiElement)) {
            e.getPresentation().setEnabled(true);
            return;
        }

        if (psiElement instanceof PsiParameter) {
            e.getPresentation().setEnabled(true);
            return;
        }
        if (psiElement instanceof PsiLocalVariable) {
            e.getPresentation().setEnabled(true);
            return;
        }
        if (psiElement instanceof PsiNewExpression) {
            e.getPresentation().setEnabled(true);
            return;
        }
        if (psiElement instanceof PsiReferenceExpression) {
            e.getPresentation().setEnabled(true);
            return;
        }
        if(psiElement instanceof PsiJavaCodeReferenceElement){
            e.getPresentation().setEnabled(true);
            return;
        }
        e.getPresentation().setEnabled(false);
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        DataContext dataContext = e.getDataContext();
        PsiElement psiElement = CommonDataKeys.PSI_ELEMENT.getData(dataContext);
        String jsonString = PsiParserToJson.getInstance().toJSONString(psiElement);
        if (StringUtils.isBlank(jsonString)) {
            String emptyData = "parse element json data empty or parse error";
            ClipboardUtils.setClipboardString("empty json");
            NotifyUtils.notifyMessage(e.getProject(), emptyData, NotificationType.WARNING);
            return;
        }
        ClipboardUtils.setClipboardString(jsonString);
        String emptyData = "parse element json data copied to clipboard";
        NotifyUtils.notifyMessage(e.getProject(), emptyData);
    }

    @Override
    public @NotNull ActionUpdateThread getActionUpdateThread() {
        return ActionUpdateThread.BGT;
    }
}
