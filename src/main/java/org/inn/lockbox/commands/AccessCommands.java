package org.inn.lockbox.commands;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.inn.lockbox.services.LockboxSentinel;
import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.jline.terminal.Terminal;
import org.springframework.core.io.ResourceLoader;
import org.springframework.shell.core.command.annotation.Command;
import org.springframework.shell.jline.tui.component.ConfirmationInput;
import org.springframework.shell.jline.tui.component.ConfirmationInput.ConfirmationInputContext;
import org.springframework.shell.jline.tui.style.TemplateExecutor;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class AccessCommands {

    private final LockboxSentinel sentinel;
    private final Terminal terminal;
    private final ResourceLoader resourceLoader;
    private final TemplateExecutor templateExecutor;

    @Command(name = "getin", description = "Enter the vault or initialize a new one")
    public String getIn() {
        try {
            if (sentinel.isUnlocked()) return "Maya: You are already authorized.";

            if (!sentinel.isExistingLockbox()) {
                String p1 = askSecure("Create New Master Passphrase");
                String p2 = askSecure("Confirm Master Passphrase");

                if (p1 == null || p1.isEmpty() || !p1.equals(p2)) {
                    return "\u001B[31mMaya: Initialization failed. Passphrases must match.\u001B[0m";
                }
                sentinel.permitEntry(p1);
                return "Maya: New vault created. Welcome.";
            }

            String password = askSecure("Master Passphrase: ");
            if (password == null || password.isEmpty()) return "Maya: Login cancelled.";

            sentinel.permitEntry(password);
            return "\u001B[32mMaya: Access Granted.\u001B[0m";

        } catch (Throwable t) {
            // By catching Throwable and returning a String, we bypass Spring Shell's stack trace printer.
            return "\u001B[31mMaya: " + t.getMessage() + "\u001B[0m";
        }
    }

    @Command(name = "change-passphrase")
    public String update() {
        if (!sentinel.isUnlocked()) return "Maya: You need to 'getin' first.";

        String oldP = askSecure("Current Passphrase: ");
        String newP1 = askSecure("New Passphrase: ");
        String newP2 = askSecure("Confirm New Passphrase: ");

        if (newP1 == null || !newP1.equals(newP2)) return "Maya: Passphrases do not match.";

        try {
            sentinel.updatePassphrase(oldP, newP1);
            return "Maya: lockbox re-keyed successfully.";
        } catch (Throwable t) {
            return "\u001B[31mMaya: " + t.getMessage() + "\u001B[0m";
        }
    }

    @Command(name = "lockbox-empty")
    public String reset() {
        ConfirmationInput component = new ConfirmationInput(terminal, "Destroy all data?", false);
        component.setResourceLoader(resourceLoader);
        component.setTemplateExecutor(templateExecutor);

        var context = component.run(ConfirmationInputContext.empty());
        if (Boolean.TRUE.equals(context.getResultValue())) {
            return sentinel.nuke() ? "Maya: lockbox destroyed." : "Maya: Error deleting files.";
        }
        return "Maya: Reset aborted.";
    }

    private String askSecure(String promptLabel) {
        // 1. Create a direct LineReader. This bypasses the Spring Shell UI bugs.
        LineReader reader = LineReaderBuilder.builder()
                .terminal(terminal)
                .build();

        // 2. Use the 'reading a variable' approach which is 100% stable with backspace
        // The 'null' is the mask character. For passwords, use '*'
        try {
            String input = reader.readLine(promptLabel + ": ", '*');

            // 3. Clean the line manually after Enter so Maya's next message is fresh
            terminal.writer().print("\r\u001B[K");
            terminal.flush();

            return input;
        } catch (Exception e) {
            return null;
        }
    }
}