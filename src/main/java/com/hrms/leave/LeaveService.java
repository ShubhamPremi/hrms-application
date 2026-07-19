package com.hrms.leave;

import com.hrms.leave.dto.ApplyLeaveRequest;
import com.hrms.leave.dto.LeaveBalanceResponse;
import com.hrms.leave.dto.LeaveRequestResponse;
import java.util.List;

public interface LeaveService {

    LeaveRequestResponse applyLeave(Long employeeId, ApplyLeaveRequest request);

    LeaveRequestResponse approveLeave(Long leaveRequestId, Long approverEmployeeId);

    LeaveRequestResponse rejectLeave(Long leaveRequestId, Long approverEmployeeId, String reason);

    LeaveRequestResponse cancelLeave(Long leaveRequestId, Long employeeId);

    List<LeaveRequestResponse> getEmployeeLeaveHistory(Long employeeId);

    List<LeaveRequestResponse> getPendingLeaveRequests();

    List<LeaveBalanceResponse> getEmployeeLeaveBalances(Long employeeId, int year);
}