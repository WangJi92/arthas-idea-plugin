package com.github.idea.json.parser.toolkit;

import com.google.common.collect.Lists;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.psi.search.GlobalSearchScope;
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
     * psiType的名称会存在 泛型的信息 这种构建ognl 不准确
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
     * 获取clazz 内名称，处理了内部类的情况 这种构建ognl 更加准确
     * @param classType
     * @return
     */
    public static String getPsiTypeQualifiedNameClazzName(PsiClassType classType) {
        // clazz 直接返回这个类的字符串
        PsiClass currentContainingClass = classType.resolve();
        assert currentContainingClass != null;
        return getPsiTypeQualifiedNameClazzName(currentContainingClass);
    }

    /**
     * 获取clazz 内名称，处理了内部类的情况 这种构建ognl 更加准确
     *
     * @param currentContainingClass
     * @return
     */
    public static String getPsiTypeQualifiedNameClazzName(PsiClass currentContainingClass){
        // clazz 直接返回这个类的字符串
        assert currentContainingClass != null;
        PsiClass nextContainingClass = currentContainingClass.getContainingClass();
        if (nextContainingClass == null) {
            // 不是内部类
            return currentContainingClass.getQualifiedName();
        }
        // 内部类的处理 OutClass$InnerClass
        List<String> qualifiedClassNameArray = Lists.newArrayList();
        qualifiedClassNameArray.add(Objects.requireNonNull(currentContainingClass.getNameIdentifier()).getText());
        currentContainingClass = currentContainingClass.getContainingClass();
        while (currentContainingClass != null) {
            qualifiedClassNameArray.add("$");
            String name = Objects.requireNonNull(currentContainingClass.getNameIdentifier()).getText();
            nextContainingClass = currentContainingClass.getContainingClass();
            if (nextContainingClass == null) {
                qualifiedClassNameArray.add(currentContainingClass.getQualifiedName());
            } else {
                qualifiedClassNameArray.add(name);
            }
            currentContainingClass = nextContainingClass;
        }
        Collections.reverse(qualifiedClassNameArray);
        return String.join("", qualifiedClassNameArray);
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

    /**
     * 查询当前工程是否有这个类
     *
     * @param clazz
     * @param project
     * @return
     */
    public static boolean findClass(String clazz, Project project) {
        try {
            PsiClass psiClass = JavaPsiFacade.getInstance(project).findClass(clazz, GlobalSearchScope.allScope(project));
            return psiClass != null;
        } catch (Exception e) {
            return false;
        }
    }



}
