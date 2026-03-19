package org.inn.lockbox.config;

import org.dizitart.no2.Nitrite;
import org.dizitart.no2.mvstore.MVStoreModule;
import org.dizitart.no2.common.mapper.JacksonMapperModule;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import java.io.File;

@Configuration
public class DatabaseConfig {

    @Value("${lockbox.path}")
    private String dbPath;

    @Bean
    public Nitrite nitriteDatabase() {
        // Create the folder if it doesn't exist (e.g., .lockbox/ in home)
        File file = new File(dbPath);
        if (file.getParentFile() != null && !file.getParentFile().exists()) {
            file.getParentFile().mkdirs();
        }

        // Configure Storage Engine
        MVStoreModule storeModule = MVStoreModule.withConfig()
                .filePath(dbPath)
                .compress(true)
                .build();

        // Build Database
        return Nitrite.builder()
                .loadModule(storeModule)
                .loadModule(new JacksonMapperModule())
                .openOrCreate();
    }
}