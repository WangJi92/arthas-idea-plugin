package com.github.idea.json.parser.model;

import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiField;
import com.intellij.psi.PsiType;
import lombok.Getter;

import java.util.List;
import java.util.Map;

/**
 *  PsiClass 类型的信息
 * @author wangji
 * @date 2024/5/19 22:10
 */
@Getter
public class JPsiTypeClazz extends JPsiTypeBase {

    private final PsiClass psiClass;

    public JPsiTypeClazz(PsiClass psiClass, Map<String, PsiType> psiClassGenerics) {
        this(psiClass, psiClassGenerics, null, 0);
    }

    public JPsiTypeClazz(PsiClass psiClass, Map<String, PsiType> psiClassGenerics, List<String> ignoreProperties, int recursionLevel) {
        this.psiClass = psiClass;
        if (ignoreProperties == null) {
            this.ignoreProperties = List.of();
        } else {
            this.ignoreProperties = ignoreProperties;
        }
        if (psiClassGenerics == null) {
            this.psiTypeGenerics = Map.of();
        } else {
            this.psiTypeGenerics = psiClassGenerics;
        }
        this.recursionLevel = recursionLevel;
    }


    public JPsiType toJPsiVariable(PsiField psiField) {
        return new JPsiType(psiField.getType(), this.getPsiTypeGenerics(), this.getIgnoreProperties(), this.getRecursionLevel());

    }

}
