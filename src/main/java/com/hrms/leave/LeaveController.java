package com.hrms.leave;

import com.hrms.common.response.ApiResponse;
import com.hrms.leave.dto.ApplyLeaveRequest;
import com.hrms.leave.dto.LeaveBalanceResponse;
import com.hrms.leave.dto.LeaveRequestResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/v1/leave")
@RequiredArgsConstructor
public class LeaveController {

    private final LeaveService leaveService;

    // Employee applies for leave
    @PostMapping("/apply/{employeeId}")
    public ResponseEntity<ApiResponse<LeaveRequestResponse>> applyLeave(
            @PathVariable Long employeeId,
            @Valid @RequestBody ApplyLeaveRequest request) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Leave request submitted successfully",
                        leaveService.applyLeave(employeeId, request)));
    }

    // HR/Admin approves a leave request
    @PutMapping("/{leaveRequestId}/approve")
    public ResponseEntity<ApiResponse<LeaveRequestResponse>> approveLeave(
            @PathVariable Long leaveRequestId,
            @RequestParam Long approverEmployeeId) {
        return ResponseEntity.ok(
                ApiResponse.success("Leave request approved",
                        leaveService.approveLeave(leaveRequestId, approverEmployeeId)));
    }

    // HR/Admin rejects a leave request
    @PutMapping("/{leaveRequestId}/reject")
    public ResponseEntity<ApiResponse<LeaveRequestResponse>> rejectLeave(
            @PathVariable Long leaveRequestId,
            @RequestParam Long approverEmployeeId,
            @RequestParam(required = false) String reason) {
        return ResponseEntity.ok(
                ApiResponse.success("Leave request rejected",
                        leaveService.rejectLeave(leaveRequestId, approverEmployeeId, reason)));
    }

    // Employee cancels their own leave
    @PutMapping("/{leaveRequestId}/cancel")
    public ResponseEntity<ApiResponse<LeaveRequestResponse>> cancelLeave(
            @PathVariable Long leaveRequestId,
            @RequestParam Long employeeId) {
        return ResponseEntity.ok(
                ApiResponse.success("Leave request cancelled",
                        leaveService.cancelLeave(leaveRequestId, employeeId)));
    }

    // Get all leave requests for an employee
    @GetMapping("/employee/{employeeId}")
    public ResponseEntity<ApiResponse<List<LeaveRequestResponse>>> getEmployeeLeaves(
            @PathVariable Long employeeId) {
        return ResponseEntity.ok(
                ApiResponse.success("Leave history retrieved",
                        leaveService.getEmployeeLeaveHistory(employeeId)));
    }

    // HR/Admin view — all pending requests awaiting decision
    @GetMapping("/pending")
    public ResponseEntity<ApiResponse<List<LeaveRequestResponse>>> getPendingLeaves() {
        return ResponseEntity.ok(
                ApiResponse.success("Pending leave requests retrieved",
                        leaveService.getPendingLeaveRequests()));
    }

    // Employee's remaining balance for the year
    @GetMapping("/balance/{employeeId}")
    public ResponseEntity<ApiResponse<List<LeaveBalanceResponse>>> getLeaveBalance(
            @PathVariable Long employeeId,
            @RequestParam(defaultValue = "2026") int year) {
        return ResponseEntity.ok(
                ApiResponse.success("Leave balances retrieved",
                        leaveService.getEmployeeLeaveBalances(employeeId, year)));
    }
}