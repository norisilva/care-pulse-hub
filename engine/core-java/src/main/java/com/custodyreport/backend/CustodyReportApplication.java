package com.custodyreport.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class CustodyReportApplication {

	public static void main(String[] args) {
		SpringApplication.run(CustodyReportApplication.class, args);
	}

}
