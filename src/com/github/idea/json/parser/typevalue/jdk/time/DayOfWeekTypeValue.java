package com.github.idea.json.parser.typevalue.jdk.time;

import com.github.idea.json.parser.typevalue.TypeDefaultValue;
import com.github.idea.json.parser.typevalue.TypeValueContext;

import java.time.DayOfWeek;
import java.time.LocalDate;

/**
 * @author wangji
 * @date 2024/5/19 14:26
 */
public class DayOfWeekTypeValue implements TypeDefaultValue {
    DayOfWeek todayDayOfWeek = LocalDate.now().getDayOfWeek();

    @Override
    public Object getValue(TypeValueContext context) {
        return todayDayOfWeek.toString();
    }

    @Override
    public String getQualifiedName(TypeValueContext context) {
        return DayOfWeek.class.getName();
    }
}
