package com.example.jobworldserver.controller;

import com.example.jobworldserver.domain.auth.jwt.constants.DashboardConstants;
import com.example.jobworldserver.domain.auth.service.UserService;
import com.example.jobworldserver.dto.auth.common.ApiResponse;
import com.example.jobworldserver.dto.auth.request.LoginRequest;
import com.example.jobworldserver.dto.auth.response.AuthResponse;
import com.example.jobworldserver.dto.user.request.BulkRegisterRequest;
import com.example.jobworldserver.dto.user.request.RegisterRequest;
import com.example.jobworldserver.dto.student.response.StudentAccountResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/job-world")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping("/register/students/bulk")
    @PreAuthorize("hasAuthority('TEACHER')")
    public ResponseEntity<ApiResponse<List<StudentAccountResponse>>> bulkRegisterStudents(
            @Valid @RequestBody BulkRegisterRequest request) {
        List<StudentAccountResponse> responses = userService.registerStudentsBulk(
                request.getTeacherId(),
                request.getGrade(),
                request.getClassNum(),
                request.getCount(),
                request.getPassword(),
                request.getSchool()
        );
        return ResponseEntity.ok(ApiResponse.success(responses, "학생 계정 일괄 등록 성공"));
    }

    @GetMapping("/dashboard")
    @PreAuthorize("hasAnyAuthority('TEACHER', 'STUDENT', 'NORMAL')")
    public ResponseEntity<ApiResponse<String>> getDashboard(Authentication authentication) {
        String role = authentication.getAuthorities().iterator().next().getAuthority();
        String message = DashboardConstants.getDashboardMessage(role);
        return ResponseEntity.ok(ApiResponse.success(message, role + " 대시보드 접근 성공"));
    }

}