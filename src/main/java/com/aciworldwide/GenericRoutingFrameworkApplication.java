package com.aciworldwide;

import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

@SpringBootApplication
public class GenericRoutingFrameworkApplication {

	public static void main(String[] args) {
		new SpringApplicationBuilder(GenericRoutingFrameworkApplication.class)
				.web(WebApplicationType.NONE)
				.run(args);
	}

}
