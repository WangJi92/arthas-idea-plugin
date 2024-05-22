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


    /**
     * 设置泛型
     * @param psiClassGenerics
     */
    public void setPsiClassGenerics(Map<String, PsiType> psiClassGenerics) {
        this.putCache(String.format(CACHE_KEY_PSI_CLASS_GENERICS, this.getQualifiedName()), psiClassGenerics);
    }


    /**
     * 获取缓存信息
     *
     * @param key
     * @return
     */
    public Object getCache(Object key) {
        return processCache.get(key);
    }

    /**
     * 设置缓存信息
     *
     * @param key
     * @param value
     * @return
     */
    @SuppressWarnings("unchecked")
    public void putCache(Object key, Object value) {
       this.processCache.put(key, value);
    }

    /**
     * 获取当前类的泛型数据
     *
     * @return
     */
    @SuppressWarnings("unchecked")
    public Map<String, PsiType> getPsiTypeGenerics() {
        return (Map<String, PsiType>) this.getCache(String.format(CACHE_KEY_PSI_CLASS_GENERICS, this.getQualifiedName()));
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
        Set<String> parentClazzNames = (Set<String>) this.getCache(String.format(CACHE_KEY_PARENT_PLUS_CURRENT_QUALIFIED_NAMES, this.getQualifiedName()));
        return parentClazzNames != null && parentClazzNames.contains(clazzName);
    }

    public JPsiTypeContext(PsiType owner, boolean init) {
        this.owner = owner;
        if (init) {
            this.init();
        }
    }


    private JPsiTypeContext(JPsiTypeContext old, PsiType owner, boolean init) {
        this(owner, init);
        this.processCache = old.processCache;
    }

    /**
     * 初始化
     */
    @SuppressWarnings("unchecked")
    public void init() {
        // 初始化获取父类的信息
        if (this.getCache(String.format(CACHE_KEY_PARENT_PLUS_CURRENT_QUALIFIED_NAMES, this.getQualifiedName())) == null) {
            Set<String> parentPlusCurrentQualifiedName = PsiToolkit.findParentPlusCurrentQualifiedName(this.getOwner());
            this.putCache(String.format(CACHE_KEY_PARENT_PLUS_CURRENT_QUALIFIED_NAMES, this.getQualifiedName()), parentPlusCurrentQualifiedName);
        }
        // 初始化获取泛型信息
        if (this.getCache(String.format(CACHE_KEY_PSI_CLASS_GENERICS, this.getQualifiedName())) == null) {
            Map<String, PsiType> psiClassGenerics = PsiToolkit.getPsiClassGenerics(this.getOwner());
           this.setPsiClassGenerics(psiClassGenerics);
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
        psiTypeJPsiTypeContext.setPsiClassGenerics(psiClassGenerics);
        psiTypeJPsiTypeContext.setRecursionLevel(recursionLevel);
        return psiTypeJPsiTypeContext;
    }
}
