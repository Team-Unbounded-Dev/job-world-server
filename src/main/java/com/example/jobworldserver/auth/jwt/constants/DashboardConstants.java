package com.example.jobworldserver.auth.jwt.constants;

import com.example.jobworldserver.auth.entity.Authority;

import java.util.HashMap;
import java.util.Map;

public class DashboardConstants {
    private static final Map<Authority, String> MAIN_PAGE_MESSAGES = new HashMap<>();

    static {
        MAIN_PAGE_MESSAGES.put(Authority.TEACHER, "교사 메인페이지에 오신 것을 환영합니다.");
        MAIN_PAGE_MESSAGES.put(Authority.STUDENT, "학생 메인페이지에 오신 것을 환영합니다.");
        MAIN_PAGE_MESSAGES.put(Authority.NORMAL, "일반 사용자 메인페이지에 오신 것을 환영합니다.");
    }

    public static String getMainPageMessage(Authority authority) {
        return MAIN_PAGE_MESSAGES.getOrDefault(authority, "메인페이지에 오신 것을 환영합니다.");
    }
}