package com.github.idea.json.parser.toolkit.model;

import com.github.idea.json.parser.toolkit.PsiToolkit;
import com.intellij.psi.PsiType;
import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * @author wangji
 * @date 2024/5/21 23:16
 */

public class JPsiTypeContext {

    /**
     * Set<String>
     */
    public static final String CACHE_KEY_PARENT_PLUS_CURRENT_QUALIFIED_NAMES = "PARENT_PLUS_CURRENT_QUALIFIED_NAMES_CACHE_%s";

    /**
     * Map<String, PsiType>
     */
    public static final String CACHE_KEY_PSI_CLASS_GENERICS = "PSI_CLASS_GENERICS_%s";

    @Getter
    private PsiType owner;

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

    public JPsiTypeContext(PsiType owner) {
        this(owner, true);
    }

    public JPsiTypeContext(PsiType owner, boolean init) {
        this.owner = owner;
        if (init) {
            this.init();
        }
    }

    public JPsiTypeContext(JPsiTypeContext old, PsiType owner) {
        this(old, owner, true);
    }

    public JPsiTypeContext(JPsiTypeContext old, PsiType owner, boolean init) {
        this(owner, init);
        this.processCache = old.getProcessCache();
    }

    /**
     * 初始化
     */
    @SuppressWarnings("unchecked")
    public void init() {
        if (this.getParentPlusCurrentQualifiedNames() == null) {
            Set<String> parentPlusCurrentQualifiedName = PsiToolkit.findParentPlusCurrentQualifiedName(this.getOwner());
            this.setParentPlusCurrentQualifiedNames(parentPlusCurrentQualifiedName);
        }
        if (this.getPsiTypeGenerics() == null) {
            Map<String, PsiType> psiClassGenerics = PsiToolkit.getPsiClassGenerics(this.getOwner());
            this.setPsiTypeGenerics(psiClassGenerics);
        }
    }

    public String getQualifiedName() {
        return PsiToolkit.getPsiTypeSimpleName(this.getOwner());
    }

    /**
     * 根据当前的 类型深度递归一个 变量：然后根据基本类型、泛型、类进行拆分
     *
     * @param deepType
     * @param psiClassGenerics
     * @return
     */
    public JPsiTypeContext copy(PsiType deepType, Map<String, PsiType> psiClassGenerics) {
        int recursionLevel = this.getRecursionLevel() + 1;
        return copy(deepType, psiClassGenerics, recursionLevel);
    }

    /**
     * 构建信息
     *
     * @param deepType
     * @param psiClassGenerics
     * @param recursionLevel
     * @return
     */
    public JPsiTypeContext copy(PsiType deepType, Map<String, PsiType> psiClassGenerics, int recursionLevel) {
        JPsiTypeContext psiTypeJPsiTypeContext = new JPsiTypeContext(this, deepType, false);
        psiTypeJPsiTypeContext.init();
        psiTypeJPsiTypeContext.setPsiTypeGenerics(psiClassGenerics);
        psiTypeJPsiTypeContext.setRecursionLevel(recursionLevel);
        return psiTypeJPsiTypeContext;
    }
}
