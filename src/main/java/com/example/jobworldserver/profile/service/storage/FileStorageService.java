package com.example.jobworldserver.profile.service.storage;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface FileStorageService {
    String storeFile(String fileName, MultipartFile file) throws IOException;
}
