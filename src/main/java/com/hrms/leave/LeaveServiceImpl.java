package com.hrms.leave;

import com.hrms.common.exception.EmployeeNotFoundException;
import com.hrms.employee.Employee;
import com.hrms.employee.EmployeeRepository;
import com.hrms.leave.dto.ApplyLeaveRequest;
import com.hrms.leave.dto.LeaveBalanceResponse;
import com.hrms.leave.dto.LeaveRequestResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class LeaveServiceImpl implements LeaveService {

    private final LeaveRequestRepository leaveRequestRepository;
    private final LeaveBalanceRepository leaveBalanceRepository;
    private final EmployeeRepository employeeRepository;

    @Override
    @Transactional
    public LeaveRequestResponse applyLeave(Long employeeId,
                                           ApplyLeaveRequest request) {
        log.info("Employee {} applying for {} leave from {} to {}",
                employeeId, request.leaveType(), request.startDate(), request.endDate());

        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new EmployeeNotFoundException(employeeId));

        // Validation 1: end date must be on or after start date
        if (request.endDate().isBefore(request.startDate())) {
            throw new IllegalArgumentException(
                    "End date cannot be before start date");
        }

        // Validation 2: leave cannot be in the past
        if (request.startDate().isBefore(LocalDate.now())) {
            throw new IllegalArgumentException(
                    "Cannot apply for leave with a past start date");
        }

        // Validation 3: check for overlapping approved/pending leaves
        // WHY: without this check, an employee could have two approved leaves
        //      covering the same date — both deducting from their balance
        if (leaveRequestRepository.hasOverlappingLeave(
                employeeId, request.startDate(), request.endDate())) {
            throw new IllegalArgumentException(
                    "You already have a pending or approved leave " +
                            "overlapping with the requested dates");
        }

        // Validation 4: check leave balance (skip for UNPAID leave)
        if (request.leaveType() != LeaveType.UNPAID) {
            long requestedDays = ChronoUnit.DAYS.between(
                    request.startDate(), request.endDate()) + 1;

            LeaveBalance balance = leaveBalanceRepository
                    .findByEmployeeIdAndLeaveTypeAndYear(
                            employeeId, request.leaveType(),
                            request.startDate().getYear())
                    .orElseThrow(() -> new IllegalArgumentException(
                            "No leave balance found for " + request.leaveType() +
                                    " in " + request.startDate().getYear() +
                                    ". Contact HR to initialise your leave balance."));

            if (balance.getRemainingDays() < requestedDays) {
                throw new IllegalArgumentException(
                        "Insufficient leave balance. Available: " +
                                balance.getRemainingDays() + " days, Requested: " +
                                requestedDays + " days");
            }
        }

        LeaveRequest leaveRequest = LeaveRequest.builder()
                .employee(employee)
                .leaveType(request.leaveType())
                .startDate(request.startDate())
                .endDate(request.endDate())
                .reason(request.reason())
                .status(LeaveStatus.PENDING)
                .build();

        LeaveRequest saved = leaveRequestRepository.save(leaveRequest);
        log.info("Leave request {} created for employee {}", saved.getId(), employeeId);
        return LeaveRequestResponse.from(saved);
    }

    @Override
    @Transactional
    public LeaveRequestResponse approveLeave(Long leaveRequestId,
                                             Long approverEmployeeId) {
        log.info("Approving leave request {} by employee {}", leaveRequestId, approverEmployeeId);

        LeaveRequest leaveRequest = leaveRequestRepository.findById(leaveRequestId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Leave request not found: " + leaveRequestId));

        if (leaveRequest.getStatus() != LeaveStatus.PENDING) {
            throw new IllegalArgumentException(
                    "Only PENDING leave requests can be approved. " +
                            "Current status: " + leaveRequest.getStatus());
        }

        Employee approver = employeeRepository.findById(approverEmployeeId)
                .orElseThrow(() -> new EmployeeNotFoundException(approverEmployeeId));

        leaveRequest.setStatus(LeaveStatus.APPROVED);
        leaveRequest.setApprovedBy(approver);

        // Deduct from leave balance atomically
        // WHY atomic UPDATE instead of load-modify-save:
        // If two managers approve leave for the same employee at the same time,
        // load-modify-save could result in only one deduction being recorded.
        // The atomic UPDATE in the DB prevents this race condition.
        if (leaveRequest.getLeaveType() != LeaveType.UNPAID) {
            long days = ChronoUnit.DAYS.between(
                    leaveRequest.getStartDate(), leaveRequest.getEndDate()) + 1;

            int updated = leaveBalanceRepository.incrementUsedDays(
                    leaveRequest.getEmployee().getId(),
                    leaveRequest.getLeaveType(),
                    leaveRequest.getStartDate().getYear(),
                    (int) days);

            if (updated == 0) {
                throw new IllegalStateException(
                        "Failed to update leave balance — balance record not found");
            }
        }

        log.info("Leave request {} approved", leaveRequestId);
        return LeaveRequestResponse.from(leaveRequest);
    }

    @Override
    @Transactional
    public LeaveRequestResponse rejectLeave(Long leaveRequestId,
                                            Long approverEmployeeId,
                                            String reason) {
        LeaveRequest leaveRequest = leaveRequestRepository.findById(leaveRequestId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Leave request not found: " + leaveRequestId));

        if (leaveRequest.getStatus() != LeaveStatus.PENDING) {
            throw new IllegalArgumentException(
                    "Only PENDING leave requests can be rejected");
        }

        Employee approver = employeeRepository.findById(approverEmployeeId)
                .orElseThrow(() -> new EmployeeNotFoundException(approverEmployeeId));

        leaveRequest.setStatus(LeaveStatus.REJECTED);
        leaveRequest.setApprovedBy(approver);
        if (reason != null) leaveRequest.setReason(reason);

        // No balance deduction on rejection — request was never approved
        return LeaveRequestResponse.from(leaveRequest);
    }

    @Override
    @Transactional
    public LeaveRequestResponse cancelLeave(Long leaveRequestId, Long employeeId) {
        LeaveRequest leaveRequest = leaveRequestRepository.findById(leaveRequestId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Leave request not found: " + leaveRequestId));

        // Only the employee who applied can cancel
        if (!leaveRequest.getEmployee().getId().equals(employeeId)) {
            throw new IllegalArgumentException(
                    "You can only cancel your own leave requests");
        }

        if (leaveRequest.getStatus() == LeaveStatus.REJECTED ||
                leaveRequest.getStatus() == LeaveStatus.CANCELLED) {
            throw new IllegalArgumentException(
                    "Cannot cancel a " + leaveRequest.getStatus() + " leave request");
        }

        // If it was APPROVED, restore the balance
        boolean wasApproved = leaveRequest.getStatus() == LeaveStatus.APPROVED;
        leaveRequest.setStatus(LeaveStatus.CANCELLED);

        if (wasApproved && leaveRequest.getLeaveType() != LeaveType.UNPAID) {
            long days = ChronoUnit.DAYS.between(
                    leaveRequest.getStartDate(), leaveRequest.getEndDate()) + 1;
            leaveBalanceRepository.decrementUsedDays(
                    employeeId,
                    leaveRequest.getLeaveType(),
                    leaveRequest.getStartDate().getYear(),
                    (int) days);
        }

        return LeaveRequestResponse.from(leaveRequest);
    }

    @Override
    @Transactional(readOnly = true)
    public List<LeaveRequestResponse> getEmployeeLeaveHistory(Long employeeId) {
        if (!employeeRepository.existsById(employeeId)) {
            throw new EmployeeNotFoundException(employeeId);
        }
        return leaveRequestRepository.findByEmployeeId(employeeId)
                .stream()
                .map(LeaveRequestResponse::from)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<LeaveRequestResponse> getPendingLeaveRequests() {
        return leaveRequestRepository.findByStatus(LeaveStatus.PENDING)
                .stream()
                .map(LeaveRequestResponse::from)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<LeaveBalanceResponse> getEmployeeLeaveBalances(Long employeeId,
                                                               int year) {
        if (!employeeRepository.existsById(employeeId)) {
            throw new EmployeeNotFoundException(employeeId);
        }
        return leaveBalanceRepository.findByEmployeeIdAndYear(employeeId, year)
                .stream()
                .map(LeaveBalanceResponse::from)
                .toList();
    }
}