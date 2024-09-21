package com.github.idea.arthas.plugin.common.exception;

/**
 * 没有找到编译的文件
 *
 * @author 汪小哥
 * @date 21-12-2020
 */
public class CompilerFileNotFoundException extends RuntimeException {
    public CompilerFileNotFoundException(String message) {
        super(message);
    }

    public CompilerFileNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
