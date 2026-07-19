package com.hrms.leave.dto;

import com.hrms.leave.LeaveRequest;
import com.hrms.leave.LeaveStatus;
import com.hrms.leave.LeaveType;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

public record LeaveRequestResponse(
        Long id,
        Long employeeId,
        String employeeName,
        LeaveType leaveType,
        LocalDate startDate,
        LocalDate endDate,
        long durationDays,
        LeaveStatus status,
        String reason,
        String approvedByName,
        LocalDateTime createdAt
) {
    public static LeaveRequestResponse from(LeaveRequest request) {
        return new LeaveRequestResponse(
                request.getId(),
                request.getEmployee().getId(),
                request.getEmployee().getName(),
                request.getLeaveType(),
                request.getStartDate(),
                request.getEndDate(),
                // Duration: end - start + 1 (inclusive of both days)
                ChronoUnit.DAYS.between(request.getStartDate(),
                        request.getEndDate()) + 1,
                request.getStatus(),
                request.getReason(),
                request.getApprovedBy() != null
                        ? request.getApprovedBy().getName() : null,
                request.getCreatedAt()
        );
    }
}