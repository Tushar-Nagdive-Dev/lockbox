package org.inn.lockbox;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.Banner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.EnableScheduling;

import jakarta.annotation.PostConstruct;

@SpringBootApplication
@EnableScheduling
public class LockboxApplication {

	@Autowired
	private Environment env;

	public static void main(String[] args) {
		SpringApplication app = new SpringApplication(LockboxApplication.class);
        app.setBannerMode(Banner.Mode.CONSOLE);
        app.run(args);
	}

	@PostConstruct
	public void setUpEnvironment() throws IOException {
		Boolean isProd = Arrays.toString(env.getActiveProfiles()).contains("prod");

		if(isProd) {
			Path path = Paths.get(System.getProperty("user.home"), ".lockbox", "logs");
			if (!Files.exists(path)) {
                Files.createDirectories(path);
                // Simple feedback for the dev
                System.out.println("Production directories initialized at: " + path);
            }
		}
	}
}
