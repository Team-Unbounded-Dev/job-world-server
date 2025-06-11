package com.example.jobworldserver.profile.service.storage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;

import java.io.IOException;

@Service
public class S3FileStorageService implements FileStorageService {

    private static final Logger logger = LoggerFactory.getLogger(S3FileStorageService.class);

    private final S3Client s3Client;

    @Value("${cloud.aws.s3.bucket}")
    private String bucketName;

    @Value("${cloud.aws.region.static}")
    private String region;

    public S3FileStorageService(S3Client s3Client) {
        this.s3Client = s3Client;
    }

    @Override
    public String storeFile(String fileName, MultipartFile file) throws IOException {
        try {
            String key = "profiles/" + fileName;
            logger.info("S3에 파일 업로드 시작: 버킷={}, 키={}", bucketName, key);

            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .contentType(file.getContentType())
                    .contentLength(file.getSize())
                    .build();

            s3Client.putObject(putObjectRequest, RequestBody.fromInputStream(file.getInputStream(), file.getSize()));

            String publicUrl = String.format("https://%s.s3.%s.amazonaws.com/%s", bucketName, region, key);

            logger.info("S3 파일 업로드 성공: 퍼블릭 URL={}", publicUrl);

            return publicUrl;
        } catch (S3Exception e) {
            logger.error("S3 파일 업로드 실패: 파일명={}, S3 오류={}", fileName, e.awsErrorDetails().errorMessage(), e);
            throw new RuntimeException("파일 저장 실패: " + e.awsErrorDetails().errorMessage());
        } catch (Exception e) {
            logger.error("S3 파일 업로드 실패: 파일명={}, 오류={}", fileName, e.getMessage(), e);
            throw new RuntimeException("파일 저장 실패: " + e.getMessage());
        }
    }
}