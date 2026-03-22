package org.inn.lockbox.commands;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.inn.lockbox.common.enums.CredentialCategory;
import org.inn.lockbox.common.models.Credential;
import org.inn.lockbox.common.services.CredentialService;
import org.inn.lockbox.components.LockboxInput;
import org.inn.lockbox.services.LockboxSentinel;
import org.jline.terminal.Terminal;
import org.springframework.shell.core.command.annotation.Command;
import org.springframework.shell.jline.tui.component.flow.ComponentFlow;
import org.springframework.shell.jline.tui.component.flow.SelectItem;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class SealCommand {

    private final Terminal terminal;
    private final CredentialService credentialService;
    private final LockboxSentinel sentinel;
    private final ComponentFlow.Builder componentFlowBuilder;

    @Command(name = "seal", description = "Securely stash a new credential")
    public String seal() {
        if (!sentinel.isUnlocked()) return "Maya: Access Denied. Please 'getin' first.";

        try {
            // --- STEP 1: CATEGORY SELECTION (Keep ComponentFlow for Selectors) ---
            List<SelectItem> categoryItems = Arrays.stream(CredentialCategory.values())
                    .map(cat -> SelectItem.of(cat.getDisplayName(), cat.name()))
                    .toList();

            ComponentFlow categoryFlow = componentFlowBuilder.clone().reset()
                    .withSingleItemSelector("category")
                    .name("Select Category")
                    .selectItems(categoryItems)
                    .and().build();

            String selectedCat = categoryFlow.run().getContext().get("category");
            if (selectedCat == null) return "Maya: Operation cancelled.";
            CredentialCategory category = CredentialCategory.valueOf(selectedCat);

            // --- STEP 2: DYNAMIC INPUT COLLECTION (Using LockboxInput) ---
            terminal.writer().println("\n\u001B[36mMaya: Let's gather the details for your " + category.getDisplayName() + "...\u001B[0m");
            terminal.writer().flush();

            // Collect Title & Description
            String title = buildInput("Title", true, null).run();
            String description = buildInput("Description", false, null).run();

            // Collect Category-Specific Secrets
            Map<String, String> secretsMap = new HashMap<>();
            category.getFields().forEach((key, label) -> {
                String val = buildInput(label, isMandatory(key), isSensitive(key) ? '*' : null).run();
                secretsMap.put(key, val);
            });

            // Collect Expiry
            String expiryStr = buildInput("Expiry (YYYY-MM-DD) or 'none'", false, null).run();

            // --- STEP 3: REVIEW & OBJECT CREATION ---
            Credential credential = Credential.builder()
                    .title(title)
                    .description(description)
                    .type(category)
                    .secrets(secretsMap)
                    .expireAt(parseExpiry(expiryStr))
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .isActive(true)
                    .build();

            renderReview(credential);

            // --- STEP 4: FINAL CONFIRMATION ---
            ComponentFlow confirmFlow = componentFlowBuilder.clone().reset()
                    .withConfirmationInput("confirm")
                    .name("Maya: Should I permanently seal this in the vault?")
                    .defaultValue(true)
                    .and().build();

            boolean confirmed = confirmFlow.run().getContext().get("confirm");

            if (confirmed) {
                credentialService.sealNew(credential);
                return "\n\u001B[32mMaya: [SUCCESS] " + credential.getTitle() + " has been permanently sealed.\u001B[0m";
            } else {
                return "\n\u001B[33mMaya: Operation aborted. No data was saved.\u001B[0m";
            }

        } catch (Exception ex) {
            log.error("Seal failed", ex);
            return "\n\u001B[31mMaya: Something went wrong during sealing.\u001B[0m";
        }
    }

    /**
     * Helper to quickly build our custom LockboxInput
     */
    private LockboxInput buildInput(String label, boolean required, Character mask) {
        return LockboxInput.builder()
                .terminal(terminal)
                .label(label)
                .required(required)
                .mask(mask)
                .build();
    }

    private void renderReview(Credential cred) {
        terminal.writer().println("\n\u001B[1m--- MAYA: VAULT ENTRY REVIEW ---\u001B[0m");
        terminal.writer().println("Title:       " + cred.getTitle());
        terminal.writer().println("Category:    " + cred.getType().getDisplayName());
        terminal.writer().println("Description: " + cred.getDescription());

        terminal.writer().println("\u001B[33mSecrets Captured:\u001B[0m");
        cred.getSecrets().forEach((k, v) -> {
            // Mask the secrets in the review for safety
            String displayVal = isSensitive(k) ? "********" : v;
            terminal.writer().println("  ➔ " + k + ": " + displayVal);
        });

        terminal.writer().println("--------------------------------");
        terminal.writer().flush();
    }

    private boolean isMandatory(String key) {
        return Arrays.asList("source", "username", "password", "email", "api_key").contains(key);
    }

    private boolean isSensitive(String key) {
        String k = key.toLowerCase();
        return k.contains("password") || k.contains("key") || k.contains("pin") || k.contains("cvv") || k.contains("token") || k.contains("secret");
    }

    private LocalDateTime parseExpiry(String input) {
        if (input == null || input.equalsIgnoreCase("none") || input.isBlank()) return null;
        try {
            return LocalDateTime.parse(input + "T23:59:59");
        } catch (Exception e) {
            return null;
        }
    }
}