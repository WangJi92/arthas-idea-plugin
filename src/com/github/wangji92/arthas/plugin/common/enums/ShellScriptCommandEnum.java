package com.github.wangji92.arthas.plugin.common.enums;

import com.github.wangji92.arthas.plugin.common.enums.base.EnumCodeMsg;

/**
 * 可以直接执行的脚本通用信息
 *
 * @author 汪小哥
 * @date 04-05-2021
 */
public enum ShellScriptCommandEnum implements EnumCodeMsg<String> {
    /**
     * watch
     */
    WATCH("watch "
            + ShellScriptVariableEnum.CLASS_NAME.getCode() + " "
            + ShellScriptVariableEnum.METHOD_NAME.getCode() + " "
            + "'{params,returnObj,throwExp}'"
            + ShellScriptVariableEnum.PRINT_CONDITION_RESULT.getCode() + " -n "
            + ShellScriptVariableEnum.INVOKE_COUNT.getCode() + " "
            + " -x "
            + ShellScriptVariableEnum.PROPERTY_DEPTH.getCode() + " "
            + ShellScriptVariableEnum.CONDITION_EXPRESS_DEFAULT.getCode(),
            "display the input/output parameter, return object, and thrown exception of specified method invocation", true, false, true, false),
    /**
     * trace
     */
    TRACE("trace "
            + ShellScriptVariableEnum.CLASS_NAME.getCode() + " "
            + ShellScriptVariableEnum.METHOD_NAME.getCode() + " "
            + ShellScriptVariableEnum.PRINT_CONDITION_RESULT.getCode() + " -n "
            + ShellScriptVariableEnum.INVOKE_COUNT.getCode() + " "
            + ShellScriptVariableEnum.SKIP_JDK_METHOD.getCode() + " "
            + ShellScriptVariableEnum.CONDITION_EXPRESS_DEFAULT.getCode(),
            "trace the execution time of specified method invocation. ", true, false, true, false),

    /**
     * trace
     */
    STACK("stack "
            + ShellScriptVariableEnum.CLASS_NAME.getCode() + " "
            + ShellScriptVariableEnum.METHOD_NAME.getCode() + " "
            + ShellScriptVariableEnum.PRINT_CONDITION_RESULT.getCode() + " -n "
            + ShellScriptVariableEnum.INVOKE_COUNT.getCode() + " "
            + ShellScriptVariableEnum.CONDITION_EXPRESS_DEFAULT.getCode(),
            "display the stack trace for the specified class and method", true, false, true, false),
    /**
     * monitor
     */
    MONITOR("monitor "
            + ShellScriptVariableEnum.CLASS_NAME.getCode() + " "
            + ShellScriptVariableEnum.METHOD_NAME.getCode() + " "
            + ShellScriptVariableEnum.PRINT_CONDITION_RESULT.getCode() + " -n "
            + ShellScriptVariableEnum.INVOKE_MONITOR_COUNT.getCode() + " "
            + ShellScriptVariableEnum.INVOKE_MONITOR_COUNT.getCode() + "  --cycle "
            + ShellScriptVariableEnum.INVOKE_MONITOR_INTERVAL.getCode() + " "
            + ShellScriptVariableEnum.CONDITION_EXPRESS_DEFAULT.getCode(),
            "monitor method execution statistics ", true, false, true, false),

    /**
     * jad
     */
    JAD("jad --source-only "
            + ShellScriptVariableEnum.CLASS_NAME.getCode() + " "
            + ShellScriptVariableEnum.METHOD_NAME_NOT_STAR.getCode(),
            "decompile class", true, false, false, false),
    /**
     * sc
     */
    SC("sc -d "
            + ShellScriptVariableEnum.CLASS_NAME.getCode(),
            "search all the classes loaded by jvm", true, false, false, false),
    /**
     * sc
     */
    SM("sm -d "
            + ShellScriptVariableEnum.CLASS_NAME.getCode() + " "
            + ShellScriptVariableEnum.METHOD_NAME.getCode(),
            "search the method of classes loaded by jvm", true, false, false, false),


    /**
     * 调用静态变量 或者方法
     */
    OGNL_GETSTATIC("ognl "
            + " -x "
            + ShellScriptVariableEnum.PROPERTY_DEPTH.getCode() + " @"
            + ShellScriptVariableEnum.CLASS_NAME.getCode() + "@"
            + ShellScriptVariableEnum.EXECUTE_INFO.getCode()
            + " -c "
            + ShellScriptVariableEnum.CLASSLOADER_HASH_VALUE.getCode(),
            "ognl to get static method field", true, false, false, true),
    /**
     * 简单的字段
     */
    GETSTATIC("getstatic "
            + ShellScriptVariableEnum.CLASS_NAME.getCode() + " "
            + ShellScriptVariableEnum.FIELD_NAME.getCode(),
            "get simple static field", true, true, false, true),
    /**
     * logger
     */
    LOGGER("logger --name "
            + ShellScriptVariableEnum.CLASS_NAME.getCode() + " "
            + "--level debug "
            + " -c "
            + ShellScriptVariableEnum.CLASSLOADER_HASH_VALUE.getCode(),
            "--level debug 可以编辑修改为 info、error", true, false, false, false),


    /**
     * dump
     */
    DUMP("dump "
            + ShellScriptVariableEnum.CLASS_NAME.getCode()
            + " -d /tmp/output "
            + " -c "
            + ShellScriptVariableEnum.CLASSLOADER_HASH_VALUE.getCode(),
            "dump class byte array from jvm", true, false, false, false),


    ;


    ShellScriptCommandEnum(String code, String msg, boolean needClass, boolean needField, boolean needMethod, boolean needStatic) {
        this.code = code;
        this.msg = msg;
        this.needClass = needClass;
        this.needField = needField;
        this.needMethod = needMethod;
        this.needStatic = needStatic;
    }

    /**
     * code 脚本
     */
    private String code;

    /**
     * 提示信息
     */
    private String msg;

    private Boolean needClass;

    private Boolean needField;

    private Boolean needMethod;

    private Boolean needStatic;


    @Override
    public String getEnumMsg() {
        return msg;
    }

    @Override
    public String getCode() {
        return code;
    }

    public String getMsg() {
        return msg;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }


    public Boolean getNeedClass() {
        return needClass;
    }

    public void setNeedClass(Boolean needClass) {
        this.needClass = needClass;
    }

    public Boolean getNeedField() {
        return needField;
    }

    public void setNeedField(Boolean needField) {
        this.needField = needField;
    }

    public Boolean getNeedMethod() {
        return needMethod;
    }

    public void setNeedMethod(Boolean needMethod) {
        this.needMethod = needMethod;
    }

    @Override
    public String toString() {
        return this.getCode();
    }

    public Boolean getNeedStatic() {
        return needStatic;
    }

    public void setNeedStatic(Boolean needStatic) {
        this.needStatic = needStatic;
    }
}
