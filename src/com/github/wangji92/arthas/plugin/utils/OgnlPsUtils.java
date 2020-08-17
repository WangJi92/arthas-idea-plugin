package com.github.wangji92.arthas.plugin.utils;

import com.github.wangji92.arthas.plugin.constants.ArthasCommandConstants;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.intellij.psi.*;
import org.apache.commons.io.FilenameUtils;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * 获取Java类型构造ognl的默认值信息
 *
 * @author 汪小哥
 * @date 22-12-2019
 */
public class OgnlPsUtils {

    /**
     * 获取内部类、匿名类的class的 ognl 名称
     * <p>
     * 内部类  OuterClass$InnerClass
     * 匿名类  Outer*$*
     *
     * @param psiField
     * @return
     */
    public static String getCommonOrInnerOrAnonymousClassName(@NotNull PsiField psiField) {
        return OgnlPsUtils.getCommonOrInnerOrAnonymousClassName(Objects.requireNonNull(psiField.getContainingClass()));
    }

    /**
     * 获取内部类、匿名类的class的 ognl 名称
     * 内部类  OuterClass$InnerClass
     * 匿名类  Outer*$*
     *
     * @param psiMethod
     * @return
     */
    public static String getCommonOrInnerOrAnonymousClassName(@NotNull PsiMethod psiMethod) {
        return OgnlPsUtils.getCommonOrInnerOrAnonymousClassName(Objects.requireNonNull(psiMethod.getContainingClass()));
    }

    /**
     * 获取内部类、匿名类的class的 ognl 名称
     * 内部类  OuterClass$InnerClass
     * 匿名类  Outer*$*
     *
     * @param currentContainingClass
     * @return
     */
    public static String getCommonOrInnerOrAnonymousClassName(@NotNull PsiClass currentContainingClass) {
        String qualifiedClassName = "";
        if (currentContainingClass instanceof PsiAnonymousClass) {
            //匿名类 eg com.wangji92.arthas.plugin.demo.controller.OuterClass$InnerClass$1 or com.wangji92.arthas.plugin.demo.controller.OuterClass$InnerClass$InnerInnerClass$1
            PsiAnonymousClass anonymousClass = (PsiAnonymousClass) currentContainingClass;

            //region 这样处理直接是错误的
            // anonymousClass.getQualifiedName() return null
            //endregion


            //region 这种有点问题； 匿名类 区分度不够 com.wangji92.arthas.plugin.demo.controller.OuterClass$InnerClass$1
            //而不是 java.lang.Runnable.run
            //            PsiClass[] interfaces = anonymousClass.getInterfaces();
//            qualifiedClassName = interfaces[0].getQualifiedName();
            //endregion

            PsiJavaFile javaFile = (PsiJavaFile) anonymousClass.getContainingFile();
            //这里获取的是文件名哦
            String outClassName = FilenameUtils.getBaseName(javaFile.getName());
            String packageName = javaFile.getPackageName();
            // 匿名内部类 这里获取当前方法对应的方法有点问题.. 先通过匹配处理
            // 匿名内部类 获取到的不是非常准确
            return packageName + "." + outClassName + "*" + ArthasCommandConstants.OGNL_INNER_CLASS_SEPARATOR + "*";
        }
        PsiClass nextContainingClass = currentContainingClass.getContainingClass();
        if (nextContainingClass == null) {
            // 如果当前不是内部类  nextContainingClass= null;
            qualifiedClassName = currentContainingClass.getQualifiedName();
            return qualifiedClassName;
        }

        // 处理内部类的逻辑 Outer$Inner  eg trace Outer$Inner  method  https://github.com/alibaba/arthas/issues/71
        List<String> ognlQualifiedClassNameArray = Lists.newArrayList();
        ognlQualifiedClassNameArray.add(currentContainingClass.getNameIdentifier().getText());
        currentContainingClass = currentContainingClass.getContainingClass();
        while (currentContainingClass != null) {
            ognlQualifiedClassNameArray.add(ArthasCommandConstants.OGNL_INNER_CLASS_SEPARATOR);
            String name = currentContainingClass.getNameIdentifier().getText();
            nextContainingClass = currentContainingClass.getContainingClass();
            if (nextContainingClass == null) {
                ognlQualifiedClassNameArray.add(currentContainingClass.getQualifiedName());
            } else {
                ognlQualifiedClassNameArray.add(name);
            }
            currentContainingClass = nextContainingClass;
        }
        Collections.reverse(ognlQualifiedClassNameArray);
        qualifiedClassName = String.join("", ognlQualifiedClassNameArray);
        return qualifiedClassName;

    }

