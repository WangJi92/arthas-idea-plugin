package com.github.idea.json.parser.typevalue;

import com.intellij.psi.PsiClassType;
import com.intellij.psi.PsiType;

import java.util.HashMap;
import java.util.Map;

/**
 * 类型转换上下文
 *
 * @author wangji
 * @date 2024/5/19 14:53
 */
public class TypeValueContext {

    public static final String RESULT = "RESULT";

    private PsiClassType psiClassType;

    private PsiType type;

    private final Map<String, Object> processCache = new HashMap<>();

    private Object result;

    /**
     * 是否支持
     */
    private Boolean support = false;


    /**
     * single 的时候 PsiClassType 可能为空
     *
     * @param type
     */
    public TypeValueContext(PsiType type) {
        this.type = type;
        if (type instanceof PsiClassType) {
            this.psiClassType = (PsiClassType) type;
        }
    }


    public Object getResult() {
        return result;
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

    public PsiType getType() {
        return type;
    }

    public Boolean getSupport() {
        return support;
    }
}
