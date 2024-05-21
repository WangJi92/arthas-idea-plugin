package com.github.idea.json.parser.toolkit.model;

import com.intellij.psi.PsiElement;

/**
 * @author wangji
 * @date 2024/5/21 23:46
 */
public class JPsiElementContext extends Context<PsiElement> {

    public JPsiElementContext(PsiElement owner) {
        super(owner);
    }

    public JPsiElementContext(Context<PsiElement> old, PsiElement owner, boolean init) {
        super(old, owner, init);
    }

    public JPsiElementContext(Context<PsiElement> old, PsiElement owner) {
        super(old, owner);
    }

    public JPsiElementContext(PsiElement owner, boolean init) {
        super(owner, init);
    }


}
