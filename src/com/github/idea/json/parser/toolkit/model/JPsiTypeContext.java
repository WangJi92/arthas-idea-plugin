package com.github.idea.json.parser.toolkit.model;

import com.intellij.psi.PsiType;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;

/**
 * @author wangji
 * @date 2024/5/21 23:10
 */
@Getter
@Setter
public class JPsiTypeContext extends Context<PsiType> {


    public JPsiTypeContext(PsiType owner) {
        super(owner);
    }

    public JPsiTypeContext(JPsiTypeContext context, PsiType owner) {
        super(context, owner);
    }

    public JPsiTypeContext(PsiType owner, boolean init) {
        super(owner, init);
    }

    public JPsiTypeContext(Context<PsiType> old, PsiType owner, boolean init) {
        super(old, owner, init);
    }


    /**
     * 根据当前的 类型深度递归一个 变量：然后根据基本类型、泛型、类进行拆分
     *
     * @param deepType
     * @param psiClassGenerics
     * @return
     */
    public JPsiTypeContext copy(PsiType deepType, Map<String, PsiType> psiClassGenerics) {
        JPsiTypeContext psiTypeContext = new JPsiTypeContext(this, deepType, false);
        psiTypeContext.init();
        psiTypeContext.setPsiTypeGenerics(psiClassGenerics);
        psiTypeContext.setRecursionLevel(1 + this.getRecursionLevel());
        return psiTypeContext;
    }


}
