package com.github.idea.json.parser.typevalue.thirdlib.fastjson;

import com.github.idea.json.parser.typevalue.MultiTypeDefaultValue;
import com.github.idea.json.parser.typevalue.TypeValueContext;
import com.intellij.psi.PsiType;

/**
 * 当前fastjson的所有包路径下面的
 *
 * @author wangji
 * @date 2024/5/19 17:49
 */
public class FastJsonAllPackageTypeValue implements MultiTypeDefaultValue {

    @Override
    public boolean isSupport(TypeValueContext context) {
        PsiType type = context.getType();
        if (type == null) {
            return false;
        }
        String canonicalText = type.getCanonicalText();
        if (canonicalText.startsWith("com.alibaba.fastjson")) {
            context.setResult(DEFAULT_NULL);
            return true;
        }
        return false;
    }
}
