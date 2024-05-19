package com.github.idea.json.parser.typevalue.jdk.time;

import com.github.idea.json.parser.typevalue.TypeDefaultValue;
import com.github.idea.json.parser.typevalue.TypeValueContext;

import java.time.Period;

/**
 * @author wangji
 * @date 2024/5/19 14:06
 */
public class PeriodTypeValue implements TypeDefaultValue {
    // 创建一个表示1年2个月3天的Period对象
    private final  Period period = Period.of(1, 2, 3);

    @Override
    public Object getValue(TypeValueContext context) {
        return period.toString();
    }

    @Override
    public String getQualifiedName(TypeValueContext context) {
        return Period.class.getName();
    }
}
