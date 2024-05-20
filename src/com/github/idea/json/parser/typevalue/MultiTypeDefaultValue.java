package com.github.idea.json.parser.typevalue;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * 有多个类型
 *
 * @author wangji
 * @date 2024/5/20 22:26
 */
public interface MultiTypeDefaultValue extends TypeDefaultValue {
    /**
     * 初始信息
     */
    default void init() {
    }

    @Override
    default Object getValue(TypeValueContext context) {
        return context.getSupport() ? context.get(TypeValueContext.RESULT) : DEFAULT_NULL;
    }

    /**
     * 获取容器
     *
     * @return
     */
    default Map<String, Object> getContainer() {
        return new HashMap<>(8);
    }


    /**
     * 获取多个名称
     *
     * @return
     */
    default Set<String> getQualifiedNames() {
        return this.getContainer().keySet();
    }

    /**
     * 动态的部分是否支持
     *
     * @param context
     * @return
     */
    boolean isSupport(TypeValueContext context);
}
