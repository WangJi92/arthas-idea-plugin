package com.github.idea.arthas.plugin.utils;

import com.aliyun.oss.ClientException;
import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.OSSException;
import com.aliyun.oss.internal.OSSUtils;
import com.aliyun.oss.model.BucketInfo;
import com.aliyun.oss.model.OSSObject;
import com.aliyun.oss.model.PutObjectRequest;
import com.github.idea.arthas.plugin.setting.AppSettingsState;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.Date;

/**
 * aliyun oss 操作类
 *
 * @author 汪小哥
 * @date 18-08-2020
 */
public class AliyunOssUtils {
    private static final Logger LOG = Logger.getInstance(AliyunOssUtils.class);

    /**
     * 获取oss 客户端
     *
     * @return
     */
    public static OSS buildOssClient(String endpoint, String accessKeyId, String accessKeySecret, String bucketName, String directoryPrefix) {
        if (StringUtils.isBlank(endpoint)) {
            throw new IllegalArgumentException("配置arthas aliyun oss参数 endpoint Error");
        }
        if (StringUtils.isBlank(accessKeyId)) {
            throw new IllegalArgumentException("配置arthas aliyun oss参数 accessKeyId Error");
        }
        if (StringUtils.isBlank(accessKeySecret)) {
            throw new IllegalArgumentException("配置arthas aliyun oss参数 accessKeySecret Error");
        }
        if (StringUtils.isBlank(bucketName) || !OSSUtils.validateBucketName(bucketName)) {
            throw new IllegalArgumentException("配置arthas aliyun oss参数 bucketName Error");
        }
        // 校验key的信息
        if (StringUtils.isNotBlank(directoryPrefix) && !OSSUtils.validateObjectKey(OSSUtils.makeResourcePath(directoryPrefix))) {
            throw new IllegalArgumentException("配置arthas aliyun oss参数 directoryPrefix Error");
        }
        return new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);
    }

    public static OSS buildOssClient(Project project) {
        AppSettingsState instance = AppSettingsState.getInstance(project);
        if (!instance.aliYunOss) {
            throw new IllegalArgumentException("配置arthas idea plugin Hot Redefine Setting 阿里云oss");
        }
        return buildOssClient(instance.endpoint, instance.accessKeyId, instance.accessKeySecret, instance.bucketName, instance.directoryPrefix);
    }

    /**
     * 检查是否存在
     *
     * @param bucketName
     * @param ossClient
     */
    public static void checkBuckNameExist(String bucketName, OSS ossClient) {
        // 检查是否存在
        BucketInfo bucketInfo = null;
        try {
            bucketInfo = ossClient.getBucketInfo(bucketName);
            if (bucketInfo == null) {
                throw new IllegalArgumentException("配置aliyun oss参数错误 无法获取 bucketName");
            }
        } catch (OSSException | ClientException e) {
            LOG.info("checkBuckNameExist", e);
            throw new IllegalArgumentException("配置aliyun oss 检测bucketName 错误 " + e.getMessage());
        }
    }

    /**
     * 上传文件流
     *
     * @param ossClient
     * @param filePath  xxxPath/xxxFile.jpg
     * @return 返回 oss key的信息
     */
    public static String putFile(OSS ossClient, String bucketName, String filePath, InputStream inputStream) {

        String urlEncodeKeyPath = OSSUtils.makeResourcePath(filePath);
        if (!OSSUtils.validateObjectKey(OSSUtils.makeResourcePath(filePath))) {
            throw new IllegalArgumentException("配置arthas aliyun oss 上传错误 fileKey 错误");
        }
        PutObjectRequest putObjectRequest = new PutObjectRequest(bucketName, urlEncodeKeyPath, inputStream);
        try {
            ossClient.putObject(putObjectRequest);
        } catch (OSSException | ClientException e) {
            LOG.info("putFile", e);
            throw new IllegalArgumentException("上传文件到oss 错误");
        }

        return urlEncodeKeyPath;
    }

    /**
     * 上传字符串到oss
     *
     * @param ossClient
     * @param bucketName
     * @param filePath
     * @param content
     * @return
     */
    public static String putFile(OSS ossClient, String bucketName, String filePath, String content) {
        return putFile(ossClient, bucketName, filePath, new ByteArrayInputStream(content.getBytes()));
    }


    /**
     * Generates a signed url for accessing the {@link OSSObject} with HTTP GET method.
     *
     * @param ossClient
     * @param bucketName
     * @param key
     * @param expiration
     * @return
     */
    public static String generatePresignedUrl(OSS ossClient, String bucketName, String key, Date expiration) {
        if (expiration == null) {
            expiration = new Date(System.currentTimeMillis() + 3600L * 1000);
        }
        try {
            URL url = ossClient.generatePresignedUrl(bucketName, key, expiration);
            return url.toString();
        } catch (Exception e) {
            LOG.info("generatePresignedUrl", e);
            throw new IllegalArgumentException("获取oss 文件错误");
        }
    }


}
