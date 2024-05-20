package com.github.idea.json.parser.typevalue;

import com.github.idea.json.parser.toolkit.PsiToolkit;
import com.intellij.psi.PsiType;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * 类型转换上下文
 *
 * @author wangji
 * @date 2024/5/19 14:53
 */
public class TypeValueContext {

    public static final String RESULT = "RESULT";

    @Getter
    private PsiType type;

    private final Map<String, Object> processCache = new HashMap<>();

    @Getter
    private Object result;

    /**
     * 是否支持
     */
    @Getter
    private Boolean support = false;

    /**
     * 当前类+所有的父类的名字集合
     */
    private Set<String> parentPlusCurrentQualifiedNames;

    /**
     * 是否继承了 当前clazz
     *
     * @param clazzName
     * @return
     */
    public Boolean isInheritor(String clazzName) {
        return parentPlusCurrentQualifiedNames != null && parentPlusCurrentQualifiedNames.contains(clazzName);
    }


    /**
     * @param type
     */
    public TypeValueContext(PsiType type) {
        assert type != null;
        this.type = type;
        this.parentPlusCurrentQualifiedNames = PsiToolkit.findParentPlusCurrentQualifiedName(type);
    }


    public void setResult(Object result) {
        this.result = result;
        this.support = true;
    }


    public Object put(String key, Object value) {
        return processCache.put(key, value);
    }

    public Object get(Object key) {
        return processCache.get(key);
    }

}
