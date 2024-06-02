package com.github.idea.json.parser.typevalue.jdk.common;

import com.github.idea.json.parser.typevalue.TypeDefaultValue;
import com.github.idea.json.parser.typevalue.TypeValueContext;

import java.nio.file.Path;

/**
 * @author wangji
 * @date 2024/5/19 19:16
 */
public class JavaPathTypeValue implements TypeDefaultValue {
    @Override
    public Object getValue(TypeValueContext context) {
        return null;
    }

    @Override
    public String getQualifiedName() {
        return Path.class.getName();
    }
}
