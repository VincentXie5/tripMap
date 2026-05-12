package com.travel.plan.service.impl;

import com.travel.plan.service.FileService;
import io.minio.*;
import io.minio.errors.MinioException;
import io.minio.messages.Item;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.InputStream;

@Slf4j
@Service
@RequiredArgsConstructor
public class FileServiceImpl implements FileService {

    @Value("${minio.endpoint}")
    private String endpoint;

    @Value("${minio.access-key}")
    private String accessKey;

    @Value("${minio.secret-key}")
    private String secretKey;

    @Value("${minio.bucket}")
    private String bucket;

    private MinioClient minioClient;

    @PostConstruct
    public void init() {
        minioClient = MinioClient.builder()
                .endpoint(endpoint)
                .credentials(accessKey, secretKey)
                .build();

        try {
            boolean exists = minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucket).build());
            if (!exists) {
                minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucket).build());
                log.info("Created MinIO bucket: {}", bucket);
            }
        } catch (Exception e) {
            log.error("Failed to initialize MinIO bucket", e);
            throw new RuntimeException("MinIO initialization failed", e);
        }
    }

    @Override
    public void upload(String key, InputStream inputStream, String contentType, long size) {
        try {
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucket)
                            .object(key)
                            .stream(inputStream, size, -1)
                            .contentType(contentType)
                            .build()
            );
            log.debug("Uploaded file: {}", key);
        } catch (Exception e) {
            log.error("Failed to upload file: {}", key, e);
            throw new RuntimeException("File upload failed", e);
        }
    }

    @Override
    public InputStream download(String key) {
        try {
            return minioClient.getObject(
                    GetObjectArgs.builder()
                            .bucket(bucket)
                            .object(key)
                            .build()
            );
        } catch (Exception e) {
            log.error("Failed to download file: {}", key, e);
            throw new RuntimeException("File download failed", e);
        }
    }

    @Override
    public void delete(String key) {
        try {
            minioClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(bucket)
                            .object(key)
                            .build()
            );
            log.debug("Deleted file: {}", key);
        } catch (Exception e) {
            log.error("Failed to delete file: {}", key, e);
            throw new RuntimeException("File delete failed", e);
        }
    }

    @Override
    public void deleteByPrefix(String prefix) {
        try {
            Iterable<Result<Item>> results = minioClient.listObjects(
                    ListObjectsArgs.builder()
                            .bucket(bucket)
                            .prefix(prefix)
                            .build()
            );
            for (Result<Item> result : results) {
                Item item = result.get();
                minioClient.removeObject(
                        RemoveObjectArgs.builder()
                                .bucket(bucket)
                                .object(item.objectName())
                                .build()
                );
            }
            log.debug("Deleted files with prefix: {}", prefix);
        } catch (Exception e) {
            log.error("Failed to delete files by prefix: {}", prefix, e);
            throw new RuntimeException("File delete by prefix failed", e);
        }
    }
}
