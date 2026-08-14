package com.swico.swico.dto;

import jakarta.validation.constraints.NotBlank;
import java.time.LocalDate;

public record EmployeeSkillUpsertRequest(
        Long userId,
        @NotBlank String employeeCode,
        String employeeName,
        String jobTitle,
        String team,
        String skill,
        LocalDate hireDate,
        Long productId,
        String partName,
        String partNumber,
        Long processId,
        String process
) {}
