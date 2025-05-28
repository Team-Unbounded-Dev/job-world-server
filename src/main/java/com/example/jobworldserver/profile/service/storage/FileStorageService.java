package com.example.jobworldserver.profile.service.storage;

public interface FileStorageService {
    String storeFile(String fileName, byte[] fileData);
}