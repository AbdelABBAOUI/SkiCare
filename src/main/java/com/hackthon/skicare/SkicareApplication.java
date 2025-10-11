package com.hackthon.skicare;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class SkicareApplication {

	public static void main(String[] args) {
		SpringApplication.run(SkicareApplication.class, args);
	}

}
