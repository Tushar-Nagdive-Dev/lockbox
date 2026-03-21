package org.inn.lockbox.config;

import org.dizitart.no2.Nitrite;
import org.inn.lockbox.services.LockboxSentinel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.function.Supplier;

@Configuration
public class DatabaseConfig {

    @Bean
    public Supplier<Nitrite> dbProvider(LockboxSentinel sentinel) {
        return sentinel::getDatabase;
    }
}
