package com.github.idea.json.parser.action;

import com.github.idea.json.parser.IdeaJsonParser;
import com.github.wangji92.arthas.plugin.utils.ClipboardUtils;
import com.github.wangji92.arthas.plugin.utils.NotifyUtils;
import com.github.wangji92.arthas.plugin.utils.OgnlPsUtils;
import com.intellij.openapi.actionSystem.*;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiLocalVariable;
import com.intellij.psi.PsiNewExpression;
import com.intellij.psi.PsiParameter;
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
        if (OgnlPsUtils.isPsiFieldOrMethodOrClass(psiElement)) {
            e.getPresentation().setEnabled(true);
            return;
        }
        boolean anonymousClass = OgnlPsUtils.isAnonymousClass(psiElement);
        if (anonymousClass) {
            e.getPresentation().setEnabled(false);
            return;
        }
        if(psiElement instanceof PsiParameter){
            e.getPresentation().setEnabled(true);
            return;
        }
        if(psiElement instanceof PsiLocalVariable){
            e.getPresentation().setEnabled(true);
            return;
        }
        if(psiElement instanceof PsiNewExpression){
            e.getPresentation().setEnabled(true);
            return;
        }
        e.getPresentation().setEnabled(false);
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        DataContext dataContext = e.getDataContext();
        PsiElement psiElement = CommonDataKeys.PSI_ELEMENT.getData(dataContext);
        String jsonString = IdeaJsonParser.getInstance().toJSONString(psiElement);
        ClipboardUtils.setClipboardString(jsonString);
        NotifyUtils.notifyMessage(e.getProject(),"ok");

    }

    @Override
    public @NotNull ActionUpdateThread getActionUpdateThread() {
        return ActionUpdateThread.BGT;
    }
}
