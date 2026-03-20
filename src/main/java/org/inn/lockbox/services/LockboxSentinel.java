package org.inn.lockbox.services;

import org.dizitart.no2.Nitrite;

public interface LockboxSentinel {
    
    boolean isExistingLockbox();

    void permitEntry(String passphrase);

    void updatePassphrase(String oldPass, String newPass);

    boolean nuke();

    Nitrite getDatabase();

    void revokeAccess();

    boolean isUnlocked();
}
