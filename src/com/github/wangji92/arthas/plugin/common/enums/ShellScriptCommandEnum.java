package com.github.wangji92.arthas.plugin.common.enums;

import com.github.wangji92.arthas.plugin.common.command.CommandContext;
import com.github.wangji92.arthas.plugin.common.enums.base.EnumCodeMsg;
import com.github.wangji92.arthas.plugin.utils.OgnlPsUtils;
import com.github.wangji92.arthas.plugin.utils.SpringStaticContextUtils;
import com.github.wangji92.arthas.plugin.utils.StringUtils;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiField;
import com.intellij.psi.PsiMethod;

import static com.github.wangji92.arthas.plugin.constants.ArthasCommandConstants.SPRING_ALL_MAP_PROPERTY;

/**
 * 可以直接执行的脚本通用信息
 *
 * @author 汪小哥
 * @date 04-05-2021
 */
public enum ShellScriptCommandEnum implements EnumCodeMsg<String> {
    /**
     * 调用静态变量 或者方法
     */
    OGNL_GETSTATIC("ognl -x "
            + ShellScriptVariableEnum.PROPERTY_DEPTH.getCode() + " '@"
            + ShellScriptVariableEnum.CLASS_NAME.getCode() + "@"
            + ShellScriptVariableEnum.EXECUTE_INFO.getCode() + "'",
            "ognl to get static method field 注意需要编执行方法的参数",
            "https://arthas.aliyun.com/doc/ognl.html#%E4%BD%BF%E7%94%A8%E5%8F%82%E8%80%83") {
        @Override
        public boolean support(CommandContext context) {

            if (OgnlPsUtils.isAnonymousClass(context.getPsiElement())) {
                return false;
            }
            return OgnlPsUtils.isStaticMethodOrField(context.getPsiElement());
        }

        @Override
        public String getScCommand(CommandContext context) {
            String className = OgnlPsUtils.getCommonOrInnerOrAnonymousClassName(context.getPsiElement());
            return String.join(" ", "sc", "-d", className);
        }

        @Override
        public String getArthasCommand(CommandContext context) {
            return context.getCommandCode(this);
        }
    },

    /**
     * 简单的字段
     */
    GETSTATIC("getstatic "
            + ShellScriptVariableEnum.CLASS_NAME.getCode() + " "
            + ShellScriptVariableEnum.FIELD_NAME.getCode(),
            "get simple static field",
            "https://arthas.aliyun.com/doc/getstatic.html#%E4%BD%BF%E7%94%A8%E5%8F%82%E8%80%83") {
        @Override
        public boolean support(CommandContext context) {
            if (OgnlPsUtils.isAnonymousClass(context.getPsiElement())) {
                return false;
            }
            return OgnlPsUtils.isStaticField(context.getPsiElement());
        }

        @Override
        public String getScCommand(CommandContext context) {
            return null;
        }

        @Override
        public String getArthasCommand(CommandContext context) {
            return context.getCommandCode(this);
        }
    },

