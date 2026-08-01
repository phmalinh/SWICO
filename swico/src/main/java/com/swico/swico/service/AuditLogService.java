package com.swico.swico.service;

import com.swico.swico.dto.AuditLogResponse;
import com.swico.swico.entity.AuditLog;
import com.swico.swico.repository.AuditLogRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;

    public AuditLogService(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    @Transactional
    public AuditLogResponse record(String action, String entity, Long entityId, String username, String detail) {
        AuditLog log = new AuditLog();
        log.setAction(action);
        log.setEntity(entity);
        log.setEntityId(entityId);
        log.setUsername(username != null ? username : "SYSTEM");
        log.setDetail(detail);
        AuditLog saved = auditLogRepository.save(log);
        return AuditLogResponse.fromEntity(saved);
    }

    @Transactional(readOnly = true)
    public List<AuditLogResponse> getAllLogs() {
        return auditLogRepository.findAllByOrderByTimestampDesc().stream()
                .map(AuditLogResponse::fromEntity)
                .toList();
    }
}
