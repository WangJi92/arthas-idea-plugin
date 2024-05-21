package com.github.idea.json.parser.toolkit.model;

import com.github.idea.json.parser.toolkit.PsiToolkit;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.psi.PsiField;
import com.intellij.psi.PsiType;
import lombok.Getter;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * PsiVariable 包含 PsiField & PsiLocalVariable &  PsiParameter & PsiReceiverParameter & PsiVariableEx
 *
 * @author wangji
 * @date 2024/5/19 19:49
 */
@Getter
@Deprecated
public class JPsiType {
    private static final Logger LOG = Logger.getInstance(JPsiType.class);

    /**
     * 当前的类型
     */
    protected PsiType psiType;

    /**
     * 当前类+所有的父类的名字集合
     */
    public Set<String> parentPlusCurrentQualifiedNames;

    /**
     * 递归深度
     */
    public int recursionLevel = 0;


    /**
     * class Person<T>{
     * private T name;
     * }
     * <p>
     * <p>
     * class MainClass{
     * private Person<String> person;
     * <p>
     * public static void main(String[] args) {
     * MainClass mainC = new MainClass();
     * Person<String> person = new Person<>()
     * person.setName("name");
     * mainC.setPerson(person)
     * }
     * }
     * <p>
     * 假设解析从MainClass 开始出发,递归解析 person 字段，后续的name 需要最外层的泛型参数,所以最外层的需要传递下去
     */
    public Map<String, PsiType> psiTypeGenerics = Map.of();

    public JPsiType(PsiType psiType) {
        this(psiType, null, null, 0);
    }

    public JPsiType(PsiType psiType, Map<String, PsiType> psiClassGenerics, List<String> ignoreProperties, int recursionLevel) {
        this.psiType = psiType;
        this.parentPlusCurrentQualifiedNames = PsiToolkit.findParentPlusCurrentQualifiedName(psiType);

        if (psiClassGenerics != null) {
            this.psiTypeGenerics = psiClassGenerics;
        }
        this.recursionLevel = recursionLevel;
    }

    /**
     * 是否继承了 当前clazz
     * make simple  com.intellij.psi.util.InheritanceUtil#isInheritor(com.intellij.psi.PsiType, java.lang.String)
     *
     * @param clazzName
     * @return
     */
    public Boolean isInheritor(String clazzName) {
        return parentPlusCurrentQualifiedNames != null && parentPlusCurrentQualifiedNames.contains(clazzName);
    }


    /**
     * 根据当前的 类型深度递归一个 变量：然后根据基本类型、泛型、类进行拆分
     *
     * @param deepType
     * @param psiClassGenerics
     * @return
     */
    public JPsiType copyNew(PsiType deepType, Map<String, PsiType> psiClassGenerics) {
        return new JPsiType(deepType, psiClassGenerics, null, ++this.recursionLevel);
    }

    /**
     * 从 PsiField 创建一个 JPsiType
     *
     * @param psiField
     * @return
     */
    public JPsiType copyNew(PsiField psiField) {
        return new JPsiType(psiField.getType(), this.getPsiTypeGenerics(), null, this.getRecursionLevel());
    }
}
