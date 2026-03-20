package org.inn.lockbox.services;

import org.dizitart.no2.Nitrite;

public interface LockboxSentinel {
    
    boolean isExistingLockbox();

    void permitEntry(String passphrase);

    Nitrite getDatabase();

    void revokeAccess();

    boolean isUnlocked();
}
