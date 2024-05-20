package com.github.idea.json.parser.model;

import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiType;
import lombok.Getter;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * PsiVariable 包含 PsiField & PsiLocalVariable &  PsiParameter & PsiReceiverParameter & PsiVariableEx
 *
 * @author wangji
 * @date 2024/5/19 19:49
 */
@Getter
public class JPsiType extends JPsiTypeBase {

    /**
     * 当前的类型
     */
    protected PsiType psiType;

    /**
     * 当前类+所有的父类的名字集合
     */
    public Set<String> parentPlusCurrentQualifiedNames;


    public JPsiType(PsiType psiType, Map<String, PsiType> psiClassGenerics, List<String> ignoreProperties, int recursionLevel) {
        this.psiType = psiType;

        // 获取父类的信息
        Set<String> clazzNames = Arrays.stream(psiType.getSuperTypes()).map(PsiType::getCanonicalText).collect(Collectors.toSet());
        clazzNames.add(psiType.getCanonicalText());
        this.parentPlusCurrentQualifiedNames = clazzNames;
        if (ignoreProperties != null) {
            this.ignoreProperties = ignoreProperties;
        }

        if (psiClassGenerics != null) {
            this.psiTypeGenerics = psiClassGenerics;
        }
        this.recursionLevel = recursionLevel;
    }


    /**
     * 根据当前的 类型深度递归一个 变量：然后根据基本类型、泛型、类进行拆分
     *
     * @param deepType
     * @param psiClassGenerics
     * @return
     */
    public JPsiType deepVariable(PsiType deepType, Map<String, PsiType> psiClassGenerics) {
        return new JPsiType(deepType, psiClassGenerics, this.getIgnoreProperties(), ++this.recursionLevel);
    }

    /**
     * 当确认当前是clazz 的时候，进行递归处理
     *
     * @param psiClass
     * @param psiClassGenerics
     * @return
     */
    public JPsiTypeClazz deepClass(PsiClass psiClass, Map<String, PsiType> psiClassGenerics) {
        return new JPsiTypeClazz(psiClass, psiClassGenerics, this.ignoreProperties, ++this.recursionLevel);
    }

    /**
     * 同上只是没有泛型
     *
     * @param psiClass
     * @return
     */
    public JPsiTypeClazz deepClass(PsiClass psiClass) {
        return deepClass(psiClass, null);
    }
}
