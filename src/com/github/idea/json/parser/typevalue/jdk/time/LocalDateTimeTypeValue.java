package com.github.idea.json.parser.typevalue.jdk.time;

import com.github.idea.json.parser.typevalue.TypeDefaultValue;
import com.github.idea.json.parser.typevalue.TypeValueContext;

import java.time.LocalDateTime;

/**
 * @author wangji
 * @date 2024/5/19 13:30
 */
public class LocalDateTimeTypeValue implements TypeDefaultValue {
    private final LocalDateTime now = LocalDateTime.now();

    @Override
    public Object getValue(TypeValueContext context) {
        return now;
    }

    @Override
    public String getQualifiedName() {
        return LocalDateTime.class.getName();
    }
}
