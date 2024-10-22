package com.example.newsnexus;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;


@SpringBootApplication
@EnableScheduling
public class NewsnexusApplication {

	public static void main(String[] args) {
		SpringApplication.run(NewsnexusApplication.class, args);
	}
}
