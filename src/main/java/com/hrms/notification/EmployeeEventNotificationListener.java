package com.hrms.notification;

import com.hrms.employee.event.EmployeeCreatedEvent;
import com.hrms.employee.event.LeaveApprovedEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class EmployeeEventNotificationListener {

    // In Week 8 (Kafka), this will publish to a Kafka topic instead of logging
    // The listener interface does not change — only the implementation does
    // This is the Open/Closed Principle applied to event listeners

    @EventListener
    public void onEmployeeCreated(EmployeeCreatedEvent event) {
        log.info("[NOTIFICATION] Welcome email queued for: {} ({})",
                event.getEmployee().getName(),
                event.getEmployee().getEmail());
        // Week 8: kafkaTemplate.send("employee-onboarding", event.getEmployee().getEmail());
    }

    @EventListener
    public void onLeaveApproved(LeaveApprovedEvent event) {
        log.info("[NOTIFICATION] Leave approval notification queued for employee id: {}",
                event.getLeaveRequest().getEmployee().getId());
        // Week 8: kafkaTemplate.send("leave-approved", payload);
    }
}