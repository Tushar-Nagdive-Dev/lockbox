package org.inn.lockbox.commands;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.inn.lockbox.common.enums.CredentialCategory;
import org.inn.lockbox.common.models.Credential;
import org.inn.lockbox.common.services.CredentialService;
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

    // INJECT THIS INSTEAD of calling the static .builder()
    private final ComponentFlow.Builder componentFlowBuilder;

    @Command(name = "seal", description = "Securely stash a new credential")
    public String seal() {
        if (!sentinel.isUnlocked()) return "Please 'getin' first.";

        try {
            // --- SECTION 1: CATEGORY ---
            List<SelectItem> categoryItems = Arrays.stream(CredentialCategory.values())
                    .map(cat -> SelectItem.of(cat.getDisplayName(), cat.name()))
                    .toList();

            ComponentFlow categoryFlow = componentFlowBuilder.clone().reset()
                    .withSingleItemSelector("category")
                    .name("Select Category")
                    .selectItems(categoryItems)
                    .and().build();

            String selectedCat = categoryFlow.run().getContext().get("category");
            if (selectedCat == null || selectedCat.isEmpty()) return "Cancelled.";
            CredentialCategory category = CredentialCategory.valueOf(selectedCat);

            // --- SECTION 2: IDENTITY & DYNAMIC SECRETS ---
            ComponentFlow.Builder dataFlow = componentFlowBuilder.clone().reset();

            // Standard Identity
            dataFlow.withStringInput("title").name("Unique Title").required().and()
                    .withStringInput("description").name("Description").required().and();

            // Dynamic Fields using the new Map
            category.getFields().forEach((key, label) -> {
                var input = dataFlow.withStringInput(key).name(label);

                // Logic: Is it required?
                if (isMandatory(key)) input.required();

                // Logic: Is it sensitive?
                if (isSensitive(key)) input.maskCharacter('*');

                input.and();
            });

            // Expiry
            dataFlow.withStringInput("expireAt")
                    .name("Expire (YYYY-MM-DD) or 'none'")
                    .defaultValue("none").and();

            var context = dataFlow.build().run().getContext();

            // --- SECTION 3: REVIEW & CONFIRMATION ---

            // Build the object in memory first
            Map<String, String> secretsMap = new HashMap<>();
            category.getFields().keySet().forEach(key -> {
                String val = context.get(key);
                secretsMap.put(key, val != null ? val : "N/A");
            });

            Credential credential = Credential.builder()
                    .title(context.get("title"))
                    .description(context.get("description"))
                    .type(category)
                    .secrets(secretsMap)
                    .expireAt(parseExpiry(context.get("expireAt")))
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .isActive(true)
                    .build();

            // Display Review Table (UX Touch)
            terminal.writer().println("\n--- Maya: Review your Seal ---");
            terminal.writer().println("Title:       " + credential.getTitle());
            terminal.writer().println("Category:    " + category.getDisplayName());
            terminal.writer().println("Description: " + credential.getDescription());
            terminal.writer().println("Secrets:     " + secretsMap.size() + " fields captured.");
            terminal.writer().flush();

            // Confirmation Flow
            ComponentFlow confirmFlow = componentFlowBuilder.clone().reset()
                    .withConfirmationInput("confirm")
                    .name("Confirm permanent seal?")
                    .defaultValue(true)
                    .and()
                    .build();

            boolean confirmed = confirmFlow.run().getContext().get("confirm");

            if (confirmed) {
//                credentialService.save(credential);
                return "\n\u001B[32mMaya: [SUCCESS] " + credential.getTitle() + " has been permanently sealed.\u001B[0m";
            } else {
                return "\n\u001B[33mMaya: Operation aborted. No data was saved.\u001B[0m";
            }
        } catch (Exception ex) {
            log.error("Seal failed", ex);
            return "\n\u001B[31mMaya: Something went wrong during sealing.\u001B[0m";
        }
    }

    private boolean isMandatory(String key) {
        // Check against the slug-style keys
        return Arrays.asList("source", "username", "password", "email", "api_key").contains(key);
    }

    private boolean isSensitive(String key) {
        return key.contains("password") || key.contains("key") || key.contains("pin") || key.contains("cvv") || key.contains("token");
    }

    private LocalDateTime parseExpiry(String input) {
        if (input == null || input.equalsIgnoreCase("none")) return null;
        try {
            return LocalDateTime.parse(input + "T23:59:59");
        } catch (Exception e) {
            return null; // Fallback for bad formats
        }
    }

}
