package com.github.idea.arthas.plugin.utils;

import com.google.common.io.BaseEncoding;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.vfs.VirtualFile;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.CodeSource;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * @author 汪小哥
 * @date 17-08-2020
 */
public class IoUtils {
    private static final Logger LOG = Logger.getInstance(IoUtils.class);

    private static Map<String, String> RESOURCE_MAP = new HashMap<>();

    /**
     * 读取arthas 插件目录下的脚本文件
     *
     * @param filePath
     * @return
     */
    public static String getResourceFile(String filePath) {
        // 沙箱的文件地址在外面 插件中在jar包中有问题
        filePath = filePath.substring(1, filePath.length());
        try (InputStream resourceAsStream = IoUtils.class.getClassLoader().getResourceAsStream(filePath)) {
            return IOUtils.toString(resourceAsStream, StandardCharsets.UTF_8);
        } catch (Exception e) {
            LOG.info("getResourceFile error", e);
            try {
                String content = RESOURCE_MAP.get(filePath);
                if (StringUtils.isNotBlank(content)) {
                    return content;
                }
                // 获取jar包的地址
                final CodeSource codeSource = IoUtils.class.getProtectionDomain().getCodeSource();
                final String path = codeSource.getLocation().getPath();
                JarFile jarFile = new JarFile(path);
                final Enumeration<JarEntry> entries = jarFile.entries();

                //读取jar包的内容... 补偿一下
                while (entries.hasMoreElements()) {
                    final JarEntry jarEntry = entries.nextElement();
                    if (jarEntry.getName().endsWith(filePath)) {
                        try (InputStream input = jarFile.getInputStream(jarEntry)) {
                            content = IOUtils.toString(input, StandardCharsets.UTF_8);
                            RESOURCE_MAP.put(filePath, content);
                        }
                        return content;
                    }
                }

            } catch (Exception ioException) {
                LOG.error("getResourceFile error", e);
                throw new IllegalArgumentException("获取模板信息异常", e);
            }
        }
        return "";
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
        byte[] readFileToByteArray = IoUtils.readFileToByteArray(file);
        return BaseEncoding.base64().encode(readFileToByteArray);
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
