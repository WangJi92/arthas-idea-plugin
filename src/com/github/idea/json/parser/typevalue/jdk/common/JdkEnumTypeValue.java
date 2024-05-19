package com.github.idea.json.parser.typevalue.jdk.common;

import com.github.idea.json.parser.typevalue.TypeDefaultValue;
import com.github.idea.json.parser.typevalue.TypeValueContext;
import com.intellij.psi.*;

import java.util.Arrays;

/**
 * @author wangji
 * @date 2024/5/19 17:24
 */
public class JdkEnumTypeValue implements TypeDefaultValue {

    @Override
    public Object getValue(TypeValueContext context) {
        return context.get(TypeValueContext.RESULT);
    }

    @Override
    public String getQualifiedName(TypeValueContext context) {
        return (String) context.get(TypeValueContext.QUALIFIED_NAME);
    }

    @Override
    public boolean isSingle() {
        return false;
    }

    @Override
    public boolean isSupport(TypeValueContext context) {
        PsiType type = context.getType();
        if (!(type instanceof PsiClassType psiClassType)) {
            return false;
        }
        String canonicalText = type.getCanonicalText();
        PsiClass psiClass = psiClassType.resolve();
        if (psiClass != null && psiClass.isEnum()) {
            // enum
            String result = Arrays.stream(psiClass.getAllFields()).filter(psiField -> psiField instanceof PsiEnumConstant).findFirst().map(PsiField::getName).orElse(null);
            context.put(TypeValueContext.RESULT, result);
            context.put(TypeValueContext.QUALIFIED_NAME, canonicalText);
            return true;
        }
        return false;
    }
}
