package org.inn.lockbox.services.impl;

import java.io.File;

import org.dizitart.no2.Nitrite;
import org.dizitart.no2.common.mapper.JacksonMapperModule;
import org.dizitart.no2.mvstore.MVStoreModule;
import org.inn.lockbox.services.LockboxSentinel;
import org.inn.lockbox.services.SecurityService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class LockboxSentinelImpl implements LockboxSentinel {

    @Value("${lockbox.path}")
    private String dbPath;

    private final SecurityService securityService;

    private Nitrite activeDb;
    
    /**
     * Checks if a Lockbox already exists on this machine.
     */
    @Override
    public boolean isExistingLockbox() {
        return new File(dbPath).exists();
    }

    /**
     * The core "Unlock" logic. This takes the raw passphrase, 
     * derives the high-security key, and opens the Nitrite gate.
     */
    @Override
    public void permitEntry(String passphrase) {
        // Derive the AES-256 key from the passphrase
        char[] securekey = securityService.deriveKey(passphrase);

        MVStoreModule storeModule = MVStoreModule.withConfig()
            .filePath(dbPath)
            .compress(true)
            .encryptionKey(securekey)
            .build();
        
        this.activeDb = Nitrite.builder()
            .loadModule(storeModule)
            .loadModule(new JacksonMapperModule())
            .openOrCreate();
    }

    @Override
    public void revokeAccess() {
        if(this.activeDb != null) {
            try {
                this.activeDb.close();
            } catch (Exception e) {
                log.error("revokeAccess err", e);
            } finally {
                this.activeDb = null;
            }
        }
    }

    @Override
    public Nitrite getDatabase() {
        // In Nitrite 4, if the builder finished, the DB is open.
        // We just need to make sure we've actually run permitEntry()
        if (activeDb == null) {
            throw new IllegalStateException("The Sentinel has not granted access. Please 'open' the lockbox first.");
        }
        return activeDb;
    }

    @Override
    public boolean isUnlocked() {
        return activeDb != null;
    }
}
