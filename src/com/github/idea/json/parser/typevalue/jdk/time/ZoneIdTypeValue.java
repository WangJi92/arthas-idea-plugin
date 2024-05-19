package com.github.idea.json.parser.typevalue.jdk.time;

import com.github.idea.json.parser.typevalue.TypeDefaultValue;
import com.github.idea.json.parser.typevalue.TypeValueContext;

import java.time.ZoneId;

/**
 * @author wangji
 * @date 2024/5/19 14:13
 */
public class ZoneIdTypeValue implements TypeDefaultValue {

    // 获取系统默认时区
    private final ZoneId systemZone = ZoneId.systemDefault();

    @Override
    public Object getValue(TypeValueContext context) {
        return systemZone.toString();
    }

    @Override
    public String getQualifiedName(TypeValueContext context) {
        return ZoneId.class.getName();
    }
}
