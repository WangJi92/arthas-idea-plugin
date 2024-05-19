package com.github.idea.json.parser.typevalue;

import java.util.Objects;

/**
 * 获取类型的默认值信息
 *
 * @author wangji
 * @date 2024/5/19 13:13
 */
public interface TypeDefaultValue {

    /**
     * 获取值的信息
     *
     * @return
     */
    Object getValue(TypeValueContext context);


    /**
     * 获取包结构信息
     *
     * @return
     */
    String getQualifiedName(TypeValueContext context);

    /**
     * 单个模型
     *
     * @return
     */
    default boolean isSingle() {
        return true;
    }

    /**
     * 是否支持
     *
     * @param context
     * @return
     */
    default boolean isSupport(TypeValueContext context) {
        return isSingle() && Objects.equals(this.getQualifiedName(context), context.getType().getCanonicalText());
    }

}
