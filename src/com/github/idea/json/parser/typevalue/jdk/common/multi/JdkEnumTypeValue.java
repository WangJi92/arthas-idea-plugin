package com.github.idea.json.parser.typevalue.jdk.common.multi;

import com.github.idea.json.parser.typevalue.MultiTypeDefaultValue;
import com.github.idea.json.parser.typevalue.TypeDefaultValue;
import com.github.idea.json.parser.typevalue.TypeValueContext;
import com.intellij.psi.*;

import java.util.Arrays;

/**
 * @author wangji
 * @date 2024/5/19 17:24
 */
public class JdkEnumTypeValue implements MultiTypeDefaultValue {

    @Override
    public boolean isSupport(TypeValueContext context) {
        PsiType type = context.getType();
        if (!(type instanceof PsiClassType psiClassType)) {
            return false;
        }
        PsiClass psiClass = psiClassType.resolve();
        if (psiClass != null && psiClass.isEnum()) {
            // enum
            String result = Arrays.stream(psiClass.getAllFields()).filter(psiField -> psiField instanceof PsiEnumConstant).findFirst().map(PsiField::getName).orElse(TypeDefaultValue.DEFAULT_NULL);
            context.put(TypeValueContext.RESULT, result);
            return true;
        }
        return false;
    }
}
