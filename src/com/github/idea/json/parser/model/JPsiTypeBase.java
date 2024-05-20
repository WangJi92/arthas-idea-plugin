package com.github.idea.json.parser.model;

import com.intellij.psi.PsiType;
import lombok.Getter;

import java.util.List;
import java.util.Map;

@Getter
@Deprecated
public abstract class JPsiTypeBase {

    /**
     * 递归深度
     */
    public int recursionLevel = 0;

    /**
     * 需要过滤的属性，随Fields上定义的注释而变化
     */
    public List<String> ignoreProperties = List.of();

    /**
     *  class Person<T>{
     *      private T name;
     *  }
     *
     *
     *  class MainClass{
     *     private Person<String> person;
     *
     *      public static void main(String[] args) {
     *          MainClass mainC = new MainClass();
     *          Person<String> person = new Person<>()
     *          person.setName("name");
     *          mainC.setPerson(person)
     *     }
     *  }
     *
     *  假设解析从MainClass 开始出发,递归解析 person 字段，后续的name 需要最外层的泛型参数,所以最外层的需要传递下去
     *
     */
    public Map<String, PsiType> psiTypeGenerics = Map.of();
}
