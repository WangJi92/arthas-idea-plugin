package com.github.idea.json.parser.typevalue.jdk.time;

import com.github.idea.json.parser.typevalue.TypeDefaultValue;
import com.github.idea.json.parser.typevalue.TypeValueContext;

import java.time.OffsetTime;
import java.time.format.DateTimeFormatter;

/**
 * @author wangji
 * @date 2024/5/19 13:56
 */
public class OffsetTimeTypeValue implements TypeDefaultValue {
    private final OffsetTime now = OffsetTime.now();

    @Override
    public Object getValue(TypeValueContext context) {
        return now;
    }

    @Override
    public String getQualifiedName() {
        return OffsetTime.class.getName();
    }
}
