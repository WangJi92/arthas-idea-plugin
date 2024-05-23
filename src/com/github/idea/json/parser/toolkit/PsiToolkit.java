package com.github.idea.json.parser.toolkit;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.psi.*;
import com.intellij.psi.util.PsiUtil;
import org.apache.commons.lang3.tuple.Pair;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author wangji
 * @date 2024/5/21 02:02
 */
public class PsiToolkit {

    private static final Logger LOG = Logger.getInstance(PsiToolkit.class);

    /**
     * 查找父类的信息
     *
     * @param psiType
     * @return
     */
    private static void doFindParentPlusCurrentQualifiedName(PsiType psiType, Set<String> supperClazzNames) {
        try {
            String qualifiedName = getPsiTypeSimpleName(psiType);
            supperClazzNames.add(qualifiedName);
            PsiType[] superTypes = psiType.getSuperTypes();
            for (PsiType superType : superTypes) {
                try {
                    doFindParentPlusCurrentQualifiedName(superType, supperClazzNames);
                } catch (Exception e) {
                    LOG.warn("doFindParentClassName", e);
                }
            }
        } catch (Exception e) {
            LOG.warn("doFindParentClassName2", e);
        }
    }

    /**
     * psiType的名称会存在 泛型的信息
     * @param psiType
     * @return
     */
    public static  String getPsiTypeSimpleName(PsiType psiType){
        String qualifiedName = psiType.getCanonicalText();
        //携带有泛型的这里有点问题.. 处理一下
        if (qualifiedName.indexOf("<") > 0) {
            qualifiedName = qualifiedName.substring(0, qualifiedName.indexOf("<"));
        }
        return qualifiedName;
    }

    /**
     * 查找所有的父类的信息
     *
     * @param psiType
     * @return
     */
    public static Set<String> findParentPlusCurrentQualifiedName(PsiType psiType) {
        HashSet<String> supperClazzNames = new HashSet<>(16);
        doFindParentPlusCurrentQualifiedName(psiType, supperClazzNames);
        return supperClazzNames;
    }


    /**
     * 获取 type的泛型信息
     *
     * @param type
     * @return
     */
    public static Map<String, PsiType> getPsiClassGenerics(PsiType type) {
        PsiClass psiClass = PsiUtil.resolveClassInClassTypeOnly(type);
        if (psiClass != null) {
            return Arrays.stream(psiClass.getTypeParameters())
                    .map(p -> Pair.of(p, PsiUtil.substituteTypeParameter(type, psiClass, p.getIndex(), false)))
                    .filter(p -> p.getValue() != null)
                    .collect(Collectors.toMap(p -> p.getKey().getName(), Pair::getValue));
        }
        return Map.of();
    }

    /**
     * PsiClass 转 psiType
     * @param psiClass
     * @return
     */
    public static PsiType getPsiTypeByPisClazz(PsiClass psiClass) {
        JavaPsiFacade facade = JavaPsiFacade.getInstance(psiClass.getProject());
        PsiElementFactory factory = facade.getElementFactory();
        return factory.createType(psiClass);
    }

    /**
     * 基本信息
     *
     * @param psiType
     * @return
     */
    public static Object getPsiClassBasicTypeDefaultValue(PsiType psiType) {
        String canonicalText = psiType.getCanonicalText();
        return switch (canonicalText) {
            case "java.lang.Boolean","boolean" -> true;
            case "java.lang.String" -> "\"_AR_\"";
            case "java.lang.Integer", "java.lang.Byte", "java.lang.Short","short", "int", "byte" -> 0;
            case "java.lang.Long","long" -> 0L;
            case "java.lang.Double","double" -> 0D;
            case "java.lang.Float" -> 0F;
            case "char" -> 'c';
            default -> null;
        };
    }

    public static String getPsiClassBasicTypeDefaultStringValue(PsiType psiType) {
        String canonicalText = psiType.getCanonicalText();
        return switch (canonicalText) {
            case "java.lang.Boolean","boolean" -> "true";
            case "java.lang.String" -> "\"_AR_\"";
            case "java.lang.Integer", "java.lang.Byte", "java.lang.Short","short", "int", "byte" -> "0";
            case "java.lang.Long","long" -> "0L";
            case "java.lang.Double","double" -> "0D";
            case "java.lang.Float" -> "0F";
            case "char" -> "\'c\'";
            default -> null;
        };
    }

}
