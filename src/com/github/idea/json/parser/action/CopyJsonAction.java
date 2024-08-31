package com.github.idea.json.parser.action;

import com.github.idea.json.parser.PsiParserToJson;
import com.github.idea.json.parser.toolkit.ParserContext;
import com.github.wangji92.arthas.plugin.utils.*;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.psi.*;
import org.jetbrains.annotations.NotNull;

/**
 * json 为json
 *
 * @author wangji
 * @date 2024/5/21 21:03
 */
public class CopyJsonAction extends AnAction {

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
        if (psiElement instanceof PsiJavaCodeReferenceElement) {
            e.getPresentation().setEnabled(true);
            return;
        }
        e.getPresentation().setEnabled(false);
    }

    /**
     * 解析上下文
     */
    private static ParserContext parserContext;

    static {
        parserContext = new ParserContext();
        parserContext.setPretty(true);
        parserContext.setJsonType(ParserContext.ParserJsonType.FASTJSON);
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        DataContext dataContext = e.getDataContext();
        PsiElement psiElement = CommonDataKeys.PSI_ELEMENT.getData(dataContext);
        assert psiElement != null;
        OgnlJsonHandlerUtils.JsonType jsonType = OgnlJsonHandlerUtils.getJsonType(e.getProject());
        parserContext.setJsonType(jsonType.getType());
        String jsonString = PsiParserToJson.getInstance().toJSONString(psiElement,parserContext);
        if (StringUtils.isBlank(jsonString)) {
            String emptyData = "JSON data empty";
            ClipboardUtils.setClipboardString("{}");
            NotifyUtils.notifyMessage(e.getProject(), emptyData, NotificationType.INFORMATION);
            return;
        }
        ClipboardUtils.setClipboardString(jsonString);
        String emptyData = "JSON data copied to clipboard";
        NotifyUtils.notifyMessage(e.getProject(), emptyData);
    }
}
