package com.github.idea.json.parser.typevalue.thirdlib.jackson;

import com.github.idea.json.parser.typevalue.TypeDefaultValue;
import com.github.idea.json.parser.typevalue.TypeValueContext;
import com.intellij.psi.PsiType;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Jackson 其他的所有包路径下面的
 *
 * @author wangji
 * @date 2024/5/19 17:49
 */
public class JacksonAllPackageTypeValue implements TypeDefaultValue {

    private static final Map<String, Object> NORMAL_TYPES = new HashMap<>();

    static {

        NORMAL_TYPES.put("com.fasterxml.jackson.databind.node.BooleanNode", true);
        NORMAL_TYPES.put("com.fasterxml.jackson.databind.node.ArrayNode", List.of());
        NORMAL_TYPES.put("com.fasterxml.jackson.databind.node.BigIntegerNode", "0");
        NORMAL_TYPES.put("com.fasterxml.jackson.databind.node.DecimalNode", 0.0);
        NORMAL_TYPES.put("com.fasterxml.jackson.databind.node.DoubleNode", 0.0D);
        NORMAL_TYPES.put("com.fasterxml.jackson.databind.node.FloatNode", 0.0F);
        NORMAL_TYPES.put("com.fasterxml.jackson.databind.node.IntNode", 0);
        NORMAL_TYPES.put("com.fasterxml.jackson.databind.node.LongNode", 0L);
        NORMAL_TYPES.put("com.fasterxml.jackson.databind.node.NullNode", null);
        NORMAL_TYPES.put("com.fasterxml.jackson.databind.node.NumericNode", 0);
        NORMAL_TYPES.put("com.fasterxml.jackson.databind.node.POJONode", Map.of());
        NORMAL_TYPES.put("com.fasterxml.jackson.databind.node.ShortNode", 0);
        NORMAL_TYPES.put("com.fasterxml.jackson.databind.node.TextNode", " ");
    }

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
        if (type == null) {
            return false;
        }
        String canonicalText = type.getCanonicalText();
        Object result = NORMAL_TYPES.get(canonicalText);
        if (result != null) {
            context.put(TypeValueContext.RESULT, result);
            context.put(TypeValueContext.QUALIFIED_NAME, canonicalText);
            return true;
        }
        if (canonicalText.startsWith("com.fasterxml.jackson")) {
            context.put(TypeValueContext.RESULT, null);
            context.put(TypeValueContext.QUALIFIED_NAME, canonicalText);
            return true;
        }
        return false;
    }
}
