package io.proj3ct.SpringDemoBot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@ConfigurationPropertiesScan
@SpringBootApplication
public class SpringDemoBotApplication {

	public static void main(String[] args) {
		SpringApplication.run(SpringDemoBotApplication.class, args);
	}

}
