package com.hrms.leave;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface LeaveRequestRepository extends JpaRepository<LeaveRequest, Long> {

    @Query("SELECT lr FROM LeaveRequest lr JOIN FETCH lr.employee " +
            "WHERE lr.employee.id = :employeeId ORDER BY lr.createdAt DESC")
    List<LeaveRequest> findByEmployeeId(@Param("employeeId") Long employeeId);

    @Query("SELECT lr FROM LeaveRequest lr JOIN FETCH lr.employee " +
            "WHERE lr.status = :status ORDER BY lr.createdAt DESC")
    List<LeaveRequest> findByStatus(@Param("status") LeaveStatus status);

    // Overlap check — does any APPROVED or PENDING leave for this employee
    // overlap with the requested date range?
    // WHY: prevents an employee from having two approved leaves covering the same day
    @Query("""
           SELECT COUNT(lr) > 0 FROM LeaveRequest lr
           WHERE lr.employee.id = :employeeId
           AND lr.status IN ('PENDING', 'APPROVED')
           AND lr.startDate <= :endDate
           AND lr.endDate >= :startDate
           """)
    boolean hasOverlappingLeave(
            @Param("employeeId") Long employeeId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    @Query("SELECT lr FROM LeaveRequest lr JOIN FETCH lr.employee " +
            "WHERE lr.employee.id = :employeeId AND lr.status = :status")
    List<LeaveRequest> findByEmployeeIdAndStatus(
            @Param("employeeId") Long employeeId,
            @Param("status") LeaveStatus status);
}