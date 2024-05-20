package com.github.idea.json.parser.typevalue.thirdlib.jackson;

import com.github.idea.json.parser.typevalue.MultiTypeDefaultValue;
import com.github.idea.json.parser.typevalue.TypeValueContext;
import com.intellij.psi.PsiType;

import java.util.List;
import java.util.Map;

/**
 * Jackson 其他的所有包路径下面的
 *
 * @author wangji
 * @date 2024/5/19 17:49
 */
public class JacksonAllPackageTypeValue implements MultiTypeDefaultValue {

    @Override
    public void init() {
        Map<String, Object> container = this.getContainer();
        container.put("com.fasterxml.jackson.databind.node.BooleanNode", true);
        container.put("com.fasterxml.jackson.databind.node.ArrayNode", List.of());
        container.put("com.fasterxml.jackson.databind.node.BigIntegerNode", "0");
        container.put("com.fasterxml.jackson.databind.node.DecimalNode", 0.0);
        container.put("com.fasterxml.jackson.databind.node.DoubleNode", 0.0D);
        container.put("com.fasterxml.jackson.databind.node.FloatNode", 0.0F);
        container.put("com.fasterxml.jackson.databind.node.IntNode", 0);
        container.put("com.fasterxml.jackson.databind.node.LongNode", 0L);
        container.put("com.fasterxml.jackson.databind.node.NullNode", null);
        container.put("com.fasterxml.jackson.databind.node.NumericNode", 0);
        container.put("com.fasterxml.jackson.databind.node.POJONode", Map.of());
        container.put("com.fasterxml.jackson.databind.node.ShortNode", 0);
        container.put("com.fasterxml.jackson.databind.node.TextNode", " ");
    }

    @Override
    public boolean isSupport(TypeValueContext context) {
        PsiType type = context.getType();
        String canonicalText = type.getCanonicalText();
        if (canonicalText.startsWith("com.fasterxml.jackson")) {
            context.put(TypeValueContext.RESULT, DEFAULT_NULL);
            return true;
        }
        return false;
    }
}
