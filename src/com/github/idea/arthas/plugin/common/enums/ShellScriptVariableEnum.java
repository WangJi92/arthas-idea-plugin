package com.github.idea.arthas.plugin.common.enums;

import com.github.idea.arthas.plugin.common.enums.base.EnumCodeMsg;

/**
 * @author 汪小哥
 * @date 04-05-2021
 */
public enum ShellScriptVariableEnum implements EnumCodeMsg<String> {
    /**
     * class name
     */
    CLASS_NAME("${CLASS_NAME}", "CLASS_NAME"),
    /**
     * spring bean 的名称
     */
    SPRING_BEAN_NAME("${SPRING_BEAN_NAME}", "SPRING_BEAN_NAME"),
    /**
     * 方法名称
     */
    METHOD_NAME("${METHOD_NAME}", "METHOD_NAME"),

    /**
     * 非 * 的 methodName
     */
    METHOD_NAME_NOT_STAR("${METHOD_NAME_NOT_STAR}", "METHOD_NAME_NOT_STAR"),
    /**
     * 字段名称
     */
    FIELD_NAME("${FIELD_NAME}", "FIELD_NAME"),
    /**
     * 获取默认值
     */
    DEFAULT_FIELD_VALUE("${DEFAULT_FIELD_VALUE}", "DEFAULT_FIELD_VALUE"),
    /**
     * 首字母大写
     */
    CAPITALIZE_FIELD_VALUE("${CAPITALIZE_FIELD_VALUE}", "CAPITALIZE_FIELD_VALUE"),
    /**
     * 可以是字段 xxField
     * 可以是方法 xxMethod(xxx)
     */
    EXECUTE_INFO("${EXECUTE_INFO}", "EXECUTE_INFO"),
    /**
     * 打印深度
     */
    PROPERTY_DEPTH("${PROPERTY_DEPTH}", "PROPERTY_DEPTH"),
    /**
     * 调用次数
     */
    INVOKE_COUNT("${INVOKE_COUNT}", "INVOKE_COUNT"),
    /**
     * 监控调用次数
     */
    INVOKE_MONITOR_COUNT("${INVOKE_MONITOR_COUNT}", "INVOKE_MONITOR_COUNT"),


    INVOKE_MONITOR_INTERVAL("${INVOKE_MONITOR_INTERVAL}", "INVOKE_MONITOR_INTERVAL"),
    /**
     * 跳过JDK
     */
    SKIP_JDK_METHOD("${SKIP_JDK_METHOD}", "SKIP_JDK_METHOD"),
    /**
     * spring context
     */
    SPRING_CONTEXT("${SPRING_CONTEXT}", "SPRING_CONTEXT"),
    /**
     * 打印条件表达式
     */
    PRINT_CONDITION_RESULT("${PRINT_CONDITION_RESULT}", "PRINT_CONDITION_RESULT"),
    /**
     * 条件表达式默认值
     */
    CONDITION_EXPRESS_DEFAULT("${CONDITION_EXPRESS_DEFAULT}", "CONDITION_EXPRESS_DEFAULT"),
    /**
     * classloader hash value
     */
    CLASSLOADER_HASH_VALUE("${CLASSLOADER_HASH_VALUE}", "CLASSLOADER_HASH_VALUE"),

    /**
     * 编辑器选中的文字
     */
    EDITOR_SELECT_TEXT("${EDITOR_SELECT_TEXT}", "EDITOR_SELECT_TEXT"),


    ;


    ShellScriptVariableEnum(String code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    private String code;
    private String msg;


    @Override
    public String getEnumMsg() {
        return msg;
    }

    @Override
    public String getCode() {
        return code;
    }
}
