# 🛡️ Lockbox: Your Personal CLI Vault

**Lockbox** is a lightweight, high-security command-line credential manager. Managed by **Nagdive, Tushar**, your adaptive AI assistant, it provides a seamless "Seal and Forget" experience for your most sensitive data.

## 🌟 Features

* **Dynamic Categories:** Specialized fields for Logins, API Tokens, Financial Cards, and Servers.
* **Zero-Knowledge Philosophy:** Your data is encrypted before it ever touches the disk.
* **Smart Masking:** Automatic terminal masking for passwords, keys, and PINs.
* **Review & Seal:** A fail-safe verification step before any data is permanently stored.

---

## 🛠️ How It Works

Lockbox is built on a "Privacy First" stack, ensuring that your data remains yours, even if your hardware is compromised.

### 1. The Sentinel (Authentication)
Before you can run any command, you must pass through the **Sentinel**.
* **`getin`**: Initializes the session. You provide your Master Passphrase, which is used to derive the encryption keys.
* **Session-Only Memory:** Your Master Passphrase is never stored on disk. It exists only in the volatile memory of your current terminal session.

### 2. The Seal (Data Entry)
When you use the `seal` command, Lockbox initiates a multi-stage "Wizard" flow:
* **Category Selection:** Choose the template (Login, API, etc.).
* **Dynamic Inputs:** Maya asks for specific fields (Username, API Key, etc.) based on the category.
* **Validation:** Built-in checks for mandatory fields like **Source**, **Email**, and **Title**.
* **Review:** A final confirmation screen to verify your data before encryption.

### 3. The Vault (Storage)
Lockbox uses **Nitrite DB**, a serverless No62 (Nitrite) document store. 
* **Indexed for Speed:** Credentials are indexed by Title and Type for instant retrieval.
* **Encrypted Payloads:** The `secrets` map within the `Credential` object is encrypted using industry-standard AES-256 (or your configured provider).



---

## 🔒 Security Architecture

| Layer | Security Measure | Benefit |
| :--- | :--- | :--- |
| **Input** | JLine Masking | Prevents "shoulder surfing" in the terminal. |
| **Transport** | Java Internal POJOs | Data is handled as objects, reducing exposure to string-pool attacks. |
| **Storage** | Nitrite Repository | No external database server required; reduces the attack surface. |
| **Persistence** | AES-256 Encryption | Even if the `.db` file is stolen, the content remains unreadable without the Master Passphrase. |

---

## 🚀 Quick Start

1.  **Open the Vault**
    ```bash
    lockbox > getin
    ? Master Passphrase: ******
    ```

2.  **Seal a New Secret**
    ```bash
    lockbox (in) > seal
    ? Select Category: LOGIN
    ? Unique Title: Personal Github
    ? Password: ************
    ```

3.  **List Your Secrets**
    ```bash
    lockbox (in) > list
    ```

---

## 📂 Project Structure

* `org.inn.lockbox.commands`: The CLI interface and Maya's personality.
* `org.inn.lockbox.common.enums`: Type-safe credential categories.
* `org.inn.lockbox.common.models`: The `Credential` POJO (the heart of the vault).
* `org.inn.lockbox.services`: The logic for encryption and persistence.
