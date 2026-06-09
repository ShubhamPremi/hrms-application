package com.hrms.domain.leave;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

public class LeaveCalculator {
    // 1. How many days is this leave request?
    public long calculateLeaveDays(LeaveRequest request) {
        // ChronoUnit.DAYS.between is exclusive of end date
        // A leave from July 1 to July 3 = 3 working days, so we add 1
        return ChronoUnit.DAYS.between(request.getStartDate(), request.getEndDate()) + 1;
    }

    // 2. Does this new request overlap with an existing approved leave?
    // This is a real production bug if not handled — employee gets double leave credit
    public boolean overlaps(LeaveRequest newRequest, LeaveRequest existingRequest) {
        return !newRequest.getEndDate().isBefore(existingRequest.getStartDate())
                && !newRequest.getStartDate().isAfter(existingRequest.getEndDate());
    }

    // 3. Is the employee still in probation? (first 3 months, no annual leave allowed)
    public boolean isInProbation(java.time.LocalDate joiningDate) {
        return LocalDate.now().isBefore(joiningDate.plusMonths(3));
    }

    // 4. Get all pending leave requests for a given employee
    public List<LeaveRequest> getPendingLeaves(Long employeeId, List<LeaveRequest> allLeaves) {
        return allLeaves.stream()
                .filter(lr -> lr.getEmployeeId().equals(employeeId))
                .filter(lr -> lr.getStatus() == LeaveStatus.PENDING)
                .collect(Collectors.toList());
    }

    // 5. How many annual leave days has an employee taken this year?
    public long countAnnualLeaveTakenThisYear(Long employeeId, List<LeaveRequest> allLeaves) {
        int currentYear = LocalDate.now().getYear();
        return allLeaves.stream()
                .filter(lr -> lr.getEmployeeId().equals(employeeId))
                .filter(lr -> lr.getLeaveType() == LeaveType.ANNUAL)
                .filter(lr -> lr.getStatus() == LeaveStatus.APPROVED)
                .filter(lr -> lr.getStartDate().getYear() == currentYear)
                .mapToLong(this::calculateLeaveDays)
                .sum();
    }
}
