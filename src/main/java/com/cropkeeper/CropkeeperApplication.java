package com.cropkeeper;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class CropkeeperApplication {

	public static void main(String[] args) {
		SpringApplication.run(CropkeeperApplication.class, args);
	}

}
