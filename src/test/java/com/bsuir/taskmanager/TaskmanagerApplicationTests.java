package com.bsuir.taskmanager;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
@Disabled("Requires a running PostgreSQL instance for full application context startup")
class TaskmanagerApplicationTests {
	@Test
	void contextLoads() {
	}

}
