package com.github.wangji92.arthas.plugin.common.param;

import com.intellij.openapi.project.Project;

/**
 * 通用脚本构造参数
 *
 * @author 汪小哥
 * @date 05-05-2021
 */
public class ScriptParam {

    /**
     * 工程名称
     */
    private Project project;
    /**
     * class全路径名称
     */
    private String className;
    /**
     * 字段名称
     */
    private String fieldName;
    /**
     * 方法名称
     */
    private String methodName;
    /**
     * 执行详情 如果是 字段 或者方法  // complexParameterCall(#{" ":" "})
     */
    private String executeInfo;
    /**
     * 是否为静态方法
     */
    private Boolean modifierStatic =false;

    /**
     * bean 的名称
     */
    private String beanName;

    /**
     * 匿名类
     */
    private Boolean anonymousClass = false;

    public Project getProject() {
        return project;
    }

    public void setProject(Project project) {
        this.project = project;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getFieldName() {
        return fieldName;
    }

    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public String getExecuteInfo() {
        return executeInfo;
    }

    public void setExecuteInfo(String executeInfo) {
        this.executeInfo = executeInfo;
    }


    public Boolean getModifierStatic() {
        return modifierStatic;
    }

    public void setModifierStatic(Boolean modifierStatic) {
        this.modifierStatic = modifierStatic;
    }

    public String getBeanName() {
        return beanName;
    }

    public void setBeanName(String beanName) {
        this.beanName = beanName;
    }

    public Boolean getAnonymousClass() {
        return anonymousClass;
    }

    public void setAnonymousClass(Boolean anonymousClass) {
        this.anonymousClass = anonymousClass;
    }
}
