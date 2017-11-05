package com.paxos;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;

@ComponentScan
@EnableAutoConfiguration(exclude = {DataSourceAutoConfiguration.class})
public class AppStart {
	public static void main(String[] args) {
		SpringApplication.run(AppStart.class, args);
	}
}
