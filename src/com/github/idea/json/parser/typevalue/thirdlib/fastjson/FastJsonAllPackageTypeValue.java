package com.github.idea.json.parser.typevalue.thirdlib.fastjson;

import com.github.idea.json.parser.typevalue.TypeDefaultValue;
import com.github.idea.json.parser.typevalue.TypeValueContext;
import com.intellij.psi.PsiType;

/**
 * Jackson 其他的所有包路径下面的
 *
 * @author wangji
 * @date 2024/5/19 17:49
 */
public class FastJsonAllPackageTypeValue implements TypeDefaultValue {

    @Override
    public Object getValue(TypeValueContext context) {
        return context.get(TypeValueContext.RESULT);
    }

    @Override
    public boolean isSingle() {
        return false;
    }


    @Override
    public boolean isSupport(TypeValueContext context) {
        PsiType type = context.getType();
        if (type == null) {
            return false;
        }
        String canonicalText = type.getCanonicalText();
        if (canonicalText.startsWith("com.alibaba.fastjson")) {
            context.put(TypeValueContext.RESULT, null);
            return true;
        }
        return false;
    }
}
