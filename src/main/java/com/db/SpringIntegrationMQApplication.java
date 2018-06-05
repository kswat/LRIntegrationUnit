package com.db;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ImportResource;
import org.springframework.integration.config.EnableIntegration;
import org.springframework.jms.annotation.EnableJms;

@SpringBootApplication
@EnableIntegration
//@ImportResource("classpath*:/http-outbound-config.xml")
@ImportResource("classpath*:/bvr-config.xml")
@EnableJms
public class SpringIntegrationMQApplication {
	
	public static void main(String[] args) {
		ConfigurableApplicationContext context = SpringApplication.run(SpringIntegrationMQApplication.class, args);
	}
    
}
