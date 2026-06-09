package com.hrms;

import com.hrms.domain.department.Department;
import com.hrms.domain.employee.Employee;
import com.hrms.domain.employee.EmployeeAnalytics;
import com.hrms.domain.employee.EmployeeStatus;
import com.hrms.domain.leave.LeaveCalculator;
import com.hrms.domain.leave.LeaveRequest;
import com.hrms.domain.leave.LeaveStatus;
import com.hrms.domain.leave.LeaveType;

import java.time.LocalDate;
import java.util.List;

public class HrmsDataRunner {
    public static void main(String[] args) {

        List<Employee> employees = List.of(
                new Employee(1L, "Shubham", "shubham@hrms.com", "Software Engineer",
                        Department.ENGINEERING, 80000, LocalDate.of(2022, 1, 15), EmployeeStatus.ACTIVE),
                new Employee(2L, "Priya", "priya@hrms.com", "HR Manager",
                        Department.HUMAN_RESOURCES, 75000, LocalDate.of(2021, 6, 1), EmployeeStatus.ACTIVE),
                new Employee(3L, "Rohit", "rohit@hrms.com", "Finance Analyst",
                        Department.FINANCE, 65000, LocalDate.of(2023, 3, 20), EmployeeStatus.ON_LEAVE),
                new Employee(4L, "Ananya", "ananya@hrms.com", "Senior Engineer",
                        Department.ENGINEERING, 120000, LocalDate.of(2019, 9, 10), EmployeeStatus.ACTIVE),
                new Employee(5L, "Vikram", "vikram@hrms.com", "Marketing Lead",
                        Department.MARKETING, 70000, LocalDate.of(2020, 11, 5), EmployeeStatus.RESIGNED)
        );

        EmployeeAnalytics analytics = new EmployeeAnalytics();

        System.out.println("=== Active employee emails ===");
        analytics.getActiveEmployeeEmails(employees).forEach(System.out::println);

        System.out.println("\n=== Total payroll ===");
        System.out.println("₹" + analytics.calculateTotalPayroll(employees));

        System.out.println("\n=== Headcount by department ===");
        analytics.headcountByDepartment(employees).forEach((dept, count) ->
                System.out.println(dept + ": " + count));

        System.out.println("\n=== Top 2 earners ===");
        analytics.getTopEarners(employees, 2).forEach(System.out::println);

        System.out.println("\n=== Highest paid ===");
        analytics.findHighestPaid(employees)
                .ifPresent(e -> System.out.println(e.getName() + " — ₹" + e.getSalary()));


        // DateTime demo
        LeaveCalculator calculator = new LeaveCalculator();
        LeaveRequest leave = new LeaveRequest(1L, 1L, LeaveType.ANNUAL,
                LocalDate.of(2025, 7, 1), LocalDate.of(2025, 7, 5),
                LeaveStatus.PENDING, "Family vacation");

        System.out.println("\n=== Leave calculation ===");
        System.out.println("Days: " + calculator.calculateLeaveDays(leave));
        System.out.println("In probation: " + calculator.isInProbation(
                employees.get(0).getJoiningDate()));
    }
}
