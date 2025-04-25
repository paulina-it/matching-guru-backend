package uk.bovykina.matching_guru;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@EnableAsync
@SpringBootApplication
public class MatchingGuruApplication {

	public static void main(String[] args) {
		SpringApplication.run(MatchingGuruApplication.class, args);
	}

}
