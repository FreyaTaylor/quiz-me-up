package com.example.quizmeup;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan(basePackages = "com.example.quizmeup.infra.mapper")
public class QuizMeUpApplication {

	public static void main(String[] args) {
		SpringApplication.run(QuizMeUpApplication.class, args);
	}

}
