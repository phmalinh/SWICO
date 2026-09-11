package com.swico.swico.dto;

public record ProductionReportLotDto(
        Long id,
        String lotNo,
        Integer inputQuantity,
        Integer goodQuantity,
        Integer defectQuantity,
        Integer internalDefectQuantity,
        Integer externalDefectQuantity
) {}
