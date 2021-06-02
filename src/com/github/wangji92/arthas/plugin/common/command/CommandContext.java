package com.github.wangji92.arthas.plugin.common.command;

import com.github.wangji92.arthas.plugin.common.enums.ShellScriptCommandEnum;
import com.github.wangji92.arthas.plugin.common.enums.ShellScriptVariableEnum;
import com.github.wangji92.arthas.plugin.constants.ArthasCommandConstants;
import com.github.wangji92.arthas.plugin.setting.AppSettingsState;
import com.github.wangji92.arthas.plugin.utils.OgnlPsUtils;
import com.github.wangji92.arthas.plugin.utils.StringUtils;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;

import java.util.HashMap;
import java.util.Map;

/**
 * 命令上下文
 *
 * @author 汪小哥
 * @date 02-06-2021
 */
public class CommandContext {
    /**
     * 上下文
     */
    private Map<String, String> params = new HashMap<>();

    private Project project;

    private PsiElement psiElement;
    /**
     * maybe null
     */
    private Editor editor;

    public CommandContext(AnActionEvent event) {
        DataContext dataContext = event.getDataContext();
        this.project = CommonDataKeys.PROJECT.getData(dataContext);
        this.editor = CommonDataKeys.EDITOR.getData(dataContext);
        this.psiElement = CommonDataKeys.PSI_ELEMENT.getData(dataContext);
    }

    public CommandContext(Project project, PsiElement psiElement) {
        this.project = project;
        this.psiElement = psiElement;
    }

    /**
     * 初始化上下文信息
     */
    private synchronized void initContextParam() {
        if (!params.isEmpty()) {
            return;
        }
        AppSettingsState instance = AppSettingsState.getInstance(project);
        String methodName = OgnlPsUtils.getMethodName(this.psiElement);
        String className = OgnlPsUtils.getCommonOrInnerOrAnonymousClassName(this.psiElement);
        String fieldName = OgnlPsUtils.getFieldName(this.psiElement);
        String executeInfo = OgnlPsUtils.getExecuteInfo(this.psiElement);
        params.put(ShellScriptVariableEnum.PROPERTY_DEPTH.getEnumMsg(), instance.depthPrintProperty);
        params.put(ShellScriptVariableEnum.CLASS_NAME.getEnumMsg(), className);
        params.put(ShellScriptVariableEnum.METHOD_NAME.getEnumMsg(), methodName);
        params.put(ShellScriptVariableEnum.EXECUTE_INFO.getEnumMsg(), executeInfo);
        params.put(ShellScriptVariableEnum.FIELD_NAME.getEnumMsg(), fieldName);
        params.put(ShellScriptVariableEnum.SPRING_CONTEXT.getEnumMsg(), instance.staticSpringContextOgnl);
        params.put(ShellScriptVariableEnum.INVOKE_COUNT.getEnumMsg(), instance.invokeCount);
        params.put(ShellScriptVariableEnum.INVOKE_MONITOR_COUNT.getEnumMsg(), instance.invokeMonitorCount);
        params.put(ShellScriptVariableEnum.INVOKE_MONITOR_INTERVAL.getEnumMsg(), instance.invokeMonitorInterval);
        String skpJdkMethodCommand = instance.traceSkipJdk ? "" : ArthasCommandConstants.DEFAULT_SKIP_JDK_FALSE;
        params.put(ShellScriptVariableEnum.SKIP_JDK_METHOD.getEnumMsg(), skpJdkMethodCommand);
        String printConditionExpress = instance.printConditionExpress ? "-v" : "";
        params.put(ShellScriptVariableEnum.PRINT_CONDITION_RESULT.getEnumMsg(), printConditionExpress);
        params.put(ShellScriptVariableEnum.CLASSLOADER_HASH_VALUE.getEnumMsg(), "${CLASSLOADER_HASH_VALUE}");
        String methodNameNotStar = "*".equals(methodName) ? "" : methodName;
        params.put(ShellScriptVariableEnum.METHOD_NAME_NOT_STAR.getEnumMsg(), methodNameNotStar);
        String conditionExpressDisplay = instance.conditionExpressDisplay ? ArthasCommandConstants.DEFAULT_CONDITION_EXPRESS : "";
        params.put(ShellScriptVariableEnum.CONDITION_EXPRESS_DEFAULT.getEnumMsg(), conditionExpressDisplay);
        String beanName = OgnlPsUtils.getSpringBeanName(this.psiElement);
        params.put(ShellScriptVariableEnum.SPRING_BEAN_NAME.getEnumMsg(), beanName);
        params.put(ShellScriptVariableEnum.DEFAULT_FIELD_VALUE.getEnumMsg(), OgnlPsUtils.getFieldDefaultValue(this.psiElement));
        if (StringUtils.isNotBlank(fieldName)) {
            String capitalizeFieldName = StringUtils.capitalize(fieldName);
            params.put(ShellScriptVariableEnum.CAPITALIZE_FIELD_VALUE.getEnumMsg(), capitalizeFieldName);
        }
    }

    /**
     * 获取代码
     *
     * @param commandEnum
     * @return
     */
    public String getCommandCode(ShellScriptCommandEnum commandEnum) {
        String codeValue = commandEnum.getCode();
        if (this.params.isEmpty()) {
            this.initContextParam();
        }
        return StringUtils.stringSubstitutorFromText(codeValue, this.params);
    }

    /**
     * 或取代码
     *
     * @param commandCode
     * @return
     */
    public String getCommandCode(String commandCode) {
        if (this.params.isEmpty()) {
            this.initContextParam();
        }
        return StringUtils.stringSubstitutorFromText(commandCode, this.params);
    }

    public Project getProject() {
        return project;
    }

    public PsiElement getPsiElement() {
        return psiElement;
    }

    public Editor getEditor() {
        return editor;
    }
}
