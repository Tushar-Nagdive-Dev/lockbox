package org.inn.lockbox;

import org.springframework.boot.Banner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class LockboxApplication {

	public static void main(String[] args) {
		SpringApplication app = new SpringApplication(LockboxApplication.class);
        app.setBannerMode(Banner.Mode.CONSOLE);
        app.run(args);
	}

}
