package com.hrms.leave;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface LeaveBalanceRepository extends JpaRepository<LeaveBalance, Long> {

    Optional<LeaveBalance> findByEmployeeIdAndLeaveTypeAndYear(
            Long employeeId, LeaveType leaveType, int year);

    List<LeaveBalance> findByEmployeeIdAndYear(Long employeeId, int year);

    // Atomic increment of used_days in the database
    // WHY: doing this in Java (load → modify → save) risks a race condition
    // if two requests approve leave for the same employee simultaneously
    // Doing it with a single SQL UPDATE is atomic — the DB handles the lock
    @Modifying
    @Query("""
           UPDATE LeaveBalance lb
           SET lb.usedDays = lb.usedDays + :days
           WHERE lb.employee.id = :employeeId
           AND lb.leaveType = :leaveType
           AND lb.year = :year
           """)
    int incrementUsedDays(
            @Param("employeeId") Long employeeId,
            @Param("leaveType") LeaveType leaveType,
            @Param("year") int year,
            @Param("days") int days);

    @Modifying
    @Query("""
           UPDATE LeaveBalance lb
           SET lb.usedDays = lb.usedDays - :days
           WHERE lb.employee.id = :employeeId
           AND lb.leaveType = :leaveType
           AND lb.year = :year
           AND lb.usedDays >= :days
           """)
    int decrementUsedDays(
            @Param("employeeId") Long employeeId,
            @Param("leaveType") LeaveType leaveType,
            @Param("year") int year,
            @Param("days") int days);
}