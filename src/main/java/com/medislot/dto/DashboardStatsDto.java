package com.medislot.dto;

public class DashboardStatsDto {
    private long totalAppointmentsToday;
    private long upcomingAppointments;
    private long completedAppointments;
    private long cancelledAppointments;
    private long waitingCount;
    private long totalRegisteredPatients;
    private long activeDoctors;

    public DashboardStatsDto() {
    }

    public DashboardStatsDto(long totalAppointmentsToday, long upcomingAppointments,
                             long completedAppointments, long cancelledAppointments,
                             long waitingCount, long totalRegisteredPatients, long activeDoctors) {
        this.totalAppointmentsToday = totalAppointmentsToday;
        this.upcomingAppointments = upcomingAppointments;
        this.completedAppointments = completedAppointments;
        this.cancelledAppointments = cancelledAppointments;
        this.waitingCount = waitingCount;
        this.totalRegisteredPatients = totalRegisteredPatients;
        this.activeDoctors = activeDoctors;
    }

    public long getTotalAppointmentsToday() {
        return totalAppointmentsToday;
    }

    public void setTotalAppointmentsToday(long totalAppointmentsToday) {
        this.totalAppointmentsToday = totalAppointmentsToday;
    }

    public long getUpcomingAppointments() {
        return upcomingAppointments;
    }

    public void setUpcomingAppointments(long upcomingAppointments) {
        this.upcomingAppointments = upcomingAppointments;
    }

    public long getCompletedAppointments() {
        return completedAppointments;
    }

    public void setCompletedAppointments(long completedAppointments) {
        this.completedAppointments = completedAppointments;
    }

    public long getCancelledAppointments() {
        return cancelledAppointments;
    }

    public void setCancelledAppointments(long cancelledAppointments) {
        this.cancelledAppointments = cancelledAppointments;
    }

    public long getWaitingCount() {
        return waitingCount;
    }

    public void setWaitingCount(long waitingCount) {
        this.waitingCount = waitingCount;
    }

    public long getTotalRegisteredPatients() {
        return totalRegisteredPatients;
    }

    public void setTotalRegisteredPatients(long totalRegisteredPatients) {
        this.totalRegisteredPatients = totalRegisteredPatients;
    }

    public long getActiveDoctors() {
        return activeDoctors;
    }

    public void setActiveDoctors(long activeDoctors) {
        this.activeDoctors = activeDoctors;
    }
}
