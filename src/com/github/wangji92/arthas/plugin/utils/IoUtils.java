package com.github.wangji92.arthas.plugin.utils;

import com.google.common.io.BaseEncoding;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.vfs.VirtualFile;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

/**
 * @author 汪小哥
 * @date 17-08-2020
 */
public class IoUtils {
    private static final Logger LOG = Logger.getInstance(IoUtils.class);

    /**
     * 读取arthas 插件目录下的脚本文件
     *
     * @param filePath
     * @return
     */
    public static String getResourceFile(String filePath) {
        // 沙箱的文件地址在外面 插件中在jar包中有问题
        try (InputStream resourceAsStream = IoUtils.class.getClassLoader().getResourceAsStream(filePath)) {
            return IOUtils.toString(resourceAsStream, StandardCharsets.UTF_8);
        } catch (IOException e) {
            LOG.error("getResourceFile error", e);
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
            LOG.error("readFileToByteArray error", e);
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

    /**
     * 读取virtualFile text
     *
     * @param virtualFile
     * @return
     */
    public static String readVirtualFile(VirtualFile virtualFile) {
        assert virtualFile != null;
        try (InputStream inputStream = virtualFile.getInputStream()) {

            return IOUtils.toString(inputStream, StandardCharsets.UTF_8);
        } catch (Exception e) {
            LOG.error(" read virtual File  text error", e);
            throw new IllegalArgumentException("读取virtualFile 文件失败", e);
        }
    }
}
