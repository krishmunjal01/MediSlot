package com.medislot.service.impl;

import com.medislot.model.AuditLog;
import com.medislot.repository.AuditLogRepository;
import com.medislot.service.AuditService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class AuditServiceImpl implements AuditService {

    private final AuditLogRepository auditLogRepository;

    public AuditServiceImpl(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    @Override
    public void log(Long userId, String action, String entityType, String entityId, String details) {
        AuditLog auditLog = new AuditLog(null, userId, action, entityType, entityId, details, "127.0.0.1", LocalDateTime.now());
        try {
            auditLogRepository.save(auditLog);
        } catch (Exception ignored) {
            // Audit failures should not abort the main transaction
        }
    }

    @Override
    public List<AuditLog> getRecentLogs(int limit) {
        return auditLogRepository.findRecent(limit);
    }
}
