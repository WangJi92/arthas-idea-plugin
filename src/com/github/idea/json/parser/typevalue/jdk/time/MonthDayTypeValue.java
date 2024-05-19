package com.github.idea.json.parser.typevalue.jdk.time;

import com.github.idea.json.parser.typevalue.TypeDefaultValue;
import com.github.idea.json.parser.typevalue.TypeValueContext;

import java.time.MonthDay;
import java.time.format.DateTimeFormatter;

/**
 * @author wangji
 * @date 2024/5/19 13:53
 */
public class MonthDayTypeValue implements TypeDefaultValue {
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM-dd");
    private final MonthDay now = MonthDay.now();

    @Override
    public Object getValue(TypeValueContext context) {
        return now.format(formatter);
    }

    @Override
    public String getQualifiedName(TypeValueContext context) {
        return MonthDay.class.getName();
    }
}
