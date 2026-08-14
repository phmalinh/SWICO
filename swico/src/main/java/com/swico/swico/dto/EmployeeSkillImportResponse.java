package com.swico.swico.dto;

public record EmployeeSkillImportResponse(
        int employeesImported,
        int productsScanned,
        int skillsImported
) {}
