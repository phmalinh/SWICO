package com.swico.swico.dto;

public record EmployeeSkillResponse(
        Long id,
        Long userId,
        String employeeCode,
        String employeeName,
        String jobTitle,
        String team,
        String skill,
        String hireDate,
        Long productId,
        String partName,
        String partNumber,
        Long processId,
        String process
) {}
