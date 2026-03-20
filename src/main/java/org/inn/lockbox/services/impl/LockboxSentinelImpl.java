package org.inn.lockbox.services.impl;

import java.io.File;
import java.util.Arrays;

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

    @Override
    public boolean isExistingLockbox() {
        return new File(dbPath).exists();
    }

    @Override
    public void permitEntry(String passphrase) {
        char[] securekey = securityService.deriveKey(passphrase);
        try {
            MVStoreModule storeModule = MVStoreModule.withConfig()
                .filePath(dbPath)
                .compress(true)
                .encryptionKey(securekey)
                .build();
            
            this.activeDb = Nitrite.builder()
                .loadModule(storeModule)
                .loadModule(new JacksonMapperModule())
                .openOrCreate();
        } finally {
            // Memory Safety
            Arrays.fill(securekey, ' ');
        }
    }

    /**
     * Update Passphrase Feature.
     * We close the DB and re-open it with a new configuration to trigger re-keying.
     */
    public void updatePassphrase(String oldPass, String newPass) {
        // 1. Verify we can open it with the old pass
        permitEntry(oldPass);
        
        char[] oldKey = securityService.deriveKey(oldPass);
        char[] newKey = securityService.deriveKey(newPass);

        try {
            // In Nitrite 4, the re-keying is best handled by the Store's 
            // setPassword or changePassword if accessible, otherwise 
            // we use the String variant for the migration as Nitrite expects.
            
            activeDb.close(); // Close to ensure file handles are clean
            
            MVStoreModule rekeyModule = MVStoreModule.withConfig()
                .filePath(dbPath)
                .encryptionKey(oldKey)
                .build();

            // We use the String conversion ONLY within this local scope to satisfy the API
            // while keeping the keys as char[] for as long as possible.
            String sOld = new String(oldKey);
            String sNew = new String(newKey);

            this.activeDb = Nitrite.builder()
                .loadModule(rekeyModule)
                .addMigrations(new org.dizitart.no2.migration.Migration(1, 2) {
                    @Override
                    public void migrate(org.dizitart.no2.migration.InstructionSet instructions) {
                        // Nitrite 4 migration API uses (user, oldPassword, newPassword)
                        instructions.forDatabase().changePassword("user", sOld, sNew);
                    }
                })
                .schemaVersion(2)
                .openOrCreate();

        } finally {
            Arrays.fill(oldKey, ' ');
            Arrays.fill(newKey, ' ');
        }
    }

    public boolean nuke() {
        revokeAccess();
        File file = new File(dbPath);
        return !file.exists() || file.delete();
    }

    @Override
    public void revokeAccess() {
        if (activeDb != null) {
            activeDb.close();
            activeDb = null;
        }
    }

    @Override
    public boolean isUnlocked() {
        return activeDb != null;
    }

    @Override
    public Nitrite getDatabase() {
        if (activeDb == null) throw new IllegalStateException("Vault is locked.");
        return activeDb;
    }
}