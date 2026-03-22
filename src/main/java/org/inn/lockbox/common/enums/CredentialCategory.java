package org.inn.lockbox.common.enums;

import lombok.Getter;
import java.util.Map;

@Getter
public enum CredentialCategory {

    LOGIN("Login Credentials", Map.of(
            "source", "Source (e.g. Github)",
            "username", "Username",
            "password", "Password",
            "email", "Email Address",
            "url", "Login URL"
    )),

    API_TOKEN("API/Token Credentials", Map.of(
            "source", "Service Name (e.g. AWS)",
            "api_key", "API Key",
            "secret_key", "Secret Key",
            "access_token", "Access Token",
            "refresh_token", "Refresh Token",
            "email", "Associated Email"
    )),

    SECURE_NOTE("Secure Notes", Map.of(
            "source", "Note Title/Source",
            "sensitive_data", "Sensitive Content/Note"
    )),

    FINANCIAL("Financial Credentials", Map.of(
            "source", "Bank/Provider",
            "card_number", "Card Number",
            "cvv", "CVV (Security Code)",
            "expiry", "Expiry (MM/YY)",
            "pin", "ATM/Card PIN"
    )),

    SERVER("System/Server Credentials", Map.of(
            "host", "Hostname/Label",
            "ip", "IP Address",
            "port", "Port Number",
            "username", "SSH/Login Username",
            "password_ssh", "Password or SSH Key Path"
    )),

    KEY_BASED("Key-Based Credentials", Map.of(
            "source", "Key Label",
            "private_key", "Private Key Content",
            "public_key", "Public Key Content",
            "passphrase", "Key Passphrase",
            "system", "Target System"
    ));

    private final String displayName;
    private final Map<String, String> fields;

    CredentialCategory(String displayName, Map<String, String> fields) {
        this.displayName = displayName;
        // LinkedHashMap is used internally if order matters,
        // but Map.of is fine for standard key-value pairs.
        this.fields = fields;
    }
}