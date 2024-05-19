package com.github.idea.json.parser.typevalue.jdk.time;

import com.github.idea.json.parser.typevalue.TypeDefaultValue;
import com.github.idea.json.parser.typevalue.TypeValueContext;

import java.time.ZoneOffset;

/**
 * @author wangji
 * @date 2024/5/19 14:19
 */
public class ZoneOffsetTypeValue implements TypeDefaultValue {

    // 获取系统默认的时区偏移量
    private final ZoneOffset defaultOffset = ZoneOffset.UTC;

    @Override
    public Object getValue(TypeValueContext context) {
        return defaultOffset.toString();
    }

    @Override
    public String getQualifiedName(TypeValueContext context) {
        return ZoneOffset.class.getName();
    }
}
