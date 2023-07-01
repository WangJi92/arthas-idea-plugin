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
public class ArthasVmtoolSetFieldCommandAction extends AnAction {


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
        // 构造方法不支持
        if (OgnlPsUtils.isConstructor(psiElement)) {
            e.getPresentation().setEnabled(false);
            return;
        }
        if (OgnlPsUtils.isNonStaticField(psiElement)) {
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
        String invokeCommand = "";
        String title = "";
        if (OgnlPsUtils.isNonStaticField(commandContext.getPsiElement()) && OgnlPsUtils.isFinalField(commandContext.getPsiElement())) {
            invokeCommand = commandContext.getCommandCode(ShellScriptCommandEnum.VM_TOOL_INVOKE_REFLECT_FINAL_FIELD);
            title = ShellScriptCommandEnum.VM_TOOL_INVOKE_REFLECT_FINAL_FIELD.getEnumMsg();
        } else if (!OgnlPsUtils.fieldHaveSetMethod(commandContext.getPsiElement()) && OgnlPsUtils.isNonStaticField(commandContext.getPsiElement()) && !OgnlPsUtils.isFinalField(commandContext.getPsiElement())) {
            // 没有默认的set 方法，通过反射处理
            invokeCommand = commandContext.getCommandCode(ShellScriptCommandEnum.VM_TOOL_INVOKE_REFLECT_FIELD);
            title = ShellScriptCommandEnum.VM_TOOL_INVOKE_REFLECT_FIELD.getEnumMsg();
        } else if (OgnlPsUtils.fieldHaveSetMethod(commandContext.getPsiElement()) && OgnlPsUtils.isNonStaticField(commandContext.getPsiElement()) && !OgnlPsUtils.isFinalField(commandContext.getPsiElement())) {
            // 有默认的set方法自动识别
            invokeCommand = commandContext.getCommandCode(ShellScriptCommandEnum.VMTOOL_SET_FIELD);
            title = ShellScriptCommandEnum.VMTOOL_SET_FIELD.getEnumMsg();
        }
        String instancesCommand = commandContext.getCommandCode(ShellScriptCommandEnum.VM_TOOL_INSTANCE);
        ArthasVmToolDialog dialog = new ArthasVmToolDialog(project, className, invokeCommand, instancesCommand);
        dialog.open(title);


    }
}
