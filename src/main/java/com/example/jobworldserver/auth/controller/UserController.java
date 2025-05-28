package com.example.jobworldserver.auth.controller;

import com.example.jobworldserver.auth.entity.User;
import com.example.jobworldserver.auth.jwt.constants.DashboardConstants;
import com.example.jobworldserver.user.dto.request.BulkRegisterRequest;
import com.example.jobworldserver.user.dto.response.StudentAccountResponse;
import com.example.jobworldserver.dto.auth.common.ApiResponse;
import com.example.jobworldserver.user.service.StudentAccountService;
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

    private final StudentAccountService studentAccountService;

    @PostMapping("/bulk-register-students")
    @PreAuthorize("hasAuthority('TEACHER')")
    public ResponseEntity<ApiResponse<List<StudentAccountResponse>>> bulkRegisterStudents(
            @Valid @RequestBody BulkRegisterRequest request, Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        Long teacherId = user.getId();
        List<StudentAccountResponse> responses = studentAccountService.registerStudentsBulk(
                teacherId,
                request.getGrade(),
                request.getClassNum(),
                request.getCount(),
                request.getPassword(),
                request.getSchool()
        );
        return ResponseEntity.ok(ApiResponse.success(responses, "학생 일괄 등록 성공"));
    }

    @GetMapping("/dashboard")
    @PreAuthorize("hasAnyAuthority('TEACHER', 'STUDENT', 'NORMAL')")
    public ResponseEntity<ApiResponse<String>> getDashboard(Authentication authentication) {
        String role = authentication.getAuthorities().iterator().next().getAuthority();
        String message = DashboardConstants.getDashboardMessage(role);
        return ResponseEntity.ok(ApiResponse.success(message, role + " 대시보드 접근 성공"));
    }
}