package com.medislot.service;

import com.medislot.model.AuditLog;
import java.util.List;

public interface AuditService {
    void log(Long userId, String action, String entityType, String entityId, String details);
    List<AuditLog> getRecentLogs(int limit);
}

