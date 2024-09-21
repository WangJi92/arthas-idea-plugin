package com.github.idea.arthas.plugin.common.enums.base;

import java.util.Objects;
import java.util.Optional;

/**
 * https://blog.csdn.net/u012881904/article/details/104763791
 * @author 汪小哥
 * @date 21-03-2020
 */
public interface EnumCodeMsg<T> extends EnumCode<T> {
    /**
     * 获取枚举的备注信息
     * @return
     */
    String getEnumMsg();

    /**
     * 根据code 获取到当前的枚举
     *
     * @param enumClass
     * @param code
     * @param <E>
     * @param <T>
     * @return
     */
     static <E extends Enum<E> & EnumCodeMsg<T>, T> Optional<E> getEnumByCode(Class<E> enumClass, T code) {
        if (code == null) {
            return Optional.empty();
        }
        for (E enumConstant : enumClass.getEnumConstants()) {
            if (Objects.equals(enumConstant.getCode(), code)) {
                return Optional.of(enumConstant);
            }
        }
        return Optional.empty();
    }

    /**
     * 根据code 获取到枚举的msg的信息
     *
     * @param enumClass
     * @param code
     * @param defaultMsg 默认返回值
     * @param <E>
     * @param <T>
     * @return
     */
     static <E extends Enum<E> & EnumCodeMsg<T>, T> String getEnumMsgByCode(Class<E> enumClass, T code, String defaultMsg) {
        Optional<E> enumByCode = EnumCodeMsg.getEnumByCode(enumClass, code);
        if (enumByCode.isPresent()) {
            return enumByCode.get().getEnumMsg();
        }
        return defaultMsg;
    }

    /**
     * 根据code 获取到枚举的msg的信息,默认返回为空
     * @param enumClass
     * @param code
     * @param <E>
     * @param <T>
     * @return
     */
    static <E extends Enum<E> & EnumCodeMsg<T>, T> String getEnumMsgByCode(Class<E> enumClass, T code) {
        return EnumCodeMsg.getEnumMsgByCode(enumClass,code,"");
    }

    /**
     * 这个是否是一个有效的code
     *
     * @param enumClass
     * @param code
     * @param <E>
     * @param <T>
     * @return
     */
    static <E extends Enum<E> & EnumCodeMsg<T>, T> boolean isValidEnumCode(Class<E> enumClass, T code) {
        Optional<E> enumByCode = EnumCodeMsg.getEnumByCode(enumClass, code);
        return enumByCode.isPresent();
    }
}