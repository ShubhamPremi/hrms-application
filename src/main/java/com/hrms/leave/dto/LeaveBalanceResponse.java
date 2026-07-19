package com.hrms.leave.dto;

import com.hrms.leave.LeaveBalance;
import com.hrms.leave.LeaveType;

public record LeaveBalanceResponse(
        LeaveType leaveType,
        int year,
        int totalDays,
        int usedDays,
        int remainingDays
) {
    public static LeaveBalanceResponse from(LeaveBalance balance) {
        return new LeaveBalanceResponse(
                balance.getLeaveType(),
                balance.getYear(),
                balance.getTotalDays(),
                balance.getUsedDays(),
                balance.getRemainingDays()
        );
    }
}