package com.github.idea.json.parser.toolkit;

import com.github.idea.json.parser.toolkit.model.JPsiType;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.psi.PsiType;

import java.util.HashSet;
import java.util.Set;

/**
 * @author wangji
 * @date 2024/5/21 02:02
 */
public class PsiToolkit {

    private static final Logger LOG = Logger.getInstance(JPsiType.class);

    /**
     * 查找父类的信息
     *
     * @param psiType
     * @return
     */
    private static void doFindParentPlusCurrentQualifiedName(PsiType psiType, Set<String> supperClazzNames) {
        try {
            String qualifiedName = psiType.getCanonicalText();
            //携带有泛型的这里有点问题.. 处理一下
            if (qualifiedName.indexOf("<") > 0) {
                qualifiedName = qualifiedName.substring(0, qualifiedName.indexOf("<"));
            }
            supperClazzNames.add(qualifiedName);
            PsiType[] superTypes = psiType.getSuperTypes();
            for (PsiType superType : superTypes) {
                try {
                    doFindParentPlusCurrentQualifiedName(superType, supperClazzNames);
                } catch (Exception e) {
                    LOG.warn("doFindParentClassName", e);
                }
            }
        } catch (Exception e) {
            LOG.warn("doFindParentClassName2", e);
        }
    }

    /**
     * 查找所有的父类的信息
     *
     * @param psiType
     * @return
     */
    public static Set<String> findParentPlusCurrentQualifiedName(PsiType psiType) {
        HashSet<String> supperClazzNames = new HashSet<>(16);
        doFindParentPlusCurrentQualifiedName(psiType, supperClazzNames);
        return supperClazzNames;
    }
}
