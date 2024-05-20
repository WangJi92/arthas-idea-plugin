package com.github.idea.json.parser.model;

import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiField;
import com.intellij.psi.PsiType;
import lombok.Getter;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * PsiClass 类型的信息
 *
 * @author wangji
 * @date 2024/5/19 22:10
 */
@Getter
@Deprecated
public class JPsiTypeClazz extends JPsiTypeBase {

    private final PsiClass psiClass;

    /**
     * 当前类+所有的父类的名字集合
     */
    public Set<String> parentPlusCurrentQualifiedNames;


    public JPsiTypeClazz(PsiClass psiClass, Map<String, PsiType> psiClassGenerics, List<String> ignoreProperties, int recursionLevel) {
        this.psiClass = psiClass;
        // 获取父类的信息
        Set<String> clazzNames = Arrays.stream(psiClass.getSuperTypes()).map(PsiType::getCanonicalText).collect(Collectors.toSet());
        clazzNames.add(psiClass.getQualifiedName());
        this.parentPlusCurrentQualifiedNames = clazzNames;
        if (ignoreProperties != null) {
            this.ignoreProperties = ignoreProperties;
        }
        if (psiClassGenerics != null) {
            this.psiTypeGenerics = psiClassGenerics;
        }
        this.recursionLevel = recursionLevel;
    }


    public JPsiType toJPsiVariable(PsiField psiField) {
        return new JPsiType(psiField.getType(), this.getPsiTypeGenerics(), this.getIgnoreProperties(), this.getRecursionLevel());

    }

}
