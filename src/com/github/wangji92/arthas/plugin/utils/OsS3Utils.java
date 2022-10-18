package com.github.wangji92.arthas.plugin.utils;


import com.aliyun.oss.internal.OSSUtils;
import com.amazonaws.ClientConfiguration;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.github.wangji92.arthas.plugin.setting.AppSettingsState;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.Date;


/**
 * aws s3 本地使用 mini io
 * http://docs.minio.org.cn/docs/master/how-to-use-aws-sdk-for-java-with-minio-server
 *
 * @author 汪小哥
 * @date 16-10-2022
 */
public class OsS3Utils {
    private static final Logger LOG = Logger.getInstance(OsS3Utils.class);


    /**
     * 获取oss 客户端
     *
     * @return
     */
    public static AmazonS3 buildS3Client(String endpoint, String accessKeyId, String accessKeySecret, String bucketName, String region, String directoryPrefix) {
        if (StringUtils.isBlank(endpoint)) {
            throw new IllegalArgumentException("配置arthas os s3 endpoint Error");
        }
        if (StringUtils.isBlank(accessKeyId)) {
            throw new IllegalArgumentException("配置arthas os s3 accessKeyId Error");
        }
        if (StringUtils.isBlank(accessKeySecret)) {
            throw new IllegalArgumentException("配置arthas os s3 accessKeySecret Error");
        }
        if (StringUtils.isBlank(bucketName) || !OSSUtils.validateBucketName(bucketName)) {
            throw new IllegalArgumentException("配置arthas s3  bucketName Error");
        }
        // 校验key的信息
        if (StringUtils.isNotBlank(directoryPrefix) && !OSSUtils.validateObjectKey(OSSUtils.makeResourcePath(directoryPrefix))) {
            throw new IllegalArgumentException("配置arthas  s3 directoryPrefix Error");
        }
        AWSCredentials credentials = new BasicAWSCredentials(accessKeyId, accessKeySecret);
        ClientConfiguration clientConfiguration = new ClientConfiguration();
        return AmazonS3ClientBuilder
                .standard()
                .withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration(endpoint, region))
                .withPathStyleAccessEnabled(true)
                .withClientConfiguration(clientConfiguration)
                .withCredentials(new AWSStaticCredentialsProvider(credentials))
                .build();
    }

    public static AmazonS3 buildS3Client(Project project) {
        AppSettingsState instance = AppSettingsState.getInstance(project);
        if (!instance.awsS3) {
            throw new IllegalArgumentException("arthas idea plugin object Object Storage Setting s3");
        }
        return buildS3Client(instance.s3Endpoint, instance.s3AccessKeyId, instance.s3AccessKeySecret, instance.s3BucketName, instance.s3Region,
                instance.s3DirectoryPrefix);
    }

    /**
     * 检查是否存在
     *
     * @param bucketName
     * @param s3Client
     */
    public static void checkBuckNameExist(String bucketName, AmazonS3 s3Client) {
        // 检查是否存在
        ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
        try {
            // https://github.com/WangJi92/arthas-idea-plugin/issues/79
            // https://youtrack.jetbrains.com/issue/BDIDE-1894/javalangIllegalArgumentException-awssdkconfigoverridejson-if-there-is-S3-connection-in-BDT-Panel
            Thread.currentThread().setContextClassLoader(null);
            if (!s3Client.doesBucketExistV2(bucketName)) {
                throw new IllegalArgumentException("s3 bucketName not found");
            }
        } catch (Exception e) {
            LOG.info("checkBuckNameExist", e);
            throw new IllegalArgumentException("s3 bucketName not found" + e.getMessage());
        } finally {
            Thread.currentThread().setContextClassLoader(contextClassLoader);
        }
    }

    /**
     * 上传文件流
     *
     * @param s3
     * @param filePath xxxPath/xxxFile.jpg
     * @return 返回 oss key的信息
     */
    public static String putFile(AmazonS3 s3, String bucketName, String filePath, InputStream inputStream) {

        String urlEncodeKeyPath = OSSUtils.makeResourcePath(filePath);
        if (!OSSUtils.validateObjectKey(OSSUtils.makeResourcePath(filePath))) {
            throw new IllegalArgumentException("配置arthas 对象存储 上传错误 fileKey 错误");
        }
        ObjectMetadata objectMetadata = new ObjectMetadata();
        objectMetadata.setContentType("application/octet-stream");
        PutObjectRequest putObjectRequest = new PutObjectRequest(bucketName, urlEncodeKeyPath, inputStream, objectMetadata);
        ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
        try {
            // https://youtrack.jetbrains.com/issue/BDIDE-1894/javalangIllegalArgumentException-awssdkconfigoverridejson-if-there-is-S3-connection-in-BDT-Panel
            Thread.currentThread().setContextClassLoader(null);
            s3.putObject(putObjectRequest);
        } catch (Exception e) {
            LOG.info("putFile", e);
            throw new IllegalArgumentException("上传文件到对象存储错误", e);
        } finally {
            Thread.currentThread().setContextClassLoader(contextClassLoader);
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
    public static String putFile(AmazonS3 ossClient, String bucketName, String filePath, String content) {
        return putFile(ossClient, bucketName, filePath, new ByteArrayInputStream(content.getBytes()));
    }


    /**
     * Generates a signed url for accessing the {@link AmazonS3} with HTTP GET method.
     *
     * @param ossClient
     * @param bucketName
     * @param key
     * @param expiration
     * @return
     */
    public static String generatePresignedUrl(AmazonS3 ossClient, String bucketName, String key, Date expiration) {
        if (expiration == null) {
            expiration = new Date(System.currentTimeMillis() + 3600L * 1000);
        }
        ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
        try {
            // https://youtrack.jetbrains.com/issue/BDIDE-1894/javalangIllegalArgumentException-awssdkconfigoverridejson-if-there-is-S3-connection-in-BDT-Panel
            Thread.currentThread().setContextClassLoader(null);
            URL url = ossClient.generatePresignedUrl(bucketName, key, expiration);
            return url.toString();
        } catch (Exception e) {
            LOG.info("generatePresignedUrl", e);
            throw new IllegalArgumentException("generatePresignedUrl error", e);
        } finally {
            Thread.currentThread().setContextClassLoader(contextClassLoader);
        }
    }

//    public static void main(String[] args) throws FileNotFoundException {
//        AmazonS3 amazonS3 = OsS3Utils.buildOssClient("http://localhost:9000",
//                "Ez0doWOOZpmWM17x", "eSf1TGTPCdn4mtUT2i8aO8m2uvbVYSYj",
//                "demo", "test-region", "arthas/");
//
//        OsS3Utils.checkBuckNameExist("demo",amazonS3);
//
//        OsS3Utils.putFile(amazonS3, "demo", "arthas/" + UUID.randomUUID().toString(),
//                new FileInputStream(new File("/Users/wangji/Documents/project/arthas-idea-plugin/arthas-idea-plugin.zip")));
//
//    }


}
