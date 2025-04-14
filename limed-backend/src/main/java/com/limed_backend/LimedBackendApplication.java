package com.limed_backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class LimedBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(LimedBackendApplication.class, args);
	}

}
