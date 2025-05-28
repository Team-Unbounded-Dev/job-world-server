package com.example.jobworldserver.user.service;

import com.example.jobworldserver.user.dto.response.StudentAccountResponse;
import java.util.List;

public interface StudentAccountService {
    List<StudentAccountResponse> createStudentAccounts(int grade, int classNum, int studentCount);
    List<StudentAccountResponse> registerStudentsBulk(Long teacherId, int grade, int classNum, int count, String password, String school);
}