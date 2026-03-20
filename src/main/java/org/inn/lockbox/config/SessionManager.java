package org.inn.lockbox.config;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.atomic.AtomicReference;

import org.inn.lockbox.services.LockboxSentinel;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class SessionManager {

    private final LockboxSentinel sentinel;

    private final AtomicReference<Instant> lastActivity = new AtomicReference<>(Instant.now());

    private static final Duration IDLE_THRESOLD = Duration.ofMinutes(05);

    /**
     * This method resets the idle timer. 
     * You should call this at the start of every command or use an EventListener.
     */
    public void recordActivity() {
        lastActivity.set(Instant.now());
    }

    /**
     * Runs every 10 seconds to check if the user has walked away.
     */
    @Scheduled(fixedDelay = 60000)
    public void checkIdleTimeout() {
        if(!sentinel.isUnlocked()) return;

        long idleTime = Duration.between(lastActivity.get(), Instant.now()).toMinutes();

        if(idleTime >= IDLE_THRESOLD.toMinutes()) {
            log.info("User has been idle for {} minutes. Locking the lockbox.", idleTime);
            sentinel.revokeAccess();
        }
    }
}
