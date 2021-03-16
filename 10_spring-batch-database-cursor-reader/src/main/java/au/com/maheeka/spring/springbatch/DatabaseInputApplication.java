package au.com.maheeka.spring.springbatch;

import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@EnableBatchProcessing
public class DatabaseInputApplication {

	public static void main(String[] args) {
		SpringApplication.run(DatabaseInputApplication.class, args);
	}

}
