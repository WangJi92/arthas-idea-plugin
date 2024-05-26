package com.github.idea.json.parser.toolkit;

import com.google.common.collect.Lists;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.util.PsiUtil;

import java.util.*;

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
            Map<String,PsiType> psiClazzGenerics = new HashMap<>();
            for (PsiTypeParameter typeParameter : psiClass.getTypeParameters()) {
                PsiType substituteTypeParameter = PsiUtil.substituteTypeParameter(type, psiClass, typeParameter.getIndex(), false);
                if (substituteTypeParameter != null) {
                    psiClazzGenerics.put(typeParameter.getName(), substituteTypeParameter);
                } else if (typeParameter.getExtendsListTypes().length > 0) {
                    // TestGeneratesClazz<T extends String,B extends User> 获取泛型参数 T 为String B 为User
                    psiClazzGenerics.put(typeParameter.getName(), typeParameter.getExtendsListTypes()[0]);
                }
            }
            return psiClazzGenerics;
        }
        return Map.of();
    }

    /**
     * List<?>  Class<? extends LanguageDriver>
     * 获取内部的泛型信息
     *
     * @param parameter
     * @return
     */
    public static PsiType getPsiTypeGenericsType(PsiType parameter){
        if(parameter instanceof PsiClassType psiClassType){
            return  psiClassType;
        }else if (parameter instanceof PsiWildcardType wildcardType) {
            if (wildcardType.isExtends()) {
                // 获取上界限定的类型 上界限定通配符 (? extends T): 指定了类型的上界，表示该类型可以是 T 或 T 的子类。
               return wildcardType.getExtendsBound();

            } else if (wildcardType.isSuper()) {
                // 获取下界限定的类型 下界限定通配符 (? super T): 指定了类型的下界，表示该类型可以是 T 或 T 的超类。
                return wildcardType.getSuperBound();
            }else{
                return null;
            }
        }
        return parameter;
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

    /**
     * 是否有无参构造函数
     *
     * @param psiClass
     * @return
     */
    public static boolean hasNoArgConstructor(PsiClass psiClass) {
        // 查找类的所有构造函数
        PsiMethod[] constructors = psiClass.getConstructors();
        // 检查是否有无参构造函数
        boolean hasNoArgConstructor = false;
        for (PsiMethod constructor : constructors) {
            // 检查构造函数是否无参（参数数量为0）且不是私有（private）
            if (constructor.getParameterList().getParametersCount() == 0 &&
                    !constructor.hasModifierProperty(PsiModifier.PRIVATE)) {
                hasNoArgConstructor = true;
                break;
            }
        }
        return hasNoArgConstructor;
    }

}
