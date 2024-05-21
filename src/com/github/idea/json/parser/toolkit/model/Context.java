package com.github.idea.json.parser.toolkit.model;

import com.github.idea.json.parser.toolkit.PsiToolkit;
import com.intellij.psi.PsiType;
import com.intellij.psi.PsiVariable;
import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * @author wangji
 * @date 2024/5/21 23:16
 */

public abstract class Context<T> {

    /**
     * Set<String>
     */
    public static final String CACHE_KEY_PARENT_PLUS_CURRENT_QUALIFIED_NAMES = "PARENT_PLUS_CURRENT_QUALIFIED_NAMES_CACHE_%s";

    /**
     * Map<String, PsiType>
     */
    public static final String CACHE_KEY_PSI_CLASS_GENERICS = "PSI_CLASS_GENERICS_%s";

    @Getter
    private T owner;

    /**
     * 递归深度
     */
    @Getter
    @Setter
    public int recursionLevel = 0;


    @Getter
    public Map processCache = new HashMap<String, Object>();

    public void setParentPlusCurrentQualifiedNames(Set<String> parentPlusCurrentQualifiedNames) {
        this.processCache.put(String.format(CACHE_KEY_PARENT_PLUS_CURRENT_QUALIFIED_NAMES, this.getQualifiedName()), parentPlusCurrentQualifiedNames);
    }

    @SuppressWarnings("unchecked")
    public Set<String> getParentPlusCurrentQualifiedNames() {
        return (Set<String>) this.processCache.get(String.format(CACHE_KEY_PARENT_PLUS_CURRENT_QUALIFIED_NAMES, this.getQualifiedName()));
    }

    public void setPsiTypeGenerics(Map<String, PsiType> psiTypeGenerics) {
        this.processCache.put(String.format(CACHE_KEY_PSI_CLASS_GENERICS, this.getQualifiedName()), psiTypeGenerics);
    }

    @SuppressWarnings("unchecked")
    public Map<String, PsiType> getPsiTypeGenerics() {
        return (Map<String, PsiType>) this.processCache.get(String.format(CACHE_KEY_PSI_CLASS_GENERICS, this.getQualifiedName()));
    }

    /**
     * 是否继承了 当前clazz
     * make simple  com.intellij.psi.util.InheritanceUtil#isInheritor(com.intellij.psi.PsiType, java.lang.String)
     *
     * @param clazzName
     * @return
     */
    @SuppressWarnings("unchecked")
    public Boolean isInheritor(String clazzName) {
        Set<String> parentClazzNames = (Set<String>) this.getProcessCache().getOrDefault(String.format(CACHE_KEY_PARENT_PLUS_CURRENT_QUALIFIED_NAMES, this.getQualifiedName()), null);
        return parentClazzNames != null && parentClazzNames.contains(clazzName);
    }

    public Context(T owner) {
        this(owner, true);
    }

    public Context(T owner, boolean init) {
        this.owner = owner;
        if (init) {
            this.init();
        }
    }

    public Context(Context<T> old, T owner) {
        this(old, owner, true);
    }

    public Context(Context<T> old, T owner, boolean init) {
        this(owner, init);
        this.processCache = old.getProcessCache();
    }

    /**
     * 初始化
     */
    @SuppressWarnings("unchecked")
    public void init() {
        if (this.getParentPlusCurrentQualifiedNames() == null) {
            if (this.getOwner() instanceof PsiVariable psiVariable) {
                PsiType type = psiVariable.getType();
                Set<String> parentPlusCurrentQualifiedName = PsiToolkit.findParentPlusCurrentQualifiedName(type);
                this.setParentPlusCurrentQualifiedNames(parentPlusCurrentQualifiedName);
            }
        }
        if (this.getPsiTypeGenerics() == null) {
            if (this.getOwner() instanceof PsiVariable psiVariable) {
                PsiType type = psiVariable.getType();
                Map<String, PsiType> psiClassGenerics = PsiToolkit.getPsiClassGenerics(type);
                this.setPsiTypeGenerics(psiClassGenerics);
            }
//            else if (this.getOwner() instanceof PsiClass psiClass) {
//                PsiTypeParameter[] typeParameters = psiClass.getTypeParameters();
//                Map<String,PsiType> psiTypeMap =  Map.of();
//                for (PsiTypeParameter typeParameter : typeParameters) {
//                    psiTypeMap.put(typeParameter.getText(),typeParameter)
//                }
//            }
        }
    }

    public String getQualifiedName() {
        if (this instanceof JPsiTypeContext context) {
            return context.getOwner().getCanonicalText();
        } else if (this instanceof JPsiElementContext context) {
            return context.getOwner().getText();
        }
        return "";
    }
}
