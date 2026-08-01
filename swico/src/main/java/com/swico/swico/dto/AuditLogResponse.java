package com.swico.swico.dto;

import com.swico.swico.entity.AuditLog;

import java.time.LocalDateTime;

public record AuditLogResponse(
        Long id,
        String action,
        String entity,
        Long entityId,
        String username,
        String detail,
        LocalDateTime timestamp
) {
    public static AuditLogResponse fromEntity(AuditLog log) {
        return new AuditLogResponse(
                log.getId(),
                log.getAction(),
                log.getEntity(),
                log.getEntityId(),
                log.getUsername(),
                log.getDetail(),
                log.getTimestamp()
        );
    }
}
