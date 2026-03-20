package org.inn.lockbox.commands;

import lombok.RequiredArgsConstructor;
import org.inn.lockbox.services.LockboxSentinel;
import org.jline.terminal.Terminal;
import org.springframework.core.io.ResourceLoader;
import org.springframework.shell.core.command.annotation.Command;
import org.springframework.shell.jline.tui.component.ConfirmationInput;
import org.springframework.shell.jline.tui.component.StringInput;
import org.springframework.shell.jline.tui.component.ConfirmationInput.ConfirmationInputContext;
import org.springframework.shell.jline.tui.component.StringInput.StringInputContext;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AccessCommands {
    
    private final LockboxSentinel sentinel;
    private final Terminal terminal;
    private final ResourceLoader resourceLoader;

    @Command(name = "getin", description = "Enter the vault or initialize a new one")
    public String getIn() {
        if (sentinel.isUnlocked()) return "You are already authorized.";

        // --- FIRST TIME SETUP ---
        if (!sentinel.isExistingLockbox()) {
            String p1 = askSecure("Create New Master Passphrase: ");
            String p2 = askSecure("Confirm Master Passphrase: ");

            if (p1 == null || p1.isEmpty() || !p1.equals(p2)) {
                return "Initialization failed. Passphrases must match and cannot be empty.";
            }
            sentinel.permitEntry(p1);
            return "New vault created and encrypted. Welcome.";
        }

        // --- LOGIN ---
        String password = askSecure("Master Passphrase: ");
        try {
            if (password == null || password.isEmpty()) return "Login cancelled.";
            sentinel.permitEntry(password);
            return "Access Granted.";
        } catch (Exception e) {
            return "Access Denied. Passphrase incorrect.";
        }
    }

    @Command(name = "change-passphrase", description = "Change your master lock")
    public String update() {
        if (!sentinel.isUnlocked()) return "You need to 'getin' before you can change the lock.";

        String oldP = askSecure("Current Passphrase: ");
        String newP1 = askSecure("New Passphrase: ");
        String newP2 = askSecure("Confirm New Passphrase: ");

        if (newP1 == null || !newP1.equals(newP2)) return "New passphrases do not match.";

        try {
            sentinel.updatePassphrase(oldP, newP1);
            return "Passphrase updated. The vault has been re-keyed.";
        } catch (Exception e) {
            return "Update failed. Please ensure your current passphrase is correct.";
        }
    }

    @Command(name = "lockbox-empty", description = "Destroy the vault (All data lost)")
    public String reset() {
        ConfirmationInput component = new ConfirmationInput(terminal, 
            "Are you sure? This will permanently delete the lockbox.", false);
        component.setResourceLoader(resourceLoader);
        
        var context = component.run(ConfirmationInputContext.empty());
        if (Boolean.TRUE.equals(context.getResultValue())) {
            boolean success = sentinel.nuke();
            return success ? "The lockbox has been destroyed." : "Error deleting file.";
        }
        return "Reset aborted.";
    }

    private String askSecure(String prompt) {
        StringInput component = new StringInput(terminal, prompt, "");
        component.setResourceLoader(resourceLoader);
        component.setMaskCharacter('*');
        StringInputContext context = component.run(StringInputContext.empty());
        return context.getResultValue();
    }
}