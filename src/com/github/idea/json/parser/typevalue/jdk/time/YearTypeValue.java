package com.github.idea.json.parser.typevalue.jdk.time;

import com.github.idea.json.parser.typevalue.TypeDefaultValue;
import com.github.idea.json.parser.typevalue.TypeValueContext;

import java.time.Year;

/**
 * @author wangji
 * @date 2024/5/19 13:59
 */
public class YearTypeValue implements TypeDefaultValue {
    private final Year now = Year.now();

    @Override
    public Object getValue(TypeValueContext context) {
        return now;
    }

    @Override
    public String getQualifiedName() {
        return Year.class.getName();
    }
}
