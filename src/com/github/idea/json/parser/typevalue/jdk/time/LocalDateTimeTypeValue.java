package com.github.idea.json.parser.typevalue.jdk.time;

import com.github.idea.json.parser.typevalue.TypeDefaultValue;
import com.github.idea.json.parser.typevalue.TypeValueContext;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * @author wangji
 * @date 2024/5/19 13:30
 */
public class LocalDateTimeTypeValue implements TypeDefaultValue {
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private final LocalDateTime now = LocalDateTime.now();

    @Override
    public Object getValue(TypeValueContext context) {
        return now.format(formatter);
    }

    @Override
    public String getQualifiedName(TypeValueContext context) {
        return LocalDateTime.class.getName();
    }
}