    /**
     * watch static field
     */
    WATCH_STATIC_FILED("watch "
            + ShellScriptVariableEnum.CLASS_NAME.getCode() + " * "
            + " '{params,returnObj,throwExp,@" + ShellScriptVariableEnum.CLASS_NAME.getCode() + "@" + ShellScriptVariableEnum.FIELD_NAME.getCode() + "}'"
            + ShellScriptVariableEnum.PRINT_CONDITION_RESULT.getCode() + " -n "
            + ShellScriptVariableEnum.INVOKE_COUNT.getCode() + " "
            + " -x "
            + ShellScriptVariableEnum.PROPERTY_DEPTH.getCode() + " "
            + ShellScriptVariableEnum.CONDITION_EXPRESS_DEFAULT.getCode(),
            "watch  static field",
            "https://arthas.aliyun.com/doc/watch.html#%E8%8E%B7%E5%8F%96%E7%B1%BB%E7%9A%84%E9%9D%99%E6%80%81%E5%AD%97%E6%AE%B5%E3%80%81%E8%B0%83%E7%94%A8%E7%B1%BB%E7%9A%84%E9%9D%99%E6%80%81%E5%87%BD%E6%95%B0%E7%9A%84%E4%BE%8B%E5%AD%90") {
        @Override
        public boolean support(CommandContext context) {
            if (OgnlPsUtils.isAnonymousClass(context.getPsiElement())) {
                return false;
            }
            return OgnlPsUtils.isStaticField(context.getPsiElement());
        }

        @Override
        public String getScCommand(CommandContext context) {
            return null;
        }

        @Override
        public String getArthasCommand(CommandContext context) {
            return context.getCommandCode(this);
        }
    },
    /**
     * watch non static field
     */
    WATCH_NON_STATIC_FILED("watch "
            + ShellScriptVariableEnum.CLASS_NAME.getCode() + " "
            + ShellScriptVariableEnum.METHOD_NAME.getCode() + " "
            + " '{params,returnObj,throwExp,target." + ShellScriptVariableEnum.FIELD_NAME.getCode() + "}' "
            + ShellScriptVariableEnum.PRINT_CONDITION_RESULT.getCode() + " -n "
            + ShellScriptVariableEnum.INVOKE_COUNT.getCode() + " "
            + " -x "
            + ShellScriptVariableEnum.PROPERTY_DEPTH.getCode() + " "
            + "'method.initMethod(),method.constructor!=null || !@java.lang.reflect.Modifier@isStatic(method.method.getModifiers())'",
            "watch non static field",
            "https://arthas.aliyun.com/doc/watch.html#%E8%A7%82%E5%AF%9F%E5%BD%93%E5%89%8D%E5%AF%B9%E8%B1%A1%E4%B8%AD%E7%9A%84%E5%B1%9E%E6%80%A7") {
        @Override
        public boolean support(CommandContext context) {
            return OgnlPsUtils.isNonStaticField(context.getPsiElement());
        }

        @Override
        public String getScCommand(CommandContext context) {
            return null;
        }

        @Override
        public String getArthasCommand(CommandContext context) {
            return context.getCommandCode(this);
        }
    },
    /**
     * watch
     */
    WATCH("watch "
            + ShellScriptVariableEnum.CLASS_NAME.getCode() + " "
            + ShellScriptVariableEnum.METHOD_NAME.getCode() + " "
            + "'{params,returnObj,throwExp}' "
            + ShellScriptVariableEnum.PRINT_CONDITION_RESULT.getCode() + " -n "
            + ShellScriptVariableEnum.INVOKE_COUNT.getCode() + " "
            + " -x "
            + ShellScriptVariableEnum.PROPERTY_DEPTH.getCode() + " "
            + ShellScriptVariableEnum.CONDITION_EXPRESS_DEFAULT.getCode(),
            "watch input/output parameter, return object,exception",
            "https://arthas.aliyun.com/doc/watch.html#%E5%90%8C%E6%97%B6%E8%A7%82%E5%AF%9F%E5%87%BD%E6%95%B0%E8%B0%83%E7%94%A8%E5%89%8D%E5%92%8C%E5%87%BD%E6%95%B0%E8%BF%94%E5%9B%9E%E5%90%8E") {
        @Override
        public boolean support(CommandContext context) {
            return OgnlPsUtils.isPsiFieldOrMethodOrClass(context.getPsiElement());
        }

        @Override
        public String getScCommand(CommandContext context) {
            return null;
        }

        @Override
        public String getArthasCommand(CommandContext context) {
            return context.getCommandCode(this);
        }
    },
    WATCH_PRINT_STACK("watch "
            + ShellScriptVariableEnum.CLASS_NAME.getCode() + " "
            + ShellScriptVariableEnum.METHOD_NAME.getCode() + " "
            + "'new Throwable()' "
            + ShellScriptVariableEnum.PRINT_CONDITION_RESULT.getCode() + " -n "
            + ShellScriptVariableEnum.INVOKE_COUNT.getCode() + " "
            + " -x "
            + ShellScriptVariableEnum.PROPERTY_DEPTH.getCode() + " "
            + ShellScriptVariableEnum.CONDITION_EXPRESS_DEFAULT.getCode(),
            "watch print stack",
            "https://mp.weixin.qq.com/s/8ezzcBEdfmsAdcnlLWlq4w") {
        @Override
        public boolean support(CommandContext context) {
            return OgnlPsUtils.isPsiFieldOrMethodOrClass(context.getPsiElement());
        }

        @Override
        public String getScCommand(CommandContext context) {
            return null;
        }

        @Override
        public String getArthasCommand(CommandContext context) {
            return context.getCommandCode(this);
        }
    },

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
            "trace the execution time of specified method invocation. ",
            "https://arthas.aliyun.com/doc/trace.html") {
        @Override
        public boolean support(CommandContext context) {
            return OgnlPsUtils.isPsiFieldOrMethodOrClass(context.getPsiElement());
        }

        @Override
        public String getScCommand(CommandContext context) {
            return null;
        }

        @Override
        public String getArthasCommand(CommandContext context) {
            return context.getCommandCode(this);
        }
    },

    /**
     * vm tool
     */
    VM_TOOL_INVOKE("vmtool -x "
            + ShellScriptVariableEnum.PROPERTY_DEPTH.getCode() + " "
            + "--action getInstances --className "
            + ShellScriptVariableEnum.CLASS_NAME.getCode() + " "
            + " --express 'instances[0]."
            + ShellScriptVariableEnum.EXECUTE_INFO.getCode() + "' ",
            "vmtool get instance invoke method field,you can edit express params,find first instance",
            "https://arthas.aliyun.com/doc/vmtool.html#%E6%89%A7%E8%A1%8C%E8%A1%A8%E8%BE%BE%E5%BC%8F") {
        @Override
        public boolean support(CommandContext context) {
            if (OgnlPsUtils.isAnonymousClass(context.getPsiElement())) {
                return false;
            }
            // 构造方法不支持
            if (OgnlPsUtils.isConstructor(context.getPsiElement())) {
                return false;
            }
            return OgnlPsUtils.isNonStaticMethodOrField(context.getPsiElement());
        }

        @Override
        public String getScCommand(CommandContext context) {
            String className = OgnlPsUtils.getCommonOrInnerOrAnonymousClassName(context.getPsiElement());
            return String.join(" ", "sc", "-d", className);
        }

        @Override
        public String getArthasCommand(CommandContext context) {
            return context.getCommandCode(this);
        }
    },
    VMTOOL_SET_FIELD("vmtool -x "
            + ShellScriptVariableEnum.PROPERTY_DEPTH.getCode() + " "
            + "--action getInstances --className "
            + ShellScriptVariableEnum.CLASS_NAME.getCode() + " "
            + " --express '#instances[0].set" + ShellScriptVariableEnum.CAPITALIZE_FIELD_VALUE.getCode() + "(" + ShellScriptVariableEnum.DEFAULT_FIELD_VALUE.getCode() + ")'",
            "vmtool set field method,method parameters need to be edited",
            "https://arthas.aliyun.com/doc/vmtool.html#%E6%89%A7%E8%A1%8C%E8%A1%A8%E8%BE%BE%E5%BC%8F") {
        @Override
        public boolean support(CommandContext context) {
            if (OgnlPsUtils.isAnonymousClass(context.getPsiElement())) {
                return false;
            }
            // 构造方法不支持
            if (OgnlPsUtils.isConstructor(context.getPsiElement())) {
                return false;
            }
            if (OgnlPsUtils.isNonStaticField(context.getPsiElement()) && !OgnlPsUtils.isFinalField(context.getPsiElement()) && OgnlPsUtils.fieldHaveSetMethod(context.getPsiElement())) {
                return true;
            }
            return false;
        }

        @Override
        public String getScCommand(CommandContext context) {
            String className = OgnlPsUtils.getCommonOrInnerOrAnonymousClassName(context.getPsiElement());
            return String.join(" ", "sc", "-d", className);
        }

        @Override
        public String getArthasCommand(CommandContext context) {
            return context.getCommandCode(this);
        }
    },
    /**
     * 通过反射设置变量
     * vmtool -x 3 --action getInstances --className com.xxx.cache.CacheAspect  --express '#field=instances[0].getClass().getDeclaredField("cacheEnabled"),#field.setAccessible(true),#field.set(instances[0],false)'  -c 3bd94634
     */
    VM_TOOL_INVOKE_REFLECT_FIELD("vmtool -x "
            + ShellScriptVariableEnum.PROPERTY_DEPTH.getCode() + " "
            + "--action getInstances --className "
            + ShellScriptVariableEnum.CLASS_NAME.getCode() + " "
            + " --express '#field=instances[0].getClass()"
            + ".getDeclaredField(\"" + ShellScriptVariableEnum.EXECUTE_INFO.getCode() + "\"),#field.setAccessible(true),"
            + "#field.set(instances[0]," + ShellScriptVariableEnum.DEFAULT_FIELD_VALUE.getCode() + ")'",
            "vmtool 获取到实例后通过反射修改字段的值,需要编辑设置的值的信息",
            "https://arthas.aliyun.com/doc/vmtool.html#%E6%89%A7%E8%A1%8C%E8%A1%A8%E8%BE%BE%E5%BC%8F") {
        @Override
        public boolean support(CommandContext context) {
            if (OgnlPsUtils.isAnonymousClass(context.getPsiElement())) {
                return false;
            }
            // 构造方法不支持
            if (OgnlPsUtils.isConstructor(context.getPsiElement())) {
                return false;
            }
            return OgnlPsUtils.isNonStaticField(context.getPsiElement()) && !OgnlPsUtils.isFinalField(context.getPsiElement());
        }

        @Override
        public String getScCommand(CommandContext context) {
            String className = OgnlPsUtils.getCommonOrInnerOrAnonymousClassName(context.getPsiElement());
            return String.join(" ", "sc", "-d", className);
        }

        @Override
        public String getArthasCommand(CommandContext context) {
            return context.getCommandCode(this);
        }
    },
    /**
     * 通过反射设置变量
     * vmtool -x 3 --action getInstances --className com.xxx.cache.CacheAspect  --express '#field=instances[0].getClass().getDeclaredField("cacheEnabled"),#field.setAccessible(true),#field.set(instances[0],false)'  -c 3bd94634
     */
    VM_TOOL_INVOKE_REFLECT_FINAL_FIELD("vmtool -x "
            + ShellScriptVariableEnum.PROPERTY_DEPTH.getCode() + " "
            + "--action getInstances --className "
            + ShellScriptVariableEnum.CLASS_NAME.getCode() + " "
            + " --express '#field=instances[0].getClass()"
            + ".getDeclaredField(\"" + ShellScriptVariableEnum.EXECUTE_INFO.getCode() + "\"),#modifiers=#field.getClass().getDeclaredField(\"modifiers\"),#modifiers.setAccessible(true),#modifiers.setInt(#field,#field.getModifiers() & ~@java.lang.reflect.Modifier@FINAL),#field.setAccessible(true),"
            + "#field.set(instances[0]," + ShellScriptVariableEnum.DEFAULT_FIELD_VALUE.getCode() + ")'",
            "vmtool 获取到实例后通过反射修改final字段的值,需要编辑设置的值的信息",
            "https://arthas.aliyun.com/doc/vmtool.html#%E6%89%A7%E8%A1%8C%E8%A1%A8%E8%BE%BE%E5%BC%8F") {
        @Override
        public boolean support(CommandContext context) {
            if (OgnlPsUtils.isAnonymousClass(context.getPsiElement())) {
                return false;
            }
            // 构造方法不支持
            if (OgnlPsUtils.isConstructor(context.getPsiElement())) {
                return false;
            }
            return OgnlPsUtils.isNonStaticField(context.getPsiElement()) && OgnlPsUtils.isFinalField(context.getPsiElement());
        }

        @Override
        public String getScCommand(CommandContext context) {
            String className = OgnlPsUtils.getCommonOrInnerOrAnonymousClassName(context.getPsiElement());
            return String.join(" ", "sc", "-d", className);
        }

        @Override
        public String getArthasCommand(CommandContext context) {
            return context.getCommandCode(this);
        }
    },
    VM_TOOL_SPRING_SELECT_ENV("vmtool -x "
            + ShellScriptVariableEnum.PROPERTY_DEPTH.getCode() + " "
            + "--action getInstances --className org.springframework.core.env.ConfigurableEnvironment "
            + " --express 'instances[0].getProperty(\""
            + ShellScriptVariableEnum.EDITOR_SELECT_TEXT.getCode() + "\") '",
            "vmtool get selected spring property default key: spring.profiles.active",
            "https://github.com/WangJi92/arthas-idea-plugin/issues/100") {
        @Override
        public boolean support(CommandContext context) {
            return true;
        }

        @Override
        public String getScCommand(CommandContext context) {
            return String.join(" ", "sc", "-d", "org.springframework.core.env.ConfigurableEnvironment");
        }

        @Override
        public String getArthasCommand(CommandContext context) {
            return context.getCommandCode(this);
        }
    },
    VM_TOOL_SPRING_ENV("vmtool -x "
            + ShellScriptVariableEnum.PROPERTY_DEPTH.getCode() + " "
            + "--action getInstances --className org.springframework.core.env.ConfigurableEnvironment "
            + " --express '#standardServletEnvironment=instances[0]," + SPRING_ALL_MAP_PROPERTY + "' ",
            "vmtool get all spring property,first keyword has the highest priority",
            "https://github.com/WangJi92/arthas-idea-plugin/issues/100") {
        @Override
        public boolean support(CommandContext context) {
            return true;
        }

        @Override
        public String getScCommand(CommandContext context) {
            return String.join(" ", "sc", "-d", "org.springframework.core.env.ConfigurableEnvironment");
        }

        @Override
        public String getArthasCommand(CommandContext context) {
            return context.getCommandCode(this);
        }
    },
    VM_TOOL_INSTANCE("vmtool -x  1 "
            + "--action getInstances --className "
            + ShellScriptVariableEnum.CLASS_NAME.getCode() + " "
            + " --limit 5 ",
            "vmtool get all instance",
            "https://arthas.aliyun.com/doc/vmtool.html#%E8%8E%B7%E5%8F%96%E5%AF%B9%E8%B1%A1") {
        @Override
        public boolean support(CommandContext context) {
            if (OgnlPsUtils.isAnonymousClass(context.getPsiElement())) {
                return false;
            }
            return OgnlPsUtils.isPsiFieldOrMethodOrClass(context.getPsiElement());
        }

        @Override
        public String getScCommand(CommandContext context) {
            String className = OgnlPsUtils.getCommonOrInnerOrAnonymousClassName(context.getPsiElement());
            return String.join(" ", "sc", "-d", className);
        }

        @Override
        public String getArthasCommand(CommandContext context) {
            return context.getCommandCode(this);
        }
    },

    /**
     * spring get bean
     */
    SPRING_GET_BEAN("ognl -x "
            + ShellScriptVariableEnum.PROPERTY_DEPTH.getCode() + " "
            + "'#springContext=" + ShellScriptVariableEnum.SPRING_CONTEXT.getCode() + ",#springContext.getBean(\"" + ShellScriptVariableEnum.SPRING_BEAN_NAME.getCode() + "\")."
            + ShellScriptVariableEnum.EXECUTE_INFO.getCode() + "' ",
            "invoke static spring bean【手动编辑填写参数】【bean名称可能不正确,可以手动修改】",
            "") {
        //todo link
        @Override
        public boolean support(CommandContext context) {
            //todo 判断是否为spring bean
            if (OgnlPsUtils.isAnonymousClass(context.getPsiElement())) {
                return false;
            }
            // 必须要配置spring static context
            if (!SpringStaticContextUtils.booleanConfigStaticSpringContext(context.getProject())) {
                return false;
            }
            // 构造方法不支持
            if (OgnlPsUtils.isConstructor(context.getPsiElement())) {
                return false;
            }
            // spring bean 的名称
            String springBeanName = OgnlPsUtils.getSpringBeanName(context.getPsiElement());
            if (StringUtils.isBlank(springBeanName) || "errorBeanName".equals(springBeanName)) {
                return false;
            }
            String className = OgnlPsUtils.getCommonOrInnerOrAnonymousClassName(context.getPsiElement());
            // 非 java.**
            if (className.startsWith("java.")) {
                return false;
            }
            if (OgnlPsUtils.psiElementInEnum(context.getPsiElement())) {
                return false;
            }
            return OgnlPsUtils.isNonStaticMethodOrField(context.getPsiElement());
        }

        @Override
        public String getScCommand(CommandContext context) {
            String springContextClassName = SpringStaticContextUtils.getStaticSpringContextClassName(context.getProject());
            return String.join(" ", "sc", "-d", springContextClassName);
        }

        @Override
        public String getArthasCommand(CommandContext context) {
            return context.getCommandCode(this);
        }
    },
    /**
     * spring get bean to set field
     */
    SPRING_GET_BEAN_SET_FIELD("ognl -x "
            + ShellScriptVariableEnum.PROPERTY_DEPTH.getCode() + " "
            + "'#springContext=" + ShellScriptVariableEnum.SPRING_CONTEXT.getCode()
            + ",#springContext.getBean(\"" + ShellScriptVariableEnum.SPRING_BEAN_NAME.getCode() + "\").set" + ShellScriptVariableEnum.CAPITALIZE_FIELD_VALUE.getCode() + "(" + ShellScriptVariableEnum.DEFAULT_FIELD_VALUE.getCode() + ")' ",
            "invoke static spring bean set field method 【需要编辑set方法的值】【bean名称可能不正确,可以手动修改】 ",
            "") {
        //todo help link
        @Override
        public boolean support(CommandContext context) {
            //todo 判断是否为spring bean
            if (OgnlPsUtils.isAnonymousClass(context.getPsiElement())) {
                return false;
            }
            // 必须要配置spring static context
            if (!SpringStaticContextUtils.booleanConfigStaticSpringContext(context.getProject())) {
                return false;
            }
            // 构造方法不支持
            if (OgnlPsUtils.isConstructor(context.getPsiElement())) {
                return false;
            }
            // spring bean 的名称
            String springBeanName = OgnlPsUtils.getSpringBeanName(context.getPsiElement());
            if (StringUtils.isBlank(springBeanName) || "errorBeanName".equals(springBeanName)) {
                return false;
            }
            String className = OgnlPsUtils.getCommonOrInnerOrAnonymousClassName(context.getPsiElement());
            // 非 java.**
            if (className.startsWith("java.")) {
                return false;
            }
            if (!OgnlPsUtils.isNonStaticMethodOrField(context.getPsiElement())) {
                return false;
            }
            if (OgnlPsUtils.psiElementInEnum(context.getPsiElement())) {
                return false;
            }
            // 含有set 字段的方法
            if (context.getPsiElement() instanceof PsiField) {
                PsiField psiField = (PsiField) context.getPsiElement();
                String fieldName = OgnlPsUtils.getFieldName(context.getPsiElement());
                String capitalizeFieldName = StringUtils.capitalize(fieldName);
                PsiClass containingClass = psiField.getContainingClass();
                if (containingClass != null) {
                    for (PsiMethod method : containingClass.getMethods()) {
                        if (method.getName().equals("set" + capitalizeFieldName)) {
                            return true;
                        }
                    }
                }
            }
            return false;
        }

        @Override
        public String getScCommand(CommandContext context) {
            String springContextClassName = SpringStaticContextUtils.getStaticSpringContextClassName(context.getProject());
            return String.join(" ", "sc", "-d", springContextClassName);
        }

        @Override
        public String getArthasCommand(CommandContext context) {
            return context.getCommandCode(this);
        }
    },

    /**
     * trace
     */
    STACK("stack "
            + ShellScriptVariableEnum.CLASS_NAME.getCode() + " "
            + ShellScriptVariableEnum.METHOD_NAME.getCode() + " "
            + ShellScriptVariableEnum.PRINT_CONDITION_RESULT.getCode() + " -n "
            + ShellScriptVariableEnum.INVOKE_COUNT.getCode() + " "
            + ShellScriptVariableEnum.CONDITION_EXPRESS_DEFAULT.getCode(),
            "display the stack trace for the specified class and method",
            "https://arthas.aliyun.com/doc/stack.html") {
        @Override
        public boolean support(CommandContext context) {

            return OgnlPsUtils.isPsiFieldOrMethodOrClass(context.getPsiElement());
        }

        @Override
        public String getScCommand(CommandContext context) {
            return null;
        }

        @Override
        public String getArthasCommand(CommandContext context) {
            return context.getCommandCode(this);
        }
    },
    /**
     * monitor
     */
    MONITOR("monitor "
            + ShellScriptVariableEnum.CLASS_NAME.getCode() + " "
            + ShellScriptVariableEnum.METHOD_NAME.getCode() + " "
            + ShellScriptVariableEnum.PRINT_CONDITION_RESULT.getCode() + " -n "
            + ShellScriptVariableEnum.INVOKE_MONITOR_COUNT.getCode() + "  --cycle "
            + ShellScriptVariableEnum.INVOKE_MONITOR_INTERVAL.getCode() + " "
            + ShellScriptVariableEnum.CONDITION_EXPRESS_DEFAULT.getCode(),
            "monitor method execution statistics ",
            "https://arthas.aliyun.com/doc/monitor.html") {
        @Override
        public boolean support(CommandContext context) {
            return OgnlPsUtils.isPsiFieldOrMethodOrClass(context.getPsiElement());
        }

        @Override
        public String getScCommand(CommandContext context) {
            return null;
        }

        @Override
        public String getArthasCommand(CommandContext context) {
            return context.getCommandCode(this);
        }
    },
    /**
     * jad
     */
    JAD("jad --source-only "
            + ShellScriptVariableEnum.CLASS_NAME.getCode() + " "
            + ShellScriptVariableEnum.METHOD_NAME_NOT_STAR.getCode() + " ",
            "decompile class",
            "https://arthas.aliyun.com/doc/jad.html") {
        @Override
        public boolean support(CommandContext context) {
            if (OgnlPsUtils.isAnonymousClass(context.getPsiElement())) {
                return false;
            }
            if (OgnlPsUtils.getCommonOrInnerOrAnonymousClassName(context.getPsiElement()).startsWith("java.")) {
                return false;
            }
            return OgnlPsUtils.isPsiFieldOrMethodOrClass(context.getPsiElement());
        }

        @Override
        public String getScCommand(CommandContext context) {
            String className = OgnlPsUtils.getCommonOrInnerOrAnonymousClassName(context.getPsiElement());
            return String.join(" ", "sc", "-d", className);
        }

        @Override
        public String getArthasCommand(CommandContext context) {
            return context.getCommandCode(this);
        }
    },
    /**
     * jad
     */
    JAD_JAVA("jad --source-only "
            + ShellScriptVariableEnum.CLASS_NAME.getCode() + " "
            + ShellScriptVariableEnum.METHOD_NAME_NOT_STAR.getCode(),
            "decompile class",
            "https://arthas.aliyun.com/doc/jad.html") {
        @Override
        public boolean support(CommandContext context) {
            if (!OgnlPsUtils.getCommonOrInnerOrAnonymousClassName(context.getPsiElement()).startsWith("java.")) {
                return false;
            }
            if (OgnlPsUtils.isAnonymousClass(context.getPsiElement())) {
                return false;
            }
            return OgnlPsUtils.isPsiFieldOrMethodOrClass(context.getPsiElement());
        }

        @Override
        public String getScCommand(CommandContext context) {
            return null;
        }

        @Override
        public String getArthasCommand(CommandContext context) {
            return context.getCommandCode(this);
        }
    },
    /**
     * sc
     */
    SC("sc -d "
            + ShellScriptVariableEnum.CLASS_NAME.getCode(),
            "search all the classes loaded by jvm",
            "https://arthas.aliyun.com/doc/sc.html") {
        @Override
        public boolean support(CommandContext context) {
            return OgnlPsUtils.isPsiFieldOrMethodOrClass(context.getPsiElement());
        }

        @Override
        public String getScCommand(CommandContext context) {
            return null;
        }

        @Override
        public String getArthasCommand(CommandContext context) {
            return context.getCommandCode(this);
        }
    },
    /**
     * sc
     */
    SM("sm -d "
            + ShellScriptVariableEnum.CLASS_NAME.getCode() + " "
            + ShellScriptVariableEnum.METHOD_NAME.getCode(),
            "search the method of classes loaded by jvm",
            "https://arthas.aliyun.com/doc/sc.html") {
        @Override
        public boolean support(CommandContext context) {
            return OgnlPsUtils.isPsiFieldOrMethodOrClass(context.getPsiElement());
        }

        @Override
        public String getScCommand(CommandContext context) {
            return null;
        }

        @Override
        public String getArthasCommand(CommandContext context) {
            return context.getCommandCode(this);
        }
    },
    /**
     * ognl reflect to modify static field 注意需要被修改的字段的值
     */
    OGNL_TO_MODIFY_NO_FINAL_STATIC_FIELD("ognl -x "
            + ShellScriptVariableEnum.PROPERTY_DEPTH.getCode() +
            " '#field=@"
            + ShellScriptVariableEnum.CLASS_NAME.getCode() + "@class.getDeclaredField(\"" + ShellScriptVariableEnum.FIELD_NAME.getCode() + "\"),#field.setAccessible(true),#field.set(null,"
            + ShellScriptVariableEnum.DEFAULT_FIELD_VALUE.getCode() + ")' ",
            "ognl reflect to modify static  not final field 注意需要被修改的字段的值",
            "https://github.com/WangJi92/arthas-idea-plugin/issues/1") {
        @Override
        public boolean support(CommandContext context) {
            if (OgnlPsUtils.isAnonymousClass(context.getPsiElement())) {
                return false;
            }
            return OgnlPsUtils.isStaticField(context.getPsiElement()) && !OgnlPsUtils.isFinalField(context.getPsiElement());
        }

        @Override
        public String getScCommand(CommandContext context) {
            String className = OgnlPsUtils.getCommonOrInnerOrAnonymousClassName(context.getPsiElement());
            return String.join(" ", "sc", "-d", className);
        }

        @Override
        public String getArthasCommand(CommandContext context) {
            return context.getCommandCode(this);
        }
    },
    /**
     * ognl reflect to modify static final field 注意需要被修改的字段的值
     */
    OGNL_TO_MODIFY_FINAL_STATIC_FIELD("ognl -x "
            + ShellScriptVariableEnum.PROPERTY_DEPTH.getCode() +
            " '#field=@"
            + ShellScriptVariableEnum.CLASS_NAME.getCode() + "@class.getDeclaredField(\"" + ShellScriptVariableEnum.FIELD_NAME.getCode() + "\"),#modifiers=#field.getClass().getDeclaredField(\"modifiers\"),#modifiers.setAccessible(true),#modifiers.setInt(#field,#field.getModifiers() & ~@java.lang.reflect.Modifier@FINAL),#field.setAccessible(true),#field.set(null,"
            + ShellScriptVariableEnum.DEFAULT_FIELD_VALUE.getCode() + ")' ",
            "ognl reflect to modify static  final field 注意需要被修改的字段的值",
            "https://github.com/WangJi92/arthas-idea-plugin/issues/1") {
        @Override
        public boolean support(CommandContext context) {
            if (OgnlPsUtils.isAnonymousClass(context.getPsiElement())) {
                return false;
            }
            return OgnlPsUtils.isStaticField(context.getPsiElement()) && OgnlPsUtils.isFinalField(context.getPsiElement());
        }

        @Override
        public String getScCommand(CommandContext context) {
            String className = OgnlPsUtils.getCommonOrInnerOrAnonymousClassName(context.getPsiElement());
            return String.join(" ", "sc", "-d", className);
        }

        @Override
        public String getArthasCommand(CommandContext context) {
            return context.getCommandCode(this);
        }
    },

    /**
     * watch * to execute static method
     */
    WATCH_EXECUTE_STATIC_METHOD("watch "
            + ShellScriptVariableEnum.CLASS_NAME.getCode() + " * "
            + " '{params,returnObj,throwExp,@" + ShellScriptVariableEnum.CLASS_NAME.getCode() + "@" + ShellScriptVariableEnum.EXECUTE_INFO.getCode() + "}' "
            + ShellScriptVariableEnum.PRINT_CONDITION_RESULT.getCode() + " -n "
            + ShellScriptVariableEnum.INVOKE_COUNT.getCode() + " "
            + " -x "
            + ShellScriptVariableEnum.PROPERTY_DEPTH.getCode() + " "
            + ShellScriptVariableEnum.CONDITION_EXPRESS_DEFAULT.getCode(),
            "watch * to execute static method 注意需要编辑执行静态方法的参数",
            "https://arthas.aliyun.com/doc/watch.html#%E8%8E%B7%E5%8F%96%E7%B1%BB%E7%9A%84%E9%9D%99%E6%80%81%E5%AD%97%E6%AE%B5%E3%80%81%E8%B0%83%E7%94%A8%E7%B1%BB%E7%9A%84%E9%9D%99%E6%80%81%E5%87%BD%E6%95%B0%E7%9A%84%E4%BE%8B%E5%AD%90") {
        @Override
        public boolean support(CommandContext context) {
            if (OgnlPsUtils.isAnonymousClass(context.getPsiElement())) {
                return false;
            }
            return OgnlPsUtils.isStaticMethod(context.getPsiElement());
        }

        @Override
        public String getScCommand(CommandContext context) {
            return null;
        }

        @Override
        public String getArthasCommand(CommandContext context) {
            return context.getCommandCode(this);
        }
    },

    /**
     * watch 执行 非静态方法
     */
    WATCH_EXECUTE_NO_STATIC_METHOD("watch "
            + ShellScriptVariableEnum.CLASS_NAME.getCode() + " * "
            + " '{params,returnObj,throwExp,target." + ShellScriptVariableEnum.EXECUTE_INFO.getCode() + "}'"
            + ShellScriptVariableEnum.PRINT_CONDITION_RESULT.getCode() + " -n "
            + ShellScriptVariableEnum.INVOKE_COUNT.getCode() + " "
            + " -x "
            + ShellScriptVariableEnum.PROPERTY_DEPTH.getCode() + " "
            + "'method.initMethod(),method.constructor!=null || !@java.lang.reflect.Modifier@isStatic(method.method.getModifiers())'",
            "watch * to execute method 注意需要编执行方法的参数",
            "https://arthas.aliyun.com/doc/watch.html#%E8%8E%B7%E5%8F%96%E7%B1%BB%E7%9A%84%E9%9D%99%E6%80%81%E5%AD%97%E6%AE%B5%E3%80%81%E8%B0%83%E7%94%A8%E7%B1%BB%E7%9A%84%E9%9D%99%E6%80%81%E5%87%BD%E6%95%B0%E7%9A%84%E4%BE%8B%E5%AD%90") {
        @Override
        public boolean support(CommandContext context) {
            if (OgnlPsUtils.isConstructor(context.getPsiElement())) {
                return false;
            }

            return OgnlPsUtils.isNonStaticMethod(context.getPsiElement());
        }

        @Override
        public String getScCommand(CommandContext context) {
            return null;
        }

        @Override
        public String getArthasCommand(CommandContext context) {
            return context.getCommandCode(this);
        }
    },
    /**
     * logger
     */
    LOGGER("logger --name "
            + ShellScriptVariableEnum.CLASS_NAME.getCode() + " "
            + "--level debug ",
            "--level debug 可以编辑修改为 info、error",
            "https://arthas.aliyun.com/doc/logger.html#%E6%9B%B4%E6%96%B0-logger-level") {
        @Override
        public boolean support(CommandContext context) {
            if (OgnlPsUtils.isAnonymousClass(context.getPsiElement())) {
                return false;
            }
            return OgnlPsUtils.isPsiFieldOrMethodOrClass(context.getPsiElement());
        }

        @Override
        public String getScCommand(CommandContext context) {
            String className = OgnlPsUtils.getCommonOrInnerOrAnonymousClassName(context.getPsiElement());
            return String.join(" ", "logger", "--name", className);
        }

        @Override
        public String getArthasCommand(CommandContext context) {
            return context.getCommandCode(this);
        }
    },


    /**
     * dump
     */
    DUMP("dump "
            + ShellScriptVariableEnum.CLASS_NAME.getCode()
            + " -d /tmp/output ",
            "dump class byte array from jvm",
            "https://arthas.aliyun.com/doc/dump.html") {
        @Override
        public boolean support(CommandContext context) {
            if (OgnlPsUtils.isAnonymousClass(context.getPsiElement())) {
                return false;
            }
            return OgnlPsUtils.isPsiFieldOrMethodOrClass(context.getPsiElement());
        }

        @Override
        public String getScCommand(CommandContext context) {
            String className = OgnlPsUtils.getCommonOrInnerOrAnonymousClassName(context.getPsiElement());
            return String.join(" ", "sc", "-d", className);
        }

        @Override
        public String getArthasCommand(CommandContext context) {
            return context.getCommandCode(this);
        }
    };

    /**
     * @param code
     * @param msg
     */
    ShellScriptCommandEnum(String code, String msg,String refLink) {
        this.code = code;
        this.msg = msg;
        this.refLink = refLink;
    }

    /**
     * code 脚本
     */
    private String code;

    /**
     * 提示信息
     */
    private String msg;

    private String refLink;


    /**
     * 是否支持
     *
     * @param context
     * @return
     */
    public abstract boolean support(CommandContext context);

    /**
     * 获取sc-d classloader 命令的信息,return null is not need
     *
     * @param context
     * @return
     */
    public abstract String getScCommand(CommandContext context);

    /**
     * 获取arthas的命令
     *
     * @param context
     * @return
     */
    public abstract String getArthasCommand(CommandContext context);


    @Override
    public String getEnumMsg() {
        return msg;
    }

    @Override
    public String getCode() {
        return code;
    }

    public String getRefLink() {
        return refLink;
    }
}
