package com.hrms.employee.event;

import com.hrms.employee.Employee;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

// ApplicationEvent is Spring's base event class
// It carries the event source and any data listeners need
@Getter
public class EmployeeCreatedEvent extends ApplicationEvent {

    private final Employee employee;
    private final String createdByEmail;

    public EmployeeCreatedEvent(Object source, Employee employee, String createdByEmail) {
        super(source);                    // source = the object that published the event
        this.employee = employee;
        this.createdByEmail = createdByEmail;
    }
}