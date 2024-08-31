package com.github.idea.json.parser.typevalue.thirdlib.fastjson;

import com.github.idea.json.parser.typevalue.TypeDefaultValue;
import com.github.idea.json.parser.typevalue.TypeValueContext;

import java.util.HashMap;

/**
 * @author wangji
 * @date 2024/5/19 17:41
 */
public class JsonObjectTypeValue implements TypeDefaultValue {
    @Override
    public Object getValue(TypeValueContext context) {
        return new HashMap<>();
    }

    @Override
    public String getQualifiedName() {
        return "com.alibaba.fastjson.JSONObject";
    }
}
