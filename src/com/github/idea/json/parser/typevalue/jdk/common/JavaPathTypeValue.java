package com.github.idea.json.parser.typevalue.jdk.common;

import com.github.idea.json.parser.typevalue.TypeDefaultValue;
import com.github.idea.json.parser.typevalue.TypeValueContext;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * @author wangji
 * @date 2024/5/19 19:16
 */
public class JavaPathTypeValue implements TypeDefaultValue {
    @Override
    public Object getValue(TypeValueContext context) {
        return Paths.get("/home/user");
    }

    @Override
    public String getQualifiedName() {
        return Path.class.getName();
    }
}
