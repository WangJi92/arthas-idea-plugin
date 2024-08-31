package com.github.idea.json.parser.typevalue.thirdlib.jackson;

import com.fasterxml.jackson.databind.node.*;
import com.github.idea.json.parser.typevalue.MultiTypeDefaultValue;
import com.github.idea.json.parser.typevalue.TypeValueContext;
import com.intellij.psi.PsiType;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Jackson 其他的所有包路径下面的
 *
 * @author wangji
 * @date 2024/5/19 17:49
 */
public class JacksonAllPackageTypeValue implements MultiTypeDefaultValue {

    private final Map<String, Object> container = new HashMap<>(16);

    @Override
    public Map<String, Object> getContainer() {
        return container;
    }

    public void init() {
        Map<String, Object> container = this.getContainer();
        container.put("com.fasterxml.jackson.databind.node.BooleanNode", BooleanNode.valueOf(true));
        container.put("com.fasterxml.jackson.databind.node.ArrayNode", List.of());
        container.put("com.fasterxml.jackson.databind.node.BigIntegerNode", BigIntegerNode.valueOf(BigInteger.ONE));
        container.put("com.fasterxml.jackson.databind.node.DecimalNode", DecimalNode.valueOf(BigDecimal.ONE));
        container.put("com.fasterxml.jackson.databind.node.DoubleNode", DoubleNode.valueOf(1d));
        container.put("com.fasterxml.jackson.databind.node.FloatNode", FloatNode.valueOf(1f));
        container.put("com.fasterxml.jackson.databind.node.IntNode", IntNode.valueOf(1));
        container.put("com.fasterxml.jackson.databind.node.LongNode", LongNode.valueOf(1L));
        container.put("com.fasterxml.jackson.databind.node.NullNode", NullNode.getInstance());
        container.put("com.fasterxml.jackson.databind.node.NumericNode", IntNode.valueOf(1));
        container.put("com.fasterxml.jackson.databind.node.POJONode", Map.of());
        container.put("com.fasterxml.jackson.databind.node.ShortNode", ShortNode.valueOf((short) 1));
        container.put("com.fasterxml.jackson.databind.node.TextNode", TextNode.valueOf(" "));
    }

    @Override
    public boolean isSupport(TypeValueContext context) {
        PsiType type = context.getType();
        String canonicalText = type.getCanonicalText();
        if (canonicalText.startsWith("com.fasterxml.jackson")) {
            context.setResult(DEFAULT_NULL);
            return true;
        }
        return false;
    }
}
