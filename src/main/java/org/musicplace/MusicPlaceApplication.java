package org.musicplace;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;


@SpringBootApplication
@EnableJpaAuditing
public class MusicPlaceApplication {

	public static void main(String[] args) {
		SpringApplication.run(MusicPlaceApplication.class, args);

	}

}
