package org.inn.lockbox.services;

public interface SecurityService {

    char[] deriveKey(String passphrase);
}
