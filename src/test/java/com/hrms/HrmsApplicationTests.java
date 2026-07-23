package com.hrms;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

//@SpringBootTest
// Deliberately not annotated with @SpringBootTest
// That annotation starts the full context and requires a running database
// Integration tests using TestContainers come in Week 4 Day 17
// This class is intentionally minimal — it just confirms the test suite loads
class HrmsApplicationTests {

	@Test
	void contextLoads() {
	}

}
