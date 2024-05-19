package com.github.idea.json.parser.typevalue.jdk.time;

import com.github.idea.json.parser.typevalue.TypeDefaultValue;
import com.github.idea.json.parser.typevalue.TypeValueContext;

import java.time.Duration;

/**
 *
 * @author wangji
 * @date 2024/5/19 13:25
 */
public class DurationTypeValue implements TypeDefaultValue {

    @Override
    public Object getValue(TypeValueContext context) {
        return "1s";
    }

    @Override
    public String getQualifiedName(TypeValueContext context) {
        return Duration.class.getName();
    }
}
