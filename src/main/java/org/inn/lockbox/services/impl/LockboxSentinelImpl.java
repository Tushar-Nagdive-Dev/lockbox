package org.inn.lockbox.services.impl;

import java.io.File;
import java.nio.file.Files;
import java.util.Arrays;

import org.dizitart.no2.Nitrite;
import org.dizitart.no2.common.mapper.JacksonMapperModule;
import org.dizitart.no2.mvstore.MVStoreModule;
import org.inn.lockbox.services.LockboxSentinel;
import org.inn.lockbox.services.SecurityService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

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
        return new File(dbPath).exists() && new File(dbPath + ".meta").exists();
    }

    @Override
    public void permitEntry(String passphrase) {
        if (isUnlocked()) return;

        File file = new File(dbPath);
        File metaFile = new File(dbPath + ".meta");

        // 1. Instant Pre-Verification (The "Maya Shield")
        if (metaFile.exists()) {
            try {
                String storedHash = Files.readString(metaFile.toPath());
                String inputHash = DigestUtils.md5DigestAsHex(passphrase.getBytes());
                if (!storedHash.equals(inputHash)) {
                    throw new RuntimeException("Passphrase incorrect. Access Denied.");
                }
            } catch (Exception e) {
                throw new RuntimeException("Security metadata is unreadable.");
            }
        }

        // 2. Prepare Directories
        if (file.getParentFile() != null && !file.getParentFile().exists()) {
            file.getParentFile().mkdirs();
        }

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

            // If this is a brand new vault, create the meta file now
            if (!metaFile.exists()) {
                Files.writeString(metaFile.toPath(), DigestUtils.md5DigestAsHex(passphrase.getBytes()));
            }

            log.info("Lockbox opened successfully.");
        } catch (Exception e) {
            log.error("Lockbox access error: {}", e.getMessage());
            throw new RuntimeException("Vault is corrupted or key derivation failed.");
        } finally {
            Arrays.fill(securekey, ' ');
        }
    }

    @Override
    public void updatePassphrase(String oldPass, String newPass) {
        if (!isUnlocked()) permitEntry(oldPass);

        char[] oldKey = securityService.deriveKey(oldPass);
        char[] newKey = securityService.deriveKey(newPass);

        try {
            revokeAccess();

            MVStoreModule rekeyModule = MVStoreModule.withConfig()
                    .filePath(dbPath)
                    .encryptionKey(oldKey)
                    .build();

            String sOld = new String(oldKey);
            String sNew = new String(newKey);

            this.activeDb = Nitrite.builder()
                    .loadModule(rekeyModule)
                    .addMigrations(new org.dizitart.no2.migration.Migration(1, 2) {
                        @Override
                        public void migrate(org.dizitart.no2.migration.InstructionSet instructions) {
                            instructions.forDatabase().changePassword("user", sOld, sNew);
                        }
                    })
                    .schemaVersion(2)
                    .openOrCreate();

            // Update the meta file with the new passphrase hash
            Files.writeString(new File(dbPath + ".meta").toPath(), DigestUtils.md5DigestAsHex(newPass.getBytes()));

        } catch (Exception e) {
            log.error("Passphrase update failed", e);
            throw new RuntimeException("Update failed. Could not re-key the vault.");
        } finally {
            Arrays.fill(oldKey, ' ');
            Arrays.fill(newKey, ' ');
        }
    }

    @Override
    public boolean nuke() {
        revokeAccess();
        new File(dbPath + ".meta").delete();
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
    public boolean isUnlocked() { return activeDb != null; }

    @Override
    public Nitrite getDatabase() {
        if (activeDb == null) throw new IllegalStateException("Vault is locked.");
        return activeDb;
    }
}