package com.hrms.domain.employee;

import com.hrms.domain.department.Department;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class EmployeeAnalytics {

    // 1. Get emails of all active employees
    public List<String> getActiveEmployeeEmails(List<Employee> employees){
        return employees.stream()
                .filter(emp -> emp.getStatus() == EmployeeStatus.ACTIVE)
                .map(Employee::getEmail)
                .collect(Collectors.toList());
    }

    // 2. Total payroll cost for active employees
    public double calculateTotalPayroll(List<Employee> employees){
        return employees.stream()
                .filter(emp -> emp.getStatus() == EmployeeStatus.ACTIVE)
                .mapToDouble(Employee::getSalary)
                .sum();
    }

    // 3. Group employees by department — HR needs this for headcount reports
    public Map<Department, List<Employee>> groupByDepartment(List<Employee> employees){
        return employees.stream()
                .collect(Collectors.groupingBy(Employee::getDepartment));
    }

    // 4. Headcount per department
    public Map<Department, Long> headcountByDepartment(List<Employee> employees){
        return employees.stream()
                .collect(Collectors.groupingBy(Employee::getDepartment, Collectors.counting()));
    }

    // 5. Average salary per department — used in payroll benchmarking
    public Map<Department, Double> averageSalaryByDepartment(List<Employee> employees){
        return employees.stream()
                .collect(Collectors.groupingBy(
                        Employee::getDepartment,
                        Collectors.averagingDouble(Employee::getSalary)
                ));
    }

    // 6. Find highest paid employee
    public Optional<Employee> findHighestPaid(List<Employee> employees){
        return employees.stream()
                .filter(emp -> emp.getStatus() == EmployeeStatus.ACTIVE)
                .max(Comparator.comparingDouble(Employee::getSalary));
    }

    // 7. Check if ANY employee in a department is on leave — manager dashboard
    public boolean hasDepartmentAnyoneOnLeave(List<Employee> employees, Department dept){
        return employees.stream()
                .filter(emp -> emp.getDepartment() == dept)
                .anyMatch(emp -> emp.getStatus() == EmployeeStatus.ON_LEAVE);
    }

    // 8. Get top 3 highest paid — for compensation review
    public List<Employee> getTopEarners(List<Employee> employees, int n) {
        return employees.stream()
                .filter(emp -> emp.getStatus() == EmployeeStatus.ACTIVE)
                .sorted(Comparator.comparingDouble(Employee::getSalary).reversed())
                .limit(n)
                .collect(Collectors.toList());
    }

}
