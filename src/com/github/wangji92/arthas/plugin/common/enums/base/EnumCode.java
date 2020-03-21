package com.github.wangji92.arthas.plugin.common.enums.base;

import java.io.Serializable;

/**
 * @author 汪小哥
 * @date 21-03-2020
 */
public interface EnumCode<T> extends Serializable {
    /**
     * 获取枚举的返回Code
     *
     * @param <T>
     * @return
     */
    T getCode();
}
