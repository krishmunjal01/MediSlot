package com.medislot.service;

import com.medislot.dto.WaitingListRequest;
import com.medislot.model.WaitingListEntry;
import java.time.LocalDate;
import java.util.List;

public interface WaitingListService {
    WaitingListEntry addToWaitingList(WaitingListRequest request, Long currentUserId);
    List<WaitingListEntry> getActiveWaitingList();
    List<WaitingListEntry> getWaitingListForDoctorAndDate(Long doctorId, LocalDate date);
    void notifyNextInQueue(Long waitingId, Long currentUserId);
    void cancelWaitingEntry(Long waitingId, Long currentUserId);
    long getWaitingCount();
}
