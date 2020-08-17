package com.github.wangji92.arthas.plugin.utils;

import com.google.common.io.BaseEncoding;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;

/**
 * @author 汪小哥
 * @date 17-08-2020
 */
public class IoUtils {
    /**
     * 读取arthas 插件目录下的脚本文件
     *
     * @param filePath
     * @return
     */
    public static String getResourceFile(String filePath) {
        try {
            URL resource = IoUtils.class.getClassLoader().getResource(filePath);
            if (resource == null) {
                throw new IllegalArgumentException("文件不存在");
            }
            return FileUtils.readFileToString(new File(resource.getFile()), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new IllegalArgumentException("读取文件异常");
        }

    }

    /**
     * 读取文件
     *
     * @param file
     * @return
     */
    public static byte[] readFileToByteArray(File file) {
        try {
            return FileUtils.readFileToByteArray(file);
        } catch (IOException e) {
            throw new IllegalArgumentException("读取文件异常");
        }
    }

    /**
     * 读取文件 base64 加密
     *
     * @param file
     * @return
     */
    public static String readFileToBase64String(File file) {
        byte[] bytes = IoUtils.readFileToByteArray(file);
        return BaseEncoding.base64().encode(bytes);
    }
}
