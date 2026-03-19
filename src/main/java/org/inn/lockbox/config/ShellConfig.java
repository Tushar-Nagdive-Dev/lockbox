package org.inn.lockbox.config;

import org.jline.utils.AttributedString;
import org.jline.utils.AttributedStyle;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.shell.jline.PromptProvider;

@Configuration
public class ShellConfig {

    @Bean
    public PromptProvider lockboxPromptProvider() {
        return () -> new AttributedString("lockbox > ", AttributedStyle.DEFAULT.foreground(AttributedStyle.BLUE).bold());
    }
}
