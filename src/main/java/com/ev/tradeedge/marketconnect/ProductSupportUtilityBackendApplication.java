package com.ev.tradeedge.marketconnect;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class ProductSupportUtilityBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(ProductSupportUtilityBackendApplication.class, args);
	}

}
