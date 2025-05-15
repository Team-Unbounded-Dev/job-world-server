package com.example.jobworldserver.domain.profile.service.storage;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
public class LocalFileStorageService implements FileStorageService {

    @Value("${file.storage.local.directory:/uploads}")
    private String uploadDirectory;

    @Override
    public String storeFile(String fileName, byte[] fileData) {
        try {
            Path directory = Paths.get(uploadDirectory);
            if (!Files.exists(directory)) {
                Files.createDirectories(directory);
            }
            Path filePath = directory.resolve(fileName);
            Files.write(filePath, fileData);
            return filePath.toString();
        } catch (IOException e) {
            throw new RuntimeException("파일 저장 실패: " + e.getMessage());
        }
    }
}