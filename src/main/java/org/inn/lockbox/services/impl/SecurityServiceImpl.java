package org.inn.lockbox.services.impl;

import java.util.Base64;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

import org.inn.lockbox.services.SecurityService;
import org.springframework.stereotype.Service;

@Service
public class SecurityServiceImpl implements SecurityService {
    
    private static final int INTERATION = 65536;

    private static final int KEY_LENGTH = 256;

    private static final String ALGORITHM = "PBKDF2WithHmacSHA256";

    private static final byte[] SALT = "bHMvrfVHy+JKvGTAdhNPZumQvwkJfmT3c33dDbxTmjE=".getBytes();

    @Override
    public char[] deriveKey(String passphrase) {
        try {
            PBEKeySpec spec = new PBEKeySpec(passphrase.toCharArray(), SALT, INTERATION, KEY_LENGTH);
            SecretKeyFactory factory = SecretKeyFactory.getInstance(ALGORITHM);
            
            byte[] keyBytes = factory.generateSecret(spec).getEncoded();
            String base64Key = Base64.getEncoder().encodeToString(keyBytes);
            return base64Key.toCharArray();
        }catch (Exception e) {
            throw new RuntimeException("Encryption setup failed", e);
        }
    }
}
