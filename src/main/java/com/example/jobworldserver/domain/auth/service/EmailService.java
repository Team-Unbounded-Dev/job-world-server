package com.example.jobworldserver.domain.auth.service;

import com.example.jobworldserver.dto.auth.response.EmailVerificationResponse;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;


@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private static final int CODE_LENGTH = 6;
    private static final String CHARACTERS = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ";

    private final JavaMailSender mailSender;
    private final EmailVerificationService verificationService;

    public String generateVerificationCode() {
        StringBuilder code = new StringBuilder(CODE_LENGTH);
        for (int i = 0; i < CODE_LENGTH; i++) {
            code.append(CHARACTERS.charAt((int) (Math.random() * CHARACTERS.length())));
        }
        return code.toString();
    }

    public void sendVerificationEmail(String to, String verificationCode) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        helper.setFrom("testdisk1110@gmail.com");
        helper.setTo(to);
        helper.setSubject("이메일 인증 코드");
        helper.setText(generateHtmlContent(verificationCode), true);

        mailSender.send(message);
        verificationService.storeVerificationCode(to, verificationCode);
    }

    private String generateHtmlContent(String verificationCode) {
        return """
                <!DOCTYPE html>
                <html lang="ko">
                <head>
                    <meta charset="UTF-8">
                    <meta name="viewport" content="width=device-width, initial-scale=1.0">
                    <title>이메일 인증 코드</title>
                    <style>
                        body {font-family:'Arial',sans-serif;background-color:#f4f4f4;margin:0;padding:0;color:#000000}
                        .container {max-width:600px;margin:40px auto;background-color:#ffffff;border-radius:10px;box-shadow:0 4px 8px rgba(0,0,0,0.1);overflow:hidden}
                        .header {background-color:#FFC107;padding:20px;text-align:center;color:#000000}
                        .header h1 {margin:0;font-size:24px}
                        .content {padding:30px;text-align:center}
                        .content p {font-size:16px;line-height:1.6;margin:0 0 20px}
                        .code-box {display:inline-block;background-color:#FFC107;color:#000000;padding:15px 25px;font-size:24px;font-weight:bold;border-radius:5px;margin:20px 0;letter-spacing:5px}
                        .footer {background-color:#f4f4f4;padding:15px;text-align:center;font-size:14px;color:#555555}
                    </style>
                </head>
                <body>
                    <div class="container">
                        <div class="header"><h1>이메일 인증 코드</h1></div>
                        <div class="content"><p>안녕하세요!</p><p>아래의 인증 코드를 사용하여 이메일 인증을 완료해 주세요.</p><div class="code-box">%s</div><p>이 코드는 10분 이내에 입력해야 합니다.</p></div>
                        <div class="footer"><p>© 2025 JobWorld. All rights reserved.</p></div>
                    </div>
                </body>
                </html>
                """.formatted(verificationCode);
    }

    public EmailVerificationResponse verifyEmail(String email) throws MessagingException {
        String verificationCode = generateVerificationCode();
        sendVerificationEmail(email, verificationCode);
        return new EmailVerificationResponse(verificationCode, "인증 코드가 이메일로 전송되었습니다.");
    }
}