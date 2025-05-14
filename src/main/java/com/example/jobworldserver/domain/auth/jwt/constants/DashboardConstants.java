package com.example.jobworldserver.domain.auth.jwt.constants;

import java.util.HashMap;
import java.util.Map;

public class DashboardConstants {
    private static final Map<String, String> DASHBOARD_MESSAGES = new HashMap<>();

    static {
        DASHBOARD_MESSAGES.put("TEACHER", "교사 대시보드에 오신 것을 환영합니다.");
        DASHBOARD_MESSAGES.put("STUDENT", "학생 대시보드에 오신 것을 환영합니다.");
        DASHBOARD_MESSAGES.put("NORMAL", "일반 사용자 대시보드에 오신 것을 환영합니다.");
    }

    public static String getDashboardMessage(String role) {
        return DASHBOARD_MESSAGES.getOrDefault(role, "대시보드에 오신 것을 환영합니다.");
    }
}