package com.github.idea.json.parser.typevalue.jdk.time;

import com.github.idea.json.parser.typevalue.TypeDefaultValue;
import com.github.idea.json.parser.typevalue.TypeValueContext;

import java.time.Instant;

/**
 * @author wangji
 * @date 2024/5/19 13:46
 */
public class InstantTypeValue implements TypeDefaultValue {

    private final  Instant now = Instant.now();

    @Override
    public Object getValue(TypeValueContext context) {
        return now;
    }

    @Override
    public String getQualifiedName() {
        return Instant.class.getName();
    }
}
