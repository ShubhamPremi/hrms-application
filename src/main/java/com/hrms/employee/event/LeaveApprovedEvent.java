package com.hrms.employee.event;

import com.hrms.leave.LeaveRequest;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class LeaveApprovedEvent extends ApplicationEvent {

    private final LeaveRequest leaveRequest;
    private final String approvedByEmail;

    public LeaveApprovedEvent(Object source, LeaveRequest leaveRequest, String approvedByEmail) {
        super(source);
        this.leaveRequest = leaveRequest;
        this.approvedByEmail = approvedByEmail;
    }
}