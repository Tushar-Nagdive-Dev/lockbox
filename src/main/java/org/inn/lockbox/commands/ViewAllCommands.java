package org.inn.lockbox.commands;

import org.inn.lockbox.components.ListDataView;
import org.inn.lockbox.services.LockboxSentinel;
import org.springframework.shell.core.command.annotation.Command;
import org.springframework.stereotype.Component;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ViewAllCommands {
    
    private final LockboxSentinel sentinel;

    @Command(name = "showcmd", description = "View all available commands of lockbox")
    public String viewAllCommands() {
        // MOVE THIS INSIDE THE METHOD so it resets every time!
        StringBuilder ui = new StringBuilder();

        // System Access Card
        ui.append(new ListDataView("System Access")
            .add("getin", "Authorize session for lockbox", "> getin")
            .add("exit", "Close connection", "> exit")
            .add("change-pass", "Change your master lock", "> change-pass")
            .add("lockbox-empty", "Destroy the vault (All data)", "> nuke")
            .render()
        );

        // Vault Operations Card
        if (sentinel.isUnlocked()) {
            ui.append(new ListDataView("Vault Operations")
                .add("store", "Encrypt new secret", "store --id fb")
                .add("list", "View all secrets", "list --all")
                .render());
        }

        return ui.toString();
    }
}