package com.example.jobworldserver.auth.service;

import com.example.jobworldserver.dto.auth.response.EmailVerificationCheckResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class EmailVerificationService {

    private static final long EXPIRY_MINUTES = 10;
    private final Map<String, VerificationData> codeStore = new ConcurrentHashMap<>();

    public void storeVerificationCode(String email, String verificationCode) {
        Instant expiryTime = Instant.now().plusSeconds(TimeUnit.MINUTES.toSeconds(EXPIRY_MINUTES));
        codeStore.put(email, new VerificationData(verificationCode, expiryTime));
        log.debug("Stored verification code for email: {}, code: {}, expiry: {}", email, verificationCode, expiryTime);
    }

    public EmailVerificationCheckResponse checkVerificationCode(String email, String verificationCode) {
        VerificationData data = codeStore.get(email);
        if (data == null) {
            return new EmailVerificationCheckResponse(false, "인증 코드가 존재하지 않습니다.");
        }

        if (Instant.now().isAfter(data.getExpiryTime())) {
            codeStore.remove(email);
            return new EmailVerificationCheckResponse(false, "인증 코드가 만료되었습니다.");
        }

        if (!data.getVerificationCode().equals(verificationCode)) {
            return new EmailVerificationCheckResponse(false, "잘못된 인증 코드입니다.");
        }

        codeStore.remove(email); // 성공 후 제거
        return new EmailVerificationCheckResponse(true, "인증 코드가 확인되었습니다.");
    }

    private static class VerificationData {
        private final String verificationCode;
        private final Instant expiryTime;

        public VerificationData(String verificationCode, Instant expiryTime) {
            this.verificationCode = verificationCode;
            this.expiryTime = expiryTime;
        }

        public String getVerificationCode() {
            return verificationCode;
        }

        public Instant getExpiryTime() {
            return expiryTime;
        }
    }
}