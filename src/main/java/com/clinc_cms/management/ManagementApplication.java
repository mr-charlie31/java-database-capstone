package com.clinc_cms.management;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import com.clinc_cms.management.config.JwtConfigProperties;

@SpringBootApplication
@EnableConfigurationProperties(JwtConfigProperties.class)
public class ManagementApplication {

	public static void main(String[] args) {
		SpringApplication.run(ManagementApplication.class, args);
	}

}
