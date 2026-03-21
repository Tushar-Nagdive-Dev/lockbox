package org.inn.lockbox.common.enums;

import lombok.Getter;

import java.util.Arrays;
import java.util.List;

@Getter
public enum CredentialCategory {

    LOGIN("Login Credentials", Arrays.asList("Source", "Username", "Password", "Email", "URL", "Number")),
    API_TOKEN("API/Token Credentials", Arrays.asList("Source", "API Key", "Secret Key", "Access Token", "Refresh Token", "Email")),
    SECURE_NOTE("Secure Notes", Arrays.asList("Source", "Sensitive Data")),
    FINANCIAL("Financial Credentials", Arrays.asList("Card Number", "CVV", "Expiry", "PIN")),
    SERVER("System/Server Credentials", Arrays.asList("Host", "Port", "Username", "Password/SSH Key", "IP")),
    KEY_BASED("Key-Based Credentials", Arrays.asList("Source", "Private Key", "Public Key", "Passphrase", "System"));

    private final String displayName;
    private final List<String> fields;

    CredentialCategory(String displayName, List<String> fields) {
        this.displayName = displayName;
        this.fields = fields;
    }
}
