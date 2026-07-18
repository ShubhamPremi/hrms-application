package com.hrms.auth;

// Simple enum — role is stored as a VARCHAR in the users table
// Spring Security expects roles prefixed with "ROLE_" internally
// but we store without the prefix and add it in UserDetails
public enum Role {
    ADMIN,
    HR,
    EMPLOYEE
}