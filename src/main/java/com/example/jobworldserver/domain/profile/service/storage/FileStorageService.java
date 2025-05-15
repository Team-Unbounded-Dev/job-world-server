package com.example.jobworldserver.domain.profile.service.storage;

public interface FileStorageService {
    String storeFile(String fileName, byte[] fileData);
}