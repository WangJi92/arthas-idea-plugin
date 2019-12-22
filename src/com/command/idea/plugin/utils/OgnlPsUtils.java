package com.command.idea.plugin.utils;

import com.intellij.psi.*;
import org.assertj.core.util.Sets;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

/**
 * 获取Java类型构造ognl的默认值信息
 *
 * @author jet
 * @date 22-12-2019
 */
public class OgnlPsUtils {

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

        //基本类型  数字
        if (PsiType.LONG.equals(psiType) || canonicalText.equals("java.lang.Long")
                ||
                PsiType.INT.equals(psiType) || canonicalText.equals("java.lang.Integer")
                ||
                PsiType.DOUBLE.equals(psiType) || canonicalText.equals("java.lang.Double")
                ||
                PsiType.FLOAT.equals(psiType) || canonicalText.equals("java.lang.Float")
                ||
                PsiType.BYTE.equals(psiType) || canonicalText.equals("java.lang.Byte")
                ||
                PsiType.SHORT.equals(psiType) || canonicalText.equals("java.lang.Short")
        ) {
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
    public static String getClassBeanName(@NotNull PsiClass psiClass) {
        PsiModifierList psiModifierList = psiClass.getModifierList();
        PsiAnnotation[] annotations = psiModifierList.getAnnotations();
        String beanName = "";
        if (annotations.length > 0) {
            for (PsiAnnotation annotation : annotations) {
                beanName = getAttributeFromAnnotation(annotation, Sets.newLinkedHashSet("org.springframework.stereotype.Service", "org.springframework.stereotype.Controller", "org.springframework.stereotype.Repository", "org.springframework.web.bind.annotation.RestController"), "value");
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
