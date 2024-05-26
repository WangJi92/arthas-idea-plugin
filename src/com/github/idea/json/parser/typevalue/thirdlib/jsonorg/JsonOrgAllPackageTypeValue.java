package com.github.idea.json.parser.typevalue.thirdlib.jsonorg;

import com.github.idea.json.parser.typevalue.MultiTypeDefaultValue;
import com.github.idea.json.parser.typevalue.TypeValueContext;
import com.intellij.psi.PsiType;

/**
 * json org 其他的所有包路径下面的
 *
 * @author wangji
 * @date 2024/5/19 17:49
 */
public class JsonOrgAllPackageTypeValue implements MultiTypeDefaultValue {

    @Override
    public boolean isSupport(TypeValueContext context) {
        PsiType type = context.getType();
        String canonicalText = type.getCanonicalText();
        if (canonicalText.startsWith("org.json")) {
            context.setResult(DEFAULT_NULL);
            return true;
        }
        return false;
    }
}
