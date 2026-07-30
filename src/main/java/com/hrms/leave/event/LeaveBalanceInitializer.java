package com.hrms.leave.event;

import com.hrms.employee.event.EmployeeCreatedEvent;
import com.hrms.employee.Employee;
import com.hrms.leave.LeaveBalance;
import com.hrms.leave.LeaveBalanceRepository;
import com.hrms.leave.LeaveType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Component
@RequiredArgsConstructor
@Slf4j
public class LeaveBalanceInitializer {

    private final LeaveBalanceRepository leaveBalanceRepository;

    // @EventListener marks this as a handler for EmployeeCreatedEvent
    // Spring automatically calls this when the event is published
    // @Transactional — this runs in its own transaction
    // WHY: if this fails, the employee was already created successfully
    //      We don't want a balance initialisation failure to roll back the employee creation
    @EventListener
    @Transactional
    public void onEmployeeCreated(EmployeeCreatedEvent event) {
        Employee employee = event.getEmployee();
        int currentYear = LocalDate.now().getYear();

        log.info("Initialising leave balances for new employee: {} (year: {})",
                employee.getName(), currentYear);

        // Create standard annual, sick, and casual leave balances
        createBalance(employee, LeaveType.ANNUAL, currentYear, 18);
        createBalance(employee, LeaveType.SICK,   currentYear, 12);
        createBalance(employee, LeaveType.CASUAL, currentYear, 6);

        log.info("Leave balances initialised for employee: {}", employee.getName());
    }

    private void createBalance(Employee employee, LeaveType leaveType, int year, int totalDays) {
        LeaveBalance balance = LeaveBalance.builder()
                .employee(employee)
                .leaveType(leaveType)
                .year(year)
                .totalDays(totalDays)
                .usedDays(0)
                .build();
        leaveBalanceRepository.save(balance);
    }
}