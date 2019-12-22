package com.command.idea.plugin.utils;

import com.intellij.psi.PsiType;

/**
 * 获取Java类型构造ognl的默认值信息
 * @author jet
 * @date 22-12-2019
 */
public class OgnlPsUtils {

    /**
     * 构造ognl 的默认值
     * @param psiType
     * @return
     */
    public static  String getDefaultString(PsiType psiType) {
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

}
