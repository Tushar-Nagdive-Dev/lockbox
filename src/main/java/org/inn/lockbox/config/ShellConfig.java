package org.inn.lockbox.config;

import org.inn.lockbox.services.LockboxSentinel;
import org.jline.utils.AttributedString;
import org.jline.utils.AttributedStyle;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.shell.jline.PromptProvider;

@Configuration
public class ShellConfig {

    private final LockboxSentinel lockboxSentinel;

    // Inject the Sentinel to check the lock status
    public ShellConfig(LockboxSentinel lockboxSentinel) {
        this.lockboxSentinel = lockboxSentinel;
    }

    @Bean
    public PromptProvider lockboxPromptProvider() {
        return () -> {
            // Check if the database is currently open (active)
            if(this.lockboxSentinel.isUnlocked()) {
                return new AttributedString("lockbox (in) > ", AttributedStyle.DEFAULT.foreground(AttributedStyle.GREEN).bold());
            } else {
                return new AttributedString("lockbox > ", AttributedStyle.DEFAULT.foreground(AttributedStyle.BLUE).bold());
            }
        };
    }
}
