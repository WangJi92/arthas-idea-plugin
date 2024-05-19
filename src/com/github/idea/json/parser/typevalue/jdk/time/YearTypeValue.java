package com.github.idea.json.parser.typevalue.jdk.time;

import com.github.idea.json.parser.typevalue.TypeDefaultValue;
import com.github.idea.json.parser.typevalue.TypeValueContext;

import java.time.Year;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;

/**
 * @author wangji
 * @date 2024/5/19 13:59
 */
public class YearTypeValue implements TypeDefaultValue {
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy");
    private final YearMonth now = YearMonth.now();

    @Override
    public Object getValue(TypeValueContext context) {
        return now.format(formatter);
    }

    @Override
    public String getQualifiedName() {
        return Year.class.getName();
    }
}
