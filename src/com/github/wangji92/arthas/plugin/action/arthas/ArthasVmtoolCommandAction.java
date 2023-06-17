package com.github.wangji92.arthas.plugin.action.arthas;

import com.github.wangji92.arthas.plugin.common.command.CommandContext;
import com.github.wangji92.arthas.plugin.common.enums.ShellScriptCommandEnum;
import com.github.wangji92.arthas.plugin.common.enums.ShellScriptVariableEnum;
import com.github.wangji92.arthas.plugin.ui.ArthasVmToolDialog;
import com.github.wangji92.arthas.plugin.utils.OgnlPsUtils;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;

/**
 * vmtool get instance to invoke method field
 *
 * @author 汪小哥
 * @date 01-06-2021
 */
public class ArthasVmtoolCommandAction extends AnAction {


    @Override
    public void update(@NotNull AnActionEvent e) {
        super.update(e);
        DataContext dataContext = e.getDataContext();
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
        if (OgnlPsUtils.isPsiFieldOrMethodOrClass(psiElement)) {
            e.getPresentation().setEnabled(true);
            return;
        }
        e.getPresentation().setEnabled(false);
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Project project = e.getProject();
        assert project != null;
        CommandContext commandContext = new CommandContext(e);
        String className = commandContext.getKeyValue(ShellScriptVariableEnum.CLASS_NAME);
        String invokeCommand = commandContext.getCommandCode(ShellScriptCommandEnum.VM_TOOL_INVOKE);
        // vmtool 使用的比较多，在各种地方都可以弹出来.. 不然使用不方便
        if (OgnlPsUtils.isConstructor(commandContext.getPsiElement()) ||
                OgnlPsUtils.isStaticMethodOrField(commandContext.getPsiElement())
                || OgnlPsUtils.isPsiClass(commandContext.getPsiElement())) {
            //构造方法、静态方法 这里特殊处理一下 将后面的text 全部干掉
            invokeCommand = invokeCommand.substring(0, invokeCommand.indexOf("'instances[0].")) + "'instances[0]'";
        }

        String instancesCommand = commandContext.getCommandCode(ShellScriptCommandEnum.VM_TOOL_INSTANCE);
        ArthasVmToolDialog dialog = new ArthasVmToolDialog(project, className, invokeCommand, instancesCommand);
        dialog.open("vmtool command,you can edit params use ognl grammar");


    }
}
