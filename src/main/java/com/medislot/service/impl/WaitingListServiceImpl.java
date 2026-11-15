package com.medislot.service.impl;

import com.medislot.dto.WaitingListRequest;
import com.medislot.exception.ResourceNotFoundException;
import com.medislot.model.WaitingListEntry;
import com.medislot.model.enums.WaitingStatus;
import com.medislot.repository.WaitingListRepository;
import com.medislot.service.AuditService;
import com.medislot.service.WaitingListService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class WaitingListServiceImpl implements WaitingListService {

    private final WaitingListRepository waitingListRepository;
    private final AuditService auditService;

    public WaitingListServiceImpl(WaitingListRepository waitingListRepository, AuditService auditService) {
        this.waitingListRepository = waitingListRepository;
        this.auditService = auditService;
    }

    @Override
    @Transactional
    public WaitingListEntry addToWaitingList(WaitingListRequest request, Long currentUserId) {
        int nextPos = waitingListRepository.getNextQueuePosition(request.getDoctorId(), request.getRequestedDate());

        WaitingListEntry entry = new WaitingListEntry();
        entry.setDoctorId(request.getDoctorId());
        entry.setPatientId(request.getPatientId());
        entry.setRequestedDate(request.getRequestedDate());
        entry.setQueuePosition(nextPos);
        entry.setStatus(WaitingStatus.WAITING);
        entry.setNotes(request.getNotes());
        entry.setCreatedAt(LocalDateTime.now());

        Long id = waitingListRepository.save(entry);
        entry.setWaitingId(id);

        auditService.log(currentUserId, "ADD_WAITING_LIST", "WAITING_LIST",
                String.valueOf(id), "Added patient id=" + request.getPatientId() + " to waiting list at queue position #" + nextPos);

        return entry;
    }

    @Override
    public List<WaitingListEntry> getActiveWaitingList() {
        return waitingListRepository.findActiveQueue();
    }

    @Override
    public List<WaitingListEntry> getWaitingListForDoctorAndDate(Long doctorId, LocalDate date) {
        return waitingListRepository.findByDoctorAndDate(doctorId, date);
    }

    @Override
    @Transactional
    public void notifyNextInQueue(Long waitingId, Long currentUserId) {
        waitingListRepository.updateStatus(waitingId, WaitingStatus.NOTIFIED);
        auditService.log(currentUserId, "NOTIFY_WAITING", "WAITING_LIST",
                String.valueOf(waitingId), "Notified patient on waiting list that a slot opened");
    }

    @Override
    @Transactional
    public void cancelWaitingEntry(Long waitingId, Long currentUserId) {
        waitingListRepository.updateStatus(waitingId, WaitingStatus.CANCELLED);
        auditService.log(currentUserId, "CANCEL_WAITING", "WAITING_LIST",
                String.valueOf(waitingId), "Cancelled waiting list entry");
    }

    @Override
    public long getWaitingCount() {
        return waitingListRepository.countWaitingEntries();
    }
}

