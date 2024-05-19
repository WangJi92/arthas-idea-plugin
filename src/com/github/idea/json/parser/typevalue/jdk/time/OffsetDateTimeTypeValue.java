package com.github.idea.json.parser.typevalue.jdk.time;

import com.github.idea.json.parser.typevalue.TypeDefaultValue;
import com.github.idea.json.parser.typevalue.TypeValueContext;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;

/**
 * @author wangji
 * @date 2024/5/19 14:03
 */
public class OffsetDateTimeTypeValue implements TypeDefaultValue {
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss XXX");
    private final OffsetDateTime now = OffsetDateTime.now();

    @Override
    public Object getValue(TypeValueContext context) {
        return now.format(formatter);
    }

    @Override
    public String getQualifiedName(TypeValueContext context) {
        return OffsetDateTime.class.getName();
    }
}
