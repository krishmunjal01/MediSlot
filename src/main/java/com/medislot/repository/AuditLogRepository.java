package com.medislot.repository;

import com.medislot.model.AuditLog;
import java.util.List;

public interface AuditLogRepository {
    Long save(AuditLog log);
    List<AuditLog> findRecent(int limit);
    List<AuditLog> findByEntityType(String entityType, String entityId);
}
