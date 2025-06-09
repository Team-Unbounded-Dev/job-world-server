package com.example.jobworldserver.profile.service.storage;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;

@Service
public class S3FileStorageService implements FileStorageService {

    private static final Logger logger = LoggerFactory.getLogger(S3FileStorageService.class);

    private final AmazonS3 amazonS3;

    @Value("${cloud.aws.s3.bucket}")
    private String bucketName;

    public S3FileStorageService(AmazonS3 amazonS3) {
        this.amazonS3 = amazonS3;
    }

    @Override
    public String storeFile(String fileName, byte[] fileData) {
        try {
            String key = "profiles/" + fileName;
            logger.info("S3에 파일 업로드 시작: 버킷={}, 키={}", bucketName, key);

            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentLength(fileData.length);
            metadata.setContentType("image/jpeg");

            PutObjectRequest putObjectRequest = new PutObjectRequest(bucketName, key, new ByteArrayInputStream(fileData), metadata)
                    .withCannedAcl(CannedAccessControlList.PublicRead);

            amazonS3.putObject(putObjectRequest);
            String fileUrl = amazonS3.getUrl(bucketName, key).toString();
            logger.info("S3 파일 업로드 성공: URL={}", fileUrl);

            return fileUrl;
        } catch (Exception e) {
            logger.error("S3 파일 업로드 실패: 파일명={}", fileName, e);
            throw new RuntimeException("파일 저장 실패: " + e.getMessage());
        }
    }
}