package com.github.wangji92.arthas.plugin.action.arthas;

import com.github.wangji92.arthas.plugin.common.command.CommandContext;
import com.github.wangji92.arthas.plugin.common.enums.ShellScriptCommandEnum;
import com.github.wangji92.arthas.plugin.common.enums.ShellScriptVariableEnum;
import com.github.wangji92.arthas.plugin.ui.ArthasActionStaticDialog;
import com.github.wangji92.arthas.plugin.utils.OgnlPsUtils;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiField;
import org.jetbrains.annotations.NotNull;

/**
 * 通过反射获取字段，通过ognl 设置字段的值
 * https://github.com/alibaba/arthas/issues/641
 * 设置static final 特殊处理 https://www.cnblogs.com/noKing/p/9038234.html
 *
 * @author 汪小哥
 * @date 17-01-2020
 */
public class ArthasOgnlSetStaticFieldCommandAction extends AnAction {

    @Override
    public void update(@NotNull AnActionEvent e) {
        super.update(e);
        DataContext dataContext = e.getDataContext();
        PsiElement psiElement = CommonDataKeys.PSI_ELEMENT.getData(dataContext);
        boolean anonymousClass = OgnlPsUtils.isAnonymousClass(psiElement);
        if (anonymousClass) {
            e.getPresentation().setEnabled(false);
            return;
        }
        if (OgnlPsUtils.isStaticField(psiElement)) {
            e.getPresentation().setEnabled(true);
            return;
        }
        e.getPresentation().setEnabled(false);
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent event) {
        DataContext dataContext = event.getDataContext();
        Editor editor = CommonDataKeys.EDITOR.getData(dataContext);
        Project project = CommonDataKeys.PROJECT.getData(dataContext);
        if (editor == null || project == null) {
            return;
        }
        PsiElement psiElement = CommonDataKeys.PSI_ELEMENT.getData(dataContext);
        CommandContext commandContext = new CommandContext(event);
        ShellScriptCommandEnum scriptCommandEnum = null;
        if (ShellScriptCommandEnum.OGNL_TO_MODIFY_FINAL_STATIC_FIELD.support(commandContext)) {
            scriptCommandEnum = ShellScriptCommandEnum.OGNL_TO_MODIFY_FINAL_STATIC_FIELD;
        } else if (ShellScriptCommandEnum.OGNL_TO_MODIFY_NO_FINAL_STATIC_FIELD.support(commandContext)) {
            scriptCommandEnum = ShellScriptCommandEnum.OGNL_TO_MODIFY_NO_FINAL_STATIC_FIELD;
        }
        String command = commandContext.getCommandCode(scriptCommandEnum);
        if (psiElement instanceof PsiField) {
            String className = commandContext.getKeyValue(ShellScriptVariableEnum.CLASS_NAME);
            new ArthasActionStaticDialog(project, className, command, "").open("Ognl Reflect To Modify Static Field");
        }
    }
}