    /**
     * 构造方法的参数信息  complexParameterCall(#{" ":" "}) 后面这个部分需要构造
     *
     * @param psiMethod
     * @return
     */
    public static String getMethodParameterDefault(@NotNull PsiMethod psiMethod) {
        // Experimental API method JvmField.getName() is invoked in Action.arthas.ArthasOgnlStaticCommandAction.actionPerformed().
        // This method can be changed in a future release leading to incompatibilities
        String methodName = psiMethod.getNameIdentifier().getText();
        StringBuilder builder = new StringBuilder(methodName).append("(");
        PsiParameter[] parameters = psiMethod.getParameterList().getParameters();
        if (parameters.length > 0) {
            int index = 0;
            for (PsiParameter parameter : parameters) {
                String defaultParamValue = OgnlPsUtils.getDefaultString(parameter.getType());
                builder.append(defaultParamValue);
                if (!(index == parameters.length - 1)) {
                    builder.append(",");
                }
                index++;
            }
        }
        builder.append(")");
        return builder.toString();
    }

    /**
     * 构造ognl 的默认值
     *
     * @param psiType
     * @return
     */
    public static String getDefaultString(PsiType psiType) {
        String result = " ";
        String canonicalText = psiType.getCanonicalText();

        //基本类型  boolean
        if (PsiType.BOOLEAN.equals(psiType) || canonicalText.equals("java.lang.Boolean")) {
            result = "true";
            return result;
        }

        //基本类型  String
        if (canonicalText.endsWith("java.lang.String")) {
            result = "\" \"";
            return result;
        }

        if (PsiType.LONG.equals(psiType) || "java.lang.Long".equals(canonicalText)) {
            result = "0L";
            return result;
        }

        if (PsiType.DOUBLE.equals(psiType) || "java.lang.Double".equals(canonicalText)) {
            result = "0D";
            return result;
        }

        if (PsiType.FLOAT.equals(psiType) || "java.lang.Float".equals(canonicalText)) {
            result = "0F";
            return result;
        }
        //Class xx 特殊class 字段的判断
        //java.lang.Class
        if ("java.lang.Class".equals(canonicalText)) {
            result = "@java.lang.Object@class";
            return result;
        }
        //Class<XXX> x
        //java.lang.Class<com.wangji92.arthas.plugin.demo.controller.user>
        if (canonicalText.startsWith("java.lang.Class")) {
            result = "@" + canonicalText.substring(canonicalText.indexOf("<") + 1, canonicalText.length() - 1) + "@class";
            return result;
        }

        //基本类型  数字
        if (PsiType.INT.equals(psiType) || canonicalText.equals("java.lang.Integer")
                ||
                PsiType.BYTE.equals(psiType) || canonicalText.equals("java.lang.Byte")
                ||
                PsiType.SHORT.equals(psiType) || canonicalText.equals("java.lang.Short")) {
            result = "0";
            return result;
        }

        //常见的List 和Map
        if (canonicalText.startsWith("java.util.")) {
            if (canonicalText.contains("Map")) {
                result = "#{\" \":\" \"}";
                return result;
            }
            if (canonicalText.contains("List")) {
                result = "{}";
                return result;
            }
        }

        //原生的数组
        if (canonicalText.contains("[]")) {
            result = "new " + canonicalText + "{}";
            return result;
        }

        //不管他的构造函数了，太麻烦了
        result = "new " + canonicalText + "()";
        return result;

    }

    /**
     * 获取Bean的名称
     *
     * @param psiClass
     * @return
     */
    public static String getClassBeanName(PsiClass psiClass) {
        if (psiClass == null) {
            return "errorBeanName";
        }
        PsiModifierList psiModifierList = psiClass.getModifierList();
        if (psiModifierList == null) {
            return "errorBeanName";
        }
        PsiAnnotation[] annotations = psiModifierList.getAnnotations();
        String beanName = "";
        if (annotations.length > 0) {
            for (PsiAnnotation annotation : annotations) {
                beanName = getAttributeFromAnnotation(annotation, Sets.newHashSet("org.springframework.stereotype.Service", "org.springframework.stereotype.Controller", "org.springframework.stereotype.Repository", "org.springframework.web.bind.annotation.RestController"), "value");
                if (StringUtils.isNotBlank(beanName)) {
                    break;
                }
            }
        }
        //注解上没有获取值，使用默认的名称首字母小写
        if (StringUtils.isBlank(beanName)) {
            beanName = StringUtils.toLowerFristChar(psiClass.getName());
        }
        return beanName;

    }

    /**
     * 获取注解的值得信息
     *
     * @param annotation
     * @param annotationTypes
     * @param attribute
     * @return
     */
    private static String getAttributeFromAnnotation(
            PsiAnnotation annotation, Set<String> annotationTypes, final String attribute) {

        String annotationQualifiedName = annotation.getQualifiedName();
        if (annotationQualifiedName == null) {
            return "";
        }
        if (annotationTypes.contains(annotationQualifiedName)) {
            PsiAnnotationMemberValue annotationMemberValue = annotation.findAttributeValue(attribute);
            if (annotationMemberValue == null) {
                return "";
            }

            String httpMethodWithQuotes = annotationMemberValue.getText();
            return httpMethodWithQuotes.substring(1, httpMethodWithQuotes.length() - 1);
        }
        return "";
    }

}
