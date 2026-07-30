package com.hrms.common.aop;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

// @Target(METHOD) — only applicable to methods
// @Retention(RUNTIME) — visible at runtime via reflection (needed by Spring AOP)
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface LogExecutionTime {
    // Marker annotation — no parameters needed
    // Presence of this annotation triggers the AOP advice
}