package com.hrms.domain.leave;

// sealed = only the listed classes can extend this
// permits = the exhaustive list of subtypes
public sealed interface LeavePolicy
        permits AnnualLeavePolicy, SickLeavePolicy, MaternityLeavePolicy {

    int getAllowedDaysPerYear();
    boolean requiresApproval();
}

// Each permitted subtype is either final (no further extension) or sealed
record AnnualLeavePolicy() implements LeavePolicy {
    public int getAllowedDaysPerYear() { return 18; }
    public boolean requiresApproval() { return true; }
}

record SickLeavePolicy() implements LeavePolicy {
    public int getAllowedDaysPerYear() { return 12; }
    public boolean requiresApproval() { return false; }
}

record MaternityLeavePolicy() implements LeavePolicy {
    public int getAllowedDaysPerYear() { return 180; }
    public boolean requiresApproval() { return true; }
}
