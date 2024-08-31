package com.github.idea.json.parser.typevalue.jdk.time;

import com.github.idea.json.parser.typevalue.TypeDefaultValue;
import com.github.idea.json.parser.typevalue.TypeValueContext;

import java.time.OffsetDateTime;

/**
 * @author wangji
 * @date 2024/5/19 14:03
 */
public class OffsetDateTimeTypeValue implements TypeDefaultValue {
    private final OffsetDateTime now = OffsetDateTime.now();

    @Override
    public Object getValue(TypeValueContext context) {
        return now;
    }

    @Override
    public String getQualifiedName() {
        return OffsetDateTime.class.getName();
    }
}
