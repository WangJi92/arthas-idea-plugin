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
     * dashboard
     */
    DASHBOARD_N_1("dashboard -n 1", "Dashboard Info", false, false, false, false),
    DUMP("dump ${className} -d /tmp/output", "Dump Class Byte Array from JVM", true, true, false, false),
    GETSTATIC("getstatic ${className} ${fieldName} ", "Get Simple Static Field", true, true, true, false),
    TRACE("trace ${className} ${methodName} -v -n 5 --skipJDKMethod false", "Get Simple Static Field", false, true, false, true),
    WATCH("watch ${className} ${methodName} '{params,returnObj,throwExp}' -v -n 5 -x 3 ", "Get Simple Static Field", false, true, false, true),


    ;


    ShellScriptCommandEnum(String code, String msg, boolean needHash, boolean needClass, boolean needField, boolean needMethod) {
        this.code = code;
        this.msg = msg;
        this.needHash = needHash;
        this.needClass = needClass;
        this.needField = needField;
        this.needMethod = needMethod;
    }

    /**
     * code 脚本
     */
    private String code;

    /**
     * 提示信息
     */
    private String msg;
    /**
     * 是否需要classloader hash value
     */
    private Boolean needHash;

    private Boolean needClass;

    private Boolean needField;

    private Boolean needMethod;

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

    public Boolean getNeedHash() {
        return needHash;
    }

    public void setNeedHash(Boolean needHash) {
        this.needHash = needHash;
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
}
