package com.medislot.repository;

import com.medislot.model.WaitingListEntry;
import com.medislot.model.enums.WaitingStatus;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface WaitingListRepository {
    Optional<WaitingListEntry> findById(Long waitingId);
    List<WaitingListEntry> findByDoctorAndDate(Long doctorId, LocalDate date);
    List<WaitingListEntry> findActiveQueue();
    Long save(WaitingListEntry entry);
    int updateStatus(Long waitingId, WaitingStatus status);
    int getNextQueuePosition(Long doctorId, LocalDate date);
    long countWaitingEntries();
}

