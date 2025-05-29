package com.example.jobworldserver.auth.controller;

import com.example.jobworldserver.auth.entity.Authority;
import com.example.jobworldserver.auth.entity.User;
import com.example.jobworldserver.auth.jwt.constants.DashboardConstants;
import com.example.jobworldserver.user.dto.request.BulkRegisterRequest;
import com.example.jobworldserver.user.dto.response.StudentAccountResponse;
import com.example.jobworldserver.dto.auth.common.ApiResponse;
import com.example.jobworldserver.user.service.StudentAccountService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "User Management", description = "사용자 관리 API")
@RestController
@RequestMapping("/job-world")
@RequiredArgsConstructor
public class UserController {

    private final StudentAccountService studentAccountService;

    @Operation(summary = "학생 일괄 등록", description = "TEACHER 권한을 가진 사용자가 학생 계정을 일괄 등록합니다.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "학생 일괄 등록 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "TEACHER 권한 필요")
    })
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

    @Operation(summary = "메인 페이지 접근", description = "권한에 따라 메인 페이지 메시지를 반환합니다.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "메인 페이지 접근 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "TEACHER, STUDENT, NORMAL 권한 필요")
    })
    @GetMapping("/main")
    @PreAuthorize("hasAnyAuthority('TEACHER', 'STUDENT', 'NORMAL')")
    public ResponseEntity<ApiResponse<String>> getMainPage(Authentication authentication) {
        String authorityStr = authentication.getAuthorities().iterator().next().getAuthority();
        Authority authority;
        try {
            authority = Authority.valueOf(authorityStr);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.ok(ApiResponse.success("메인페이지에 오신 것을 환영합니다.", "알 수 없는 권한으로 메인페이지 접근 성공"));
        }
        String message = DashboardConstants.getMainPageMessage(authority);
        return ResponseEntity.ok(ApiResponse.success(message, authorityStr + " 메인페이지 접근 성공"));
    }
}